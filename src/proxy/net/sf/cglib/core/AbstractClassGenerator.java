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
package net.sf.cglib.core;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

abstract public class AbstractClassGenerator
implements ClassGenerator
{
    private static final NamingPolicy DEFAULT_NAMING_POLICY = new NamingPolicy() {
        public String getClassName(String prefix, String source, int counter) {
            StringBuffer sb = new StringBuffer();
            sb.append((prefix != null) ? prefix : "net.sf.cglib.empty.Object");
            sb.append("$$");
            sb.append(source.substring(source.lastIndexOf('.') + 1));
            sb.append("ByCGLIB$$");
            sb.append(counter);
            return sb.toString();
        }
    };

    private NamingPolicy namingPolicy;
    private Source source;
    private ClassLoader classLoader;
    private String namePrefix;
    private int counter;

    protected static class Source {
        String name;
        Map cache;
        int counter = 1;

        public Source(String name, boolean useCache) {
            this.name = name;
            if (useCache) {
                cache = new WeakHashMap();
            }
        }
    }

    protected AbstractClassGenerator(Source source) {
        this.source = source;
    }

    protected void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    protected String getClassName() {
        NamingPolicy np = (namingPolicy != null) ? namingPolicy : DEFAULT_NAMING_POLICY;
        return np.getClassName(namePrefix, source.name, counter);
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setNamingPolicy(NamingPolicy namingPolicy) {
        this.namingPolicy = namingPolicy;
    }

    // TODO: pluggable policy?
    protected ClassLoader getClassLoader() {
        ClassLoader t = classLoader;
        if (t == null) {
            t = getDefaultClassLoader();
        }
        if (t == null) {
            t = getClass().getClassLoader();
        }
        if (t == null) {
            throw new IllegalStateException("Cannot determine classloader");
        }
        return t;
    }

    abstract protected ClassLoader getDefaultClassLoader();

    protected Object create(Object key) {
        try {
            Object instance = null;
            synchronized (source) {
                counter = source.counter++;
                ClassLoader loader = getClassLoader();
                Map cache2 = null;
                if (source.cache != null) {
                    cache2 = (Map)source.cache.get(loader);
                    if (cache2 != null) {
                        instance = cache2.get(key);
                    } else {
                        source.cache.put(loader, cache2 = new HashMap());
                    }
                }
                if (instance == null) {
                    DebuggingClassWriter w = new DebuggingClassWriter(true);
                    generateClass(w);
                    byte[] b = w.toByteArray();
                    Class gen = ReflectUtils.defineClass(getClassName(), b, loader);
                    instance = firstInstance(gen);

                    if (cache2 != null) {
                        cache2.put(key, instance);
                    }
                    return instance;
                }
            }
            return nextInstance(instance);
        } catch (RuntimeException e) {
            throw e;
        } catch (Error e) {
            throw e;
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }
    }

    abstract protected Object firstInstance(Class type) throws Exception;
    abstract protected Object nextInstance(Object instance) throws Exception;
}
