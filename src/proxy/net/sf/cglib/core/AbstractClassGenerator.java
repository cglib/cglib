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
    private static final String KEY_FIELD = "CGLIB$ACGKEY";
    private static final Object NAME_KEY = new Object();
    
    private static final NamingPolicy DEFAULT_NAMING_POLICY = new NamingPolicy() {
        public String getClassName(String prefix, String source, Object key) {
            StringBuffer sb = new StringBuffer();
            sb.append((prefix != null) ? prefix : "net.sf.cglib.empty.Object");
            sb.append("$$");
            sb.append(source.substring(source.lastIndexOf('.') + 1));
            sb.append("ByCGLIB$$");
            sb.append(Integer.toHexString(key.hashCode()));
            return sb.toString();
        }
    };

    private NamingPolicy namingPolicy;
    private Source source;
    private ClassLoader classLoader;
    private String namePrefix;
    private Object key;
    private Transformer transformer;

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

    protected String getClassName() {
        NamingPolicy np = (namingPolicy != null) ? namingPolicy : DEFAULT_NAMING_POLICY;
        return np.getClassName(namePrefix, source.name, key);
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setNamingPolicy(NamingPolicy namingPolicy) {
        this.namingPolicy = namingPolicy;
    }

    public void setTransformer(Transformer transformer) {
        this.transformer = transformer;
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
                ClassLoader loader = getClassLoader();
                Map cache2 = (Map)source.cache.get(loader);
                if (cache2 != null) {
                    instance = cache2.get(key);
                } else {
                    cache2 = new HashMap();
                    cache2.put(NAME_KEY, new HashSet());
                    source.cache.put(loader, cache2);
                }
                if (instance == null) {
                    this.key = key;
                    final String keyFieldValue = key.toString();
                    String className = getClassName();
                    Class gen = null;
                    try {
                        gen = loader.loadClass(className);
                        if (gen.getClassLoader() != loader) {
                            gen = null;
                        } else if (!keyFieldValue.equals(getKeyField(gen))) {
                            // TODO: log?
                            gen = null;
                        }
                    } catch (ClassNotFoundException e) {
                    }
                    if (gen == null) {
                        className = uniquify(className, (Set)cache2.get(NAME_KEY));
                        DebuggingClassWriter w = new DebuggingClassWriter(true) {
                            public void visitEnd() {
                                visitField(Constants.ACC_STATIC | Constants.ACC_PUBLIC,
                                           KEY_FIELD,
                                           Constants.TYPE_STRING.getDescriptor(),
                                           keyFieldValue);
                                super.visitEnd();
                            }
                        };
                        ClassGenerator cg = this;
                        if (transformer != null) {
                            cg = (ClassGenerator)transformer.transform(this);
                        }
                        cg.generateClass(w);
                        byte[] b = w.toByteArray();
                        if (!className.equals(w.getClassName())) {
                            throw new IllegalStateException("Class name " + className +
                                                            " does not match generated name: " + w.getClassName());
                        }
                        gen = ReflectUtils.defineClass(className, b, loader);
                    }
                    cache2.put(key, instance = firstInstance(gen));
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

    private static String uniquify(String origName, Set names) {
        String className = origName;
        int index = 2;
        while (names.contains(className)) {
            className = origName + "-" + index++;
        }
        names.add(className);
        return className;
    }

    private static String getKeyField(Class type) {
        try {
            return (String)type.getDeclaredField(KEY_FIELD).get(null);
        } catch (NoSuchFieldException e) {
            return null;
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }
    }

    abstract protected Object firstInstance(Class type) throws Exception;
    abstract protected Object nextInstance(Object instance) throws Exception;
}
