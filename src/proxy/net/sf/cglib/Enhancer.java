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
import net.sf.cglib.util.*;

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
 * @version $Id: Enhancer.java,v 1.48 2003/09/09 18:01:36 herbyderby Exp $
 */
public class Enhancer {
    private static final FactoryCache cache = new FactoryCache(Enhancer.class);
    private static final EnhancerKey KEY_FACTORY =
      (EnhancerKey)KeyFactory.create(EnhancerKey.class, null);
   
    interface EnhancerKey {
        public Object newInstance(Class type, Class[] interfaces,
                                  CallbackFilter filter);
    }
    
    private Enhancer() { }

    /**
     * Overrides non-abstract methods and implements all abstract methods.  
     * The returned instance extends/implements the supplied class, and
     * additionally implements the Factory interface.
     * @param cls Class or interface to extend or implement
     * @param ih interceptor used to handle implemented methods
     * @return instance of supplied Class; new Class is defined in the same class loader
     */
    public static Factory enhance(Class cls, Callback callback) {
        return (Factory)enhanceHelper(cls.isInterface() ? null : cls,
                                      cls.isInterface() ? new Class[]{ cls } : null,
                                      callback, cls.getClassLoader(), null );
    }
     
    /**
     * Helper method, has same effect as <pre>return enhance(cls, interfaces, ih, null, null);</pre>
     * @see #enhance(Class, Class[], MethodInterceptor, ClassLoader, Method, MethodFilter)
     */
    public static Factory enhance(Class cls, Class interfaces[], Callback callback) {
        return enhanceHelper(cls, interfaces, callback, null, null);
    }

    /**
     * Helper method, has same effect as <pre>return enhance(cls, interfaces, ih, loader, null);</pre>
     * @see #enhance(Class, Class[], MethodInterceptor, ClassLoader, Method, MethodFilter)
     */
    public static Factory enhance(Class cls, Class interfaces[], Callback callback, ClassLoader loader) {
        return enhanceHelper(cls, interfaces, callback, loader, null);
    } 

    /**
     * Enhances a public non-final class. Source class must have a public or protected
     * no-args constructor. Code is generated for protected and public non-final methods,
     * and package methods if the source class is not in a the java.* hierarchy.
     * @param cls class to extend, uses Object.class if null
     * @param interfaces interfaces to implement, can be null or empty
     * @param ih interceptor used to handle implemented methods
     * @param loader ClassLoader for enhanced class, uses "current" if null
     * @param filter a filter to prevent certain methods from being intercepted, may be null to intercept all possible methods
     * @return an instance of the enhanced class. Will extend the source class and implement the given
     * interfaces, plus the CGLIB Factory interface.
     * @see Factory
     */
    public static Factory enhance(Class cls, Class[] interfaces, Callbacks callbacks,
                                 ClassLoader loader, CallbackFilter filter) {
        return enhanceHelper(cls, interfaces, callbacks, loader, filter);
    }
    
    /**
     * This method can be used to enhance a class that does not have a no-args constructor.
     * @return the generated class; you must use reflection to create new object instances.
     * @param cls class to extend, uses Object.class if null
     * @param interfaces interfaces to implement, can be null or empty
     * @param loader ClassLoader for enhanced class, uses "current" if null
     * @param filter a filter to prevent certain methods from being intercepted
     */
    public static Class enhanceClass(Class cls,
                                     final Class[] interfaces,
                                     ClassLoader loader,
                                     final CallbackFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("CallbackFilter is required for enhanceClass");
        }
        final Class base = (cls == null) ? Object.class : cls;
        return (Class)
            cache.get(loader,
                      KEY_FACTORY.newInstance(base, interfaces, filter),
                      new FactoryCache.ClassOnlyCallback() {
                          public BasicCodeGenerator newGenerator() {
                              return new EnhancerGenerator(base, interfaces, filter, null);
                          }
                      });
    }

    private static Factory enhanceHelper(Class cls,
                                         Class[] interfaces,
                                         final Callback callback,
                                         ClassLoader loader,
                                         CallbackFilter filter) {
        Callbacks callbacks = new Callbacks() {
            public Callback get(int type) {
                return callback;
            }
        };
        if (filter == null) {
            filter = new SimpleFilter(CallbackUtils.determineType(callback));
        }
        return enhanceHelper(cls, interfaces, callbacks, loader, filter);
    }
    
    private static Factory enhanceHelper(Class cls,
                                         final Class[] interfaces,
                                         final Callbacks callbacks,
                                         ClassLoader loader,
                                         final CallbackFilter filter) {
        final Class base = (cls == null) ? Object.class : cls;
        Object key = KEY_FACTORY.newInstance(base, interfaces, filter);
        return (Factory)cache.get(loader, key, new FactoryCache.AbstractCallback() {
            public BasicCodeGenerator newGenerator() {
                return new EnhancerGenerator(base, interfaces, filter, callbacks);
            }
            public Object newInstance(Object factory, boolean isNew) {
                if (isNew) {
                    ((Factory)factory).setCallbacks(callbacks);
                    return factory;
                } else {
                    return ((Factory)factory).newInstance(callbacks);
                }
            }
        });
    }
}
