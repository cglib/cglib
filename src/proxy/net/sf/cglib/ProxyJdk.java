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

/**
 * This class is meant to be used as a drop-in replacement for 
 * <code>java.lang.reflect.Proxy</code> under JDK 1.3 or higher.
 *
 * @author Chris Nokleberg <a href="mailto:chris@nokleberg.com">chris@nokleberg.com</a>
 * @author Neeme Praks <a href="mailto:neeme@apache.org">neeme@apache.org</a>
 * @version $Id: ProxyJdk.java,v 1.4 2003/01/29 18:23:21 herbyderby Exp $
 */
public class ProxyJdk {
    private static final Class IMPL_TYPE = ProxyImpl.class;

    private static class HandlerAdapter implements MethodInterceptor {
        private java.lang.reflect.InvocationHandler handler;
        public HandlerAdapter(java.lang.reflect.InvocationHandler handler) {
            this.handler = handler;
        }

        public Object aroundAdvice(Object obj, Method method, Object[] args,
                                   MethodProxy proxy) throws Throwable {
            return handler.invoke(obj, method, args);
        }
    }

    protected ProxyJdk(java.lang.reflect.InvocationHandler h) {
        ((Factory)this).setInterceptor(new HandlerAdapter(h));
    }

    // private for security of isProxyClass
    private static class ProxyImpl extends ProxyJdk {
        protected ProxyImpl(java.lang.reflect.InvocationHandler h) {
            super(h);
        }
    }

    public static java.lang.reflect.InvocationHandler getInvocationHandler(Object proxy) {
        return ((HandlerAdapter)((Factory)proxy).getInterceptor()).handler;
    }

    public static Class getProxyClass(ClassLoader loader, Class[] interfaces) {
        return Enhancer.enhanceClass(IMPL_TYPE, interfaces, loader, null);
    }

    public static boolean isProxyClass(Class cl) {
        return cl.getSuperclass().equals(IMPL_TYPE);
    }

    // TODO: optimize away reflection via ConstructorProxy? (maybe not necessary)
    public static Object newProxyInstance(ClassLoader loader, Class[] interfaces, java.lang.reflect.InvocationHandler h) {
        try {
            Class clazz = getProxyClass(loader, interfaces);
            return clazz.getConstructor(new Class[]{ java.lang.reflect.InvocationHandler.class }).newInstance(new Object[]{ h });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }
    }

}
