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
import net.sf.cglib.util.*;

class EnhancerGenerator
extends CodeGenerator
{
    private static final int PRIVATE_FINAL_STATIC = Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC;
    private static final String CONSTRUCTOR_PROXY_MAP = "CGLIB$CONSTRUCTOR_PROXY_MAP";
    private static final String CONSTRUCTED_FIELD = "CGLIB$CONSTRUCTED";
    private static final String SET_THREAD_CALLBACKS = "CGLIB$SET_THREAD_CALLBACKS";

    private static final Method MAKE_CONSTRUCTOR_PROXY =
      ReflectUtils.findMethod("ConstructorProxy.create(Constructor)");
    private static final Method PROXY_NEW_INSTANCE = 
      ReflectUtils.findMethod("ConstructorProxy.newInstance(Object[])");
    private static final Method NEW_CLASS_KEY = 
      ReflectUtils.findMethod("ConstructorProxy.newClassKey(Class[])");
    private static final Method INTERNAL_WRITE_REPLACE =
      ReflectUtils.findMethod("Enhancer$InternalReplace.writeReplace(Object)");
    private static final Method NEW_INSTANCE =
      ReflectUtils.findMethod("Factory.newInstance(MethodInterceptor)");
    private static final Method MULTIARG_NEW_INSTANCE = 
      ReflectUtils.findMethod("Factory.newInstance(Class[], Object[], MethodInterceptor)");
    private static final Method GET_INTERCEPTOR =
      ReflectUtils.findMethod("Factory.interceptor()");
    private static final Method SET_INTERCEPTOR =
      ReflectUtils.findMethod("Factory.interceptor(MethodInterceptor)");
    private static final Method CALLBACK_NEW_INSTANCE =
      ReflectUtils.findMethod("Factory.newInstance(Callbacks)");
    private static final Method CALLBACK_MULTIARG_NEW_INSTANCE = 
      ReflectUtils.findMethod("Factory.newInstance(Class[], Object[], Callbacks)");
    private static final Method GET_CALLBACK =
      ReflectUtils.findMethod("Factory.callback(int)");
    private static final Method SET_CALLBACK =
      ReflectUtils.findMethod("Factory.callback(int, Callback)");
    private static final Method SET_CALLBACKS =
      ReflectUtils.findMethod("Factory.callbacks(Callbacks)");
    private static final Method CALLBACKS_GET =
      ReflectUtils.findMethod("Callbacks.get(int)");
    
    private final Class[] interfaces;
    private final Method wreplace;
    private final CallbackFilter filter;
    private final Callbacks initialCallbacks;
    private final BitSet usedCallbacks = new BitSet();

    public EnhancerGenerator(Class type,
                             Class[] interfaces,
                             Method wreplace,
                             CallbackFilter filter,
                             Callbacks callbacks) {
        setSuperclass(type);
        this.interfaces = interfaces;
        this.wreplace = wreplace;
        this.filter = filter;
        initialCallbacks = callbacks;

        if (interfaces != null) {
            addInterfaces(interfaces);
        }
        addInterface(Factory.class);
    }

    protected void generate() throws Exception {
        Class type = getSuperclass();

        List constructors = new ArrayList(Arrays.asList(type.getDeclaredConstructors()));
        filterMembers(constructors, new VisibilityFilter(type));
        if (constructors.size() == 0) {
            throw new IllegalArgumentException("No visible constructors in " + type);
        }

        ensureLoadable(type);
        ensureLoadable(interfaces);
        
        // Order is very important: must add superclass, then
        // its superclass chain, then each interface and
        // its superinterfaces.
        List methods = new ArrayList();
        addDeclaredMethods(methods, getSuperclass());

        Set forcePublic = Collections.EMPTY_SET;
        if (interfaces != null) {
            List interfaceMethods = new ArrayList();
            for (int i = 0; i < interfaces.length; i++) {
                addDeclaredMethods(interfaceMethods, interfaces[i]);
            }
            forcePublic = MethodWrapper.createSet(interfaceMethods);
            methods.addAll(interfaceMethods);
        }

        filterMembers(methods, new VisibilityFilter(getSuperclass()));
        filterMembers(methods, new DuplicatesFilter());
        filterMembers(methods, new ModifierFilter(Modifier.FINAL, 0));

        boolean declaresWriteReplace = false;
        int len = Callbacks.MAX_VALUE + 1;
        CallbackGenerator[] generators = new CallbackGenerator[len];
        List[] group = new List[len];
        for (Iterator it = methods.iterator(); it.hasNext();) {
            Method method = (Method)it.next();
            int ctype = (filter == null) ? Callbacks.INTERCEPT : filter.accept(method);
            if (ctype > Callbacks.MAX_VALUE) {
                // TODO: error
            }
            if (group[ctype] == null) {
                group[ctype] = new ArrayList(methods.size());
                generators[ctype] = Callbacks.getGenerator(ctype);
            }
            group[ctype].add(method);

            if (method.getName().equals("writeReplace") &&
                method.getParameterTypes().length == 0) {
                declaresWriteReplace = true;
            }
        }
        if (!declaresWriteReplace) {
            generateWriteReplace();
        }

        // the order of these methods is important
        generateConstructors(constructors); // #1
        generateMethods(generators, group, forcePublic); // #2
        // #3 .. N
        generateLegacyFactory();
        generateFactory();
        generateStatic(constructors, generators, group);
        generateSetThreadCallbacks();
    }

    protected void postDefine(Class type) throws Exception {
        Method setter = type.getDeclaredMethod(SET_THREAD_CALLBACKS,
                                               new Class[]{ Callbacks.class });
        setter.invoke(null, new Object[]{ initialCallbacks });
    }
    
    private static void filterMembers(List members, MethodFilter filter) {
        Iterator it = members.iterator();
        while (it.hasNext()) {
            if (!filter.accept((Member)it.next())) {
                it.remove();
            }
        }
    }

    private static void addDeclaredMethods(List methodList, Class type) {
        methodList.addAll(java.util.Arrays.asList(type.getDeclaredMethods()));
      
        Class superclass = type.getSuperclass();
        if (superclass != null) {
            addDeclaredMethods(methodList, superclass);
        }

        Class[] interfaces = type.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            addDeclaredMethods(methodList, interfaces[i]);
        }
    }

    private void generateConstructors(List constructors) throws NoSuchMethodException {
        declare_field(Modifier.PRIVATE, Boolean.TYPE, CONSTRUCTED_FIELD);
        for (Iterator i = constructors.iterator(); i.hasNext();) {
            Constructor constructor = (Constructor)i.next();
            begin_constructor(constructor);
            load_this();
            dup();
            load_args();
            super_invoke(constructor);
            push(1);
            putfield(CONSTRUCTED_FIELD);
            return_value();
            end_method();
        }
    }

    private void generateWriteReplace() throws ClassNotFoundException {
        Method wr = wreplace;
        if (wr == null) {
            wr = INTERNAL_WRITE_REPLACE;
        } else if (!Modifier.isStatic(wr.getModifiers()) ||
                   !Modifier.isPublic(wr.getModifiers()) ||
                   wr.getReturnType() != Object.class || 
                   wr.getParameterTypes().length != 1 ||
                   wr.getParameterTypes()[0] != Object.class) {
            throw new IllegalArgumentException(wr.toString());
        }
        ensureLoadable(wr.getDeclaringClass());

        begin_method(Modifier.PRIVATE,
                     Object.class, 
                     "writeReplace",
                     null,
                     new Class[]{ ObjectStreamException.class });
        load_this();
        invoke(wr);
        return_value();
        end_method();
    }

    private void generateLegacyFactory() {
        if (!usedCallbacks.get(Callbacks.INTERCEPT)) {
            throwIllegalState(GET_INTERCEPTOR);
            throwIllegalState(SET_INTERCEPTOR);
            throwIllegalState(NEW_INSTANCE);
            throwIllegalState(MULTIARG_NEW_INSTANCE);
        } else {
            // Factory.interceptor()
            begin_method(GET_INTERCEPTOR);
            load_this();
            push(Callbacks.INTERCEPT);
            invoke(GET_CALLBACK);
            return_value();
            end_method();

            // Factory.interceptor(MethodInterceptor)
            begin_method(SET_INTERCEPTOR);
            load_this();
            push(Callbacks.INTERCEPT);
            load_arg(0);
            invoke(SET_CALLBACK);
            return_value();
            end_method();

            // Factory.newInstance(MethodInterceptor)
            begin_method(NEW_INSTANCE);
            load_this();
            getfield(getThreadLocal(Callbacks.INTERCEPT));
            load_arg(0);
            invoke(MethodConstants.THREADLOCAL_SET);
            new_instance_this();
            dup();
            invoke_constructor_this();
            dup();
            load_arg(0);
            putfield(getCallbackField(Callbacks.INTERCEPT));
            return_value();
            end_method();

            // Factory.newInstance(Class[], Object[], MethodInterceptor)
            begin_method(MULTIARG_NEW_INSTANCE);
            load_this();
            getfield(getThreadLocal(Callbacks.INTERCEPT));
            load_arg(2);
            invoke(MethodConstants.THREADLOCAL_SET);
            load_this();
            load_arg(0);
            load_arg(1);
            aconst_null();
            invoke(CALLBACK_MULTIARG_NEW_INSTANCE);
            load_this();
            load_arg(2);
            putfield(getCallbackField(Callbacks.INTERCEPT));
            return_value();
            end_method();
        }
    }

    private void generateFactory() throws Exception {
        int[] keys = getCallbackKeys();

        // Factory.callback(int)
        begin_method(GET_CALLBACK);
        load_this();
        load_arg(0);
        process_switch(keys, new ProcessSwitchCallback() {
                public void processCase(int key, Label end) throws Exception {
                    getfield(getCallbackField(key));
                    goTo(end);
                }
                public void processDefault() throws Exception {
                    pop(); // stack height
                    aconst_null();
                }
            });
        return_value();
        end_method();

        // Factory.callback(int, Callback)
        begin_method(SET_CALLBACK);
        load_this();
        load_arg(1);
        load_arg(0);
        process_switch(keys, new ProcessSwitchCallback() {
                public void processCase(int key, Label end) throws Exception {
                    putfield(getCallbackField(key));
                    goTo(end);
                }
                public void processDefault() {
                    pop2(); // stack height
                }
            });
        return_value();
        end_method();
        
        // Factory.callbacks(Callbacks);
        begin_method(SET_CALLBACKS);
        load_this();
        load_arg(0);
        generateSetCallbacks();
        return_value();
        end_method();

        // Factory.newInstance(Callbacks)
        begin_method(CALLBACK_NEW_INSTANCE);
        load_arg(0);
        invoke_static_this(SET_THREAD_CALLBACKS, Void.TYPE, new Class[]{ Callbacks.class });
        new_instance_this();
        dup();
        invoke_constructor_this();
        dup();
        load_arg(0);
        generateSetCallbacks();
        return_value();
        end_method();

        // Factory.newInstance(Class[], Object[], Callbacks)
        Label skipSetCallbacks = make_label();
        Label fail = make_label();
        declare_field(PRIVATE_FINAL_STATIC, Map.class, CONSTRUCTOR_PROXY_MAP); 
        begin_method(CALLBACK_MULTIARG_NEW_INSTANCE);

        load_arg(2);
        invoke_static_this(SET_THREAD_CALLBACKS, Void.TYPE, new Class[]{ Callbacks.class });

        getfield(CONSTRUCTOR_PROXY_MAP);
        load_arg(0); // Class[] types
        invoke(NEW_CLASS_KEY); // key
        invoke(MethodConstants.MAP_GET); // PROXY_MAP.get(key(types))
        checkcast(ConstructorProxy.class);
        dup();
        ifnull(fail);
        load_arg(1);
        invoke(PROXY_NEW_INSTANCE);
        checkcast_this();
        load_arg(2);
        ifnull(skipSetCallbacks);
        dup();
        load_arg(2);
        generateSetCallbacks();
        
        mark(skipSetCallbacks);
        return_value();

        mark(fail);
        throw_exception(IllegalArgumentException.class, "Constructor not found");
        end_method();
    }

    private void generateSetCallbacks() {
        if (usedCallbacks.length() == 0) {
            pop2(); // stack height
        } else {
            for (int i = 0; i <= Callbacks.MAX_VALUE; i++) {
                if (usedCallbacks.get(i)) {
                    if (i + 1 < usedCallbacks.length())
                        dup2();
                    push(i);
                    invoke(CALLBACKS_GET);
                    checkcast(Callbacks.getType(i));
                    putfield(getCallbackField(i));
                }
            }
        }
    }

    private int[] getCallbackKeys() {
        int c = 0;
        for (int i = 0; i <= Callbacks.MAX_VALUE; i++) {
            if (usedCallbacks.get(i)) {
                c++;
            }
        }
        int[] keys = new int[c];
        c = 0;
        for (int i = 0; i <= Callbacks.MAX_VALUE; i++) {
            if (usedCallbacks.get(i)) {
                keys[c++] = i;
            }
        }
        return keys;
    }

    private void throwIllegalState(Method method) {
        begin_method(method);
        throw_exception(IllegalStateException.class, "MethodInterceptor does not apply to this object");
        return_value();
        end_method();
    }

    private void generateMethods(CallbackGenerator[] generators, List[] methods, final Set forcePublic) throws Exception {
        for (int i = 0; i <= Callbacks.MAX_VALUE; i++) {
            final int type = i;
            if (generators[type] != null) {
                generators[type].generate(this, methods[i], new CallbackGenerator.Context() {
                    public void emitCallback() {
                        generateCurrentCallback(type);
                    }
                    public int getModifiers(Method method) {
                        int modifiers = getDefaultModifiers(method);
                        if (forcePublic.contains(MethodWrapper.create(method))) {
                            modifiers = (modifiers & ~Modifier.PROTECTED) | Modifier.PUBLIC;
                        }
                        return modifiers;
                    }
                });
            }
        }
    }

    private void generateSetThreadCallbacks() {
        begin_method(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL,
                     Void.TYPE,
                     SET_THREAD_CALLBACKS,
                     new Class[]{ Callbacks.class },
                     null);
        Label end = make_label();
        load_arg(0);
        ifnull(end);
        for (int i = 0; i <= Callbacks.MAX_VALUE; i++) {
            if (usedCallbacks.get(i)) {
                load_arg(0);
                push(i);
                invoke(CALLBACKS_GET);
                getfield(getThreadLocal(i));
                swap();
                invoke(MethodConstants.THREADLOCAL_SET);
            }
        }
        mark(end);
        return_value();
        end_method();
    }

    private void generateCurrentCallback(int type) {
        if (!usedCallbacks.get(type)) {
            declare_field(Modifier.PRIVATE, Callbacks.getType(type), getCallbackField(type));
            declare_field(PRIVATE_FINAL_STATIC, ThreadLocal.class, getThreadLocal(type));
            usedCallbacks.set(type);
        }
        load_this();
        getfield(getCallbackField(type));
        dup();
        Label end = make_label();
        ifnonnull(end);
        load_this();
        getfield(CONSTRUCTED_FIELD);
        ifne(end);
        pop();
        getfield(getThreadLocal(type));
        invoke(MethodConstants.THREADLOCAL_GET);
        checkcast(Callbacks.getType(type));
        mark(end);
    }

    private String getCallbackField(int type) {
        return "CGLIB$CALLBACK_" + type;
    }

    private String getThreadLocal(int type) {
        return "CGLIB$TL_CALLBACK_" + type;
    }

    private void generateStatic(List constructors, CallbackGenerator[] generators, List[] methods)  throws Exception {
        begin_static();

        new_instance(HashMap.class);
        dup();
        invoke_constructor(HashMap.class);
        dup();
        putfield(CONSTRUCTOR_PROXY_MAP);
        Local map = make_local();
        store_local(map);
        for (int i = 0, size = constructors.size(); i < size; i++) {
            Constructor constructor = (Constructor)constructors.get(i);
            Class[] types = constructor.getParameterTypes();
            load_local(map);
            push(types);
            invoke(NEW_CLASS_KEY); // key
            load_class_this();
            push(types);
            invoke(MethodConstants.GET_DECLARED_CONSTRUCTOR);
            invoke(MAKE_CONSTRUCTOR_PROXY); // value
            invoke(MethodConstants.MAP_PUT); // put(key(agrgTypes[]), proxy)
            pop();
        }

        for (int i = 0; i <= Callbacks.MAX_VALUE; i++) {
            if (usedCallbacks.get(i)) {
                new_instance(ThreadLocal.class);
                dup();
                invoke_constructor(ThreadLocal.class);
                putfield(getThreadLocal(i));
            }
        }

        for (int i = 0; i <= Callbacks.MAX_VALUE; i++) {
            if (generators[i] != null) {
                generators[i].generateStatic(this, methods[i]);
            }
        }

        return_value();
        end_method();
    }
}
