/*
 * Copyright 2002,2003,2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.cglib.proxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import net.sf.cglib.core.*;
import net.sf.cglib.transform.*;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.Label;

/**
 * Generates dynamic subclasses to enable method interception. This
 * class started as a substitute for the standard Dynamic Proxy support
 * included with JDK 1.3, but one that allowed the proxies to extend a
 * concrete base class, in addition to implementing interfaces. The dynamically
 * generated subclasses override the non-final methods of the superclass and
 * have hooks which callback to user-defined interceptor
 * implementations.
 * <p>
 * The original and most general callback type is the {@link MethodInterceptor}, which
 * in AOP terms enables "around advice"--that is, you can invoke custom code both before
 * and after the invocation of the "super" method. In addition you can modify the
 * arguments before calling the super method, or not call it at all.
 * <p>
 * Although <code>MethodInterceptor</code> is generic enough to meet any
 * interception need, it is often overkill. For simplicity and performance, additional
 * specialized callback types, such as {@link LazyLoader} are also available.
 * Often a single callback will be used per enhanced class, but you can control
 * which callback is used on a per-method basis with a {@link CallbackFilter}.
 * <p>
 * The most common uses of this class are embodied in the static helper methods. For
 * advanced needs, such as customizing the <code>ClassLoader</code> to use, you should create
 * a new instance of <code>Enhancer</code>. Other classes within CGLIB follow a similar pattern.
 * <p>
 * All enhanced objects implement the {@link Factory} interface, unless {@link #setUseFactory} is
 * used to explicitly disable this feature. The <code>Factory</code> interface provides an API
 * to change the callbacks of an existing object, as well as a faster and easier way to create
 * new instances of the same type.
 * <p>
 * For an almost drop-in replacement for
 * <code>java.lang.reflect.Proxy</code>, see the {@link Proxy} class.
 */
public class Enhancer extends AbstractClassGenerator
{
    private static final Source SOURCE = new Source(Enhancer.class.getName());
    private static final EnhancerKey KEY_FACTORY =
      (EnhancerKey)KeyFactory.create(EnhancerKey.class, KeyFactory.CLASS_BY_NAME);

    private static final String BOUND_FIELD = "CGLIB$BOUND";
    private static final String THREAD_CALLBACKS_FIELD = "CGLIB$THREAD_CALLBACKS";
    private static final String DEFAULT_CALLBACKS_FIELD = "CGLIB$DEFAULT_CALLBACKS";
    private static final String SET_THREAD_CALLBACKS_NAME = "CGLIB$SET_THREAD_CALLBACKS";
    private static final String SET_DEFAULT_CALLBACKS_NAME = "CGLIB$SET_DEFAULT_CALLBACKS";

    private static final Type FACTORY =
      TypeUtils.parseType("net.sf.cglib.proxy.Factory");
    private static final Type ILLEGAL_STATE_EXCEPTION =
      TypeUtils.parseType("IllegalStateException");
    private static final Type ILLEGAL_ARGUMENT_EXCEPTION =
      TypeUtils.parseType("IllegalArgumentException");
    private static final Type THREAD_LOCAL =
      TypeUtils.parseType("ThreadLocal");
    private static final Type CALLBACK =
      TypeUtils.parseType("net.sf.cglib.proxy.Callback");
    private static final Type CALLBACK_ARRAY =
      TypeUtils.parseType("net.sf.cglib.proxy.Callback[]");

    private static final Signature CSTRUCT_NULL =
      TypeUtils.parseConstructor("");
    private static final Signature SET_THREAD_CALLBACKS =
      new Signature(SET_THREAD_CALLBACKS_NAME, Type.VOID_TYPE, new Type[]{ CALLBACK_ARRAY });
    private static final Signature SET_DEFAULT_CALLBACKS =
      new Signature(SET_DEFAULT_CALLBACKS_NAME, Type.VOID_TYPE, new Type[]{ CALLBACK_ARRAY });
    private static final Signature NEW_INSTANCE =
      TypeUtils.parseSignature("Object newInstance(net.sf.cglib.proxy.Callback[])");
    private static final Signature MULTIARG_NEW_INSTANCE =
      TypeUtils.parseSignature("Object newInstance(Class[], Object[], net.sf.cglib.proxy.Callback[])");
    private static final Signature SINGLE_NEW_INSTANCE =
      TypeUtils.parseSignature("Object newInstance(net.sf.cglib.proxy.Callback)");
    private static final Signature SET_CALLBACK =
      TypeUtils.parseSignature("void setCallback(int, net.sf.cglib.proxy.Callback)");
    private static final Signature GET_CALLBACK =
      TypeUtils.parseSignature("net.sf.cglib.proxy.Callback getCallback(int)");
    private static final Signature SET_CALLBACKS =
      TypeUtils.parseSignature("void setCallbacks(net.sf.cglib.proxy.Callback[])");
    private static final Signature GET_CALLBACKS =
      TypeUtils.parseSignature("net.sf.cglib.proxy.Callback[] getCallbacks()");
    private static final Signature THREAD_LOCAL_GET =
      TypeUtils.parseSignature("Object get()");
    private static final Signature THREAD_LOCAL_SET =
      TypeUtils.parseSignature("void set(Object)");
    private static final Signature BIND_CALLBACKS =
      TypeUtils.parseSignature("void CGLIB$BIND_CALLBACKS(Object)");

