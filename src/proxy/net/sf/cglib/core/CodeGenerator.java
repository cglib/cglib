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

abstract public class CodeGenerator
{
    private static String debugLocation;
    private static RuntimePermission DEFINE_CGLIB_CLASS_IN_JAVA_PACKAGE_PERMISSION =
      new RuntimePermission("defineCGLIBClassInJavaPackage");

    private Source source;
    private ClassLoader classLoader;

    // TODO: make these package-protected so emitter can access them?
    private String className;
    private Class superclass;
    private Class[] interfaces;
    private boolean used;

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

    private void used() {
        if (used) {
            throw new IllegalStateException(getClass().getName() + " has already been used");
        }
        used = true;
    }

    protected CodeGenerator(Source source) {
        this.source = source;
    }

    protected void setSuperclass(Class superclass) {
        this.superclass = superclass;
    }

    protected void setInterfaces(Class[] interfaces) {
        this.interfaces = interfaces;
    }

    protected Class getSuperclass() {
        return superclass;
    }

    protected Class[] getInterfaces() {
        return interfaces;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    // TODO: pluggable policy?
    protected String getClassName() {
        if (className != null) {
            return className;
        } else {
            // TODO: use package of interface if applicable
            return ((superclass != null) ? superclass : Object.class).getName()
                + "$$" + ReflectUtils.getNameWithoutPackage(source.type)
                + "ByCGLIB$$" + source.counter++;
        }
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    // TODO: pluggable policy?
    protected ClassLoader getClassLoader() {
        ClassLoader t = classLoader;
        if (t != null) {
            return t;
        }
        if (superclass != null) {
            t = superclass.getClassLoader();
        }
        if (t != null) {
            return t;
        }
        t = getClass().getClassLoader();
        if (t != null) {
            return t;
        }
        throw new IllegalStateException("Cannot determine classloader");
        // return Thread.currentThread().getContextClassLoader();
    }

    protected Object create(Object key) {
        used();
        try {
            Object factory = null;
            boolean isNew = false;
            synchronized (source) {
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
                isNew = factory == null;
                if (isNew) {
                    factory = newFactory(defineClass(source.defineClass, getClassName(), generate(), loader));
                    if (cache2 != null) {
                        cache2.put(key, factory);
                    }
                }
            }
            return newInstance(factory, isNew);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        } catch (Error e) {
            throw e;
        }
    }

    abstract protected byte[] generate() throws Exception;
    abstract protected Object newFactory(Class type) throws Exception;
    abstract protected Object newInstance(Object factory, boolean isNew);

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
