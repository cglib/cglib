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

/**
 * @author Chris Nokleberg <a href="mailto:chris@nokleberg.com">chris@nokleberg.com</a>
 * @version $Id: MethodProxy.java,v 1.6 2002/12/21 08:36:45 herbyderby Exp $
 */
abstract public class MethodProxy {
    private static/* final */Method INVOKE_SUPER = null;//bug in jdk1.2 javac
    private static final String CLASS_PREFIX = "net.sf.cglib";
    private static final String CLASS_SUFFIX = "$$ProxiedByCGLIB$$";
    private static int index = 0;

    static {
        try {
            Class[] types = new Class[]{ Object.class, Object[].class };
            INVOKE_SUPER = MethodProxy.class.getDeclaredMethod("invokeSuper", types);
        } catch (NoSuchMethodException e) {
            throw new CodeGenerationException(e);
        }
    }

    abstract public Object invokeSuper(Object obj, Object[] args) throws Throwable;

    protected MethodProxy() { }

    public static MethodProxy generate(Method method) {
        return generate(method, null);
    }

    public static MethodProxy generate(Method method, ClassLoader loader) {
        try {
            Class methodClass = method.getDeclaringClass();
            if (loader == null) {
                loader = methodClass.getClassLoader();
                if (loader == null) {
                    loader = MethodProxy.class.getClassLoader();
                }
            }
            Class gen = new Generator(getNextName(methodClass), method, loader).define();
            return (MethodProxy)gen.getConstructor(Constants.TYPES_EMPTY).newInstance(null);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }
    }

    // TODO: move to helper class
    private static String getNextName(Class cls) {
        String class_name = cls.getName() + CLASS_SUFFIX;
        if (class_name.startsWith("java")) {
            class_name = CLASS_PREFIX + class_name;
        }
        class_name += index++;
        return class_name;
    }

    private static class Generator extends CodeGenerator {
        private Method method;
        
        public Generator(String className, Method method, ClassLoader loader) {
            super(className, MethodProxy.class, loader);
            this.method = method;
        }

        protected void generate() {
            generateNullConstructor();
            begin_method(INVOKE_SUPER);
            load_arg(0);
            checkcast(method.getDeclaringClass());
            Class[] types = method.getParameterTypes();
            for (int i = 0; i < types.length; i++) {
                load_arg(1);
                push(i);
                aaload();
                unbox(types[i]);
            }
            invoke(method);
            box(method.getReturnType());
            return_value();
            end_method();
        }
    }
}