    /** Internal interface, only public due to ClassLoader issues. */
    public interface EnhancerKey {
        public Object newInstance(Class type,
                                  Class[] interfaces,
                                  Object filter,
                                  Type[] callbackTypes,
                                  boolean useFactory,
                                  Long serialVersionUID);
    }

    private Class[] interfaces;
    private Object filter;
    private Callback[] callbacks;
    private Type[] callbackTypes;
    private boolean classOnly;
    private Class superclass;
    private Class[] argumentTypes;
    private Object[] arguments;
    private boolean useFactory = true;
    private int depth;
    private Long serialVersionUID;

    /**
     * Create a new <code>Enhancer</code>. A new <code>Enhancer</code>
     * object should be used for each generated object, and should not
     * be shared across threads. To create additional instances of a
     * generated class, use the <code>Factory</code> interface.
     * @see Factory
     */
    public Enhancer() {
        super(SOURCE);
    }

    /**
     * Set the class which the generated class will extend. As a convenience,
     * if the supplied superclass is actually an interface, <code>setInterfaces</code>
     * will be called with the appropriate argument instead.
     * A non-interface argument must not be declared as final, and must have an
     * accessible constructor.
     * @param superclass class to extend or interface to implement
     * @see #setInterfaces(Class[])
     */
    public void setSuperclass(Class superclass) {
        if (superclass != null && superclass.isInterface()) {
            setInterfaces(new Class[]{ superclass });
        } else if (superclass != null && superclass.equals(Object.class)) {
            // affects choice of ClassLoader
            this.superclass = null;
        } else {
            this.superclass = superclass;
        }
    }

    /**
     * Set the interfaces to implement. The <code>Factory</code> interface will
     * always be implemented regardless of what is specified here.
     * @param interfaces array of interfaces to implement, or null
     * @see Factory
     */
    public void setInterfaces(Class[] interfaces) {
        this.interfaces = interfaces;
    }

    /**
     * Set the {@link CallbackFilter} used to map the generated class' methods
     * to a particular callback index.
     * New object instances will always use the same mapping, but may use different
     * actual callback objects.
     * @param filter the callback filter to use when generating a new class
     * @see #setCallbacks
     * @see @setCallbackFilter2
     */
    public void setCallbackFilter(CallbackFilter filter) {
        this.filter = filter;
    }


    /**
     * Set the {@link CallbackFilter} used to map the generated class' methods
     * to a particular callback index.
     * New object instances will always use the same mapping, but may use different
     * actual callback objects.
     * <p>
     * A <code>CallbackFilter2</code> must be used when calling
     * {@link #createTransformer}, but may also be used for <code>create</code>
     * or <code>createClass</code>.
     * @param filter the callback filter to use when generating a new class
     * @see #setCallbacks
     */
    public void setCallbackFilter2(CallbackFilter2 filter) {
        this.filter = filter;
    }

    /**
     * Set the single {@link Callback} to use.
     * Ignored if you use {@link #createClass}.
     * @param callback the callback to use for all methods
     * @see #setCallbacks
     */
    public void setCallback(final Callback callback) {
        setCallbacks(new Callback[]{ callback });
    }

    /**
     * Set the array of callbacks to use.
     * Ignored if you use {@link #createClass}.
     * You must use a {@link CallbackFilter} to specify the index into this
     * array for each method in the proxied class.
     * @param callbacks the callback array
     * @see #setCallbackFilter
     * @see #setCallback
     */
    public void setCallbacks(Callback[] callbacks) {
        if (callbacks != null && callbacks.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty");
        }
        this.callbacks = callbacks;
    }

    /**
     * Set whether the enhanced object instances should implement
     * the {@link Factory} interface.
     * This was added for tools that need for proxies to be more
     * indistinguishable from their targets. Also, in some cases it may
     * be necessary to disable the <code>Factory</code> interface to
     * prevent code from changing the underlying callbacks.
     * @param useFactory whether to implement <code>Factory</code>; default is <code>true</code>
     */
    public void setUseFactory(boolean useFactory) {
        this.useFactory = useFactory;
    }

    /**
     * Set the single type of {@link Callback} to use.
     * This may be used instead of {@link #setCallback} when calling
     * {@link #createClass}, since it may not be possible to have
     * an array of actual callback instances.
     * @param callbackType the type of callback to use for all methods
     * @see #setCallbackTypes
     */     
    public void setCallbackType(Class callbackType) {
        setCallbackTypes(new Class[]{ callbackType });
    }
    
    /**
     * Set the array of callback types to use.
     * This may be used instead of {@link #setCallbacks} when calling
     * {@link #createClass}, since it may not be possible to have
     * an array of actual callback instances.
     * You must use a {@link CallbackFilter} to specify the index into this
     * array for each method in the proxied class.
     * @param callbackTypes the array of callback types
     */
    public void setCallbackTypes(Class[] callbackTypes) {
        if (callbackTypes != null && callbackTypes.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty");
        }
        this.callbackTypes = CallbackInfo.determineTypes(callbackTypes);
    }

