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
package net.sf.cglib.beans;

import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import net.sf.cglib.core.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;
import java.util.*;

/**
 * @author Chris Nokleberg
 */
abstract public class BeanCopier
{
    private static final BeanCopierKey KEY_FACTORY =
      (BeanCopierKey)KeyFactory.create(BeanCopierKey.class);
    private static final Signature COPY =
      TypeUtils.parseSignature("void copy(Object, Object, net.sf.cglib.core.Converter)");
    private static final Signature CONVERT =
      TypeUtils.parseSignature("Object convert(Object, Class, Object)");
    private static final Type CONVERTER =
      TypeUtils.parseType("net.sf.cglib.core.Converter");
    private static final Type BEAN_COPIER =
      TypeUtils.parseType("net.sf.cglib.beans.BeanCopier");
    
    interface BeanCopierKey {
        public Object newInstance(Class source, Class target, boolean useConverter);
    }

    public static BeanCopier create(Class source, Class target, boolean useConverter) {
        Generator gen = new Generator();
        gen.setSource(source);
        gen.setTarget(target);
        gen.setUseConverter(useConverter);
        return gen.create();
    }

    abstract public void copy(Object from, Object to, Converter converter);

    public static class Generator extends AbstractClassGenerator {
        private static final Source SOURCE = new Source(BeanCopier.class.getName());
        private Class source;
        private Class target;
        private boolean useConverter;

        public Generator() {
            super(SOURCE);
        }

        public void setSource(Class source) {
            if(!Modifier.isPublic(source.getModifiers())){ 
               setNamePrefix(source.getName());
            }
            this.source = source;
        }
        
        public void setTarget(Class target) {
            if(!Modifier.isPublic(target.getModifiers())){ 
               setNamePrefix(target.getName());
            }
          
            this.target = target;
        }

        public void setUseConverter(boolean useConverter) {
            this.useConverter = useConverter;
        }

        protected ClassLoader getDefaultClassLoader() {
            return source.getClassLoader();
        }

        public BeanCopier create() {
            Object key = KEY_FACTORY.newInstance(source, target, useConverter);
            return (BeanCopier)super.create(key);
        }

        public void generateClass(ClassVisitor v) {
            Type sourceType = Type.getType(source);
            Type targetType = Type.getType(target);
            ClassEmitter ce = new ClassEmitter(v);
            ce.begin_class(Constants.ACC_PUBLIC,
                           getClassName(),
                           BEAN_COPIER,
                           null,
                           Constants.SOURCE_FILE);

            EmitUtils.null_constructor(ce);
            CodeEmitter e = ce.begin_method(Constants.ACC_PUBLIC, COPY, null, null);
            PropertyDescriptor[] getters = ReflectUtils.getBeanGetters(source);
            PropertyDescriptor[] setters = ReflectUtils.getBeanGetters(target);

            Map names = new HashMap();
            for (int i = 0; i < getters.length; i++) {
                names.put(getters[i].getName(), getters[i]);
            }
            Local targetLocal = e.make_local();
            Local sourceLocal = e.make_local();
            if (useConverter) {
                e.load_arg(1);
                e.checkcast(targetType);
                e.store_local(targetLocal);
                e.load_arg(0);                
                e.checkcast(sourceType);
                e.store_local(sourceLocal);
            } else {
                e.load_arg(1);
                e.checkcast(targetType);
                e.load_arg(0);
                e.checkcast(sourceType);
            }
            for (int i = 0; i < setters.length; i++) {
                PropertyDescriptor setter = setters[i];
                PropertyDescriptor getter = (PropertyDescriptor)names.get(setter.getName());
                if (getter != null) {
                    if (useConverter) {
                        Type getterType = Type.getType(getter.getPropertyType());
                        Type setterType = Type.getType(setter.getPropertyType());
                        e.load_local(targetLocal);
                        e.load_arg(2);
                        e.load_local(sourceLocal);
                        e.invoke(getter.getReadMethod());
                        e.box(getterType);
                        EmitUtils.load_class(e, setterType);
                        e.push(setter.getName());
                        e.invoke_interface(CONVERTER, CONVERT);
                        e.unbox_or_zero(setterType);
                        e.invoke(setter.getWriteMethod());
                    } else if (compatible(getter, setter)) {
                        e.dup2();
                        e.invoke(getter.getReadMethod());
                        e.invoke(setter.getWriteMethod());
                    }
                }
            }
            e.return_value();
            e.end_method();
            ce.end_class();
        }

        private static boolean compatible(PropertyDescriptor getter, PropertyDescriptor setter) {
            // TODO: allow automatic widening conversions?
            return setter.getPropertyType().isAssignableFrom(getter.getPropertyType());
        }

        protected Object firstInstance(Class type) {
            return ReflectUtils.newInstance(type);
        }

        protected Object nextInstance(Object instance) {
            return instance;
        }
    }
}
