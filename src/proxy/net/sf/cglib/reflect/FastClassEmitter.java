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
package net.sf.cglib.reflect;

import java.lang.reflect.*;
import java.util.*;
import net.sf.cglib.core.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
    
class FastClassEmitter extends ClassEmitter {
    private static final Signature CSTRUCT_CLASS =
      TypeUtils.parseConstructor("Class");
    private static final Signature METHOD_GET_INDEX =
      TypeUtils.parseSignature("int getIndex(String, Class[])");
    private static final Signature SIGNATURE_GET_INDEX =
      TypeUtils.parseSignature("int getIndex(net.sf.cglib.core.Signature)");
    private static final Signature TO_STRING =
      TypeUtils.parseSignature("String toString()");
    private static final Signature CONSTRUCTOR_GET_INDEX =
      TypeUtils.parseSignature("int getIndex(Class[])");
    private static final Signature INVOKE =
      TypeUtils.parseSignature("Object invoke(int, Object, Object[])");
    private static final Signature NEW_INSTANCE =
      TypeUtils.parseSignature("Object newInstance(int, Object[])");
    private static final Signature GET_MAX_INDEX =
      TypeUtils.parseSignature("int getMaxIndex()");
    private static final Signature GET_SIGNATURE_WITHOUT_RETURN_TYPE =
      TypeUtils.parseSignature("String getSignatureWithoutReturnType(String, Class[])");
    private static final Type FAST_CLASS =
      TypeUtils.parseType("net.sf.cglib.reflect.FastClass");
    private static final Type ILLEGAL_ARGUMENT_EXCEPTION =
      TypeUtils.parseType("IllegalArgumentException");
    private static final Type INVOCATION_TARGET_EXCEPTION =
      TypeUtils.parseType("java.lang.reflect.InvocationTargetException");
    private static final Type[] INVOCATION_TARGET_EXCEPTION_ARRAY = { INVOCATION_TARGET_EXCEPTION };
    
    public FastClassEmitter(ClassVisitor v, String className, Class type) {
        super(v);
        begin_class(Constants.ACC_PUBLIC, className, FAST_CLASS, null, Constants.SOURCE_FILE);

        // constructor
        CodeEmitter e = begin_method(Constants.ACC_PUBLIC, CSTRUCT_CLASS, null, null);
        e.load_this();
        e.load_args();
        e.super_invoke_constructor(CSTRUCT_CLASS);
        e.return_value();
        e.end_method();

        VisibilityPredicate vp = new VisibilityPredicate(type, false);
        List methodList = ReflectUtils.addAllMethods(type, new ArrayList());
        CollectionUtils.filter(methodList, vp);
        CollectionUtils.filter(methodList, new DuplicatesPredicate());
        final Method[] methods = (Method[])methodList.toArray(new Method[methodList.size()]);
        final Constructor[] constructors = (Constructor[])CollectionUtils.filter(type.getDeclaredConstructors(), vp);
        
        // getIndex(String)
        emitIndexBySignature(methods);

        // getIndex(String, Class[])
        emitIndexByClassArray(methods);
        
        // getIndex(Class[])
        e = begin_method(Constants.ACC_PUBLIC, CONSTRUCTOR_GET_INDEX, null, null);
        e.load_args();
        EmitUtils.constructor_switch(e, constructors, new GetIndexCallback(e, constructors));
        e.end_method();

        // invoke(int, Object, Object[])
        e = begin_method(Constants.ACC_PUBLIC, INVOKE, INVOCATION_TARGET_EXCEPTION_ARRAY, null);
        e.load_arg(1);
        e.checkcast(Type.getType(type));
        e.load_arg(0);
        invokeSwitchHelper(e, methods, 2);
        e.end_method();

        // newInstance(int, Object[])
        e = begin_method(Constants.ACC_PUBLIC, NEW_INSTANCE, INVOCATION_TARGET_EXCEPTION_ARRAY, null);
        e.new_instance(Type.getType(type));
        e.dup();
        e.load_arg(0);
        invokeSwitchHelper(e, constructors, 1);
        e.end_method();

        // getMaxIndex()
        e = begin_method(Constants.ACC_PUBLIC, GET_MAX_INDEX, null, null);
        e.push(methods.length - 1);
        e.return_value();
        e.end_method();

        end_class();
    }

