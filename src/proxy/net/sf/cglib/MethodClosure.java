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
import java.lang.reflect.Modifier;

/**
 * @author Chris Nokleberg <a href="mailto:chris@nokleberg.com">chris@nokleberg.com</a>
 * @version $Id: MethodClosure.java,v 1.7 2002/12/22 00:21:17 herbyderby Exp $
 */
abstract public class MethodClosure {
    /* package */ static final Class TYPE = MethodClosure.class;
    private static final FactoryCache cache = new FactoryCache();
    private static final ClassLoader defaultLoader = TYPE.getClassLoader();
    private static final ClassNameFactory nameFactory = new ClassNameFactory("ClosuredByCGLIB");
    private static final MethodClosureKey keyFactory =
      (MethodClosureKey)KeyFactory.makeFactory(MethodClosureKey.class, null);

    // should be package-protected but causes problems on jdk1.2
    public interface MethodClosureKey {
        public Object newInstance(Class delegateClass, String methodName, Class iface);
    }

    protected Object delegate;
    protected String eqMethod;

    public boolean equals(Object obj) {
        MethodClosure other = (MethodClosure)obj;
        return delegate == other.delegate && eqMethod.equals(other.eqMethod);
    }

    public int hashCode() {
        return delegate.hashCode() ^ eqMethod.hashCode();
    }

    protected MethodClosure() {
    }

    abstract public MethodClosure newInstance(Object delegate);

    public Object getDelegate() {
        return delegate;
    }

    public static MethodClosure generate(Object delegate, String methodName, Class iface) {
        return generate(delegate, methodName, iface, null);
    }

    public static MethodClosure generate(Object delegate, String methodName, Class iface, ClassLoader loader) {
        if (loader == null) {
            loader = defaultLoader;
        }
        Class delegateClass = delegate.getClass();
        Object key = keyFactory.newInstance(delegateClass, methodName, iface);
        MethodClosure factory;
        synchronized (cache) {
            factory = (MethodClosure)cache.get(loader, key);
            if (factory == null) {
                Method method = findProxiedMethod(delegateClass, methodName, iface);
                String className = nameFactory.getNextName(delegateClass);
                Class result = new Generator(className, method, iface, loader).define();
                factory = (MethodClosure)FactoryCache.newInstance(result, Constants.TYPES_EMPTY, null);
                cache.put(loader, key, factory);
            }
        }
        return factory.newInstance(delegate);
    }

    private static Method findProxiedMethod(Class delegateClass, String methodName, Class iface) {
        if (!iface.isInterface()) {
            throw new IllegalArgumentException(iface + " is not an interface");
        }
        Method[] methods = iface.getDeclaredMethods();
        if (methods.length != 1) {
            throw new IllegalArgumentException("expecting exactly 1 method in " + iface);
        }
        Method proxy = methods[0];
        try {
            Method method = delegateClass.getMethod(methodName, proxy.getParameterTypes());
            if (method == null) {
                throw new IllegalArgumentException("no matching method found");
            }
            if (!proxy.getReturnType().isAssignableFrom(method.getReturnType())) {
                throw new IllegalArgumentException("incompatible return types");
            }
            return method;
        } catch (NoSuchMethodException e) {
            throw new CodeGenerationException(e);
        }
    }

    private static class Generator extends CodeGenerator {
        private Method method;
        private Class iface;

        public Generator(String className, Method method, Class iface, ClassLoader loader) {
            super(className, MethodClosure.class, loader);
            this.method = method;
            this.iface = iface;
        }

        protected void generate() throws NoSuchMethodException, NoSuchFieldException {
            declare_field(Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC, String.class, "eqMethod");
            declare_interface(iface);
            generateNullConstructor();

            // generate proxied method
            begin_method(iface.getDeclaredMethods()[0]);
            load_this();
            super_getfield("delegate");
            checkcast(method.getDeclaringClass());
            load_args();
            invoke(method);
            return_value();
            end_method();

            // newInstance
            Method newInstance = MethodClosure.TYPE.getMethod("newInstance", TYPES_OBJECT);
            begin_method(newInstance);
            new_instance_this();
            dup();
            dup2();
            invoke_constructor_this();
            getstatic("eqMethod");
            super_putfield("eqMethod");
            load_arg(0);
            super_putfield("delegate");
            return_value();
            end_method();

            // static initializer
            begin_static();
            push(CodeGenerator.getMethodSignature(method));
            putstatic("eqMethod");
            return_value();
            end_static();
        }

    }
}