    /**
     * Generate a new class if necessary and uses the specified
     * callbacks (if any) to create a new object instance.
     * Uses the no-arg constructor of the superclass.
     * @return a new instance
     */
    public Object create() {
        classOnly = false;
        argumentTypes = null;
        return createHelper();
    }

    /**
     * Generate a new class if necessary and uses the specified
     * callbacks (if any) to create a new object instance.
     * Uses the constructor of the superclass matching the <code>argumentTypes</code>
     * parameter, with the given arguments.
     * @param argumentTypes constructor signature
     * @param arguments compatible wrapped arguments to pass to constructor
     * @return a new instance
     */
    public Object create(Class[] argumentTypes, Object[] arguments) {
        classOnly = false;
        if (argumentTypes == null || arguments == null || argumentTypes.length != arguments.length) {
            throw new IllegalArgumentException("Arguments must be non-null and of equal length");
        }
        this.argumentTypes = argumentTypes;
        this.arguments = arguments;
        return createHelper();
    }

    /**
     * Generate a new class if necessary and return it without creating a new instance.
     * This ignores any callbacks that have been set.
     * To create a new instance you will have to use reflection, and methods
     * called during the constructor will not be intercepted. To avoid this problem,
     * use the multi-arg <code>create</code> method.
     * @see #create(Class[], Object[])
     */
    public Class createClass() {
        classOnly = true;
        return (Class)createHelper();
    }

    /**
     * Insert a static serialVersionUID field into the generated class.
     * @param sUID the field value, or null to avoid generating field.
     */
    public void setSerialVersionUID(Long sUID) {
        serialVersionUID = sUID;
    }

    private void validate(boolean transforming) {
        if (transforming && filter != null && !(filter instanceof CallbackFilter2)) {
            throw new IllegalStateException("CallbackFilter2 must be used when transforming");
        }
        if ((classOnly || transforming) ^ (callbacks == null)) {
            if (classOnly || transforming) {
                throw new IllegalStateException("createClass and createTransformer do not accept callbacks");
            } else {
                throw new IllegalStateException("Callbacks are required");
            }
        }
        if ((classOnly || transforming) && callbackTypes == null) {
            throw new IllegalStateException("Callback types are required");
        }
        if (callbacks != null && callbackTypes != null) {
            if (callbacks.length != callbackTypes.length) {
                throw new IllegalStateException("Lengths of callback and callback types array must be the same");
            }
            Type[] check = CallbackInfo.determineTypes(callbacks);
            for (int i = 0; i < check.length; i++) {
                if (!check[i].equals(callbackTypes[i])) {
                    throw new IllegalStateException("Callback " + check[i] + " is not assignable to " + callbackTypes[i]);
                }
            }
        } else if (callbacks != null) {
            callbackTypes = CallbackInfo.determineTypes(callbacks);
        }
        if (filter == null) {
            if (callbackTypes.length > 1) {
                throw new IllegalStateException("Multiple callback types possible but no filter specified");
            }
            filter = CallbackFilter2.ALL_ZERO;
        }
        if (interfaces != null) {
            for (int i = 0; i < interfaces.length; i++) {
                if (interfaces[i] == null) {
                    throw new IllegalStateException("Interfaces cannot be null");
                }
                if (!interfaces[i].isInterface()) {
                    throw new IllegalStateException(interfaces[i] + " is not an interface");
                }
            }
        }
    }

    private Object createHelper() {
        validate(false);
        if (superclass != null) {
            setNamePrefix(superclass.getName());
        } else if (interfaces != null) {
            setNamePrefix(interfaces[ReflectUtils.findPackageProtected(interfaces)].getName());
        }
        Object key = KEY_FACTORY.newInstance(superclass, interfaces, filter, callbackTypes, useFactory, serialVersionUID);
        return super.create(key);
    }

    protected ClassLoader getDefaultClassLoader() {
        if (superclass != null) {
            return superclass.getClassLoader();
        } else if (interfaces != null) {
            return interfaces[0].getClassLoader();
        } else {
            return null;
        }
    }

    private Signature rename(Signature sig, int index) {
        return new Signature("CGLIB$" + sig.getName() + "$" + depth + "$" + index,
                             sig.getDescriptor());
    }
    
    /**
     * Create a transformer that can be used to enhance classes directly, instead of
     * generating subclasses. Differences include:
     * <ul>
     * <li>Requires the use of a {@link CallbackFilter2} to
     * map methods signatures to callback types.
     * <li>The superclass, if any, is ignored.
     * <li>There is no restriction on intercepting final classes and methods.
     * <li>A {@link Callback} array is not allowed. {@link #setCallbackTypes} must
     * be used instead.
     * </ul>
     * @return A thread-safe ClassTransformer
     * @see #setCallbackFilter2
     */
    public ClassTransformer createTransformer() {
        validate(true);
        return new EnhancerTransformer();
    }

