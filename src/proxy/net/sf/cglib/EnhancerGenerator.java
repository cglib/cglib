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

/*package*/ class EnhancerGenerator extends CodeGenerator {
    private static final String INTERCEPTOR_FIELD = "CGLIB$INTERCEPTOR";
    private static final String DELEGATE_FIELD = "CGLIB$DELEGATE";
    private static final String CONSTRUCTOR_PROXY_MAP = "CGLIB$CONSTRUCTOR_PROXY_MAP";
    private static final Class[] NORMAL_ARGS = new Class[]{ MethodInterceptor.class };
    private static final Class[] DELEGATE_ARGS = new Class[]{ MethodInterceptor.class, Object.class };
    private static final  int PRIVATE_FINAL_STATIC = Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC;
    private static final Method AROUND_ADVICE =
      ReflectUtils.findMethod("MethodInterceptor.aroundAdvice(Object, Method, Object[], MethodProxy)");
    private static final Method MAKE_PROXY =
      ReflectUtils.findMethod("MethodProxy.create(Method)");
    private static final Method MAKE_CONSTRUCTOR_PROXY =
      ReflectUtils.findMethod("ConstructorProxy.create(Constructor)");
    private static final Method INTERNAL_WRITE_REPLACE =
      ReflectUtils.findMethod("Enhancer$InternalReplace.writeReplace(Object)");
    private static final Method NEW_CALSS_KEY = 
     ReflectUtils.findMethod("ConstructorProxy.newClassKey(Class[])");
    private static final Method NEW_INSTANCE = 
       ReflectUtils.findMethod("Factory.newInstance(Class[],Object[],MethodInterceptor)");
   
    
    private Class[] interfaces;
    private Method wreplace;
    private MethodInterceptor ih;
    private boolean delegating;
    private MethodFilter filter;
    private Constructor cstruct;
    private List constructorList;
    private List constructorTypes = new ArrayList();
        
    /* package */ EnhancerGenerator( String className, Class clazz, 
                                     Class[] interfaces,
                                     MethodInterceptor ih, ClassLoader loader, 
                                     Method wreplace, boolean delegating,
                                     MethodFilter filter) {
        super(className, clazz, loader);
        this.interfaces = interfaces;
        this.ih = ih;
        this.wreplace = wreplace;
        this.delegating = delegating;
        this.filter = filter;  
     
        if (wreplace != null && 
            (!Modifier.isStatic(wreplace.getModifiers()) ||
             !Modifier.isPublic(wreplace.getModifiers()) ||
             wreplace.getReturnType() != TYPE_OBJECT || 
             wreplace.getParameterTypes().length != 1 ||
             wreplace.getParameterTypes()[0] != TYPE_OBJECT)) {
            throw new IllegalArgumentException(wreplace.toString());
        }

        try {
            
            
            try {
                cstruct = clazz.getDeclaredConstructor(delegating ? DELEGATE_ARGS : NORMAL_ARGS);
            } catch (NoSuchMethodException e) {
                cstruct = clazz.getDeclaredConstructor(TYPES_EMPTY);
            }

            if (!VisibilityFilter.accept(cstruct, clazz.getPackage())) {
                 cstruct = null;
            }

            Constructor[] constructors = clazz.getDeclaredConstructors();
            
            constructorList = new ArrayList(constructors.length);
            for(int i = 0; i< constructors.length; i++  ){
               if( VisibilityFilter.accept(constructors[i], clazz.getPackage()) ){
                 constructorList.add(constructors[i]);    
               }
            }
            
            if( constructorList.size() == 0  ){
                  throw new IllegalArgumentException(clazz.getName());
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

        filterMethods(methods, new VisibilityFilter(getSuperclass()));
        if (delegating) {
            filterMethods(methods, new ModifierFilter(Modifier.PROTECTED, 0));
        }
        filterMethods(methods, new DuplicatesFilter());
        filterMethods(methods, new ModifierFilter(Modifier.FINAL, 0));
        if (filter != null) {
            filterMethods(methods, filter);
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

    private void filterMethods(List methods, MethodFilter filter) {
        Iterator it = methods.iterator();
        while (it.hasNext()) {
            if (!filter.accept((Method)it.next())) {
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
    
        for( Iterator i = constructorList.iterator(); i.hasNext(); ){
          Constructor constructor = (Constructor)i.next();
          
          Class[] types = constructor.getParameterTypes();
          Class[] argTypes = new Class[
                  types.length + 
                  ( delegating ? 1 : 0 ) + 
                  ( ( types.length == 0 || 
                      types[types.length - 1] != MethodInterceptor.class)  ? 1 : 0 )
           ];
                      
          System.arraycopy(types, 0, argTypes, 0, types.length );
          
          if(delegating){
            argTypes[ argTypes.length - 1 ] = Object.class; 
            argTypes[ argTypes.length - 2 ] = MethodInterceptor.class; 
          }else{
             argTypes[ argTypes.length - 1 ] = MethodInterceptor.class; 
          }
          
          constructorTypes.add(argTypes);
          
          begin_constructor(argTypes);
           load_this();
           dup();
           load_args(0,types.length);
           super_invoke_constructor(types);
           load_arg(argTypes.length - ( delegating ? 2 : 1 ));
           putfield(INTERCEPTOR_FIELD);
         if (delegating) {
            load_this();
            load_arg(argTypes.length - 1);
            checkcast(getSuperclass());
            putfield(DELEGATE_FIELD);
        }
         return_value();       
        
        end_constructor();
        
      }
        
    }
    
    
    private void generateFactory() throws NoSuchMethodException {
        generateFactoryHelper(NORMAL_ARGS, !delegating);
        generateFactoryHelper(DELEGATE_ARGS, delegating);

        begin_method(Factory.class.getMethod("getDelegate", null));
        if (delegating) {
            load_this();
            getfield(DELEGATE_FIELD);
        } else {
            aconst_null();
        }
        return_value();
        end_method();

        begin_method(Factory.class.getMethod("setDelegate", TYPES_OBJECT));
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

        begin_method(Factory.class.getMethod("getInterceptor", null));
        load_this();
        getfield(INTERCEPTOR_FIELD);
        return_value();
        end_method();
    }

    private void throwWrongType() {
        
        throwException(UnsupportedOperationException.class,
        "Using a delegating enhanced class as non-delegating, or the reverse");
        
    }

    private void generateFactoryHelper(Class[] types, boolean enabled) throws NoSuchMethodException {
        begin_method(Factory.class.getMethod("newInstance", types));
        if( cstruct == null ){
          throwException(IllegalStateException.class, "can't find constructor" );
        }else if (enabled) {
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
                     TYPES_EMPTY,
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
            throwException(AbstractMethodError.class, method.toString() + " is abstract" );
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
            invoke_constructor(UndeclaredThrowableException.class, TYPES_THROWABLE);
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
        declare_field(PRIVATE_FINAL_STATIC, Map.class, CONSTRUCTOR_PROXY_MAP ); 
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
            putstatic(fieldName);

            String accessName = getAccessName(method, i);
            load_class_this();
            push(accessName);
            load_local("args");
            invoke(MethodConstants.GET_DECLARED_METHOD);
            invoke(MAKE_PROXY);
            putstatic(accessName);
        }
        new_instance(HashMap.class);
        dup();
        dup();
        invoke_constructor(HashMap.class);
        putstatic(CONSTRUCTOR_PROXY_MAP);
        store_local("args");//reuses alocal for map
        for( Iterator i = constructorTypes.iterator(); i.hasNext(); ){
            Class[] types = (Class[])i.next();
            load_local("args");
            push_object(types);//constructor types
            dup();
            invoke(NEW_CALSS_KEY);//key
            swap();
            load_class_this();
            swap();
            invoke(MethodConstants.GET_DECLARED_CONSTRUCTOR);
            invoke(MAKE_CONSTRUCTOR_PROXY);//value
            invoke(MethodConstants.MAP_PUT);// put( key( agrgTypes[] ), proxy  )
            
        }
        
        return_value();
        end_static();
    }
}
