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

import java.io.*;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides methods to create dynamic proxies of any class, not just interfaces.
 * <p>
 * This code enhances a Vector object for tracing, by intercepting all methods:
 * <pre>
 *   java.util.Vector vector = (java.util.Vector)Enhancer.enhance(
 *     java.util.Vector.class,                      // extend Vector
 *     new Class[]{ java.util.List.class },         // implement List
 *     new MethodInterceptor() {
 *         public Object intercept(Object obj, java.lang.reflect.Method method,
 *                                 Object[] args, MethodProxy proxy) throws Throwable {
 *             System.out.println(method);
 *             return proxy.invokeSuper(obj, args); // invoke original method
 *         }
 *     });
 * </pre>
 * <p>
 * Enhancer is comparable to the standard <a
 * href="http://java.sun.com/j2se/1.4.1/docs/api/java/lang/reflect/Proxy.html">Dynamic
 * Proxy</a> feature built into Java, but with some important
 * differences, including:
 * <ul>
 * <li>Runs on JDK 1.2.
 * <li>Can proxy (almost) any class, not just interfaces.
 * <li>Less overhead on intercepted calls (due to the use of less reflection).
 * </ul>
 * <p>
 * All enhanced objects implement the Factory interface. 
 *
 * @see MethodInterceptor
 * @see Factory
 * @author Juozas Baliuka <a href="mailto:baliuka@mwm.lt">baliuka@mwm.lt</a>
 * @version $Id: Enhancer.java,v 1.39 2003/05/28 03:56:30 herbyderby Exp $
 */
public class Enhancer {
    private static final FactoryCache cache = new FactoryCache();
    private static final FactoryCache classCache = new FactoryCache();
    private static final ClassLoader defaultLoader = Enhancer.class.getClassLoader();
    private static final EnhancerKey keyFactory =
      (EnhancerKey)KeyFactory.create(EnhancerKey.class, null);

    private static final ClassNameFactory nameFactory = new ClassNameFactory("EnhancedByCGLIB");

   
     interface EnhancerKey {
        public Object newInstance( Class cls, Class[] interfaces, 
                                   Method wreplace, MethodFilter filter);
    }
    
    private Enhancer() {}

    /**
     * Helper method to get the current interceptor for an enhanced object.
     * @throws ClassCastException If the object is not an enhanced object (i.e. does not implement Factory).
     * @see Factory#interceptor()
     */
    public static MethodInterceptor getMethodInterceptor(Object enhanced) {
        return ((Factory)enhanced).interceptor();
    }
    
    /**
     * Overrides non-abstract methods and implements all abstract methods.  
     * The returned instance extends/implements the supplied class, and
     * additionally implements the Factory interface.
     * @param cls Class or interface to extend or implement
     * @param ih interceptor used to handle implemented methods
     * @return instance of supplied Class; new Class is defined in the same class loader
     */
    public static Factory enhance(Class cls, MethodInterceptor ih) {
        return (Factory)enhanceHelper(cls.isInterface() ? null : cls,
                                      cls.isInterface() ? new Class[]{ cls } : null,
                                      ih, cls.getClassLoader(), null, null );
    }
     
    /**
     * Helper method, has same effect as <pre>return enhance(cls, interfaces, ih, null, null, null);</pre>
     * @see #enhance(Class, Class[], MethodInterceptor, ClassLoader, Method, MethodFilter)
     */
    public static Object enhance(Class cls, Class interfaces[], MethodInterceptor ih) {
        return enhanceHelper(cls, interfaces, ih, null, null, null);
    }

    /**
     * Helper method, has same effect as <pre>return enhance(cls, interfaces, ih, loader, null, null);</pre>
     * @see #enhance(Class, Class[], MethodInterceptor, ClassLoader, Method, MethodFilter)
     */
    public static Object enhance(Class cls, Class interfaces[], MethodInterceptor ih,
                                 ClassLoader loader) {
        return enhanceHelper(cls, interfaces, ih, loader, null, null);
   
    } 

    /**
     * Helper method, has same effect as <pre>return enhance(cls, interfaces, ih, loader, wreplace, null);</pre>
     * @see #enhance(Class, Class[], MethodInterceptor, ClassLoader, Method, MethodFilter)
     */
    public static Object enhance(Class cls, Class[] interfaces, MethodInterceptor ih,
                                 ClassLoader loader, Method wreplace) {
        return enhanceHelper(cls, interfaces, ih, loader, wreplace, null);
    }

    /**
     * Enhances a public non-final class. Source class must have a public or protected
     * no-args constructor. Code is generated for protected and public non-final methods,
     * and package methods if the source class is not in a the java.* hierarchy.
     * @param cls class to extend, uses Object.class if null
     * @param interfaces interfaces to implement, can be null or empty
     * @param ih interceptor used to handle implemented methods
     * @param loader ClassLoader for enhanced class, uses "current" if null
     * @param wreplace static method to implement writeReplace, must have
     * a single Object type parameter (to replace) and return type of Object.
     * If null, a default implementation is used.
     * @param filter a filter to prevent certain methods from being intercepted, may be null to intercept all possible methods
     * @return an instance of the enhanced class. Will extend the source class and implement the given
     * interfaces, plus the CGLIB Factory interface.
     * @see InternalReplace#writeReplace(Object)
     * @see Factory
     */
    public static Object enhance(Class cls, Class[] interfaces, MethodInterceptor ih,
                                 ClassLoader loader, Method wreplace, MethodFilter filter) {
        return enhanceHelper(cls, interfaces, ih, loader, wreplace, filter);
    }