    /**
     * Finds all of the methods that will be extended by an
     * Enhancer-generated class * using the specified superclass and
     * interfaces. This can be useful in * building a list of Callback
     * objects. The methods are added to the end of the given list.  Due
     * to the subclassing nature of the classes generated by Enhancer,
     * the methods are guaranteed to be non-static, non-final, and
     * non-private. Each method signature will only occur once, even if
     * it occurs in multiple classes.
     * @param superclass the class that will be extended, or null
     * @param interfaces the list of interfaces that will be implemented, or null
     * @param methods the list into which to copy the applicable methods
     */
    public static void getMethods(Class superclass, Class[] interfaces, List methods)
    {
        getMethods(superclass, interfaces, methods, null, null);
    }

    private static void getMethods(Class superclass, Class[] interfaces, List methods, List interfaceMethods, Set forcePublic)
    {
        ReflectUtils.addAllMethods(superclass, methods);
        List target = (interfaceMethods != null) ? interfaceMethods : methods;
        if (interfaces != null) {
            for (int i = 0; i < interfaces.length; i++) {
                if (interfaces[i] != Factory.class) {
                    ReflectUtils.addAllMethods(interfaces[i], target);
                }
            }
        }
        if (interfaceMethods != null) {
            if (forcePublic != null) {
                forcePublic.addAll(MethodWrapper.createSet(interfaceMethods));
            }
            methods.addAll(interfaceMethods);
        }
        CollectionUtils.filter(methods, new RejectModifierPredicate(Constants.ACC_STATIC));
        CollectionUtils.filter(methods, new VisibilityPredicate(superclass, true));
        CollectionUtils.filter(methods, new DuplicatesPredicate());
        CollectionUtils.filter(methods, new RejectModifierPredicate(Constants.ACC_FINAL));
    }

    public void generateClass(ClassVisitor v) throws Exception {
        Class sc = (superclass == null) ? Object.class : superclass;
        
        if (TypeUtils.isFinal(sc.getModifiers()))
            throw new IllegalArgumentException("Cannot subclass final class " + sc);

        depth = calculateDepth(sc);
        
        List constructors = new ArrayList(Arrays.asList(sc.getDeclaredConstructors()));
        CollectionUtils.filter(constructors, new VisibilityPredicate(sc, true));
        if (constructors.size() == 0) {
            throw new IllegalArgumentException("No visible constructors in " + sc);
        }


        ClassEmitter e = new ClassEmitter(v);
        e.begin_class(Constants.V1_2,
                      Constants.ACC_PUBLIC,
                      getClassName(),
                      Type.getType(sc),
                      (useFactory ?
                       TypeUtils.add(TypeUtils.getTypes(interfaces), FACTORY) :
                       TypeUtils.getTypes(interfaces)),
                      Constants.SOURCE_FILE);

        // Order is very important: must add superclass, then
        // its superclass chain, then each interface and
        // its superinterfaces.
        List actualMethods = new ArrayList();
        List interfaceMethods = new ArrayList();
        final Set forcePublic = new HashSet();
        getMethods(sc, interfaces, actualMethods, interfaceMethods, forcePublic);

        List methods = CollectionUtils.transform(actualMethods, new Transformer() {
            public Object transform(Object value) {
                Method method = (Method)value;
                int modifiers = Constants.ACC_FINAL
                    | (method.getModifiers()
                       & ~Constants.ACC_ABSTRACT
                       & ~Constants.ACC_NATIVE
                       & ~Constants.ACC_SYNCHRONIZED);
                if (forcePublic.contains(MethodWrapper.create(method))) {
                    modifiers = (modifiers & ~Constants.ACC_PROTECTED) | Constants.ACC_PUBLIC;
                }
                return ReflectUtils.getMethodInfo(method, modifiers);
            }
        });

        emit(e,
             false,
             CollectionUtils.transform(constructors, MethodInfoTransformer.getInstance()),
             methods,
             actualMethods);
        e.end_class();
    }

    protected Object firstInstance(Class type) throws Exception {
        if (classOnly) {
            return type;
        } else {
            return createUsingReflection(type);
        }
    }

    protected Object nextInstance(Object instance) {
        Class protoclass = (instance instanceof Class) ? (Class)instance : instance.getClass();
        if (classOnly) {
            return protoclass;
        } else if (instance instanceof Factory) {
            if (argumentTypes != null) {
                return ((Factory)instance).newInstance(argumentTypes, arguments, callbacks);
            } else {
                return ((Factory)instance).newInstance(callbacks);
            }
        } else {
            return createUsingReflection(protoclass);
        }
    }

    /**
     * Call this method to register the {@link Callback} array to use before
     * creating a new instance of the generated class via reflection. If you are using
     * an instance of <code>Enhancer</code> or the {@link Factory} interface to create
     * new instances, this method is unnecessary. Its primary use is for when you want to
     * cache and reuse a generated class yourself, and the generated class does
     * <i>not</i> implement the {@link Factory} interface.
     * @see #setUseFactory
     */
    public static void registerCallbacks(Class generatedClass, Callback[] callbacks) {
        setDefaultCallbacks(generatedClass, callbacks);
        setThreadCallbacks(generatedClass, callbacks);
    }

