package net.sf.cglib.proxy;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.*;
import java.util.*;
import net.sf.cglib.util.*;

/* package */ class EnhancerGenerator extends CodeGenerator {
    private static final String INTERCEPTOR_FIELD = "CGLIB$INTERCEPTOR";
    private static final String DELEGATE_FIELD = "CGLIB$DELEGATE";
    private static final Class[] NORMAL_ARGS = new Class[]{ MethodInterceptor.class };
    private static final Class[] DELEGATE_ARGS = new Class[]{ MethodInterceptor.class, Object.class };
    private static int index = 0;

    private Class[] interfaces;
    private Method wreplace;
    private MethodInterceptor ih;
    private boolean delegating;
        
    /* package */ EnhancerGenerator(String className, Class clazz, Class[] interfaces, MethodInterceptor ih,
                                    ClassLoader loader, Method wreplace, boolean delegating) {
        super(className, clazz, loader);
        this.interfaces = interfaces;
        this.ih = ih;
        this.wreplace = wreplace;
        this.delegating = delegating;

        if (wreplace != null && 
            (!Modifier.isStatic(wreplace.getModifiers()) ||
             !Modifier.isPublic(wreplace.getModifiers()) ||
             wreplace.getReturnType() != Object.class || 
             wreplace.getParameterTypes().length != 1 ||
             wreplace.getParameterTypes()[0] != Object.class)) {
            throw new IllegalArgumentException(wreplace.toString());
        }

        try {
            Constructor construct = clazz.getDeclaredConstructor( new Class[0] );
            int mod = construct.getModifiers();
                
            if (!(Modifier.isPublic(mod) ||
                  Modifier.isProtected(mod) ||
                  isVisible(construct, clazz.getPackage().getName()))) {
                throw new IllegalArgumentException( clazz.getName() );
            }

            if (wreplace != null) {
                loader.loadClass(wreplace.getDeclaringClass().getName());
            }
            loader.loadClass(clazz.getName());

            if (interfaces != null) {
                for (int i = 0; i < interfaces.length; i++) {
                    if (!interfaces[i].isInterface()) {
                        throw new IllegalArgumentException(interfaces[i] + " is not an interface");
                    }
                    loader.loadClass(interfaces[i].getName());
                }
            }
        } catch (NoSuchMethodException e) {
            throw new CodeGenerationException(e);
        } catch (ClassNotFoundException e) {
            throw new CodeGenerationException(e);
        }
    }

    protected void generate() throws NoSuchMethodException {
        if (wreplace == null) {
            wreplace = Enhancer.InternalReplace.class.getMethod("writeReplace", OBJECT_CLASS_ARRAY);
        }
        
        declare_interface(Factory.class);
        declare_field(Modifier.PRIVATE, MethodInterceptor.class, INTERCEPTOR_FIELD);
        if (delegating) {
            declare_field(Modifier.PRIVATE, getSuperclass(), DELEGATE_FIELD);
        }

        generateConstructor();
        generateFactory();
        generateFindClass();

        // Order is very important: must add superclass, then
        // its superclass chain, then each interface and
        // its superinterfaces.
        List allMethods = new LinkedList();
        addDeclaredMethods(allMethods, getSuperclass());
        if (interfaces != null) {
            declare_interfaces(interfaces);
            for (int i = 0; i < interfaces.length; i++) {
                addDeclaredMethods(allMethods, interfaces[i]);
            }
        }

        boolean declaresWriteReplace = false;
        String packageName = getSuperclass().getPackage().getName();
        Map methodMap = new HashMap();
        for (Iterator it = allMethods.iterator(); it.hasNext();) {
            Method method = (Method)it.next();
            int mod = method.getModifiers();
            if (!Modifier.isStatic(mod) && !Modifier.isFinal(mod) &&
                (!delegating || !Modifier.isProtected(mod)) &&
                isVisible(method, packageName)) {
                if (method.getName().equals("writeReplace") &&
                    method.getParameterTypes().length == 0) {
                    declaresWriteReplace = true;
                }
                Object methodKey = MethodWrapper.newInstance(method);
                Method other = (Method)methodMap.get(methodKey);
                if (other != null) {
                    checkReturnTypesEqual(method, other);
                }
                methodMap.put(methodKey, method);
            }
        }
        Method invokeSuper = getInvokeSuper();
        Method afterReturn = getAfterReturn();

        Map methodTable = new HashMap();
        int cntr = 0;
        for (Iterator it = methodMap.values().iterator(); it.hasNext();) {
            Method method = (Method)it.next();
            String fieldName = "METHOD_" + cntr++;
            declare_field(Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC, Method.class, fieldName);
            generateMethod(fieldName, method, invokeSuper, afterReturn);
            methodTable.put(fieldName, method);
        }
        generateClInit(methodTable);

        if (!declaresWriteReplace) {
            generateWriteReplace();
        }
    }

    private void checkReturnTypesEqual(Method m1, Method m2) {
        if (!m1.getReturnType().equals(m2.getReturnType())) {
            throw new IllegalArgumentException("Can't implement:\n" + m1.getDeclaringClass().getName() +
                                               "\n      and\n" + m2.getDeclaringClass().getName() + "\n"+
                                               m1.toString() + "\n" + m2.toString());
        }
    }

    private Method getInvokeSuper() throws NoSuchMethodException {
        Class[] types = new Class[]{
            Object.class,
            Method.class,
            Object[].class,
        };
        return MethodInterceptor.class.getDeclaredMethod("invokeSuper", types);
    }

    private Method getAfterReturn() throws NoSuchMethodException {
        Class[] types = new Class[]{
            Object.class,
            Method.class,
            Object[].class,
            Boolean.TYPE,
            Object.class,
            Throwable.class
        };
        return MethodInterceptor.class.getDeclaredMethod("afterReturn", types);
    }

    private void generateConstructor() {
        begin_constructor(delegating ? DELEGATE_ARGS : NORMAL_ARGS);
        load_this();
        dup();
        super_invoke_constructor();
        load_arg(0);
        putfield(INTERCEPTOR_FIELD);
        if (delegating) {
            load_this();
            load_arg(1);
            checkcast(getSuperclass());
            putfield(DELEGATE_FIELD);
        }
        return_value();
        end_constructor();
    }

    private void generateFactory() throws NoSuchMethodException {
        generateFactoryHelper(NORMAL_ARGS, !delegating);
        generateFactoryHelper(DELEGATE_ARGS, delegating);

        begin_method(Factory.class.getMethod("getDelegate", EMPTY_CLASS_ARRAY));
        if (delegating) {
            load_this();
            getfield(DELEGATE_FIELD);
        } else {
            aconst_null();
        }
        return_value();
        end_method();

        begin_method(Factory.class.getMethod("setDelegate", OBJECT_CLASS_ARRAY));
        if (delegating) {
            load_this();
            load_arg(0);
            checkcast(getSuperclass());
            putfield(DELEGATE_FIELD);
        } else {
            throwWrongType();
        }
        return_value();
        end_method();

        begin_method(Factory.class.getMethod("getInterceptor", EMPTY_CLASS_ARRAY));
        load_this();
        getfield(INTERCEPTOR_FIELD);
        return_value();
        end_method();
    }

    private void throwWrongType() {
        new_instance(UnsupportedOperationException.class);
        dup();
        push("Using a delegating enhanced class as non-delegating, or the reverse");
        invoke_constructor(UnsupportedOperationException.class, new Class[]{ String.class });
        athrow();
    }

    private void generateFactoryHelper(Class[] types, boolean enabled) throws NoSuchMethodException {
        begin_method(Factory.class.getMethod("newInstance", types));
        if (enabled) {
            new_instance_this();
            dup();
            load_args();
            invoke_constructor_this(types);
        } else {
            throwWrongType();
        }
        return_value();
        end_method();
    }

    private void generateWriteReplace() {
        begin_method(Modifier.PRIVATE,
                     Object.class, 
                     "writeReplace",
                     EMPTY_CLASS_ARRAY,
                     new Class[]{ ObjectStreamException.class });
        load_this();
        invoke(wreplace);
        return_value();
        end_method();
    }

    private static void addDeclaredMethods(List methodList, Class clazz) {
        methodList.addAll(java.util.Arrays.asList(clazz.getDeclaredMethods()));
        if (clazz.isInterface()) {
            Class[] interfaces = clazz.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                addDeclaredMethods(methodList, interfaces[i]);
            }
        } else {
            Class superclass = clazz.getSuperclass();
            if (superclass != null) {
                addDeclaredMethods(methodList, superclass);
            }
        }
    }

    private void generateMethod(String fieldName, Method method, Method invokeSuper, Method afterReturn) {
        Class[] args = method.getParameterTypes();
        Class returnType = method.getReturnType();
        boolean returnsValue = !returnType.equals(Void.TYPE);
        int mod = method.getModifiers();
        boolean isAbstract = Modifier.isAbstract(mod);

        begin_method(method);
        int outer_eh = begin_handler();

        create_arg_array();
        store_local("args");
        aconst_null();
        store_local("resultFromSuper");
        push(0);
        local_type("superInvoked", boolean.class);
        store_local("superInvoked");
        aconst_null();
        store_local("error");
        
        if (delegating || !isAbstract) {
            load_this();
            getfield(INTERCEPTOR_FIELD);
            load_this();
            getstatic(fieldName);
            load_local("args");
            invoke(invokeSuper);

            ifeq("endif");
            push(1);
            store_local("superInvoked");

            int eh = begin_handler();
            load_this();
            if (delegating) {
                getfield(DELEGATE_FIELD);
                load_args();
                invoke(method);
            } else {
                load_args();
                super_invoke();
            }
            if (returnsValue) {
                box(returnType);
                store_local("resultFromSuper");
            }
            goTo("endif");
            end_handler();
            
            handle_exception(eh, Throwable.class);
            store_local("error");
            nop("endif");
        }

        load_this();
        getfield(INTERCEPTOR_FIELD);
        load_this();
        getstatic(fieldName);
        load_local("args");
        load_local("superInvoked");
        load_local("resultFromSuper");
        load_local("error");
        invoke(afterReturn);

        /* generates:
           if (result == null) {
               return 0;
           } else {
               return ((Number)result).intValue();
           }
         */
        return_zero_if_null();
        end_handler();
        generateHandleUndeclared(method, outer_eh);
        end_method();
    }

    private void generateHandleUndeclared(Method method, int handler) {
        /* generates:
           } catch (RuntimeException e) {
               throw e;
           } catch (Error e) {
               throw e;
           } catch (<DeclaredException> e) {
               throw e;
           } catch (Throwable e) {
               throw new UndeclaredThrowableException(e);
           }
        */
        Class[] exceptionTypes = method.getExceptionTypes();
        Set exceptionSet = new HashSet(Arrays.asList(exceptionTypes));
        if (!(exceptionSet.contains(Exception.class) ||
              exceptionSet.contains(Throwable.class))) {
            if (!exceptionSet.contains(RuntimeException.class)) {
                handle_exception(handler, RuntimeException.class);
                athrow();
            }
            if (!exceptionSet.contains(Error.class)) {
                handle_exception(handler, Error.class);
                athrow();
            }
            for (int i = 0; i < exceptionTypes.length; i++) {
                handle_exception(handler, exceptionTypes[i]);
                athrow();
            }
            // e -> eo -> oeo -> ooe -> o
            handle_exception(handler, Throwable.class);
            new_instance(UndeclaredThrowableException.class);
            dup_x1();
            swap();
            invoke_constructor(UndeclaredThrowableException.class, new Class[]{ Throwable.class });
            athrow();
        }
    }

    private void generateClInit(Map methodTable) throws NoSuchMethodException {
        /* generates:
           static {
             Class [] args;
             Class cls = findClass("java.lang.Object");
             args = new Class[0];
             METHOD_1 = cls.getDeclaredMethod("toString", args);
             ...etc...
           }
        */

        Method getDeclaredMethod =
            Class.class.getDeclaredMethod("getDeclaredMethod",
                                          new Class[]{ String.class, Class[].class });
        begin_static();
        for (Iterator it = methodTable.keySet().iterator(); it.hasNext();) {
            String fieldName = (String)it.next();
            Method method = (Method)methodTable.get(fieldName);
            Class[] args = method.getParameterTypes();

            push(method.getDeclaringClass().getName());
            invoke_static_this(FIND_CLASS, Class.class, STRING_CLASS_ARRAY);
            store_local("cls");
            push(args.length);
            newarray(Class.class);

            for (int i = 0; i < args.length; i++) {
                dup();
                push(i);
                load_class(args[i]);
                aastore();
            }

            load_local("cls");
            swap();
            push(method.getName());
            swap();
            invoke(getDeclaredMethod);
            putstatic(fieldName);
        }
        return_value();
        end_static();
    }
}
