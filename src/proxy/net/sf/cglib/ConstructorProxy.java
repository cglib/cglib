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

import java.lang.reflect.*;
/**
 *
 * @author  baliuka
 * @version $Id: ConstructorProxy.java,v 1.5 2003/01/24 19:49:48 herbyderby Exp $
 */
public abstract class ConstructorProxy {
    
    private static final Method NEW_INSTANCE = 
      ReflectUtils.findMethod("ConstructorProxy.newInstance(Object[],MethodInterceptor)");
    
    private static final ClassNameFactory NAME_FACTORY = 
      new ClassNameFactory("ConstructorProxiedByCGLIB");
   
    private static final ClassKey CLASS_KEY_FACTORY =
      (ClassKey)KeyFactory.create(ClassKey.class, null);

    private static final ClassLoader DEFAULT_LOADER =
      ConstructorProxy.class.getClassLoader();
  
    public static Object newClassKey(Class[] args){
        return CLASS_KEY_FACTORY.newInstance(args);
    }
    
    public interface ClassKey {
        public Object newInstance(Class[] args); 
    }

    /** Creates a new instance of ConstructorProxy */
    protected ConstructorProxy() {
    }
   
    public static Object create(Constructor constructor) throws Throwable {
        Class declaring = constructor.getDeclaringClass();
        String className = NAME_FACTORY.getNextName(declaring);
        ClassLoader loader = declaring.getClassLoader();
        if (loader == null) {
            loader = DEFAULT_LOADER;
        }
        Class gen = new Generator(className, constructor, loader).define();
        return (ConstructorProxy)gen.getConstructor(Constants.TYPES_EMPTY).newInstance(null);
    }
    
    public abstract Object newInstance(Object args[], MethodInterceptor interceptor) throws Throwable;
    
    private static class Generator extends CodeGenerator {
        private Constructor constructor;
        
        public Generator(String className, Constructor constructor, ClassLoader loader) {
            super(className, ConstructorProxy.class, loader);
            this.constructor = constructor;
        }

        protected void generate() {
            generateNullConstructor();
            begin_method(NEW_INSTANCE);
            new_instance(constructor.getDeclaringClass());
            dup();
            Class types[] = constructor.getParameterTypes();
            for (int i = 0; i < types.length - 1; i++) {
                load_arg(0);
                push(i);
                aaload();
                unbox(types[i]);
            }
            load_arg(1);
            invoke_constructor(constructor.getDeclaringClass(), types);
            return_value();
            end_method();
        }
    }
}
