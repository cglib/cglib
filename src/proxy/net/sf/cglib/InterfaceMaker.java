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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Generates interfaces from a list of classes or methods.
 * By passing a generated interface to the Enhancer's list of interfaces to
 * implement, you can make your enhanced classes handle an arbitrary set
 * of method signatures.
 * @author Chris Nokleberg
 * @version $Id: InterfaceMaker.java,v 1.3 2003/05/28 03:56:30 herbyderby Exp $
 */
public class InterfaceMaker {
    private static final Class TYPE = InterfaceMaker.class;
    private static final ClassLoader DEFAULT_LOADER = TYPE.getClassLoader();
    private static final ClassNameFactory NAME_FACTORY = new ClassNameFactory("CreatedByCGLIB");

    private InterfaceMaker() { }

    /**
     * Create a interface consisting of all the public methods of the specified classes.
     * Methods from superclasses are included, except for methods declared in the base
     * Object class (e.g. getClass(), equals(), hashCode()).
     * @param classes the class array
     * @param loader ClassLoader for enhanced class, uses "current" if null
     */
    public static Class create(Class[] classes, ClassLoader loader) {
        return create(classes, NotFromObjectFilter.INSTANCE, loader);
    }

    /**
     * Create a interface consisting of all the public methods of the specified classes.
     * Methods from superclasses are included, <b>including</b> methods declared in the base
     * Object class.
     * @param classes the class array
     * @param filter the MethodFilter used to limit which methods are generated in the interface
     * @param loader ClassLoader for enhanced class, uses "current" if null
     * @see NotFromObjectFilter
     */
    public static Class create(Class[] classes, MethodFilter filter, ClassLoader loader) {
        Set keys = new HashSet();
        List methods = new ArrayList();
        for (int i = 0; i < classes.length; i++) {
            Class cls = classes[i];
            Method[] clsMethods = cls.getMethods();
            for (int j = 0; j < clsMethods.length; j++) {
                Method method = clsMethods[j];
                if (filter == null || filter.accept(method)) {
                    // TODO: need to allow for same signature, different return types?
                    Object key = MethodWrapper.create(method); 
                    if (!keys.contains(key)) {
                        keys.add(key);
                        methods.add(method);
                    }
                }
            }
        }
        Method[] array = (Method[])methods.toArray(new Method[methods.size()]);
        return create(array, loader);
    }

    /**
     * Create a interface consisting of all the specified methods. The access flags on the methods
     * are ignored (all interface methods are by definition abstract and public).
     * @param methods the methods to generate
     * @param loader ClassLoader for enhanced class, uses "current" if null
     */
    public static Class create(Method[] methods, ClassLoader loader) {
        if (loader == null)
            loader = DEFAULT_LOADER;
        String className = NAME_FACTORY.getNextName(TYPE);
        return new Generator(className, methods, loader).define();
    }

    private static class Generator extends CodeGenerator {
        private Method[] methods;

        public Generator(String className, Method[] methods, ClassLoader loader) {
            super(className, Object.class, loader);
            setInterface(true);
            this.methods = methods;
        }

        protected void generate() throws NoSuchMethodException, NoSuchFieldException {
            for (int i = 0; i < methods.length; i++) {
                begin_method(methods[i], Modifier.PUBLIC | Modifier.ABSTRACT);
                end_method();
            }
        }
    }
}
