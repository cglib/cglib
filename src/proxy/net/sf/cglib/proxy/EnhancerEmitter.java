/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package net.sf.cglib.proxy;

import java.io.ObjectStreamException;
import java.lang.reflect.*;
import java.util.*;
import net.sf.cglib.core.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

class EnhancerEmitter extends ClassEmitter {
    private static final String CONSTRUCTED_FIELD = "CGLIB$CONSTRUCTED";

    private static final String SET_THREAD_CALLBACKS_NAME = "CGLIB$SET_THREAD_CALLBACKS";

    private static final Type ILLEGAL_STATE_EXCEPTION =
      TypeUtils.parseType("IllegalStateException");
    private static final Type ILLEGAL_ARGUMENT_EXCEPTION =
      TypeUtils.parseType("IllegalArgumentException");
    private static final Type THREAD_LOCAL =
      TypeUtils.parseType("ThreadLocal");
    private static final Type FACTORY =
      TypeUtils.parseType("net.sf.cglib.proxy.Factory");
    private static final Type CALLBACKS =
      TypeUtils.parseType("net.sf.cglib.proxy.Callbacks");

    private static final Signature CSTRUCT_NULL =
      TypeUtils.parseConstructor("");
    private static final Signature SET_THREAD_CALLBACKS =
      TypeUtils.parseSignature("void " + SET_THREAD_CALLBACKS_NAME + "(net.sf.cglib.proxy.Callback[])");
    private static final Signature NEW_INSTANCE =
      TypeUtils.parseSignature("Object newInstance(net.sf.cglib.proxy.Callback[])");
    private static final Signature MULTIARG_NEW_INSTANCE =
      TypeUtils.parseSignature("Object newInstance(Class[], Object[], net.sf.cglib.proxy.Callback[])");
    private static final Signature SINGLE_NEW_INSTANCE =
      TypeUtils.parseSignature("Object newInstance(net.sf.cglib.proxy.Callback)");
    private static final Signature COPY_NEW_INSTANCE =
      TypeUtils.parseSignature("Object newInstance()");
    private static final Signature COPY_MULTIARG_NEW_INSTANCE =
      TypeUtils.parseSignature("Object newInstance(Class[], Object[])");
    private static final Signature SET_CALLBACK =
      TypeUtils.parseSignature("void setCallback(int, net.sf.cglib.proxy.Callback)");
    private static final Signature GET_CALLBACK =
      TypeUtils.parseSignature("net.sf.cglib.proxy.Callback getCallback(int)");
    private static final Signature SET_CALLBACKS =
      TypeUtils.parseSignature("void setCallbacks(net.sf.cglib.proxy.Callback[])");

    private static final Signature THREAD_LOCAL_GET =
      TypeUtils.parseSignature("Object get()");
    private static final Signature THREAD_LOCAL_SET =
      TypeUtils.parseSignature("void set(Object)");

    private Class[] callbackTypes;
    
