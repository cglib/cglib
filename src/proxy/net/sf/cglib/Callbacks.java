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

/**
 * Provides a method to map from callback types (as defined per-method by a {@link CallbackFilter})
 * to a particular {@link Callback} implementation. The callback type constants are defined here as well.
 * In practice it should be rare to need to implement this interface. See {@link SimpleCallbacks} for
 * a simple implementation.
 */
public interface Callbacks
{
    /**
     * No-op callback type. Does not generate an intercepted method
     * in the subclass--the "super" method will be called directly instead.
     * No associated <code>Callback</code> implementation.
     */
    public static final int NO_OP = 0;

    /**
     * Generic interceptor callback type. <code>Callback</code> implementation is {@link MethodInterceptor}.
     * @see MethodInterceptor
     */
    public static final int INTERCEPT = 1;

    /**
     * Special callback type used by the {@link Proxy} class for
     * <code>java.lang.reflect.Proxy</code> compatibility. You probably don't
     * want to use this directly. <code>Callback</code> implementation is {@link InvocationHandler}.
     * @see Proxy
     * @see InvocationHandler
     */
    public static final int JDK_PROXY = 2;

    /**
     * Callback type used to load an implementation as soon as the first method is called,
     * <code>Callback</code> implementation is {@link LazyLoader}.
     * @see LazyLoader
     */
    public static final int LAZY_LOAD = 3;

    /**
     * Callback type used to load an implementation for every single method invocation.
     * <code>Callback</code> implementation is {@link Dispatcher}.
     * @see Dispatcher
     */
    public static final int DISPATCH = 4;

    static final int MAX_VALUE = 4; // should be set to current max index

    /**
     * Return the actual <code>Callback</code> implementation for the specified callback type.
     * @param type the callback type
     * @return the callback implementation
     */
    Callback get(int type);
}
