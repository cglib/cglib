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

import net.sf.cglib.core.CodeGenerator;
import net.sf.cglib.core.ReflectUtils;
import java.lang.reflect.Method;
import java.util.*;

public class Enhancer extends CodeGenerator
{
    private static final Source SOURCE = new Source(Enhancer.class, true);
    private static final EnhancerKey KEY_FACTORY =
      (EnhancerKey)KeyFactory.create(EnhancerKey.class, null);

    interface EnhancerKey {
        public Object newInstance(Class type, Class[] interfaces, CallbackFilter filter, boolean classOnly);
    }

    private Class[] interfaces;
    private CallbackFilter filter;
    private Callbacks callbacks;
    private boolean classOnly;
    
    public Enhancer() {
        super(SOURCE);
    }

    public void setSuperclass(Class superclass) {
        if (superclass != null && superclass.isInterface()) {
            interfaces = new Class[]{ superclass };
        } else {
            super.setSuperclass(superclass);
        }
    }

    public void setInterfaces(Class[] interfaces) {
        this.interfaces = interfaces;
    }

    public void setCallbackFilter(CallbackFilter filter) {
        this.filter = filter;
    }

    public void setCallback(final Callback callback) {
        setCallbacks(new Callbacks() {
            public Callback get(int type) {
                return callback;
            }
        });
        setCallbackFilter(new SimpleFilter(CallbackUtils.determineType(callback)));
    }

    public void setCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    public Factory create() {
        classOnly = false;
        return (Factory)createHelper();
    }

    public Class createClass() {
        classOnly = true;
        return (Class)createHelper();
    }

    private Object createHelper() {
        Object key = KEY_FACTORY.newInstance(getSuperclass(), interfaces, filter, classOnly);
        return super.create(key);
    }

    protected byte[] getBytes() throws Exception {
        return new EnhancerEmitter(getClassName(), getSuperclass(), interfaces, filter).getBytes();
    }

    protected Object firstInstance(Class type) throws Exception {
        if (classOnly) {
            return type;
        }
        
        // this is a hack
        Method setter = type.getDeclaredMethod(EnhancerEmitter.SET_THREAD_CALLBACKS,
                                               new Class[]{ Callbacks.class });
        setter.invoke(null, new Object[]{ callbacks });

        Factory instance = (Factory)ReflectUtils.newInstance(type);
        instance.setCallbacks(callbacks);
        return instance;
    }

    protected Object nextInstance(Object instance) {
        return classOnly ? instance : ((Factory)instance).newInstance(callbacks);
    }
}
