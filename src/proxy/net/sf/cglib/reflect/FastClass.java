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
package net.sf.cglib.reflect;

import net.sf.cglib.util.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

abstract public class FastClass
{
    private static final FactoryCache CACHE = new FactoryCache(FastClass.class);

    private Class type;

    protected FastClass(Class type) {
        this.type = type;
    }

    public static FastClass create(Class type) {
        return create(type, null);
    }

    public static FastClass create(final Class type, ClassLoader loader) {
        if (loader == null) {
            loader = type.getClassLoader();
        }
        return (FastClass)CACHE.get(loader, type, new FactoryCache.AbstractCallback() {
            public BasicCodeGenerator newGenerator() {
                return new FastClassGenerator(type);
            }
            public Object newFactory(Class ftype) {
                return ReflectUtils.newInstance(ftype,
                                                new Class[]{ Class.class },
                                                new Object[]{ type });
            }
        });
    }

    // TODO: change throws clause
    public Object invoke(String name, Class[] parameterTypes, Object obj, Object[] args) throws Throwable {
        return invoke(getIndex(name, parameterTypes), obj, args);
    }

    // TODO: change throws clause
    public Object newInstance() throws Throwable {
        return newInstance(getIndex(Constants.TYPES_EMPTY), null);
    }

    // TODO: change throws clause
    public Object newInstance(Class[] parameterTypes, Object[] args) throws Throwable {
        return newInstance(getIndex(parameterTypes), args);
    }
    
    public FastMethod getMethod(Method method) {
        return new FastMethod(this, method);
    }

    public FastConstructor getConstructor(Constructor constructor) {
        return new FastConstructor(this, constructor);
    }

    public FastMethod getMethod(String name, Class[] parameterTypes) {
        try {
            return getMethod(type.getMethod(name, parameterTypes));
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    public FastConstructor getConstructor(Class[] parameterTypes) {
        try {
            return getConstructor(type.getConstructor(parameterTypes));
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    public String getName() {
        return type.getName();
    }

    public Class getJavaClass() {
        return type;
    }

    public String toString() {
        return type.toString();
    }

    public int hashCode() {
        return type.hashCode();
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof FastClass)) {
            return false;
        }
        return type.equals(((FastClass)o).type);
    }
    
    abstract public int getIndex(String name, Class[] parameterTypes);
    abstract public int getIndex(Class[] parameterTypes);
    abstract public Object invoke(int index, Object obj, Object[] args) throws Throwable;
    abstract public Object newInstance(int index, Object[] args) throws Throwable;
}