    // TODO: support constructor indices ("<init>")
    private void emitIndexBySignature(Method[] methods) {
        CodeEmitter e = begin_method(Constants.ACC_PUBLIC, SIGNATURE_GET_INDEX, null, null);
        List signatures = CollectionUtils.transform(Arrays.asList(methods), new Transformer() {
            public Object transform(Object obj) {
                return ReflectUtils.getSignature((Method)obj).toString();
            }
        });
        e.load_arg(0);
        e.invoke_virtual(Constants.TYPE_OBJECT, TO_STRING);
        signatureSwitchHelper(e, signatures);
        e.end_method();
    }

    private static final int TOO_MANY_METHODS = 100; // TODO
    private void emitIndexByClassArray(Method[] methods) {
        CodeEmitter e = begin_method(Constants.ACC_PUBLIC, METHOD_GET_INDEX, null, null);
        if (methods.length > TOO_MANY_METHODS) {
            // hack for big classes
            List signatures = CollectionUtils.transform(Arrays.asList(methods), new Transformer() {
                public Object transform(Object obj) {
                    String s = ReflectUtils.getSignature((Method)obj).toString();
                    return s.substring(0, s.lastIndexOf(')') + 1);
                }
            });
            e.load_args();
            e.invoke_static(FAST_CLASS, GET_SIGNATURE_WITHOUT_RETURN_TYPE);
            signatureSwitchHelper(e, signatures);
        } else {
            e.load_args();
            EmitUtils.method_switch(e, methods, new GetIndexCallback(e, methods));
        }
        e.end_method();
    }

    private void signatureSwitchHelper(final CodeEmitter e, final List signatures) {
        ObjectSwitchCallback callback = new ObjectSwitchCallback() {
            public void processCase(Object key, Label end) {
                // TODO: remove linear indexOf
                e.push(signatures.indexOf(key));
                e.return_value();
            }
            public void processDefault() {
                e.push(-1);
                e.return_value();
            }
        };
        EmitUtils.string_switch(e,
                                (String[])signatures.toArray(new String[signatures.size()]),
                                Constants.SWITCH_STYLE_HASH,
                                callback);
    }

    private static void invokeSwitchHelper(final CodeEmitter e, final Object[] members, final int arg) {
        final Label illegalArg = e.make_label();
        Block block = e.begin_block();
        e.process_switch(getIntRange(members.length), new ProcessSwitchCallback() {
            public void processCase(int key, Label end) {
                Member member = (Member)members[key];
                Signature sig = ReflectUtils.getSignature(member);
                Type[] types = sig.getArgumentTypes();
                for (int i = 0; i < types.length; i++) {
                    e.load_arg(arg);
                    e.aaload(i);
                    e.unbox(types[i]);
                }
                if (member instanceof Method) {
                    e.invoke((Method)member);
                    e.box(Type.getType(((Method)member).getReturnType()));
                } else {
                    e.invoke_constructor(Type.getType(member.getDeclaringClass()), sig);
                }
                e.return_value();
            }

            public void processDefault() {
                e.goTo(illegalArg);
            }
        });
        block.end();
        EmitUtils.wrap_throwable(block, INVOCATION_TARGET_EXCEPTION);
        e.mark(illegalArg);
        e.throw_exception(ILLEGAL_ARGUMENT_EXCEPTION, "Cannot find matching method/constructor");
    }

    private static class GetIndexCallback implements ObjectSwitchCallback {
        private CodeEmitter e;
        private Map indexes = new HashMap();

        public GetIndexCallback(CodeEmitter e, Object[] members) {
            this.e = e;
            for (int i = 0; i < members.length; i++) {
                indexes.put(members[i], new Integer(i));
            }
        }
            
        public void processCase(Object key, Label end) {
            e.push(((Integer)indexes.get(key)).intValue());
            e.return_value();
        }
        
        public void processDefault() {
            e.push(-1);
            e.return_value();
        }
    }
    
    private static int[] getIntRange(int length) {
        int[] range = new int[length];
        for (int i = 0; i < length; i++) {
            range[i] = i;
        }
        return range;
    }
}
