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
package net.sf.cglib.reflect;

import java.lang.reflect.*;
import net.sf.cglib.core.*;
import org.objectweb.asm.ClassVisitor;

/**
 * @author Chris Nokleberg
 * @version $Id: ConstructorDelegate.java,v 1.5 2003/09/17 20:44:27 herbyderby Exp $
 */
abstract public class ConstructorDelegate {
    private static final ConstructorKey KEY_FACTORY =
      (ConstructorKey)KeyFactory.create(ConstructorKey.class);
    
    interface ConstructorKey {
        public Object newInstance(Class declaring, Class iface);
    }

    protected ConstructorDelegate() {
    }

    public static ConstructorDelegate create(Class targetClass, Class iface) {
        Generator gen = new Generator();
        gen.setTargetClass(targetClass);
        gen.setInterface(iface);
        return gen.create();
    }

    public static class Generator extends AbstractClassGenerator {
        private static final Source SOURCE = new Source(ConstructorDelegate.class, true);

        private Class iface;
        private Class targetClass;

        public Generator() {
            super(SOURCE);
        }

        public void setInterface(Class iface) {
            this.iface = iface;
        }

        public void setTargetClass(Class targetClass) {
            this.targetClass = targetClass;
        }

        public ConstructorDelegate create() {
            Object key = KEY_FACTORY.newInstance(iface, targetClass);
            return (ConstructorDelegate)super.create(key);
        }

        protected ClassLoader getDefaultClassLoader() {
            return targetClass.getClassLoader();
        }

        public void generateClass(ClassVisitor v) {
            setNamePrefix(targetClass.getName());

            Method newInstance = ReflectUtils.findNewInstance(iface);
            if (!newInstance.getReturnType().isAssignableFrom(targetClass)) {
                throw new IllegalArgumentException("incompatible return type");
            }
            Constructor constructor;
            try {
                constructor = targetClass.getDeclaredConstructor(newInstance.getParameterTypes());
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("interface does not match any known constructor");
            }

            Emitter e = new Emitter(v);
            e.begin_class(Modifier.PUBLIC,
                          getClassName(),
                          ConstructorDelegate.class,
                          new Class[]{ iface },
                          Constants.SOURCE_FILE);
            Virt.null_constructor(e);
            e.begin_method(newInstance);
            e.new_instance(constructor.getDeclaringClass());
            e.dup();
            e.load_args();
            e.invoke(constructor);
            e.return_value();
            e.end_method();
            e.end_class();
        }

        protected Object firstInstance(Class type) {
            return ReflectUtils.newInstance(type);
        }

        protected Object nextInstance(Object instance) {
            return instance;
        }
    }
}
