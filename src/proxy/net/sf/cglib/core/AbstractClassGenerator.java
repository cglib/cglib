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
import java.util.*;
import java.lang.ref.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

/**
 * Abstract class for all code-generating CGLIB utilities.
 * In addition to caching generated classes for performance, it provides hooks for
 * customizing the <code>ClassLoader</code>, name of the generated class, and transformations
 * applied before generation.
 */
abstract public class AbstractClassGenerator
implements ClassGenerator
{
    private static final Object NAME_KEY = new Object();
    private static final ThreadLocal CURRENT = new ThreadLocal();

    private GeneratorStrategy strategy = DefaultGeneratorStrategy.INSTANCE;
    private NamingPolicy namingPolicy = DefaultNamingPolicy.INSTANCE;
    private Source source;
    private ClassLoader classLoader;
    private String namePrefix;
    private Object key;
    private boolean useCache = true;
    private String className;
    protected boolean attemptLoad;

    protected static class Source {
        String name;
        Map cache = new WeakHashMap();
        public Source(String name) {
            this.name = name;
        }
    }

    protected AbstractClassGenerator(Source source) {
        this.source = source;
    }

    protected void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    final protected String getClassName() {
        if (className == null)
            className = getClassName(getClassLoader());
        return className;
    }

    private String getClassName(ClassLoader loader) {
        final Set nameCache = getClassNameCache(loader);
        return namingPolicy.getClassName(namePrefix, source.name, key, new Predicate() {
            public boolean evaluate(Object arg) {
                return nameCache.contains(arg);
            }
        });
    }

    private Set getClassNameCache(ClassLoader loader) {
        return (Set)((Map)source.cache.get(loader)).get(NAME_KEY);
    }

    /**
     * Set the <code>ClassLoader</code> in which the class will be generated.
     * Concrete subclasses of <code>AbstractClassGenerator</code> (such as <code>Enhancer</code>)
     * will try to choose an appropriate default if this is unset.
     * <p>
     * Classes are cached per-<code>ClassLoader</code> using a <code>WeakHashMap</code>, to allow
     * the generated classes to be removed when the associated loader is garbage collected.
     * @param classLoader the loader to generate the new class with, or null to use the default
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Override the default naming policy.
     * @see DefaultNamingPolicy
     * @param namingPolicy the custom policy, or null to use the default
     */
    public void setNamingPolicy(NamingPolicy namingPolicy) {
        if (namingPolicy == null)
            namingPolicy = DefaultNamingPolicy.INSTANCE;
        this.namingPolicy = namingPolicy;
    }

    /**
     * @see #setNamingPolicy
     */
    public NamingPolicy getNamingPolicy() {
        return namingPolicy;
    }

    /**
     * Whether use and update the static cache of generated classes
     * for a class with the same properties. Default is <code>true</code>.
     */
    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    /**
     * @see #setUseCache
     */
    public boolean getUseCache() {
        return useCache;
    }

    /**
     * If set, CGLIB will attempt to load classes from the specified
     * <code>ClassLoader</code> before generating them. Because generated
     * class names are not guaranteed to be unique, the default is <code>false</code>.
     */
    public void setAttemptLoad(boolean attemptLoad) {
        this.attemptLoad = attemptLoad;
    }

    public boolean getAttemptLoad() {
        return attemptLoad;
    }
    
    /**
     * Set the strategy to use to create the bytecode from this generator.
     * By default an instance of {@see DefaultGeneratorStrategy} is used.
     */
    public void setStrategy(GeneratorStrategy strategy) {
        if (strategy == null)
            strategy = DefaultGeneratorStrategy.INSTANCE;
        this.strategy = strategy;
    }

    /**
     * @see #setStrategy
     */
    public GeneratorStrategy getStrategy() {
        return strategy;
    }

    /**
     * Used internally by CGLIB. Returns the <code>AbstractClassGenerator</code>
     * that is being used to generate a class in the current thread.
     */
    public static AbstractClassGenerator getCurrent() {
        return (AbstractClassGenerator)CURRENT.get();
    }

    public ClassLoader getClassLoader() {
        ClassLoader t = classLoader;
        if (t == null) {
            t = getDefaultClassLoader();
        }
        if (t == null) {
            t = getClass().getClassLoader();
        }
        if (t == null) {
            t = Thread.currentThread().getContextClassLoader();
        }
        if (t == null) {
            throw new IllegalStateException("Cannot determine classloader");
        }
        return t;
    }

    abstract protected ClassLoader getDefaultClassLoader();

    protected Object create(Object key) {
        Object save = CURRENT.get();
        try {
            Object instance = null;
            synchronized (source) {
                ClassLoader loader = getClassLoader();
                Map cache2 = null;
                cache2 = (Map)source.cache.get(loader);
                if (cache2 == null) {
                    cache2 = new HashMap();
                    cache2.put(NAME_KEY, new HashSet());
                    source.cache.put(loader, cache2);
                } else if (useCache) {
                    Reference ref = (Reference)cache2.get(key);
                    instance = ( ref == null ) ? null : ref.get(); 
                }
                if (instance == null) {
                    CURRENT.set(this);
                    this.key = key;
                    Class gen = null;
                    if (attemptLoad) {
                        try {
                            gen = loader.loadClass(getClassName());
                        } catch (ClassNotFoundException e) {
                            // ignore
                        }
                    }
                    if (gen == null) {
                        byte[] b = strategy.generate(this);
                        String className = ClassNameReader.getClassName(new ClassReader(b));
                        getClassNameCache(loader).add(className);
                        gen = ReflectUtils.defineClass(className, b, loader);
                    }
                    instance = firstInstance(gen);
                    if (useCache) {
                        cache2.put(key, new SoftReference(instance));
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
        } finally {
            CURRENT.set(save);
        }
    }

    abstract protected Object firstInstance(Class type) throws Exception;
    abstract protected Object nextInstance(Object instance) throws Exception;
}
