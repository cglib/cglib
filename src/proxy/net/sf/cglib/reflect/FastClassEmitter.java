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
    
class FastClassEmitter extends Emitter {
    private static final Method METHOD_GET_INDEX =
      ReflectUtils.findMethod("FastClass.getIndex(String, Class[])");
    private static final Method SIGNATURE_GET_INDEX =
      ReflectUtils.findMethod("FastClass.getIndex(String)");
    private static final Method CONSTRUCTOR_GET_INDEX =
      ReflectUtils.findMethod("FastClass.getIndex(Class[])");
    private static final Method INVOKE =
      ReflectUtils.findMethod("FastClass.invoke(int, Object, Object[])");
    private static final Method NEW_INSTANCE =
      ReflectUtils.findMethod("FastClass.newInstance(int, Object[])");
    private static final Class[] CONSTRUCTOR_TYPES = { Class.class };

    public FastClassEmitter(ClassVisitor v, String className, Class type) throws Exception {
        setClassVisitor(v);
        begin_class(Modifier.PUBLIC, className, FastClass.class, null);

        // constructor
        begin_constructor(CONSTRUCTOR_TYPES);
        load_this();
        load_args();
        super_invoke_constructor(CONSTRUCTOR_TYPES);
        return_value();
        end_method();

        VisibilityPredicate vp = new VisibilityPredicate(type, false);
        List methodList = ReflectUtils.addAllMethods(type, new ArrayList());
        CollectionUtils.filter(methodList, vp);
        CollectionUtils.filter(methodList, new DuplicatesPredicate());
        final Method[] methods = (Method[])methodList.toArray(new Method[methodList.size()]);
        final Constructor[] constructors = (Constructor[])CollectionUtils.filter(type.getDeclaredConstructors(), vp);

        // getIndex(String)
        begin_method(SIGNATURE_GET_INDEX);
        final List signatures = CollectionUtils.transform(Arrays.asList(methods), new Transformer() {
            public Object transform(Object obj) {
                Method m = (Method)obj;
                return m.getName() + ReflectUtils.getMethodDescriptor((Method)obj);
            }
        });
        load_arg(0);
        Virt.string_switch(this,
                           (String[])signatures.toArray(new String[0]),
                           Virt.SWITCH_STYLE_HASH,
                           new Virt.ObjectSwitchCallback() {
            public void processCase(Object key, Label end) {
                // TODO: remove linear indexOf
                push(signatures.indexOf(key));
                return_value();
            }
            public void processDefault() {
                push(-1);
                return_value();
            }
        });
        end_method();

        // getIndex(String, Class[])
        begin_method(METHOD_GET_INDEX);
        load_args();
        Virt.method_switch(this, methods, new GetIndexCallback(methods));
        end_method();

        // getIndex(Class[])
        begin_method(CONSTRUCTOR_GET_INDEX);
        load_args();
        Virt.constructor_switch(this, constructors, new GetIndexCallback(constructors));
        end_method();

        // invoke(int, Object, Object[])
        begin_method(INVOKE);
        load_arg(1);
        checkcast(type);
        load_arg(0);
        invokeSwitchHelper(methods, 2);
        end_method();

        // newInstance(int, Object[])
        begin_method(NEW_INSTANCE);
        new_instance(type);
        dup();
        load_arg(0);
        invokeSwitchHelper(constructors, 1);
        end_method();

        end_class();
    }

    private void invokeSwitchHelper(final Object[] members, final int arg) throws Exception {
        process_switch(getIntRange(members.length), new ProcessSwitchCallback() {
            public void processCase(int key, Label end) {
                Member member = (Member)members[key];
                Class[] types = ReflectUtils.getParameterTypes(member);
                for (int i = 0; i < types.length; i++) {
                    load_arg(arg);
                    aaload(i);
                    Virt.unbox(FastClassEmitter.this, types[i]);
                }
                if (member instanceof Method) {
                    invoke((Method)member);
                    Virt.box(FastClassEmitter.this, ((Method)member).getReturnType());
                } else {
                    invoke((Constructor)member);
                }
                return_value();
            }
            public void processDefault() {
                Virt.throw_exception(FastClassEmitter.this,
                                     NoSuchMethodError.class,
                                     "Cannot find matching method/constructor");
            }
        });
    }

    private class GetIndexCallback implements Virt.ObjectSwitchCallback {
        private Map indexes = new HashMap();

        public GetIndexCallback(Object[] members) {
            for (int i = 0; i < members.length; i++) {
                indexes.put(members[i], new Integer(i));
            }
        }
            
        public void processCase(Object key, Label end) {
            push(((Integer)indexes.get(key)).intValue());
            return_value();
        }
        
        public void processDefault() {
            push(-1);
            return_value();
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
