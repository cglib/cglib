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
package net.sf.cglib.util;

import java.lang.reflect.Constructor;
import java.util.*;

public class FactoryCache {
    private final Map cache;
    private final ClassLoader defaultLoader;
    private String suffix;
    private int counter = 0;

    public FactoryCache(Class source) {
        this(source, new WeakHashMap());
    }

    public FactoryCache(Class source, Map cache) {
        defaultLoader = source.getClassLoader();
        suffix = "$$" + ReflectUtils.getNameWithoutPackage(source) + "ByCGLIB$$";
        this.cache = cache;
    }

    public Class getClass(ClassLoader loader, Object key, Constructor gen,
                          Object a0) {
        return (Class)getHelper(false, loader, key, gen, 1, a0, null, null, null);
    }

    public Class getClass(ClassLoader loader, Object key, Constructor gen,
                          Object a0, Object a1) {
        return (Class)getHelper(false, loader, key, gen, 2, a0, a1, null, null);
    }
        
    public Class getClass(ClassLoader loader, Object key, Constructor gen,
                          Object a0, Object a1, Object a2) {
        return (Class)getHelper(false, loader, key, gen, 3, a0, a1, a2, null);
    }

    public Class getClass(ClassLoader loader, Object key, Constructor gen,
                          Object a0, Object a1, Object a2, Object a3) {
        return (Class)getHelper(false, loader, key, gen, 4, a0, a1, a2, a3);
    }

    public Object getFactory(ClassLoader loader, Object key, Constructor gen,
                             Object a0) {
        return getHelper(true, loader, key, gen, 1, a0, null, null, null);
    }

    public Object getFactory(ClassLoader loader, Object key, Constructor gen,
                             Object a0, Object a1) {
        return getHelper(true, loader, key, gen, 2, a0, a1, null, null);
    }
        
    public Object getFactory(ClassLoader loader, Object key, Constructor gen,
                             Object a0, Object a1, Object a2) {
        return getHelper(true, loader, key, gen, 3, a0, a1, a2, null);
    }

    public Object getFactory(ClassLoader loader, Object key, Constructor gen,
                             Object a0, Object a1, Object a2, Object a3) {
        return getHelper(true, loader, key, gen, 4, a0, a1, a2, a3);
    }

    private Object getHelper(boolean isFactory, ClassLoader loader, Object key, Constructor gen,
                             int numArgs, Object a0, Object a1, Object a2, Object a3) {
        if (loader == null) {
            loader = defaultLoader;
        }
        Object factory;
        synchronized (this) {
            factory = get(loader, key);
            if (factory == null) {
                Object[] args = new Object[numArgs];
                switch (numArgs) {
                case 4: args[3] = a3;
                case 3: args[2] = a2;
                case 2: args[1] = a1;
                case 1: args[0] = a0;
                }

                BasicCodeGenerator cg = (BasicCodeGenerator)ReflectUtils.newInstance(gen, args);
                cg.setNameSuffix(suffix + counter++);
                cg.setClassLoader(loader);
                factory = cg.define();
                if (isFactory) {
                    factory = ReflectUtils.newInstance((Class)factory);
                }
                if (key != null) {
                    put(loader, key, factory);
                }
            }
        }
        return factory;
    }

    private Object get(ClassLoader loader, Object key) {
        return getFactoryMap(loader).get(key);
    }

    private void put(ClassLoader loader, Object key, Object factory) {
        getFactoryMap(loader).put(key, factory);
    }

    private Map getFactoryMap(ClassLoader loader) {
        Map factories = (Map)cache.get(loader);
        if (factories == null) {
            cache.put(loader, factories = new HashMap());
        }
        return factories;
    }
}
