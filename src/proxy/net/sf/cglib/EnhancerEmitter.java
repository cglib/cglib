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
import net.sf.cglib.core.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

class EnhancerEmitter extends Emitter {
    private static final String CONSTRUCTED_FIELD = "CGLIB$CONSTRUCTED";

    private static final Type ILLEGAL_STATE_EXCEPTION =
      TypeUtils.parseType("IllegalStateException");
    private static final Type ILLEGAL_ARGUMENT_EXCEPTION =
      TypeUtils.parseType("IllegalArgumentException");
    private static final Type THREAD_LOCAL =
      TypeUtils.parseType("ThreadLocal");
    private static final Type FACTORY =
      TypeUtils.parseType("net.sf.cglib.Factory");
    private static final Type CALLBACKS =
      TypeUtils.parseType("net.sf.cglib.Callbacks");

    private static final Signature CSTRUCT_NULL =
      TypeUtils.parseConstructor("");
    private static final Signature SET_THREAD_CALLBACKS =
      TypeUtils.parseSignature("void CGLIB$SET_THREAD_CALLBACKS(net.sf.cglib.Callbacks)");
    private static final Signature NEW_INSTANCE =
      TypeUtils.parseSignature("Object newInstance(net.sf.cglib.Callbacks)");
    private static final Signature MULTIARG_NEW_INSTANCE =
      TypeUtils.parseSignature("Object newInstance(Class[], Object[], net.sf.cglib.Callbacks)");
    private static final Signature SINGLE_NEW_INSTANCE =
      TypeUtils.parseSignature("Object newInstance(net.sf.cglib.Callback)");
    private static final Signature GET_CALLBACK =
      TypeUtils.parseSignature("net.sf.cglib.Callback getCallback(int)");
    private static final Signature SET_CALLBACK =
      TypeUtils.parseSignature("void setCallback(int, net.sf.cglib.Callback)");
    private static final Signature SET_CALLBACKS =
      TypeUtils.parseSignature("void setCallbacks(net.sf.cglib.Callbacks)");
    private static final Signature CALLBACKS_GET =
      TypeUtils.parseSignature("net.sf.cglib.Callback get(int)");

    private static final Signature THREAD_LOCAL_GET =
      TypeUtils.parseSignature("Object get()");
    private static final Signature THREAD_LOCAL_SET =
      TypeUtils.parseSignature("void set(Object)");
        
    private final TinyBitSet usedCallbacks = new TinyBitSet();

