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

import net.sf.cglib.core.ReflectUtils;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @version $Id: CallbackHelper.java,v 1.1 2004/05/05 06:15:31 herbyderby Exp $
 */
abstract public class CallbackHelper
implements CallbackFilter
{
    private Map methodMap = new HashMap();
    private List callbacks = new ArrayList();
    
    public CallbackHelper(Class superclass, Class[] interfaces)
    {
        List methods = new ArrayList();
        Enhancer.getMethods(superclass, interfaces, methods);
        Map indexes = new HashMap();
        for (int i = 0, size = methods.size(); i < size; i++) {
            Method method = (Method)methods.get(i);
            Object callback = getCallback(method);
            if (callback == null)
                throw new IllegalStateException("getCallback cannot return null");
            boolean isCallback = callback instanceof Callback;
            if (!(isCallback || (callback instanceof Class)))
                throw new IllegalStateException("getCallback must return a Callback or a Class");
            if (i > 0 && ((callbacks.get(i - 1) instanceof Callback) ^ isCallback))
                throw new IllegalStateException("getCallback must return a Callback or a Class consistently for every Method");
            Integer index = (Integer)indexes.get(callback);
            if (index == null) {
                index = new Integer(callbacks.size());
                indexes.put(callback, index);
            }
            methodMap.put(method, index);
            callbacks.add(callback);
        }
    }

    abstract protected Object getCallback(Method method);

    public Callback[] getCallbacks()
    {
        if (callbacks.size() == 0)
            return new Callback[0];
        if (callbacks.get(0) instanceof Callback) {
            return (Callback[])callbacks.toArray(new Callback[callbacks.size()]);
        } else {
            throw new IllegalStateException("getCallback returned classes, not callbacks; call getCallbackTypes instead");
        }
    }

    public Class[] getCallbackTypes()
    {
        if (callbacks.size() == 0)
            return new Class[0];
        if (callbacks.get(0) instanceof Callback) {
            return ReflectUtils.getClasses(getCallbacks());
        } else {
            return (Class[])callbacks.toArray(new Class[callbacks.size()]);
        }
    }

    public int accept(Method method)
    {
        return ((Integer)methodMap.get(method)).intValue();
    }

    public int hashCode()
    {
        return methodMap.hashCode();
    }
    
    public boolean equals(Object o)
    {
        if (o == null)
            return false;
        if (!(o instanceof CallbackHelper))
            return false;
        return methodMap.equals(((CallbackHelper)o).methodMap);
    }
}
