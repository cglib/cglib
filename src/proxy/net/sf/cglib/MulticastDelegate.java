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

import java.lang.reflect.Method;
import java.util.*;
import net.sf.cglib.util.*;

abstract public class MulticastDelegate implements Cloneable {
    private static final FactoryCache cache = new FactoryCache();
    private static final ClassLoader defaultLoader = MulticastDelegate.class.getClassLoader();
    private static final ClassNameFactory nameFactory = new ClassNameFactory("MulticastByCGLIB");

    private static final MulticastDelegateKey keyFactory =
      (MulticastDelegateKey)KeyFactory.create(MulticastDelegateKey.class, null);

    private static final Method NEW_INSTANCE =
      ReflectUtils.findMethod("MulticastDelegate.cglib_newInstance()");
    private static final Method ADD =
      ReflectUtils.findMethod("MulticastDelegate.add(Object)");
    private static final Method ADD_HELPER =
      ReflectUtils.findMethod("MulticastDelegate.cglib_addHelper(Object)");

    
    interface MulticastDelegateKey {
        public Object newInstance(Class iface);
    }

    protected Object[] delegates = {};

    protected MulticastDelegate() {
    }

    public List getInvocationList() {
        return new ArrayList(Arrays.asList(delegates));
    }

    abstract public MulticastDelegate add(Object delegate);

    protected MulticastDelegate cglib_addHelper(Object delegate) {
        MulticastDelegate copy = cglib_newInstance();
        copy.delegates = new Object[delegates.length + 1];
        System.arraycopy(delegates, 0, copy.delegates, 0, delegates.length);
        copy.delegates[delegates.length] = delegate;
        return copy;
    }

    public MulticastDelegate remove(Object delegate) {
        for (int i = delegates.length - 1; i >= 0; i--) { 
            if (delegates[i].equals(delegate)) {
                MulticastDelegate copy = cglib_newInstance();
                copy.delegates = new Object[delegates.length - 1];
                System.arraycopy(delegates, 0, copy.delegates, 0, i);
                System.arraycopy(delegates, i + 1, copy.delegates, i, delegates.length - i - 1);
                return copy;
            }
        }
        return this;
    }

    abstract protected MulticastDelegate cglib_newInstance();

    public static MulticastDelegate create(Class iface) {
        return create(iface, null);
    }

    public static MulticastDelegate create(Class iface, ClassLoader loader) {
        if (loader == null) {
            loader = defaultLoader;
        }
        Object key = keyFactory.newInstance(iface);
        MulticastDelegate factory;
        synchronized (cache) {
            factory = (MulticastDelegate)cache.get(loader, key);
            if (factory == null) {
                String className = nameFactory.getNextName(iface);
                Method method = MethodDelegate.findInterfaceMethod(iface);
                Class result = new Generator(className, method, iface, loader).define();
                factory = (MulticastDelegate)ReflectUtils.newInstance(result);
                cache.put(loader, key, factory);
            }
        }
        return factory.cglib_newInstance();
    }

    private static class Generator extends CodeGenerator {
        private Method method;
        private Class iface;

        public Generator(String className, Method method, Class iface, ClassLoader loader) {
            super(className, MulticastDelegate.class, loader);
            this.method = method;
            this.iface = iface;
            addInterface(iface);
        }

        protected void generate() throws NoSuchMethodException, NoSuchFieldException {
            null_constructor();

            // generate proxied method
            begin_method(method);
            Class returnType = method.getReturnType();
            final boolean returns = returnType != Void.TYPE;
            Local result = null;
            if (returns) {
                result = make_local(returnType);
                zero_or_null(returnType);
                store_local(result);
            }
            load_this();
            super_getfield("delegates");
            final Local result2 = result;
            process_array(Object[].class, new ProcessArrayCallback() {
                    public void processElement(Class type) {
                        checkcast(iface);
                        load_args();
                        invoke(method);
                        if (returns) {
                            store_local(result2);
                        }
                    }
                });
            if (returns) {
                load_local(result);
            }
            return_value();
            end_method();

            // newInstance
            begin_method(NEW_INSTANCE);
            new_instance_this();
            dup();
            invoke_constructor_this();
            return_value();
            end_method();

            // add
            begin_method(ADD);
            load_this();
            load_arg(0);
            checkcast(iface);
            invoke(ADD_HELPER);
            return_value();
            end_method();
        }
    }
}
