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

import java.lang.reflect.*;
import net.sf.cglib.util.*;

/**
 * Classes generated by Enhancer pass this object to the
 * registered MethodInterceptors when an intercepted method is invoked. It can
 * be used to either invoke the original method, or call the same method on a different
 * object of the same type.
 * @see Enhancer
 * @see MethodInterceptor
 * @version $Id: MethodProxy.java,v 1.24 2003/09/04 18:53:46 herbyderby Exp $
 */
abstract public class MethodProxy {
    private static final FactoryCache cache = new FactoryCache(MethodProxy.class);
    private static final MethodProxyKey KEY_FACTORY =
      (MethodProxyKey)KeyFactory.create(MethodProxyKey.class, null);
    private static final Method INVOKE_SUPER =
      ReflectUtils.findMethod("MethodProxy.invokeSuper(Object, Object[])");
    private static final Method INVOKE =
      ReflectUtils.findMethod("MethodProxy.invoke(Object, Object[])");

    interface MethodProxyKey {
        Object newInstance(Method m1, Method m2);
    }

    /**
     * Invoke the original (super) method on the specified object.
     * @param obj the enhanced object, must be the object passed as the first
     * argument to the MethodInterceptor
     * @param args the arguments passed to the intercepted method; you may substitute a different
     * argument array as long as the types are compatible
     * @see MethodInterceptor#intercept
     */
    abstract public Object invokeSuper(Object obj, Object[] args) throws Throwable;

    /**
     * Invoke the original method, on a different object of the same type.
     * @param obj the compatible object; recursion will result if you use the object passed as the first
     * argument to the MethodInterceptor (usually not what you want)
     * @param args the arguments passed to the intercepted method; you may substitute a different
     * argument array as long as the types are compatible
     * @see MethodInterceptor#intercept
     */
    abstract public Object invoke(Object obj, Object[] args) throws Throwable;

    protected MethodProxy() { }

    /**
     * Create a new MethodProxy. Used internally by Enhancer.
     */
    public static MethodProxy create(Method method, Method superMethod) {
        return create(method, superMethod, null);
    }

    /**
     * Create a new MethodProxy. Used internally by Enhancer.
     */
    public static MethodProxy create(final Method method,
                                     final Method superMethod,
                                     ClassLoader loader) {
        if (loader == null) {
            loader = superMethod.getDeclaringClass().getClassLoader();
        }
        Object key = KEY_FACTORY.newInstance(method, superMethod);
        return (MethodProxy)cache.get(loader, key, new FactoryCache.AbstractCallback() {
                public BasicCodeGenerator newGenerator() {
                    return new Generator(method, superMethod);
                }
            });
    }

    static class Generator extends CodeGenerator {
        private Method method;
        private Method superMethod;
        
        public Generator(Method method, Method superMethod) {
            setSuperclass(MethodProxy.class);

            Method nonNull = (superMethod != null) ? superMethod : method;
            if (nonNull == null) {
                throw new IllegalArgumentException("Both method and superMethod cannot be null");
            }
            setNamePrefix(nonNull.getDeclaringClass().getName());
            this.superMethod = superMethod;
            this.method = method;
        }

        public void generate() {
            null_constructor();
            generate(MethodProxy.INVOKE, method);
            generate(MethodProxy.INVOKE_SUPER, superMethod);
        }

        private void generate(Method proxyMethod, Method method) {
            begin_method(proxyMethod);
            if (method == null) {
                zero_or_null(proxyMethod.getReturnType());
            } else if (Modifier.isProtected(method.getModifiers())) {
                throw_exception(IllegalAccessException.class, "protected method: " + method);
            } else {
                load_arg(0);
                checkcast(method.getDeclaringClass());
                Class[] types = method.getParameterTypes();
                for (int i = 0; i < types.length; i++) {
                    load_arg(1);
                    push(i);
                    aaload();
                    unbox(types[i]);
                }
                this.invoke(method);
                box(method.getReturnType());
            }
            return_value();
            end_method();
        }
    }
}