    public EnhancerEmitter(ClassVisitor v,
                           String className,
                           Class superclass,
                           Class[] interfaces,
                           CallbackFilter filter,
                           Class[] callbackTypes,
                           boolean useFactory) throws Exception {
        super(v);
        if (superclass == null) {
            superclass = Object.class;
        }
        this.callbackTypes = callbackTypes;

        begin_class(Constants.ACC_PUBLIC,
                    className,
                    Type.getType(superclass),
                    (useFactory ?
                     TypeUtils.add(TypeUtils.getTypes(interfaces), FACTORY) :
                     TypeUtils.getTypes(interfaces)),
                    Constants.SOURCE_FILE);
        
        List clist = new ArrayList(Arrays.asList(superclass.getDeclaredConstructors()));
        CollectionUtils.filter(clist, new VisibilityPredicate(superclass, true));
        if (clist.size() == 0) {
            throw new IllegalArgumentException("No visible constructors in " + superclass);
        }
        Constructor[] constructors = (Constructor[])clist.toArray(new Constructor[clist.size()]);

        // Order is very important: must add superclass, then
        // its superclass chain, then each interface and
        // its superinterfaces.
        List methods = new ArrayList();
        ReflectUtils.addAllMethods(superclass, methods);

        List interfaceMethods = new ArrayList();
        if (interfaces != null) {
            for (int i = 0; i < interfaces.length; i++) {
                if (interfaces[i] != Factory.class) {
                    ReflectUtils.addAllMethods(interfaces[i], interfaceMethods);
                }
            }
        }
        Set forcePublic = MethodWrapper.createSet(interfaceMethods);
        methods.addAll(interfaceMethods);
        CollectionUtils.filter(methods, new VisibilityPredicate(superclass, true));
        CollectionUtils.filter(methods, new DuplicatesPredicate());
        removeFinal(methods);

        Map groups = new HashMap();
        Map indexes = new HashMap();
        for (Iterator it = methods.iterator(); it.hasNext();) {
            Method method = (Method)it.next();
            int index = filter.accept(method);
            if (index >= callbackTypes.length) {
                throw new IllegalArgumentException("Callback filter returned an index that is too large: " + index);
            }
            indexes.put(method, new Integer(index));
            Object gen = CallbackUtils.getGenerator(callbackTypes[index]);
            List group = (List)groups.get(gen);
            if (group == null) {
                groups.put(gen, group = new ArrayList(methods.size()));
            }
            group.add(method);
        }

        declare_field(Constants.ACC_PRIVATE, CONSTRUCTED_FIELD, Type.BOOLEAN_TYPE, null, null);
        emitMethods(groups, indexes, forcePublic);
        emitConstructors(constructors);
        emitSetThreadCallbacks();

        if (useFactory) {
            int[] keys = getCallbackKeys();
            emitNewInstanceCallbacks();
            emitNewInstanceCallback();
            emitNewInstanceMultiarg(constructors);
            emitNewInstanceCopy();
            emitNewInstanceMultiargCopy(constructors);
            emitGetCallback(keys);
            emitSetCallback(keys);
            emitSetCallbacks();
        }
        end_class();
    }

    static void setThreadCallbacks(Class type, Callback[] callbacks) {
        // TODO: optimize
        try {
            Method setter = type.getDeclaredMethod(SET_THREAD_CALLBACKS_NAME, new Class[]{ Callback[].class });
            setter.invoke(null, new Object[]{ callbacks });
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(type + " is not an enhanced class");
        } catch (IllegalAccessException e) {
            throw new CodeGenerationException(e);
        } catch (InvocationTargetException e) {
            throw new CodeGenerationException(e);
        }
    }

    private void emitConstructors(Constructor[] constructors) {
        for (int i = 0; i < constructors.length; i++) {
            Signature sig = ReflectUtils.getSignature(constructors[i]);
            CodeEmitter e = begin_method(Constants.ACC_PUBLIC,
                                         sig,
                                         ReflectUtils.getExceptionTypes(constructors[i]),
                                         null);
            e.load_this();
            e.dup();
            e.load_args();
            e.super_invoke_constructor(sig);
            e.push(1);
            e.putfield(CONSTRUCTED_FIELD);
            for (int j = 0; j < callbackTypes.length; j++) {
                e.load_this();
                e.dup();
                e.getfield(getThreadLocal(j));
                e.invoke_virtual(THREAD_LOCAL, THREAD_LOCAL_GET);
                e.checkcast(Type.getType(callbackTypes[j]));
                e.putfield(getCallbackField(j));
            }
            e.return_value();
            e.end_method();
        }
    }
    
    private int[] getCallbackKeys() {
        int[] keys = new int[callbackTypes.length];
        for (int i = 0; i < callbackTypes.length; i++) {
            keys[i] = i;
        }
        return keys;
    }

