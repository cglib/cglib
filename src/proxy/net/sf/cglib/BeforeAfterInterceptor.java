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
 * An implementation of MethodInterceptor provides separate callbacks for
 * code to execute before and after the original method execution.
 * @author Juozas Baliuka <a href="mailto:baliuka@mwm.lt">baliuka@mwm.lt</a>
 * @version $Id: BeforeAfterInterceptor.java,v 1.9 2003/05/13 06:17:09 herbyderby Exp $
 */
abstract public class BeforeAfterInterceptor implements MethodInterceptor {

    public Object intercept(Object obj, java.lang.reflect.Method method, Object[] args, MethodProxy proxy) throws Throwable {
        Throwable e = null;                                                                            
        boolean invokedSuper = false;                                                                  
        Object retValFromSuper = null;
        if (  !java.lang.reflect.Modifier.isAbstract( method.getModifiers() ) && 
              invokeSuper(obj, method, args)) {
            invokedSuper = true;                                                                        
            try {
                retValFromSuper = proxy.invokeSuper(obj, args);
            } catch (Throwable t) {
                e = t;
            }
        }
        return afterReturn(obj, method, args, invokedSuper, retValFromSuper, e);
    }
    
    /**
     * This method is called before invoking the super (non-intercepted) method.
     * @param obj  "this", the enhanced object
     * @param method intercepted Method
     * @param args argument array; primitive types are wrapped
     * @return true to invoke super method, false to skip
     * @throws Throwable any exception may be thrown; super method will not be invoked
     */    
    abstract public boolean invokeSuper(Object obj, Method method, Object[] args) throws Throwable;
    
    /**
     * This method is called after invoking the super (non-intercepted) method, or
     * directly after the invokeSuper interceptor method, if it returned false.
     * @param obj  "this", the enhanced object
     * @param method intercepted Method
     * @param args argument array; primitive types are wrapped
     * @param invokedSuper whether the super method was invoked
     * @param retValFromSuper value returned from super (unless there was an exception)
     * @param e Throwable thrown by the super method (if any)
     * @return value to return from generated method; primitive will be unwrapped if necessary
     * @throws Throwable any exception may be thrown
     */    
    abstract public Object afterReturn(Object obj, Method method, Object[] args,
                                       boolean invokedSuper, Object retValFromSuper,
                                       Throwable e) throws Throwable;
}