    public EnhancerEmitter(ClassVisitor v,
                           String className,
                           Class superclass,
                           Class[] interfaces,
                           CallbackFilter filter) throws Exception {
        super(v);
        interfaces = ReflectUtils.add(interfaces, Factory.class);
        if (superclass == null) {
            superclass = Object.class;
        }

        begin_class(Constants.ACC_PUBLIC,
                    className,
                    Type.getType(superclass),
                    TypeUtils.getTypes(interfaces),
                    Constants.SOURCE_FILE);
        
        List constructors = new ArrayList(Arrays.asList(superclass.getDeclaredConstructors()));
        CollectionUtils.filter(constructors, new VisibilityPredicate(superclass, true));
        if (constructors.size() == 0) {
            throw new IllegalArgumentException("No visible constructors in " + superclass);
        }

        // Order is very important: must add superclass, then
        // its superclass chain, then each interface and
        // its superinterfaces.
        List methods = new ArrayList();
        ReflectUtils.addAllMethods(superclass, methods);

        List interfaceMethods = new ArrayList();
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i] != Factory.class) {
                ReflectUtils.addAllMethods(interfaces[i], interfaceMethods);
            }
        }
        Set forcePublic = MethodWrapper.createSet(interfaceMethods);
        methods.addAll(interfaceMethods);
        CollectionUtils.filter(methods, new VisibilityPredicate(superclass, true));
        CollectionUtils.filter(methods, new DuplicatesPredicate());
        removeFinal(methods);

        int len = Callbacks.MAX_VALUE + 1;
        CallbackGenerator[] generators = new CallbackGenerator[len];
        List[] group = new List[len];
        for (Iterator it = methods.iterator(); it.hasNext();) {
            Method method = (Method)it.next();
            int ctype = filter.accept(method);
            if (ctype > Callbacks.MAX_VALUE) {
                // TODO: error
            }
            if (group[ctype] == null) {
                group[ctype] = new ArrayList(methods.size());
                generators[ctype] = CallbackUtils.getGenerator(ctype);
            }
            group[ctype].add(method);
        }

        declare_field(Constants.ACC_PRIVATE, CONSTRUCTED_FIELD, Type.BOOLEAN_TYPE, null);
        generateConstructors(constructors);

        CallbackGenerator.Context[] contexts = createContexts(generators, group, forcePublic);
        generateMethods(generators, contexts);
        generateStatic(generators, contexts);
        generateFactory(constructors);
        generateSetThreadCallbacks();

        end_class();
    }

    private void generateConstructors(List constructors) throws NoSuchMethodException {
        for (Iterator i = constructors.iterator(); i.hasNext();) {
            Constructor constructor = (Constructor)i.next();
            ReflectOps.begin_constructor(this, constructor);
            load_this();
            dup();
            load_args();
            ReflectOps.super_invoke(this, constructor);
            push(1);
            putfield(CONSTRUCTED_FIELD);
            return_value();
        }
    }

    private void generateFactory(List constructors) throws Exception {
        int[] keys = getCallbackKeys();

        // Factory.getCallback(int)
        begin_method(Constants.ACC_PUBLIC, GET_CALLBACK, null);
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

        // Factory.setCallback(int, Callback)
        begin_method(Constants.ACC_PUBLIC, SET_CALLBACK, null);
        load_this();
        load_arg(1);
        load_arg(0);
        process_switch(keys, new ProcessSwitchCallback() {
            public void processCase(int key, Label end) throws Exception {
                checkcast(CallbackUtils.getType2(key));
                putfield(getCallbackField(key));
                goTo(end);
            }
            public void processDefault() {
                pop2(); // stack height
            }
        });
        return_value();
        
        // Factory.setCallbacks(Callbacks);
        begin_method(Constants.ACC_PUBLIC, SET_CALLBACKS, null);
        load_this();
        load_arg(0);
        generateSetCallbacks();
        return_value();

        // Factory.newInstance(Callbacks)
        begin_method(Constants.ACC_PUBLIC, NEW_INSTANCE, null);
        load_arg(0);
        invoke_static_this(SET_THREAD_CALLBACKS);
        new_instance_this();
        dup();
        invoke_constructor_this();
        dup();
        load_arg(0);
        generateSetCallbacks();
        return_value();

        // Factory.newInstance(Callback)
        begin_method(Constants.ACC_PUBLIC, SINGLE_NEW_INSTANCE, null);
        switch (usedCallbacks.cardinality()) {
        case 1:
            int type = usedCallbacks.length() - 1;
            getfield(getThreadLocal(type));
            load_arg(0);
            invoke_virtual(THREAD_LOCAL, THREAD_LOCAL_SET);
            new_instance_this();
            dup();
            invoke_constructor_this();
            dup();
            push(type);
            load_arg(0);
            invoke_virtual_this(SET_CALLBACK);
            break;
        case 0:
            // TODO: make sure Callback is null?
            new_instance_this();
            dup();
            invoke_constructor_this();
            break;
        default:
            throw_exception(ILLEGAL_STATE_EXCEPTION, "More than one callback object required");
        }
        return_value();
        
        // Factory.newInstance(Class[], Object[], Callbacks)
        Label skipSetCallbacks = make_label();
        begin_method(Constants.ACC_PUBLIC, MULTIARG_NEW_INSTANCE, null);
        load_arg(2);
        invoke_static_this(SET_THREAD_CALLBACKS);
        new_instance_this();
        dup();
        load_arg(0);
        ReflectOps.constructor_switch(this, (Constructor[])constructors.toArray(new Constructor[0]), new ObjectSwitchCallback() {
            public void processCase(Object key, Label end) throws Exception {
                Constructor constructor = (Constructor)key;
                Type types[] = TypeUtils.getTypes(constructor.getParameterTypes());
                for (int i = 0; i < types.length; i++) {
                    load_arg(1);
                    push(i);
                    aaload();
                    unbox(types[i]);
                }
                invoke_constructor_this(ReflectUtils.getSignature(constructor));
                goTo(end);
            }
            public void processDefault() {
                throw_exception(ILLEGAL_ARGUMENT_EXCEPTION, "Constructor not found");
            }
        });
        load_arg(2);
        ifnull(skipSetCallbacks);
        dup();
        load_arg(2);
        generateSetCallbacks();        
        mark(skipSetCallbacks);
        return_value();
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
                    invoke_interface(CALLBACKS, CALLBACKS_GET);
                    checkcast(CallbackUtils.getType2(i));
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

    private CallbackGenerator.Context[] createContexts(CallbackGenerator[] generators,
                                                       List[] methods,
                                                       final Set forcePublic) {
        CallbackGenerator.Context[] contexts = new CallbackGenerator.Context[Callbacks.MAX_VALUE + 1];
        for (int i = 0; i <= Callbacks.MAX_VALUE; i++) {
            final int type = i;
            final List fmethods = methods[i];
            if (generators[type] != null) {
                contexts[type] = new CallbackGenerator.Context() {
                    public Iterator getMethods() {
                        return fmethods.iterator();
                    }
                    public void emitCallback() {
                        generateCurrentCallback(type);
                    }
                    public int getModifiers(Method method) {
                        int modifiers = ReflectUtils.getDefaultModifiers(method);
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
            }
        }
        return contexts;
    }

    private void generateMethods(CallbackGenerator[] generators, CallbackGenerator.Context[] contexts) throws Exception {
        for (int i = 0; i <= Callbacks.MAX_VALUE; i++) {
            if (generators[i] != null) {
                Type callbackType = CallbackUtils.getType2(i);
                if (callbackType != null) {
                    declare_field(Constants.ACC_PRIVATE, getCallbackField(i), callbackType, null);
                    declare_field(Constants.PRIVATE_FINAL_STATIC, getThreadLocal(i), THREAD_LOCAL, null);
                }
                generators[i].generate(this, contexts[i]);
            }
        }
    }

    private void generateSetThreadCallbacks() {
        begin_method(Constants.ACC_PUBLIC | Constants.ACC_STATIC,
                     SET_THREAD_CALLBACKS,
                     null);
        Label end = make_label();
        load_arg(0);
        ifnull(end);
        for (int i = 0; i <= Callbacks.MAX_VALUE; i++) {
            if (usedCallbacks.get(i)) {
                load_arg(0);
                push(i);
                invoke_interface(CALLBACKS, CALLBACKS_GET);
                getfield(getThreadLocal(i));
                swap();
                invoke_virtual(THREAD_LOCAL, THREAD_LOCAL_SET);
            }
        }
        mark(end);
        return_value();
    }

    private void generateCurrentCallback(int type) {
        usedCallbacks.set(type);
        load_this();
        getfield(getCallbackField(type));
        dup();
        Label end = make_label();
        ifnonnull(end);
        load_this();
        getfield(CONSTRUCTED_FIELD);
        if_jump(NE, end);
        pop();
        getfield(getThreadLocal(type));
        invoke_virtual(THREAD_LOCAL, THREAD_LOCAL_GET);
        checkcast(CallbackUtils.getType2(type));
        mark(end);
    }

    private String getCallbackField(int type) {
        return "CGLIB$CALLBACK_" + type;
    }

    private String getThreadLocal(int type) {
        return "CGLIB$TL_CALLBACK_" + type;
    }

    private void generateStatic(CallbackGenerator[] generators,
                                CallbackGenerator.Context[] contexts)  throws Exception {
        begin_static();
        for (int i = 0; i <= Callbacks.MAX_VALUE; i++) {
            if (usedCallbacks.get(i)) {
                new_instance(THREAD_LOCAL);
                dup();
                invoke_constructor(THREAD_LOCAL, CSTRUCT_NULL);
                putfield(getThreadLocal(i));
            }
        }
        for (int i = 0; i <= Callbacks.MAX_VALUE; i++) {
            if (generators[i] != null) {
                generators[i].generateStatic(this, contexts[i]);
            }
        }
        return_value();
    }

    private static void removeFinal(List list) {
        CollectionUtils.filter(list, new Predicate() {
            public boolean evaluate(Object arg) {
                return !Modifier.isFinal(((Method)arg).getModifiers());
            }
        });
    }
}
