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

abstract public class AbstractClassGenerator
implements ClassGenerator
{
    private static String debugLocation;
    private static RuntimePermission DEFINE_CGLIB_CLASS_IN_JAVA_PACKAGE_PERMISSION =
      new RuntimePermission("defineCGLIBClassInJavaPackage");

    private Source source;
    private ClassLoader classLoader;

    private String className;
    private String namePrefix;
    private int counter;

    static {
        debugLocation = System.getProperty("cglib.debugLocation");
    }

    protected static class Source {
        Class type;
        Map cache;
        int counter = 1;
        final Method defineClass =
          ReflectUtils.findMethod("ClassLoader.defineClass(byte[], int, int)");

        public Source(Class type, boolean useCache) {
            this.type = type;
            if (useCache) {
                cache = new WeakHashMap();
            }
        }
    }

    protected AbstractClassGenerator(Source source) {
        this.source = source;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    protected void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    // TODO: pluggable policy?
    protected String getClassName() {
        if (className != null) {
            return className;
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append((namePrefix != null) ? namePrefix : "net.sf.cglib.empty.Object");
            sb.append("$$");
            sb.append(ReflectUtils.getNameWithoutPackage(source.type));
            sb.append("ByCGLIB$$");
            sb.append(counter);
            return sb.toString();
        }
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
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
            Object factory = null;
            synchronized (source) {
                counter = source.counter++;
                ClassLoader loader = getClassLoader();
                Map cache2 = null;
                if (source.cache != null) {
                    cache2 = (Map)source.cache.get(loader);
                    if (cache2 != null) {
                        factory = cache2.get(key);
                    } else {
                        source.cache.put(loader, cache2 = new HashMap());
                    }
                }
                if (factory == null) {
                    ClassWriter cw = new ClassWriter(true);
                    generateClass(cw);
                    byte[] b = cw.toByteArray();
                    Class gen = defineClass(source.defineClass, getClassName(), b, loader);

                    factory = firstInstance(gen);

                    if (cache2 != null) {
                        cache2.put(key, factory);
                    }
                    return factory;
                }
            }
            return nextInstance(factory);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        } catch (Error e) {
            throw e;
        }
    }

    abstract protected Object firstInstance(Class type) throws Exception;
    abstract protected Object nextInstance(Object factory) throws Exception;

    private static Class defineClass(Method m, String className, byte[] b, ClassLoader loader) throws Exception {
        if (debugLocation != null) {
            File file = new File(new File(debugLocation), className + ".class");
            // System.err.println("CGLIB writing " + file);
            OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            out.write(b);
            out.close();
        }
        
        m.setAccessible(true);
        SecurityManager sm = System.getSecurityManager();
        if (className != null && className.startsWith("java.") && sm != null) {
            sm.checkPermission(DEFINE_CGLIB_CLASS_IN_JAVA_PACKAGE_PERMISSION);
        }
        // deprecated method in jdk to define classes, used because it
        // does not throw SecurityException if class name starts with "java."
        Object[] args = new Object[]{ b, new Integer(0), new Integer(b.length) };
        return (Class)m.invoke(loader, args);
    }
}
