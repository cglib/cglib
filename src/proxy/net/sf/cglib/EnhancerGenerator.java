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
package net.sf.cglib;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.*;
import java.util.*;

/* package */ class EnhancerGenerator extends CodeGenerator {
    private static final String INTERCEPTOR_FIELD = "CGLIB$INTERCEPTOR";
    private static final String DELEGATE_FIELD = "CGLIB$DELEGATE";
    private static final Class[] NORMAL_ARGS = new Class[]{ MethodInterceptor.class };
    private static final Class[] DELEGATE_ARGS = new Class[]{ MethodInterceptor.class, Object.class };
    private static final Method AROUND_ADVICE;
    private static final Method MAKE_PROXY;

    static {
        try {
            Class[] types = new Class[]{ Object.class, Method.class, Object[].class, MethodProxy.class };
            AROUND_ADVICE = MethodInterceptor.class.getDeclaredMethod("aroundAdvice", types);

            types = new Class[]{ Method.class };
            MAKE_PROXY = MethodProxy.class.getDeclaredMethod("generate", types);
        } catch (NoSuchMethodException e) {
            throw new CodeGenerationException(e);
        }
    }

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
                  isVisible( construct, clazz.getPackage() ))) {
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
        Package packageName = getSuperclass().getPackage();
        Map methodMap = new HashMap();
        
        
        for (Iterator it = allMethods.iterator(); it.hasNext();) {
            Method method = (Method)it.next();
            int mod = method.getModifiers();
            if (!Modifier.isStatic(mod) &&
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
                   
                }else{
                     //addDeclaredMethods adds methods reverse order
                     methodMap.put(methodKey, method);
                }
                
            }
        }
        List methodList = new ArrayList(methodMap.values());
        List list = new ArrayList();
        
        int privateFinalStatic = Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC;
        for (int i = 0, j = 0, size = methodList.size(); j < size; j++) {
            Method method = (Method)methodList.get(j);
            if( Modifier.isFinal(method.getModifiers()) ){
                continue;
            }   
            String fieldName = getFieldName(method, i);
            String accessName = getAccessName(method, i);
            declare_field(privateFinalStatic, Method.class, fieldName);
            declare_field(privateFinalStatic, MethodProxy.class, accessName);
            generateAccessMethod(method, accessName);
            generateAroundMethod(method, fieldName, accessName);
            list.add(method);
            i++;
        }
        generateClInit(list);

        if (!declaresWriteReplace) {
            generateWriteReplace();
        }
    }

    private String getFieldName(Method method, int index) {
        return "METHOD_" + index;
    }
    
    private String getAccessName(Method method, int index) {
        return "CGLIB$ACCESS_" + index + "_" + method.getName();
    }

    private void checkReturnTypesEqual(Method m1, Method m2) {
        if (!m1.getReturnType().equals(m2.getReturnType())) {
            throw new IllegalArgumentException("Can't implement:\n" + m1.getDeclaringClass().getName() +
                                               "\n      and\n" + m2.getDeclaringClass().getName() + "\n"+
                                               m1.toString() + "\n" + m2.toString());
        }
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
        
        throwException(UnsupportedOperationException.class,
        "Using a delegating enhanced class as non-delegating, or the reverse");
        
    }

    // TODO: need to ensure that MethodInterceptor type is compatible
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

    private void generateAccessMethod(Method method, String accessName) {
        begin_method(Modifier.FINAL,
                     method.getReturnType(),
                     accessName,
                     method.getParameterTypes(),
                     method.getExceptionTypes());
        if (Modifier.isAbstract(method.getModifiers())) {
           // zero_or_null(method.getReturnType());
            throwException(AbstractMethodError.class, method.toString() + " is abstract" );
        } else {
            load_this();
            if (delegating) {
                getfield(DELEGATE_FIELD);
                load_args();
                invoke(method);
            } else {
                load_args();
                super_invoke(method);
            }
        }
        return_value();
        end_method();
    }

    private void generateAroundMethod(Method method, String fieldName, String accessName) {
        begin_method(method);
        int outer_eh = begin_handler();
        load_this();
        getfield(INTERCEPTOR_FIELD);
        load_this();
        getstatic(fieldName);
        create_arg_array();
        getstatic(accessName);
        invoke(AROUND_ADVICE);
        unbox_or_zero(method.getReturnType());
        return_value();
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

    private void generateClInit(List methodList) throws NoSuchMethodException {
        /* generates:
           static {
             Class [] args;
             Class cls = findClass("java.lang.Object");
             args = new Class[0];
             METHOD_1 = cls.getDeclaredMethod("toString", args);

             Class thisClass = findClass("NameOfThisClass");
             Method proxied = thisClass.getDeclaredMethod("CGLIB$ACCESS_O", args);
             CGLIB$ACCESS_0 = MethodProxy.generate(proxied);
           }
        */
        Method getDeclaredMethod =
            Class.class.getDeclaredMethod("getDeclaredMethod",
                                          new Class[]{ String.class, Class[].class });
        begin_static();
        for (int i = 0, size = methodList.size(); i < size; i++) {
            Method method = (Method)methodList.get(i);
            String fieldName = getFieldName(method, i);

            Class[] args = method.getParameterTypes();
            push(method.getDeclaringClass().getName());
            invoke_static_this(FIND_CLASS, Class.class, STRING_CLASS_ARRAY);
            store_local("cls");
            push(args.length);
            newarray(Class.class);

            for (int j = 0; j < args.length; j++) {
                dup();
                push(j);
                load_class(args[j]);
                aastore();
            }
            store_local("args");

            load_local("cls");
            push(method.getName());
            load_local("args");
            invoke(getDeclaredMethod);
            putstatic(fieldName);

            String accessName = getAccessName(method, i);
            push(getClassName());
            invoke_static_this(FIND_CLASS, Class.class, STRING_CLASS_ARRAY);
            push(accessName);
            load_local("args");
            invoke(getDeclaredMethod);
            invoke(MAKE_PROXY);
            putstatic(accessName);
        }
        return_value();
        end_static();
    }
}
