/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * 4. The names "Apache Cocoon" and "Apache Software Foundation" must
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
package net.sf.cglib.proxy;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import net.sf.cglib.util.*;

/**
 *
 * this code returns Enhanced Vector to intercept  all methods for tracing
 *   <pre>
 *         java.util.Vector vector = (java.util.Vector)Enhancer.enhance(
 *        java.util.Vector.<b>class</b>,
 *        new Class[]{java.util.List.<b>class</b>},
 *
 *        new MethodInterceptor(){
 *
 *
 *            <b>public boolean invokeSuper</b>( Object obj,java.lang.reflect.Method method,
 *            Object args[])
 *            throws java.lang.Throwable{
 *                return true;
 *            }
 *
 *
 *        <b>public</b> Object <b>afterReturn</b>(  Object obj,     java.lang.reflect.Method method,
 *        Object args[],
 *        boolean invokedSuper, Object retValFromSuper,
 *        java.lang.Throwable e )throws java.lang.Throwable{
 *            System.out.println(method);
 *            return retValFromSuper;//return the same as supper
 *        }
 *
 *    });
 * </pre>
 *@author     Juozas Baliuka <a href="mailto:baliuka@mwm.lt">
 *      baliuka@mwm.lt</a>
 *@version    $Id: Enhancer.java,v 1.31 2002/11/16 19:20:10 herbyderby Exp $
 */
public class Enhancer implements ClassFileConstants {
    private static final String CLASS_PREFIX = "net.sf.cglib.proxy";
    private static final String CLASS_SUFFIX = "$$EnhancedByCGLIB$$";
    private static int index = 0;
    private static Map factories = new HashMap();
    private static Map cache = new WeakHashMap();

    private Enhancer() {}
    
    public static MethodInterceptor getMethodInterceptor(Object enhanced){
      
            return ((Factory)enhanced).getInterceptor();
        
    }
    
    
    
    /**
     *  implemented as
     * return enhance(cls,interfaces,ih, null);
     */
    public static Object enhance(
    Class cls,
    Class interfaces[],
    MethodInterceptor ih)
    throws CodeGenerationException {
        
        return enhance(
        cls,
        interfaces,
        ih,
        null,
        null );
    }
     public synchronized static Object enhance(
    Class cls,
    Class interfaces[],
    MethodInterceptor ih,
    ClassLoader loader )
    throws CodeGenerationException {
        return enhance(
        cls,
        interfaces,
        ih,
        loader,
        null );
   
     } 
    /** enhances public not final class,
     * source class must have public or protected no args constructor.
     * Code is generated for protected and public not final methods,
     * package scope methods supported from source class package.
     * Defines new class in  source class package, if it not java*.
     * @param cls class to extend, uses Object.class if null
     * @param interfaces interfaces to implement, can be null
     * @param ih valid interceptor implementation
     * @param loader classloater for enhanced class, uses "current" if null
     * @param wreplace  static method to implement writeReplace, must have
     * single Object type parameter(to replace) and return object, 
     * default implementation from InternalReplace is used if
     * parameter is null : static public Object InternalReplace.writeReplace( 
     *                                                       Object enhanced )
     *                 throws ObjectStreamException;
     * @throws Throwable on error
     * @return instanse of enhanced  class
     */
    public synchronized static Object enhance(Class cls,
    Class interfaces[],
    MethodInterceptor ih,
    ClassLoader loader,
    java.lang.reflect.Method wreplace )
    throws CodeGenerationException {
        if (ih == null) {
            throw new NullPointerException("MethodInterceptor is null");
        }
        
        if (cls == null) {
            cls = Object.class;
        }
        
        if (loader == null) {
            loader = Enhancer.class.getClassLoader();
        }

        Object key = new Key(cls, interfaces, wreplace);
        
        Map map = (Map)cache.get(loader);
        if (map == null) {
            map = new Hashtable();
            cache.put(loader, map);
        }
        Class result = (Class) map.get(key);
        
        if ( result == null ) {
            String class_name = cls.getName() + CLASS_SUFFIX;
            if (class_name.startsWith("java")) {
                class_name = CLASS_PREFIX + class_name;
            }
            class_name += index++;
            result = new EnhancerGenerator(class_name, cls, interfaces, ih, loader, wreplace).define();
            map.put(key, result);
        }
        
        Factory factory = (Factory)factories.get(result);
        if (factory == null) {
            try {
                Class mi = Class.forName(MethodInterceptor.class.getName(), true, loader);
                factory = (Factory)result.getConstructor(new Class[]{ mi }).newInstance(new Object[] { null });
                factories.put(result,factory);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new CodeGenerationException(e);
            }
        }
        return factory.newInstance(ih);
    }

    private static final class Key {
        private static final int hashConstant = 13; // positive and odd
        private int hash = 41; // positive and odd
        private Class cls;
        private Class[] interfaces;
        private Method wreplace;

        public Key(Class cls, Class[] interfaces, Method wreplace) {
            this.cls = cls;
            this.interfaces = interfaces;
            this.wreplace = wreplace;
            hash = hash * hashConstant + cls.hashCode();
            if (interfaces != null) {
                for (int i = 0, size = interfaces.length; i < size; i++) {
                    hash = hash * hashConstant + interfaces[i].hashCode();
                }
            }
            if (wreplace != null)
                hash = hash * hashConstant + wreplace.hashCode();
        }

        public boolean equals(Object obj) {
            Key other = (Key)obj;
            return cls.equals(other.cls) &&
                (wreplace == null ? other.wreplace == null : wreplace.equals(other.wreplace)) &&
                Arrays.equals(interfaces, other.interfaces);
        }

        public int hashCode() {
            return hash;
        }
    }


    static public class InternalReplace implements Serializable {
        private String parentClassName;
        private String [] interfaceNames;
        private MethodInterceptor mi;
        
        public InternalReplace() {
        }
        
        private InternalReplace(String parentClassName, String[] interfaces,
                                MethodInterceptor mi) {
            this.parentClassName = parentClassName;
            this.interfaceNames   = interfaceNames;
            this.mi = mi;
        }
        
        public static Object writeReplace(Object enhanced) throws ObjectStreamException {
            MethodInterceptor mi = Enhancer.getMethodInterceptor(enhanced);
            String parentClassName = enhanced.getClass().getSuperclass().getName();
            Class interfaces[] = enhanced.getClass().getInterfaces();
            String [] interfaceNames = new String[interfaces.length];
            
            for (int i = 0; i < interfaces.length; i++) {
                interfaceNames[i] = interfaces[i].getName();
            }
            
            return new InternalReplace(parentClassName, interfaceNames, mi);
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
            } catch (Throwable t) { // TODO
                // throw new ObjectStreamException(t.getMessage());
                throw new RuntimeException("TODO");
            }
        }
    }
}
