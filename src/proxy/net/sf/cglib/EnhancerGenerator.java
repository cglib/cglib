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
import java.lang.reflect.*;
import java.util.*;

/*package*/ class EnhancerGenerator extends CodeGenerator {
    private static final String INTERCEPTOR_FIELD = "CGLIB$INTERCEPTOR";
    private static final String DELEGATE_FIELD = "CGLIB$DELEGATE";
    private static final String CONSTRUCTOR_PROXY_MAP = "CGLIB$CONSTRUCTOR_PROXY_MAP";

    private static final int PRIVATE_FINAL_STATIC = Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC;

    private static final Method NORMAL_NEW_INSTANCE =
      ReflectUtils.findMethod("Factory.newInstance(MethodInterceptor)");
    private static final Method DELEGATE_NEW_INSTANCE =
      ReflectUtils.findMethod("Factory.newInstance(MethodInterceptor, Object)");
    private static final Method AROUND_ADVICE =
      ReflectUtils.findMethod("MethodInterceptor.aroundAdvice(Object, Method, Object[], MethodProxy)");
    private static final Method MAKE_PROXY =
      ReflectUtils.findMethod("MethodProxy.create(Method)");
    private static final Method MAKE_CONSTRUCTOR_PROXY =
      ReflectUtils.findMethod("ConstructorProxy.create(Constructor)");
    private static final Method INTERNAL_WRITE_REPLACE =
      ReflectUtils.findMethod("Enhancer$InternalReplace.writeReplace(Object)");
    private static final Method NEW_CLASS_KEY = 
     ReflectUtils.findMethod("ConstructorProxy.newClassKey(Class[])");
    private static final Method PROXY_NEW_INSTANCE = 
     ReflectUtils.findMethod("ConstructorProxy.newInstance(Object[], Object)");
    private static final Method MULTIARG_NEW_INSTANCE = 
      ReflectUtils.findMethod("Factory.newInstance(Class[], Object[], MethodInterceptor)");
    private static final Method SET_DELEGATE =
      ReflectUtils.findMethod("Factory.setDelegate(Object)");
    private static final Method GET_DELEGATE =
      ReflectUtils.findMethod("Factory.getDelegate()");
    private static final Method GET_INTERCEPTOR =
      ReflectUtils.findMethod("Factory.getInterceptor()");

    private Class[] interfaces;
    private Method wreplace;
    private boolean delegating;
    private MethodFilter filter;
    private Constructor cstruct;
    private List constructorList;
    private List constructorTypes;
        
    /* package */ EnhancerGenerator( String className, Class clazz, 
                                     Class[] interfaces,
                                     ClassLoader loader, 
                                     Method wreplace, boolean delegating,
                                     MethodFilter filter) {
        super(className, clazz, loader);
        this.interfaces = interfaces;
        this.wreplace = wreplace;
        this.delegating = delegating;
        this.filter = filter;  
     
        if (wreplace != null && 
            (!Modifier.isStatic(wreplace.getModifiers()) ||
             !Modifier.isPublic(wreplace.getModifiers()) ||
             wreplace.getReturnType() != Object.class || 
             wreplace.getParameterTypes().length != 1 ||
             wreplace.getParameterTypes()[0] != Object.class)) {
            throw new IllegalArgumentException(wreplace.toString());
        }

        try {
            VisibilityFilter vis = new VisibilityFilter(clazz);
            try {
                cstruct = clazz.getDeclaredConstructor(Constants.TYPES_EMPTY);
                if (!vis.accept(cstruct)) {
                    cstruct = null;
                }
            } catch (NoSuchMethodException ignore) {
            }
            constructorList = new ArrayList(Arrays.asList(clazz.getDeclaredConstructors()));
            filterMembers(constructorList, vis);
            if (constructorList.size() == 0) {
                throw new IllegalArgumentException("No visible constructors in " + clazz);
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
        } catch (ClassNotFoundException e) {
            throw new CodeGenerationException(e);
        }
    }

    protected void generate() throws NoSuchMethodException {
        if (wreplace == null) {
            wreplace = INTERNAL_WRITE_REPLACE;
        }
        declare_interface(Factory.class);
        declare_field(Modifier.PRIVATE, MethodInterceptor.class, INTERCEPTOR_FIELD);
        if (delegating) {
            declare_field(Modifier.PRIVATE, getSuperclass(), DELEGATE_FIELD);
        }

        generateConstructors();
        generateFactory();

        // Order is very important: must add superclass, then
        // its superclass chain, then each interface and
        // its superinterfaces.
        List methods = new ArrayList();
        addDeclaredMethods(methods, getSuperclass());

        Set forcePublic;
        if (interfaces != null) {
            declare_interfaces(interfaces);
            List interfaceMethods = new ArrayList();
            for (int i = 0; i < interfaces.length; i++) {
                addDeclaredMethods(interfaceMethods, interfaces[i]);
            }
            forcePublic = MethodWrapper.createSet(interfaceMethods);
            methods.addAll(interfaceMethods);
        } else {
            forcePublic = Collections.EMPTY_SET;
        }

        filterMembers(methods, new VisibilityFilter(getSuperclass()));
        if (delegating) {
            filterMembers(methods, new ModifierFilter(Modifier.PROTECTED, 0));
        }
        filterMembers(methods, new DuplicatesFilter());
        filterMembers(methods, new ModifierFilter(Modifier.FINAL, 0));
        if (filter != null) {
            filterMembers(methods, filter);
        }

        boolean declaresWriteReplace = false;
       
        for (int i = 0; i < methods.size(); i++) {
            Method method = (Method)methods.get(i);
            if (method.getName().equals("writeReplace") &&
                method.getParameterTypes().length == 0) {
                declaresWriteReplace = true;
            }
            String fieldName = getFieldName(i);
            String accessName = getAccessName(method, i);
            declare_field(PRIVATE_FINAL_STATIC, Method.class, fieldName);
            declare_field(PRIVATE_FINAL_STATIC, MethodProxy.class, accessName);
            generateAccessMethod(method, accessName);
            generateAroundMethod(method, fieldName, accessName,
                                 forcePublic.contains(MethodWrapper.create(method)));
        }
       
        generateClInit(methods);

        if (!declaresWriteReplace) {
            generateWriteReplace();
        }
    }

    private void filterMembers(List members, MethodFilter filter) {
        Iterator it = members.iterator();
        while (it.hasNext()) {
            if (!filter.accept((Member)it.next())) {
                it.remove();
            }
        }
    }

    private String getFieldName(int index) {
        return "METHOD_" + index;
    }
    
    private String getAccessName(Method method, int index) {
        return "CGLIB$ACCESS_" + index + "_" + method.getName();
    }

    private void generateConstructors() throws NoSuchMethodException {
        constructorTypes = new ArrayList(constructorList.size());
        for (Iterator i = constructorList.iterator(); i.hasNext();) {
            Constructor constructor = (Constructor)i.next();

            Class[] types = constructor.getParameterTypes();
            List typeList = new ArrayList(types.length + 2);
            typeList.addAll(Arrays.asList(types));
            typeList.add(MethodInterceptor.class);
            if (delegating) {
                typeList.add(Object.class);
            }
            Class[] argTypes = (Class[])typeList.toArray(new Class[typeList.size()]);
            constructorTypes.add(argTypes);

            begin_constructor(argTypes);
            load_this();

            dup();
            load_arg(types.length);
            putfield(INTERCEPTOR_FIELD);
            
            if (delegating) {
                dup();
                load_arg(types.length + 1);
                checkcast(getSuperclass());
                putfield(DELEGATE_FIELD);
            }

            load_args(0, types.length);
            super_invoke(constructor);
            return_value();       
            end_method();
        }
    }
    
    private void generateFactory() {
        begin_method(GET_INTERCEPTOR);
        load_this();
        getfield(INTERCEPTOR_FIELD);
        return_value();
        end_method();

        if (delegating) {
            throwWrongType(NORMAL_NEW_INSTANCE);
            throwWrongType(MULTIARG_NEW_INSTANCE);
            generateFactoryMethod(DELEGATE_NEW_INSTANCE);
            generateSetDelegate();
        } else {
            throwWrongType(DELEGATE_NEW_INSTANCE);
            throwWrongType(SET_DELEGATE);
            generateFactoryMethod(NORMAL_NEW_INSTANCE);
            generateMultiArgFactory();
        }
    }

    private void generateSetDelegate() {
        begin_method(SET_DELEGATE);
        load_this();
        load_arg(0);
        checkcast(getSuperclass());
        putfield(DELEGATE_FIELD);
        return_value();
        end_method();
    }

    private void generateMultiArgFactory() {
        declare_field(PRIVATE_FINAL_STATIC, Map.class, CONSTRUCTOR_PROXY_MAP); 
        begin_method(MULTIARG_NEW_INSTANCE);
        getfield(CONSTRUCTOR_PROXY_MAP);
        load_arg(0);// Class[] types
        invoke(NEW_CLASS_KEY);//key
        invoke(MethodConstants.MAP_GET);// PROXY_MAP.get( key(types) )    
        checkcast(ConstructorProxy.class);
        dup();
        ifnull("fail");
        load_arg(1);
        load_arg(2);
        invoke(PROXY_NEW_INSTANCE);
        return_value();
        nop("fail");
        throw_exception(IllegalArgumentException.class, "Constructor not found ");
        end_method();
    }

    private void throwWrongType(Method method) {
        begin_method(method);
        throw_exception(UnsupportedOperationException.class,
                        "Using a delegating enhanced class as non-delegating, or the reverse");
        return_value();
        end_method();
    }

    private void generateWriteReplace() {
        begin_method(Modifier.PRIVATE,
                     Object.class, 
                     "writeReplace",
                     Constants.TYPES_EMPTY,
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
        if (delegating) {
            load_this();
            getfield(DELEGATE_FIELD);
            load_args();
            invoke(method);
        } else if (Modifier.isAbstract(method.getModifiers())) {
            throw_exception(AbstractMethodError.class, method.toString() + " is abstract" );
        } else {
            load_this();
            load_args();
            super_invoke(method);
        }
        return_value();
        end_method();
    }

    private void generateAroundMethod(Method method, String fieldName, String accessName, boolean forcePublic) {
        int modifiers = getDefaultModifiers(method);
        if (forcePublic) {
            modifiers = (modifiers & ~Modifier.PROTECTED) | Modifier.PUBLIC;
        }
        begin_method(method, modifiers);
        Object handler = begin_handler();
        load_this();
        getfield(INTERCEPTOR_FIELD);
        load_this();
        getfield(fieldName);
        create_arg_array();
        getfield(accessName);
        invoke(AROUND_ADVICE);
        unbox_or_zero(method.getReturnType());
        return_value();
        end_handler();
        generateHandleUndeclared(method, handler);
        end_method();
    }

    private void generateHandleUndeclared(Method method, Object handler) {
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
            invoke_constructor(UndeclaredThrowableException.class, Constants.TYPES_THROWABLE);
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
             CGLIB$ACCESS_0 = MethodProxy.create(proxied);
           }
        */
        
        begin_static();
        for (int i = 0, size = methodList.size(); i < size; i++) {
            Method method = (Method)methodList.get(i);
            String fieldName = getFieldName(i);

            load_class(method.getDeclaringClass());
            push(method.getName());
            push_object(method.getParameterTypes());
            dup();
            store_local("args");
            invoke(MethodConstants.GET_DECLARED_METHOD);
            putfield(fieldName);

            String accessName = getAccessName(method, i);
            load_class_this();
            push(accessName);
            load_local("args");
            invoke(MethodConstants.GET_DECLARED_METHOD);
            invoke(MAKE_PROXY);
            putfield(accessName);
        }

        if (!delegating) {
            new_instance(HashMap.class);
            dup();
            dup();
            invoke_constructor(HashMap.class);
            putfield(CONSTRUCTOR_PROXY_MAP);
            final String LOCAL_MAP = "map";
            store_local(LOCAL_MAP);
            for (int i = 0, size = constructorList.size(); i < size; i++) {
                Constructor constructor = (Constructor)constructorList.get(i);
                Class[] types = constructor.getParameterTypes();
                Class[] argTypes = (Class[])constructorTypes.get(i);
                load_local(LOCAL_MAP);
                push(types);
                invoke(NEW_CLASS_KEY);//key
                load_class_this();
                push(argTypes);
                invoke(MethodConstants.GET_DECLARED_CONSTRUCTOR);
                invoke(MAKE_CONSTRUCTOR_PROXY);//value
                invoke(MethodConstants.MAP_PUT);// put( key( agrgTypes[] ), proxy  )
            }
        }
        
        return_value();
        end_method();
    }
}
