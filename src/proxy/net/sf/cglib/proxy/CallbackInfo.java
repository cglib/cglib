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

class CallbackInfo
{
    public static Type[] determineTypes(Class[] callbackTypes) {
        Type[] types = new Type[callbackTypes.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = determineType(callbackTypes[i]);
        }
        return types;
    }

    public static Type[] determineTypes(Callback[] callbacks) {
        Type[] types = new Type[callbacks.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = determineType(callbacks[i]);
        }
        return types;
    }

    public static CallbackGenerator[] getGenerators(Type[] callbackTypes) {
        CallbackGenerator[] generators = new CallbackGenerator[callbackTypes.length];
        for (int i = 0; i < generators.length; i++) {
            generators[i] = getGenerator(callbackTypes[i]);
        }
        return generators;
    }

    //////////////////// PRIVATE ////////////////////

    private Class cls;
    private CallbackGenerator generator;
    private Type type;
    
    private static final CallbackInfo[] CALLBACKS = {
        new CallbackInfo(NoOp.class, NoOpGenerator.INSTANCE),
        new CallbackInfo(MethodInterceptor.class, MethodInterceptorGenerator.INSTANCE),
        new CallbackInfo(InvocationHandler.class, InvocationHandlerGenerator.INSTANCE),
        new CallbackInfo(LazyLoader.class, LazyLoaderGenerator.INSTANCE),
        new CallbackInfo(Dispatcher.class, DispatcherGenerator.INSTANCE),
        new CallbackInfo(FixedValue.class, FixedValueGenerator.INSTANCE),
    };

    private CallbackInfo(Class cls, CallbackGenerator generator) {
        this.cls = cls;
        this.generator = generator;
        type = Type.getType(cls);
    }

    private static Type determineType(Callback callback) {
        if (callback == null) {
            throw new IllegalStateException("Callback is null");
        }
        return determineType(callback.getClass());
    }

    private static Type determineType(Class callbackType) {
        Class cur = null;
        for (int i = 0; i < CALLBACKS.length; i++) {
            CallbackInfo info = CALLBACKS[i];
            if (info.cls.isAssignableFrom(callbackType)) {
                if (cur != null) {
                    throw new IllegalStateException("Callback implements both " + cur + " and " + info.cls);
                }
                cur = info.cls;
            }
        }
        if (cur == null) {
            throw new IllegalStateException("Unknown callback type " + callbackType);
        }
        return Type.getType(cur);
    }

    private static CallbackGenerator getGenerator(Type callbackType) {
        for (int i = 0; i < CALLBACKS.length; i++) {
            CallbackInfo info = CALLBACKS[i];
            if (info.type.equals(callbackType)) {
                return info.generator;
            }
        }
        throw new IllegalStateException("Unknown callback type " + callbackType);
    }
}
    