    public static boolean isEnhanced(Class type) {
        try {
            getCallbacksSetter(type, SET_THREAD_CALLBACKS_NAME);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static void setThreadCallbacks(Class type, Callback[] callbacks) {
        setCallbacksHelper(type, callbacks, SET_THREAD_CALLBACKS_NAME);
    }

    private static void setDefaultCallbacks(Class type, Callback[] callbacks) {
        setCallbacksHelper(type, callbacks, SET_DEFAULT_CALLBACKS_NAME);
    }

    private static void setCallbacksHelper(Class type, Callback[] callbacks, String methodName) {
        // TODO: optimize
        try {
            Method setter = getCallbacksSetter(type, methodName);
            setter.invoke(null, new Object[]{ callbacks });
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(type + " is not an enhanced class");
        } catch (IllegalAccessException e) {
            throw new CodeGenerationException(e);
        } catch (InvocationTargetException e) {
            throw new CodeGenerationException(e);
        }
    }

    private static Method getCallbacksSetter(Class type, String methodName) throws NoSuchMethodException {
        return type.getDeclaredMethod(methodName, new Class[]{ Callback[].class });
    }

    private Object createUsingReflection(Class type) {
        setThreadCallbacks(type, callbacks);
        if (argumentTypes != null) {
            return ReflectUtils.newInstance(type, argumentTypes, arguments);
        } else {
            return ReflectUtils.newInstance(type);
        }
    }

    /**
     * Helper method to create an intercepted object.
     * For finer control over the generated instance, use a new instance of <code>Enhancer</code>
     * instead of this static method.
     * @param type class to extend or interface to implement
     * @param callback the callback to use for all methods
     */
    public static Object create(Class type, Callback callback) {
        Enhancer e = new Enhancer();
        e.setSuperclass(type);
        e.setCallback(callback);
        return e.create();
    }

    /**
     * Helper method to create an intercepted object.
     * For finer control over the generated instance, use a new instance of <code>Enhancer</code>
     * instead of this static method.
     * @param type class to extend or interface to implement
     * @param interfaces array of interfaces to implement, or null
     * @param callback the callback to use for all methods
     */
    public static Object create(Class superclass, Class interfaces[], Callback callback) {
        Enhancer e = new Enhancer();
        e.setSuperclass(superclass);
        e.setInterfaces(interfaces);
        e.setCallback(callback);
        return e.create();
    }

    /**
     * Helper method to create an intercepted object.
     * For finer control over the generated instance, use a new instance of <code>Enhancer</code>
     * instead of this static method.
     * @param type class to extend or interface to implement
     * @param interfaces array of interfaces to implement, or null
     * @param filter the callback filter to use when generating a new class
     * @param callbacks callback implementations to use for the enhanced object
     */
    public static Object create(Class superclass, Class[] interfaces, CallbackFilter filter, Callback[] callbacks) {
        Enhancer e = new Enhancer();
        e.setSuperclass(superclass);
        e.setInterfaces(interfaces);
        e.setCallbackFilter(filter);
        e.setCallbacks(callbacks);
        return e.create();
    }

    ////////////////////////////////////////////////////////////

    private static boolean isExcluded(Type superType) {
        String desc = superType.getDescriptor();
        return
            desc.equals("Lnet/sf/cglib/reflect/FastClass;") ||
            desc.equals("Lnet/sf/cglib/core/KeyFactory;");
    }

    private class EnhancerTransformer extends ClassEmitterTransformer {
        private ClassInfo classInfo;
        private List constructors;
        private List methods;
        private boolean collect;

        public void begin_class(int version, final int access, String className, final Type superType, final Type[] interfaces, String sourceFile) {
            Type[] useInterfaces = interfaces;
            if (TypeUtils.isInterface(access) || isExcluded(superType)) {
                collect = false;
            } else {
                collect = true;
                final Type type = TypeUtils.getType(className);
                classInfo = new ClassInfo() {
                    public Type getType() {
                        return type;
                    }
                    public Type getSuperType() {
                        return superType;
                    }
                    public Type[] getInterfaces() {
                        return interfaces;
                    }
                    public int getModifiers() {
                        return access;
                    }
                };
                Class superclass;
                if (superType == null) {
                    superclass = Object.class;
                } else {
                    try {
                        superclass = getClassLoader().loadClass(superType.getClassName());
                    } catch (ClassNotFoundException e) {
                        throw new CodeGenerationException(e);
                    }
                }
                depth = calculateDepth(superclass);
                List superMethods = new ArrayList();
                ReflectUtils.addAllMethods(superclass, superMethods);
                CollectionUtils.filter(superMethods, new RejectModifierPredicate(Constants.ACC_PRIVATE | Constants.ACC_STATIC));
                methods = CollectionUtils.transform(superMethods, MethodInfoTransformer.getInstance());
                methods = new ArrayList();
                constructors = new ArrayList();
                if (useFactory) {
                    useInterfaces = TypeUtils.add(interfaces, FACTORY);
                }
            }
            super.begin_class(version, access, className, superType, useInterfaces, sourceFile);
        }

        public CodeEmitter begin_method(final int access, final Signature sig, final Type[] exceptions, final Attribute attrs) {
            if (collect) {
                MethodInfo method = new MethodInfo() {
                    public ClassInfo getClassInfo() {
                        return classInfo;
                    }
                    public int getModifiers() {
                        return access;
                    }
                    public Signature getSignature() {
                        return sig;
                    }
                    public Type[] getExceptionTypes() {
                        return exceptions;
                    }
                    public Attribute getAttribute() {
                        return attrs;
                    }
                };
                if (TypeUtils.isConstructor(method)) {
                    constructors.add(method);
                    return new CodeEmitter(super.begin_method(access, sig, exceptions, attrs)) {
                            public void	visitMethodInsn(int opcode, String owner, String name, String desc) {
                                super.visitMethodInsn(opcode, owner, name, desc);
                                if (opcode == Constants.INVOKESPECIAL && Constants.CONSTRUCTOR_NAME.equals(name)) {
                                    load_this();
                                    invoke_static_this(BIND_CALLBACKS);
                                }
                            }
                        };
                } else if (!TypeUtils.isPrivate(access) && !TypeUtils.isStatic(access)) {
                    methods.add(method);
                    return super.begin_method(Constants.ACC_FINAL,
                                              rename(sig, methods.size() - 1),
                                              exceptions,
                                              attrs);
                }
            }
            return super.begin_method(access, sig, exceptions, attrs);
        }

        public void end_class() {
            if (collect) {
                collect = false;
                Enhancer.this.emit(this,
                                   true,
                                   constructors,
                                   methods,
                                   null);
            }
            super.end_class();
        }
    }
    

    public void emit(ClassEmitter ce,
                     boolean transforming,
                     List constructors,
                     List methods,
                     List actualMethods) {

        ce.declare_field(Constants.ACC_PRIVATE, BOUND_FIELD, Type.BOOLEAN_TYPE, null, null);
        ce.declare_field(Constants.PRIVATE_FINAL_STATIC, THREAD_CALLBACKS_FIELD, THREAD_LOCAL, null, null);
        ce.declare_field(Constants.ACC_PRIVATE | Constants.ACC_STATIC, DEFAULT_CALLBACKS_FIELD, CALLBACK_ARRAY, null, null);
        if (serialVersionUID != null) {
            ce.declare_field(Constants.PRIVATE_FINAL_STATIC, Constants.SUID_FIELD_NAME, Type.LONG_TYPE, serialVersionUID, null);
        }

        for (int i = 0; i < callbackTypes.length; i++) {
            ce.declare_field(Constants.ACC_PRIVATE, getCallbackField(i), callbackTypes[i], null, null);
        }

        emitMethods(ce, transforming, methods, actualMethods);
        if (!transforming) {
            emitConstructors(ce, constructors);
        }
        emitSetDefaultCallbacks(ce);
        emitSetThreadCallbacks(ce);
        emitBindCallbacks(ce);

        if (useFactory) {
            int[] keys = getCallbackKeys();
            emitNewInstanceCallbacks(ce);
            emitNewInstanceCallback(ce);
            emitNewInstanceMultiarg(ce, constructors);
            emitGetCallback(ce, keys);
            emitSetCallback(ce, keys);
            emitGetCallbacks(ce);
            emitSetCallbacks(ce);
        }
    }

    private void emitConstructors(ClassEmitter ce, List constructors) {
        boolean seenNull = false;
        for (Iterator it = constructors.iterator(); it.hasNext();) {
            MethodInfo constructor = (MethodInfo)it.next();
            CodeEmitter e = EmitUtils.begin_method(ce, constructor, Constants.ACC_PUBLIC);
            e.load_this();
            e.dup();
            e.load_args();
            Signature sig = constructor.getSignature();
            seenNull = seenNull || sig.getDescriptor().equals("()V");
            e.super_invoke_constructor(sig);
            e.invoke_static_this(BIND_CALLBACKS);
            e.return_value();
            e.end_method();
        }
        if (!classOnly && !seenNull && arguments == null)
            throw new IllegalArgumentException("Superclass has no null constructors but no arguments were given");
    }

    private int[] getCallbackKeys() {
        int[] keys = new int[callbackTypes.length];
        for (int i = 0; i < callbackTypes.length; i++) {
            keys[i] = i;
        }
        return keys;
    }

    private void emitGetCallback(ClassEmitter ce, int[] keys) {
        final CodeEmitter e = ce.begin_method(Constants.ACC_PUBLIC, GET_CALLBACK, null, null);
        e.load_this();
        e.invoke_static_this(BIND_CALLBACKS);
        e.load_this();
        e.load_arg(0);
        e.process_switch(keys, new ProcessSwitchCallback() {
            public void processCase(int key, Label end) {
                e.getfield(getCallbackField(key));
                e.goTo(end);
            }
            public void processDefault() {
                e.pop(); // stack height
                e.aconst_null();
            }
        });
        e.return_value();
        e.end_method();
    }

    private void emitSetCallback(ClassEmitter ce, int[] keys) {
        final CodeEmitter e = ce.begin_method(Constants.ACC_PUBLIC, SET_CALLBACK, null, null);
        e.load_this();
        e.load_arg(1);
        e.load_arg(0);
        e.process_switch(keys, new ProcessSwitchCallback() {
            public void processCase(int key, Label end) {
                e.checkcast(callbackTypes[key]);
                e.putfield(getCallbackField(key));
                e.goTo(end);
            }
            public void processDefault() {
                // TODO: error?
                e.pop2(); // stack height
            }
        });
        e.return_value();
        e.end_method();
    }

    private void emitSetCallbacks(ClassEmitter ce) {
        CodeEmitter e = ce.begin_method(Constants.ACC_PUBLIC, SET_CALLBACKS, null, null);
        e.load_this();
        e.load_arg(0);
        for (int i = 0; i < callbackTypes.length; i++) {
            e.dup2();
            e.aaload(i);
            e.checkcast(callbackTypes[i]);
            e.putfield(getCallbackField(i));
        }
        e.return_value();
        e.end_method();
    }

    private void emitGetCallbacks(ClassEmitter ce) {
        CodeEmitter e = ce.begin_method(Constants.ACC_PUBLIC, GET_CALLBACKS, null, null);
        e.load_this();
        e.invoke_static_this(BIND_CALLBACKS);
        e.load_this();
        e.push(callbackTypes.length);
        e.newarray(CALLBACK);
        for (int i = 0; i < callbackTypes.length; i++) {
            e.dup();
            e.push(i);
            e.load_this();
            e.getfield(getCallbackField(i));
            e.aastore();
        }
        e.return_value();
        e.end_method();
    }

    private void emitNewInstanceCallbacks(ClassEmitter ce) {
        CodeEmitter e = ce.begin_method(Constants.ACC_PUBLIC, NEW_INSTANCE, null, null);
        e.load_arg(0);
        e.invoke_static_this(SET_THREAD_CALLBACKS);
        emitCommonNewInstance(e);
    }

    private void emitCommonNewInstance(CodeEmitter e) {
        e.new_instance_this();
        e.dup();
        e.invoke_constructor_this();
        e.return_value();
        e.end_method();
    }
    
    private void emitNewInstanceCallback(ClassEmitter ce) {
        CodeEmitter e = ce.begin_method(Constants.ACC_PUBLIC, SINGLE_NEW_INSTANCE, null, null);
        switch (callbackTypes.length) {
        case 0:
            // TODO: make sure Callback is null
            break;
        case 1:
            // for now just make a new array; TODO: optimize
            e.push(1);
            e.newarray(CALLBACK);
            e.dup();
            e.push(0);
            e.load_arg(0);
            e.aastore();
            e.invoke_static_this(SET_THREAD_CALLBACKS);
            break;
        default:
            e.throw_exception(ILLEGAL_STATE_EXCEPTION, "More than one callback object required");
        }
        emitCommonNewInstance(e);
    }

    private void emitNewInstanceMultiarg(ClassEmitter ce, List constructors) {
        final CodeEmitter e = ce.begin_method(Constants.ACC_PUBLIC, MULTIARG_NEW_INSTANCE, null, null);
        e.load_arg(2);
        e.invoke_static_this(SET_THREAD_CALLBACKS);
        e.new_instance_this();
        e.dup();
        e.load_arg(0);
        EmitUtils.constructor_switch(e, constructors, new ObjectSwitchCallback() {
            public void processCase(Object key, Label end) {
                MethodInfo constructor = (MethodInfo)key;
                Type types[] = constructor.getSignature().getArgumentTypes();
                for (int i = 0; i < types.length; i++) {
                    e.load_arg(1);
                    e.push(i);
                    e.aaload();
                    e.unbox(types[i]);
                }
                e.invoke_constructor_this(constructor.getSignature());
                e.goTo(end);
            }
            public void processDefault() {
                e.throw_exception(ILLEGAL_ARGUMENT_EXCEPTION, "Constructor not found");
            }
        });
        e.return_value();
        e.end_method();
    }

    private void emitMethods(final ClassEmitter ce, final boolean transforming, List methods, List actualMethods) {
        boolean isFilter2 = (filter instanceof CallbackFilter2);
        CallbackGenerator[] generators = CallbackInfo.getGenerators(callbackTypes);

        Map groups = new HashMap();
        final Map indexes = new HashMap();
        final Map originalModifiers = new HashMap();
        final Map positions = CollectionUtils.getIndexMap(methods);

        Iterator it1 = methods.iterator();
        Iterator it2 = (actualMethods != null) ? actualMethods.iterator() : null;

        while (it1.hasNext()) {
            MethodInfo method = (MethodInfo)it1.next();
            Method actualMethod = (it2 != null) ? (Method)it2.next() : null;
            int index;
            if (isFilter2) {
                index = ((CallbackFilter2)filter).accept(method);
            } else {
                index = ((CallbackFilter)filter).accept(actualMethod);
            }
            if (index >= callbackTypes.length) {
                throw new IllegalArgumentException("Callback filter returned an index that is too large: " + index);
            }
            originalModifiers.put(method, new Integer((actualMethod != null) ? actualMethod.getModifiers() : method.getModifiers()));
            indexes.put(method, new Integer(index));
            List group = (List)groups.get(generators[index]);
            if (group == null) {
                groups.put(generators[index], group = new ArrayList(methods.size()));
            }
            group.add(method);
        }

        Set seenGen = new HashSet();
        CodeEmitter e = ce.getStaticHook();
        e.new_instance(THREAD_LOCAL);
        e.dup();
        e.invoke_constructor(THREAD_LOCAL, CSTRUCT_NULL);
        e.putfield(THREAD_CALLBACKS_FIELD);
        
        for (int i = 0; i < callbackTypes.length; i++) {
            CallbackGenerator gen = generators[i];
            if (!seenGen.contains(gen)) {
                seenGen.add(gen);
                final List fmethods = (List)groups.get(gen);
                if (fmethods != null) {
                    CallbackGenerator.Context context = new CallbackGenerator.Context() {
                        public int getOriginalModifiers(MethodInfo method) {
                            return ((Integer)originalModifiers.get(method)).intValue();
                        }
                        public boolean isTransforming() {
                            return transforming;
                        }
                        public Iterator getMethods() {
                            return fmethods.iterator();
                        }
                        public int getIndex(MethodInfo method) {
                            return ((Integer)indexes.get(method)).intValue();
                        }
                        public void emitCallback(CodeEmitter e, int index) {
                            emitCurrentCallback(e, index);
                        }
                        public Signature getImplSignature(MethodInfo method) {
                            return rename(method.getSignature(), ((Integer)positions.get(method)).intValue());
                        }
                    };
                    try {
                        gen.generate(ce, context);
                        gen.generateStatic(e, context);
                    } catch (RuntimeException x) {
                        throw x;
                    } catch (Exception x) {
                        throw new CodeGenerationException(x);
                    }
                }
            }
        }
        e.return_value();
        e.end_method();
    }

    private void emitSetThreadCallbacks(ClassEmitter ce) {
        CodeEmitter e = ce.begin_method(Constants.ACC_PUBLIC | Constants.ACC_STATIC,
                                        SET_THREAD_CALLBACKS,
                                        null,
                                        null);
        e.getfield(THREAD_CALLBACKS_FIELD);
        e.load_arg(0);
        e.invoke_virtual(THREAD_LOCAL, THREAD_LOCAL_SET);
        e.return_value();
        e.end_method();
    }

    private void emitSetDefaultCallbacks(ClassEmitter ce) {
        CodeEmitter e = ce.begin_method(Constants.ACC_PUBLIC | Constants.ACC_STATIC,
                                        SET_DEFAULT_CALLBACKS,
                                        null,
                                        null);
        e.load_arg(0);
        e.putfield(DEFAULT_CALLBACKS_FIELD);
        e.return_value();
        e.end_method();
    }

    private void emitCurrentCallback(CodeEmitter e, int index) {
        e.load_this();
        e.getfield(getCallbackField(index));
        e.dup();
        Label end = e.make_label();
        e.ifnonnull(end);
        e.pop(); // stack height
        e.load_this();
        e.invoke_static_this(BIND_CALLBACKS);
        e.load_this();
        e.getfield(getCallbackField(index));
        e.mark(end);
    }

    private void emitBindCallbacks(ClassEmitter ce) {
        CodeEmitter e = ce.begin_method(Constants.PRIVATE_FINAL_STATIC,
                                        BIND_CALLBACKS,
                                        null,
                                        null);
        Local me = e.make_local();
        e.load_arg(0);
        e.checkcast_this();
        e.store_local(me);

        Label end = e.make_label();
        e.load_local(me);
        e.getfield(BOUND_FIELD);
        e.if_jump(e.NE, end);
        e.load_local(me);
        e.push(1);
        e.putfield(BOUND_FIELD);

        e.getfield(THREAD_CALLBACKS_FIELD);
        e.invoke_virtual(THREAD_LOCAL, THREAD_LOCAL_GET);
        e.dup();
        Label found_callback = e.make_label();
        e.ifnonnull(found_callback);

        e.pop();
        e.getfield(DEFAULT_CALLBACKS_FIELD);
        e.dup();
        e.ifnonnull(found_callback);
        Label clear = e.make_label();
        e.pop();
        e.goTo(clear);

        e.mark(found_callback);
        e.checkcast(CALLBACK_ARRAY);
        e.load_local(me);
        e.swap();
        for (int i = callbackTypes.length - 1; i >= 0; i--) {
            if (i != 0) {
                e.dup2();
            }
            e.aaload(i);
            e.checkcast(callbackTypes[i]);
            e.putfield(getCallbackField(i));
        }

        // clear thread-locals
        e.mark(clear);
        e.getfield(THREAD_CALLBACKS_FIELD);
        e.aconst_null();
        e.invoke_virtual(THREAD_LOCAL, THREAD_LOCAL_SET);

        e.mark(end);
        e.return_value();
        e.end_method();
    }

    private static String getCallbackField(int index) {
        return "CGLIB$CALLBACK_" + index;
    }

    private static int calculateDepth(Class type) {
        int depth = 0;
        while (type != null) {
            if (Enhancer.isEnhanced(type)) {
                depth++;
            }
            type = type.getSuperclass();
        }
        return depth;
    }
}