    private void emitGetCallback(int[] keys) {
        final CodeEmitter e = begin_method(Constants.ACC_PUBLIC, GET_CALLBACK, null, null);
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

    private void emitSetCallback(int[] keys) {
        final CodeEmitter e = begin_method(Constants.ACC_PUBLIC, SET_CALLBACK, null, null);
        e.load_this();
        e.load_arg(1);
        e.dup();
        e.load_arg(0);
        e.process_switch(keys, new ProcessSwitchCallback() {
            public void processCase(int key, Label end) {
                // we set thread locals too in case this method is called from within constructor (as Proxy does)
                e.getfield(getThreadLocal(key));
                e.swap();
                e.invoke_virtual(THREAD_LOCAL, THREAD_LOCAL_SET);
                e.checkcast(Type.getType(callbackTypes[key]));
                e.putfield(getCallbackField(key));
                e.goTo(end);
            }
            public void processDefault() {
                // stack height
                e.pop2(); 
                e.pop();
            }
        });
        e.return_value();
        e.end_method();
    }
        
    private void emitSetCallbacks() {
        CodeEmitter e = begin_method(Constants.ACC_PUBLIC, SET_CALLBACKS, null, null);
        emitSetThreadCallbacks(e);
        e.load_this();
        e.load_arg(0);
        for (int i = 0; i < callbackTypes.length; i++) {
            e.dup2();
            e.aaload(i);
            e.checkcast(Type.getType(callbackTypes[i]));
            e.putfield(getCallbackField(i));
        }
        e.return_value();
        e.end_method();
    }

    private void emitNewInstanceCallbacks() {
        CodeEmitter e = begin_method(Constants.ACC_PUBLIC, NEW_INSTANCE, null, null);
        e.load_arg(0);
        e.invoke_static_this(SET_THREAD_CALLBACKS);
        emitCommonNewInstance(e);
    }

    private void emitNewInstanceCopy() {
        CodeEmitter e = begin_method(Constants.ACC_PUBLIC, COPY_NEW_INSTANCE, null, null);
        emitCopyCallbacks(e);
        emitCommonNewInstance(e);
    }

    private void emitCopyCallbacks(CodeEmitter e) {
        for (int i = 0; i < callbackTypes.length; i++) {
            e.getfield(getThreadLocal(i));
            e.load_this();
            e.getfield(getCallbackField(i));
            e.invoke_virtual(THREAD_LOCAL, THREAD_LOCAL_SET);
        }
    }

    private void emitCommonNewInstance(CodeEmitter e) {
        e.new_instance_this();
        e.dup();
        e.invoke_constructor_this();
        e.return_value();
        e.end_method();
    }
    
    private void emitNewInstanceCallback() {
        CodeEmitter e = begin_method(Constants.ACC_PUBLIC, SINGLE_NEW_INSTANCE, null, null);
        switch (callbackTypes.length) {
        case 0:
            // TODO: make sure Callback is null
            break;
        case 1:
            e.getfield(getThreadLocal(0));
            e.load_arg(0);
            e.invoke_virtual(THREAD_LOCAL, THREAD_LOCAL_SET);
            break;
        default:
            e.throw_exception(ILLEGAL_STATE_EXCEPTION, "More than one callback object required");
        }
        emitCommonNewInstance(e);
    }

    private void emitNewInstanceMultiarg(Constructor[] constructors) {
        CodeEmitter e = begin_method(Constants.ACC_PUBLIC, MULTIARG_NEW_INSTANCE, null, null);
        e.load_arg(2);
        e.invoke_static_this(SET_THREAD_CALLBACKS);
        emitCommonMultiarg(constructors, e);
    }

    private void emitNewInstanceMultiargCopy(Constructor[] constructors) {
        CodeEmitter e = begin_method(Constants.ACC_PUBLIC, COPY_MULTIARG_NEW_INSTANCE, null, null);
        emitCopyCallbacks(e);
        emitCommonMultiarg(constructors, e);
    }

    private void emitCommonMultiarg(Constructor[] constructors, final CodeEmitter e) {
        e.new_instance_this();
        e.dup();
        e.load_arg(0);
        EmitUtils.constructor_switch(e, constructors, new ObjectSwitchCallback() {
            public void processCase(Object key, Label end) {
                Constructor constructor = (Constructor)key;
                Type types[] = TypeUtils.getTypes(constructor.getParameterTypes());
                for (int i = 0; i < types.length; i++) {
                    e.load_arg(1);
                    e.push(i);
                    e.aaload();
                    e.unbox(types[i]);
                }
                e.invoke_constructor_this(ReflectUtils.getSignature(constructor));
                e.goTo(end);
            }
            public void processDefault() {
                e.throw_exception(ILLEGAL_ARGUMENT_EXCEPTION, "Constructor not found");
            }
        });
        e.return_value();
        e.end_method();
    }

    private void emitMethods(Map groups, final Map indexes, final Set forcePublic) throws Exception {
        for (int i = 0; i < callbackTypes.length; i++) {
            declare_field(Constants.ACC_PRIVATE, getCallbackField(i), Type.getType(callbackTypes[i]), null, null);
            declare_field(Constants.PRIVATE_FINAL_STATIC, getThreadLocal(i), THREAD_LOCAL, null, null);
        }

        Set seenGen = new HashSet();
        CodeEmitter e = begin_static();
        for (int i = 0; i < callbackTypes.length; i++) {
            e.new_instance(THREAD_LOCAL);
            e.dup();
            e.invoke_constructor(THREAD_LOCAL, CSTRUCT_NULL);
            e.putfield(getThreadLocal(i));

            CallbackGenerator gen = CallbackUtils.getGenerator(callbackTypes[i]);
            if (!seenGen.contains(gen)) {
                seenGen.add(gen);
                final List fmethods = (List)groups.get(gen);
                CallbackGenerator.Context context = new CallbackGenerator.Context() {
                    public Iterator getMethods() {
                        return fmethods.iterator();
                    }
                    public int getIndex(Method method) {
                        return ((Integer)indexes.get(method)).intValue();
                    }
                    public void emitCallback(CodeEmitter e, int index) {
                        emitCurrentCallback(e, index);
                    }
                    public int getModifiers(Method method) {
                        int modifiers = Constants.ACC_FINAL
                            | (method.getModifiers()
                               & ~Constants.ACC_ABSTRACT
                               & ~Constants.ACC_NATIVE
                               & ~Constants.ACC_SYNCHRONIZED);
                        if (forcePublic.contains(MethodWrapper.create(method))) {
                            modifiers = (modifiers & ~Constants.ACC_PROTECTED) | Constants.ACC_PUBLIC;
                        }
                        return modifiers;
                    }
                    // TODO: this is probably slow
                    public String getUniqueName(Method method) {
                        return method.getName() + "_" + fmethods.indexOf(method);
                    }
                };
                gen.generate(this, context);
                gen.generateStatic(e, context);
            }
        }
        e.return_value();
        e.end_method();
    }

    private void emitSetThreadCallbacks() {
        CodeEmitter e = begin_method(Constants.ACC_PUBLIC | Constants.ACC_STATIC,
                                     SET_THREAD_CALLBACKS,
                                     null,
                                     null);
        emitSetThreadCallbacks(e);
        e.return_value();
        e.end_method();
    }

    private void emitSetThreadCallbacks(CodeEmitter e) {
        for (int i = 0; i < callbackTypes.length; i++) {
            e.getfield(getThreadLocal(i));
            e.load_arg(0);
            e.aaload(i);
            e.invoke_virtual(THREAD_LOCAL, THREAD_LOCAL_SET);
        }
    }

    private void emitCurrentCallback(CodeEmitter e, int index) {
        e.load_this();
        e.getfield(getCallbackField(index));
        e.dup();
        Label end = e.make_label();
        e.ifnonnull(end);
        e.load_this();
        e.getfield(CONSTRUCTED_FIELD);
        e.if_jump(e.NE, end);
        e.pop();
        e.getfield(getThreadLocal(index));
        e.invoke_virtual(THREAD_LOCAL, THREAD_LOCAL_GET);
        e.checkcast(Type.getType(callbackTypes[index]));
        e.mark(end);
    }

    private static String getCallbackField(int index) {
        return "CGLIB$CALLBACK_" + index;
    }

    private static String getThreadLocal(int index) {
        return "CGLIB$TL_CALLBACK_" + index;
    }
    
    private static void removeFinal(List list) {
        CollectionUtils.filter(list, new Predicate() {
            public boolean evaluate(Object arg) {
                return !Modifier.isFinal(((Method)arg).getModifiers());
            }
        });
    }
}