    /**
     * This method can be used to enhance a class that does not have a no-args constructor.
     * @return the generated class; you must use reflection to create new object instances.
     * @param cls class to extend, uses Object.class if null
     * @param interfaces interfaces to implement, can be null or empty
     * @param loader ClassLoader for enhanced class, uses "current" if null
     * @param filter a filter to prevent certain methods from being intercepted, may be null to intercept all possible methods
     */
    public static Class enhanceClass(Class cls, Class[] interfaces,
                                     ClassLoader loader, MethodFilter filter) {
        if (cls == null) {
            cls = Object.class;
        }
        if (loader == null) {
            loader = defaultLoader;
        }
        return enhanceClassHelper( cls, interfaces, loader, null, filter);
    }

    private static Object enhanceHelper(Class cls,
                                        Class[] interfaces, MethodInterceptor ih,
                                        ClassLoader loader, Method wreplace,
                                        MethodFilter filter) {
        if (ih == null) {
            throw new IllegalArgumentException("MethodInterceptor is null");
        }
        if (cls == null) {
            cls = Object.class;
        }
        if (loader == null) {
            loader = defaultLoader;
        }
        Object key = keyFactory.newInstance(cls, interfaces, wreplace, filter);
        Factory factory;
        synchronized (cache) {
            factory = (Factory)cache.get(loader, key);
            if (factory == null) {
                Class gen = enhanceClassHelper(cls, interfaces, loader, wreplace, filter);
                factory = (Factory)ReflectUtils.newInstance(gen);
                cache.put(loader, key, factory);
            }
        }
            return factory.newInstance(ih);
       
    }

    private static Class enhanceClassHelper(Class cls,
                                            Class[] interfaces, ClassLoader loader, Method wreplace,
                                            MethodFilter filter) {
        Object key = keyFactory.newInstance(cls, interfaces, wreplace,  filter);
        Class result;
        synchronized (classCache) {
            result = (Class)classCache.get(loader, key);
            if (result == null) {
                String className = nameFactory.getNextName(cls);
                result = new EnhancerGenerator(className, cls,
                                               interfaces, loader, wreplace, 
                                               filter).define();
                classCache.put(loader, key, result);
            }
        }
        return result;
    }

    /**
     * Class containing the default implementation of the <code>writeReplace</code> method.
     * TODO: document what I do
     */
    public static class InternalReplace implements Serializable {
        private String parentClassName;
        private String [] interfaceNames;
        private MethodInterceptor mi;
        
        public InternalReplace() {
        }
        
        private InternalReplace(String parentClassName, String[] interfaceNames,
                                MethodInterceptor mi) {
            this.parentClassName = parentClassName;
            this.interfaceNames   = interfaceNames;
            this.mi = mi;
        }

        public static Object writeReplace(Object enhanced) throws ObjectStreamException {
            MethodInterceptor mi = Enhancer.getMethodInterceptor(enhanced);
            String parentClassName = enhanced.getClass().getSuperclass().getName();
            Class interfaces[] = enhanced.getClass().getInterfaces();
            List interfaceNames = new ArrayList(interfaces.length);
            
            for (int i = 0; i < interfaces.length; i++) {
                // skip CGLIB interfaces
                if (!ReflectUtils.getPackageName(interfaces[i]).equals("net.sf.cglib")) {
                    interfaceNames.add(interfaces[i].getName());
                }
            }

            return new InternalReplace(
                parentClassName, 
                (String[]) interfaceNames.toArray(new String[interfaceNames.size()]), 
                mi
            );
        }
        
        
        private Object readResolve() throws ObjectStreamException {
            try {
                ClassLoader loader = getClass().getClassLoader();
                Class parent = loader.loadClass(parentClassName);
                Class interfaces[] = null;
                
                if (interfaceNames != null) {
                    interfaces = new Class[interfaceNames.length];
                    for (int i = 0; i< interfaceNames.length; i++) {
                        interfaces[i] = loader.loadClass(interfaceNames[i]);
                    }
                }
                return Enhancer.enhance(parent, interfaces, mi, loader);
            } catch (ClassNotFoundException e) {
                throw new ReadResolveException(e);
            } catch (CodeGenerationException e) {
                throw new ReadResolveException(e.getCause());
            }
        }
    }

     static class ReadResolveException extends ObjectStreamException {
        private Throwable cause;

        public ReadResolveException(Throwable cause) {
            super(cause.getMessage());
            this.cause = cause;

        }

        public Throwable getCause() {
            return cause;
        }
    }
}
