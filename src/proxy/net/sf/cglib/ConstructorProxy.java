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
 * @version $Id: ConstructorProxy.java,v 1.8 2003/01/25 00:44:01 herbyderby Exp $
 */
public abstract class ConstructorProxy {
    private static final Method NEW_INSTANCE = 
      ReflectUtils.findMethod("ConstructorProxy.newInstance(Object[])");

    private static final Method NEW_INSTANCE_HACK = 
      ReflectUtils.findMethod("ConstructorProxy.newInstance(Object[], Object)");
    
    private static final ClassNameFactory NAME_FACTORY = 
      new ClassNameFactory("ConstructorProxiedByCGLIB");
   
    private static final ClassKey CLASS_KEY_FACTORY =
      (ClassKey)KeyFactory.create(ClassKey.class, null);

    private static final ClassLoader DEFAULT_LOADER =
      ConstructorProxy.class.getClassLoader();
  
    public static Object newClassKey(Class[] args) {
        return CLASS_KEY_FACTORY.newInstance(args);
    }
    
    public interface ClassKey {
        public Object newInstance(Class[] args); 
    }

    /** Creates a new instance of ConstructorProxy */
    protected ConstructorProxy() {
    }
   
    public static ConstructorProxy create(Constructor constructor) {
        return createHelper(constructor, null);
    }

    public static ConstructorProxy create(Class iface, Class declaring) {
        try {
            Method newInstance = ReflectUtils.findNewInstance(iface);
            if (!newInstance.getReturnType().isAssignableFrom(declaring)) {
                throw new IllegalArgumentException("incompatible return type");
            }
            Constructor constructor = declaring.getDeclaredConstructor(newInstance.getParameterTypes());
            return createHelper(constructor, newInstance);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("interface does not match any known constructor");
        }
    }

    private static ConstructorProxy createHelper(Constructor constructor, Method newInstance) {
        try {
            Class declaring = constructor.getDeclaringClass();
            String className = NAME_FACTORY.getNextName(declaring);
            ClassLoader loader = declaring.getClassLoader();
            if (loader == null) {
                loader = DEFAULT_LOADER;
            }
            Class gen = new Generator(className, constructor, loader, newInstance).define();
            return (ConstructorProxy)gen.getConstructor(Constants.TYPES_EMPTY).newInstance(null);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }
    }

    public abstract Object newInstance(Object[] args);
    public abstract Object newInstance(Object[] args, Object hack);
    
    private static class Generator extends CodeGenerator {
        private Constructor constructor;
        private Method newInstance;
        
        public Generator(String className, Constructor constructor, ClassLoader loader, Method newInstance) {
            super(className, ConstructorProxy.class, loader);
            this.constructor = constructor;
            this.newInstance = newInstance;
        }

        protected void generate() {
            generateNullConstructor();
            generateNewInstanceHelper(NEW_INSTANCE, false);
            generateNewInstanceHelper(NEW_INSTANCE_HACK, true);

            if (newInstance != null) {
                declare_interface(newInstance.getDeclaringClass());
                begin_method(newInstance);
                new_instance(constructor.getDeclaringClass());
                dup();
                load_args();
                invoke(constructor);
                return_value();
                end_method();
            }
        }

        private void generateNewInstanceHelper(Method method, boolean isHack) {
            begin_method(method);
            new_instance(constructor.getDeclaringClass());
            dup();
            Class types[] = constructor.getParameterTypes();
            int stop = isHack ? types.length - 1 : types.length;
            for (int i = 0; i < stop; i++) {
                load_arg(0);
                push(i);
                aaload();
                unbox(types[i]);
            }
            if (isHack) {
                load_arg(1);
                unbox(types[stop]);
            }
            invoke(constructor);
            return_value();
            end_method();
        }
    }
}
