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

abstract public class Callbacks
{
    public static final int NO_OP = 0;
    public static final int INTERCEPT = 1;
    public static final int JDK_PROXY = 2;
    public static final int BEFORE_AFTER = 3;
    public static final int LAZY_LOAD = 4;
    public static final int DISPATCH = 5;
    public static final int MAX_VALUE = 5; // should be set to current max index
    
    abstract public Callback get(int type);

    static Class getType(int type) {
        switch (type) {
        case INTERCEPT:
            return MethodInterceptor.class;
        case JDK_PROXY:
            return HandlerAdapter.class;
        case BEFORE_AFTER:
            return BeforeAfterInterceptor.class;
        case LAZY_LOAD:
            return LazyLoader.class;
        case DISPATCH:
            return Dispatcher.class;
        default:
            return null;
        }
    }

    static CallbackGenerator getGenerator(int type) {
        switch (type) {
        case INTERCEPT:
            return MethodInterceptorGenerator.INSTANCE;
        case NO_OP:
            return NoOpGenerator.INSTANCE;
        case JDK_PROXY:
            return InvocationHandlerGenerator.INSTANCE;
        case BEFORE_AFTER:
            return BeforeAfterGenerator.INSTANCE;
        case LAZY_LOAD:
            return LazyLoaderGenerator.INSTANCE;
        case DISPATCH:
            return DispatcherGenerator.INSTANCE;
        default:
            return null;
        }
    }

    static int determineType(Callback callback) {
        int best = NO_OP;
        for (int i = 0; i <= MAX_VALUE; i++) {
            Class type = getType(i);
            if (type != null && type.isAssignableFrom(callback.getClass())) {
                if (best != NO_OP) {
                    throw new IllegalStateException("Callback implements more than one interceptor interface--use a CallbackFilter");
                }
                best = i;
            }
        }
        return best;
    }
}
