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
import org.objectweb.asm.Type;

abstract public class MulticastDelegate implements Cloneable {
    protected Object[] targets = {};

    protected MulticastDelegate() {
    }

    public List getTargets() {
        return new ArrayList(Arrays.asList(targets));
    }

    abstract public MulticastDelegate add(Object target);

    protected MulticastDelegate addHelper(Object target) {
        MulticastDelegate copy = newInstance();
        copy.targets = new Object[targets.length + 1];
        System.arraycopy(targets, 0, copy.targets, 0, targets.length);
        copy.targets[targets.length] = target;
        return copy;
    }

    public MulticastDelegate remove(Object target) {
        for (int i = targets.length - 1; i >= 0; i--) { 
            if (targets[i].equals(target)) {
                MulticastDelegate copy = newInstance();
                copy.targets = new Object[targets.length - 1];
                System.arraycopy(targets, 0, copy.targets, 0, i);
                System.arraycopy(targets, i + 1, copy.targets, i, targets.length - i - 1);
                return copy;
            }
        }
        return this;
    }

    abstract public MulticastDelegate newInstance();

    public static MulticastDelegate create(Class iface) {
        Generator gen = new Generator();
        gen.setInterface(iface);
        return gen.create();
    }

    public static class Generator extends AbstractClassGenerator {
        private static final Source SOURCE = new Source(MulticastDelegate.class.getName(), true);
        private static final Signature NEW_INSTANCE =
          TypeUtils.parseSignature("net.sf.cglib.reflect.MulticastDelegate newInstance()");
        private static final Signature ADD =
          TypeUtils.parseSignature("net.sf.cglib.reflect.MulticastDelegate add(Object)");
        private static final Signature ADD_HELPER =
          TypeUtils.parseSignature("net.sf.cglib.reflect.MulticastDelegate addHelper(Object)");
        private static final Type MULTICAST_DELEGATE =
          TypeUtils.parseType("net.sf.cglib.reflect.MulticastDelegate");

        private Class iface;

        public Generator() {
            super(SOURCE);
        }

        protected ClassLoader getDefaultClassLoader() {
            return iface.getClassLoader();
        }

        public void setInterface(Class iface) {
            this.iface = iface;
        }

        public MulticastDelegate create() {
            return (MulticastDelegate)super.create(iface);
        }

        public void generateClass(ClassVisitor v) throws NoSuchFieldException {
            setNamePrefix(MulticastDelegate.class.getName());
            final Method method = ReflectUtils.findInterfaceMethod(iface);
            
            final Emitter e = new Emitter(v);
            e.begin_class(Constants.ACC_PUBLIC,
                          getClassName(),
                          MULTICAST_DELEGATE,
                          new Type[]{ Type.getType(iface) },
                          Constants.SOURCE_FILE);
            e.null_constructor();

            // generate proxied method
            e.begin_method(Constants.ACC_PUBLIC,
                           ReflectUtils.getSignature(method),
                           ReflectUtils.getExceptionTypes(method));
            Type returnType = e.getReturnType();
            final boolean returns = returnType != Type.VOID_TYPE;
            Local result = null;
            if (returns) {
                result = e.make_local(returnType);
                e.zero_or_null(returnType);
                e.store_local(result);
            }
            e.load_this();
            e.super_getfield("targets", Constants.TYPE_OBJECT_ARRAY);
            final Local result2 = result;
            ComplexOps.process_array(e, Constants.TYPE_OBJECT_ARRAY, new ProcessArrayCallback() {
                public void processElement(Type type) {
                    e.checkcast(Type.getType(iface));
                    e.load_args();
                    ReflectOps.invoke(e, method);
                    if (returns) {
                        e.store_local(result2);
                    }
                }
            });
            if (returns) {
                e.load_local(result);
            }
            e.return_value();

            // newInstance
            e.begin_method(Constants.ACC_PUBLIC, NEW_INSTANCE, null);
            e.new_instance_this();
            e.dup();
            e.invoke_constructor_this();
            e.return_value();

            // add
            e.begin_method(Constants.ACC_PUBLIC, ADD, null);
            e.load_this();
            e.load_arg(0);
            e.checkcast(Type.getType(iface));
            e.invoke_virtual_this(ADD_HELPER);
            e.return_value();

            e.end_class();
        }

        protected Object firstInstance(Class type) {
            // make a new instance in case first object is used with a long list of targets
            return ((MulticastDelegate)ReflectUtils.newInstance(type)).newInstance();
        }

        protected Object nextInstance(Object instance) {
            return ((MulticastDelegate)instance).newInstance();
        }
    }
}
