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
      TypeUtils.parseSignature("void CGLIB$SET_THREAD_CALLBACKS(net.sf.cglib.proxy.Callbacks)");
    private static final Signature NEW_INSTANCE =
      TypeUtils.parseSignature("net.sf.cglib.proxy.Factory newInstance(net.sf.cglib.proxy.Callbacks)");
    private static final Signature MULTIARG_NEW_INSTANCE =
      TypeUtils.parseSignature("net.sf.cglib.proxy.Factory newInstance(Class[], Object[], net.sf.cglib.proxy.Callbacks)");
    private static final Signature SINGLE_NEW_INSTANCE =
      TypeUtils.parseSignature("net.sf.cglib.proxy.Factory newInstance(net.sf.cglib.proxy.Callback)");
    private static final Signature SET_CALLBACK =
      TypeUtils.parseSignature("void setCallback(int, net.sf.cglib.proxy.Callback)");
    private static final Signature SET_CALLBACKS =
      TypeUtils.parseSignature("void setCallbacks(net.sf.cglib.proxy.Callbacks)");
    private static final Signature GET_CALLBACK =
      TypeUtils.parseSignature("net.sf.cglib.proxy.Callback getCallback(int)");

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
        if (superclass == null) {
            superclass = Object.class;
        }

        begin_class(Constants.ACC_PUBLIC,
                    className,
                    Type.getType(superclass),
                    TypeUtils.add(TypeUtils.getTypes(interfaces), FACTORY),
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
        emitConstructors(constructors);

        CallbackGenerator.Context[] contexts = createContexts(generators, group, forcePublic);
        emitMethods(generators, contexts);
        emitStatic(generators, contexts);

        final int[] keys = getCallbackKeys();
        emitGetCallback(keys);
        emitSetCallback(keys);
        emitSetCallbacks();
        emitNewInstanceCallbacks();
        emitNewInstanceCallback();
        emitNewInstanceMultiarg(constructors);

        emitSetThreadCallbacks();
        end_class();
    }

    private void emitConstructors(Constructor[] constructors) {
        for (int i = 0; i < constructors.length; i++) {
            Signature sig = ReflectUtils.getSignature(constructors[i]);
            CodeEmitter e = begin_method(Constants.ACC_PUBLIC,
                                         sig,
                                         ReflectUtils.getExceptionTypes(constructors[i]));
            e.load_this();
            e.dup();
            e.load_args();
            e.super_invoke_constructor(sig);
            e.push(1);
            e.putfield(CONSTRUCTED_FIELD);
            e.return_value();
            e.end_method();
        }
    }
    
    private void emitGetCallback(int[] keys) {
        final CodeEmitter e = begin_method(Constants.ACC_PUBLIC, GET_CALLBACK, null);
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
        // Factory.setCallback(int, Callback)
        final CodeEmitter e = begin_method(Constants.ACC_PUBLIC, SET_CALLBACK, null);
        e.load_this();
        e.load_arg(1);
        e.load_arg(0);
        e.process_switch(keys, new ProcessSwitchCallback() {
            public void processCase(int key, Label end) {
                e.checkcast(CallbackUtils.getType(key));
                e.putfield(getCallbackField(key));
                e.goTo(end);
            }
            public void processDefault() {
                e.pop2(); // stack height
            }
        });
        e.return_value();
        e.end_method();
    }
        
    private void emitSetCallbacks() {
        CodeEmitter e = begin_method(Constants.ACC_PUBLIC, SET_CALLBACKS, null);
        e.load_this();
        e.load_arg(0);
        emitSetCallbacks(e);
        e.return_value();
        e.end_method();
    }

    private void emitNewInstanceCallbacks() {
        CodeEmitter e = begin_method(Constants.ACC_PUBLIC, NEW_INSTANCE, null);
        e.load_arg(0);
        e.invoke_static_this(SET_THREAD_CALLBACKS);
        e.new_instance_this();
        e.dup();
        e.invoke_constructor_this();
        e.dup();
        e.load_arg(0);
        emitSetCallbacks(e);
        e.return_value();
        e.end_method();
    }

    private void emitNewInstanceCallback() {
        CodeEmitter e = begin_method(Constants.ACC_PUBLIC, SINGLE_NEW_INSTANCE, null);
        switch (usedCallbacks.cardinality()) {
        case 1:
            int type = usedCallbacks.length() - 1;
            e.getfield(getThreadLocal(type));
            e.load_arg(0);
            e.invoke_virtual(THREAD_LOCAL, THREAD_LOCAL_SET);
            e.new_instance_this();
            e.dup();
            e.invoke_constructor_this();
            e.dup();
            e.push(type);
            e.load_arg(0);
            e.invoke_virtual_this(SET_CALLBACK);
            break;
        case 0:
            // TODO: make sure Callback is null?
            e.new_instance_this();
            e.dup();
            e.invoke_constructor_this();
            break;
        default:
            e.throw_exception(ILLEGAL_STATE_EXCEPTION, "More than one callback object required");
        }
        e.return_value();
        e.end_method();
    }

    private void emitNewInstanceMultiarg(Constructor[] constructors) {
        final CodeEmitter e = begin_method(Constants.ACC_PUBLIC, MULTIARG_NEW_INSTANCE, null);
        Label skipSetCallbacks = e.make_label();
        e.load_arg(2);
        e.invoke_static_this(SET_THREAD_CALLBACKS);
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
        e.load_arg(2);
        e.ifnull(skipSetCallbacks);
        e.dup();
        e.load_arg(2);
        emitSetCallbacks(e);
        e.mark(skipSetCallbacks);
        e.return_value();
        e.end_method();
    }

    private void emitSetCallbacks(CodeEmitter e) {
        if (usedCallbacks.length() == 0) {
            e.pop2(); // stack height
        } else {
            for (int i = 0; i <= Callbacks.MAX_VALUE; i++) {
                if (usedCallbacks.get(i)) {
                    if (i + 1 < usedCallbacks.length()) {
                        e.dup2();
                    }
                    e.push(i);
                    e.invoke_interface(CALLBACKS, GET_CALLBACK);
                    e.checkcast(CallbackUtils.getType(i));
                    e.putfield(getCallbackField(i));
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
                    public void emitCallback(CodeEmitter e) {
                        emitCurrentCallback(e, type);
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
            }
        }
        return contexts;
    }

    private void emitMethods(CallbackGenerator[] generators, CallbackGenerator.Context[] contexts) throws Exception {
        for (int i = 0; i <= Callbacks.MAX_VALUE; i++) {
            if (generators[i] != null) {
                Type callbackType = CallbackUtils.getType(i);
                if (callbackType != null) {
                    declare_field(Constants.ACC_PRIVATE, getCallbackField(i), callbackType, null);
                    declare_field(Constants.PRIVATE_FINAL_STATIC, getThreadLocal(i), THREAD_LOCAL, null);
                }
                generators[i].generate(this, contexts[i]);
            }
        }
    }

    private void emitSetThreadCallbacks() {
        CodeEmitter e = begin_method(Constants.ACC_PUBLIC | Constants.ACC_STATIC,
                                     SET_THREAD_CALLBACKS,
                                     null);
        Label end = e.make_label();
        e.load_arg(0);
        e.ifnull(end);
        for (int i = 0; i <= Callbacks.MAX_VALUE; i++) {
            if (usedCallbacks.get(i)) {
                e.load_arg(0);
                e.push(i);
                e.invoke_interface(CALLBACKS, GET_CALLBACK);
                e.getfield(getThreadLocal(i));
                e.swap();
                e.invoke_virtual(THREAD_LOCAL, THREAD_LOCAL_SET);
            }
        }
        e.mark(end);
        e.return_value();
        e.end_method();
    }

    private void emitCurrentCallback(CodeEmitter e, int type) {
        usedCallbacks.set(type);
        e.load_this();
        e.getfield(getCallbackField(type));
        e.dup();
        Label end = e.make_label();
        e.ifnonnull(end);
        e.load_this();
        e.getfield(CONSTRUCTED_FIELD);
        e.if_jump(e.NE, end);
        e.pop();
        e.getfield(getThreadLocal(type));
        e.invoke_virtual(THREAD_LOCAL, THREAD_LOCAL_GET);
        e.checkcast(CallbackUtils.getType(type));
        e.mark(end);
    }

    private String getCallbackField(int type) {
        return "CGLIB$CALLBACK_" + type;
    }

    private String getThreadLocal(int type) {
        return "CGLIB$TL_CALLBACK_" + type;
    }

    private void emitStatic(CallbackGenerator[] generators,
                            CallbackGenerator.Context[] contexts) throws Exception {
        CodeEmitter e = begin_static();
        for (int i = 0; i <= Callbacks.MAX_VALUE; i++) {
            if (usedCallbacks.get(i)) {
                e.new_instance(THREAD_LOCAL);
                e.dup();
                e.invoke_constructor(THREAD_LOCAL, CSTRUCT_NULL);
                e.putfield(getThreadLocal(i));
            }
        }
        for (int i = 0; i <= Callbacks.MAX_VALUE; i++) {
            if (generators[i] != null) {
                generators[i].generateStatic(e, contexts[i]);
            }
        }
        e.return_value();
        e.end_method();
    }

    private static void removeFinal(List list) {
        CollectionUtils.filter(list, new Predicate() {
            public boolean evaluate(Object arg) {
                return !Modifier.isFinal(((Method)arg).getModifiers());
            }
        });
    }
}
