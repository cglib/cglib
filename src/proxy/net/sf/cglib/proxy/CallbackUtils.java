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

import org.objectweb.asm.Type;

class CallbackUtils {
    private CallbackUtils() { }

    static Class determineType(Callback callback) {
        Class test = typeHelper(callback, null, NoOp.class);
        test = typeHelper(callback, test, MethodInterceptor.class);
        test = typeHelper(callback, test, InvocationHandler.class);
        test = typeHelper(callback, test, LazyLoader.class);
        test = typeHelper(callback, test, Dispatcher.class);
        test = typeHelper(callback, test, FixedValue.class);
        if (test == null) {
            throw new IllegalStateException("Unknown callback " + callback.getClass());
        }
        return test;
    }

    private static Class typeHelper(Callback callback, Class cur, Class callbackType) {
        if (callback == null) {
            throw new IllegalStateException("Callback is null");
        }
        if (callbackType.isAssignableFrom(callback.getClass())) {
            if (cur != null) {
                throw new IllegalStateException("Callback implements both " + cur + " and " + callbackType + "; use setCallbackTypes to distinguish");
            }
            return callbackType;
        } else {
            return cur;
        }
    }

    static CallbackGenerator getGenerator(Class type) {
        if (type.equals(NoOp.class)) {
           return NoOpGenerator.INSTANCE;
        } else if (type.equals(MethodInterceptor.class)) {
            return MethodInterceptorGenerator.INSTANCE;
        } else if (type.equals(InvocationHandler.class)) {
            return InvocationHandlerGenerator.INSTANCE;
        } else if (type.equals(LazyLoader.class)) {
            return LazyLoaderGenerator.INSTANCE;
        } else if (type.equals(Dispatcher.class)) {
            return DispatcherGenerator.INSTANCE;
        } else if (type.equals(FixedValue.class)) {
            return FixedValueGenerator.INSTANCE;
        } else {
            throw new IllegalStateException("Unknown callback " + type);
        }
    }
}
