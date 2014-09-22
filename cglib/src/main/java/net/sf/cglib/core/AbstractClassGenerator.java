/*
 * Copyright 2003,2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.cglib.core;

import org.objectweb.asm.ClassReader;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.security.ProtectionDomain;
import java.util.*;

/**
 * Abstract class for all code-generating CGLIB utilities.
 * In addition to caching generated classes for performance, it provides hooks for
 * customizing the <code>ClassLoader</code>, name of the generated class, and transformations
 * applied before generation.
 */
abstract public class AbstractClassGenerator
        implements ClassGenerator {
    private static final Object NAME_KEY = new Object();
    private static final ThreadLocal CURRENT = new ThreadLocal();

    private net.sf.cglib.core.GeneratorStrategy strategy = net.sf.cglib.core.DefaultGeneratorStrategy.INSTANCE;
    private net.sf.cglib.core.NamingPolicy namingPolicy = net.sf.cglib.core.DefaultNamingPolicy.INSTANCE;
    private Source source;
    private ClassLoader classLoader;
    private String namePrefix;
    private Object key;
    private boolean useCache = true;
    private String className;
    private boolean attemptLoad;

    protected AbstractClassGenerator(Source source) {
        this.source = source;
    }

    /**
     * Used internally by CGLIB. Returns the <code>AbstractClassGenerator</code>
     * that is being used to generate a class in the current thread.
     */
    public static AbstractClassGenerator getCurrent() {
        return (AbstractClassGenerator) CURRENT.get();
    }

    protected void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    final protected String getClassName() {
        if (className == null)
            className = getClassName(getClassLoader());
        return className;
    }

    private String getClassName(final ClassLoader loader) {
        final Set nameCache = getClassNameCache(loader);
        return namingPolicy.getClassName(namePrefix, source.name, key, new Predicate() {
            public boolean evaluate(Object arg) {
                return nameCache.contains(arg);
            }
        });
    }

    private Set getClassNameCache(ClassLoader loader) {
        return (Set) ((Map) source.cache.get(loader)).get(NAME_KEY);
    }

    /**
     * @see #setNamingPolicy
     */
    public net.sf.cglib.core.NamingPolicy getNamingPolicy() {
        return namingPolicy;
    }

    /**
     * Override the default naming policy.
     *
     * @param namingPolicy the custom policy, or null to use the default
     * @see DefaultNamingPolicy
     */
    public void setNamingPolicy(net.sf.cglib.core.NamingPolicy namingPolicy) {
        if (namingPolicy == null)
            namingPolicy = net.sf.cglib.core.DefaultNamingPolicy.INSTANCE;
        this.namingPolicy = namingPolicy;
    }

    /**
     * @see #setUseCache
     */
    public boolean getUseCache() {
        return useCache;
    }

    /**
     * Whether use and update the static cache of generated classes
     * for a class with the same properties. Default is <code>true</code>.
     */
    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    public boolean getAttemptLoad() {
        return attemptLoad;
    }

    /**
     * If set, CGLIB will attempt to load classes from the specified
     * <code>ClassLoader</code> before generating them. Because generated
     * class names are not guaranteed to be unique, the default is <code>false</code>.
     */
    public void setAttemptLoad(boolean attemptLoad) {
        this.attemptLoad = attemptLoad;
    }

    /**
     * @see #setStrategy
     */
    public net.sf.cglib.core.GeneratorStrategy getStrategy() {
        return strategy;
    }

    /**
     * Set the strategy to use to create the bytecode from this generator.
     * By default an instance of {@see DefaultGeneratorStrategy} is used.
     */
    public void setStrategy(net.sf.cglib.core.GeneratorStrategy strategy) {
        if (strategy == null)
            strategy = net.sf.cglib.core.DefaultGeneratorStrategy.INSTANCE;
        this.strategy = strategy;
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

    /**
     * Set the <code>ClassLoader</code> in which the class will be generated.
     * Concrete subclasses of <code>AbstractClassGenerator</code> (such as <code>Enhancer</code>)
     * will try to choose an appropriate default if this is unset.
     * <p/>
     * Classes are cached per-<code>ClassLoader</code> using a <code>WeakHashMap</code>, to allow
     * the generated classes to be removed when the associated loader is garbage collected.
     *
     * @param classLoader the loader to generate the new class with, or null to use the default
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    abstract protected ClassLoader getDefaultClassLoader();

    /**
     * Returns the protection domain to use when defining the class.
     * <p>
     * Default implementation returns <code>null</code> for using a default protection domain. Sub-classes may
     * override to use a more specific protection domain.
     * </p>
     *
     * @return the protection domain (<code>null</code> for using a default)
     */
    protected ProtectionDomain getProtectionDomain() {
        return null;
    }

    protected Object create(Object key) {
        try {
            Class gen = null;

            synchronized (source) {
                ClassLoader loader = getClassLoader();
                ProtectionDomain protectionDomain = getProtectionDomain();
                Map cache2 = null;
                cache2 = (Map) source.cache.get(loader);
                if (cache2 == null) {
                    cache2 = new HashMap();
                    cache2.put(NAME_KEY, new HashSet());
                    source.cache.put(loader, cache2);
                } else if (useCache) {
                    Reference ref = (Reference) cache2.get(key);
                    gen = (Class) ((ref == null) ? null : ref.get());
                }
                if (gen == null) {
                    Object save = CURRENT.get();
                    CURRENT.set(this);
                    try {
                        this.key = key;

                        if (attemptLoad) {
                            try {
                                gen = loader.loadClass(getClassName());
                            } catch (ClassNotFoundException e) {
                                // ignore
                            }
                        }
                        if (gen == null) {
                            byte[] b = strategy.generate(this);
                            String className = net.sf.cglib.core.ClassNameReader.getClassName(new ClassReader(b));
                            getClassNameCache(loader).add(className);
                            if (protectionDomain == null) {
                                gen = net.sf.cglib.core.ReflectUtils.defineClass(className, b, loader);
                            } else {
                                gen = net.sf.cglib.core.ReflectUtils.defineClass(className, b, loader, protectionDomain);
                            }
                        }

                        if (useCache) {
                            cache2.put(key, new WeakReference(gen));
                        }
                        return firstInstance(gen);
                    } finally {
                        CURRENT.set(save);
                    }
                }
            }
            return firstInstance(gen);
        } catch (RuntimeException e) {
            throw e;
        } catch (Error e) {
            throw e;
        } catch (Exception e) {
            throw new net.sf.cglib.core.CodeGenerationException(e);
        }
    }

    abstract protected Object firstInstance(Class type) throws Exception;

    abstract protected Object nextInstance(Object instance) throws Exception;

    protected static class Source {
        String name;
        Map cache = new WeakHashMap();

        public Source(String name) {
            this.name = name;
        }
    }
}
