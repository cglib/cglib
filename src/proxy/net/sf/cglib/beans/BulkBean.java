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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import net.sf.cglib.core.*;
import org.objectweb.asm.ClassVisitor;

/**
 * @author Juozas Baliuka
 */
abstract public class BulkBean
{
    private static final BulkBeanKey KEY_FACTORY =
      (BulkBeanKey)KeyFactory.create(BulkBeanKey.class);
    
    interface BulkBeanKey {
        public Object newInstance(Class target, String[] getters, String[] setters, Class[] types);
    }
    
    protected Class target;
    protected String[] getters, setters;
    protected Class[] types;
    
    protected BulkBean() { }
    
    abstract public void getPropertyValues(Object bean, Object[] values);
    abstract public void setPropertyValues(Object bean, Object[] values);

    public Object[] getPropertyValues(Object bean) {
        Object[] values = new Object[getters.length];
        getPropertyValues(bean, values);
        return values;
    }
    
    public Class[] getPropertyTypes() {
        return (Class[])types.clone();
    }
    
    public String[] getGetters() {
        return (String[])getters.clone();
    }
    
    public String[] getSetters() {
        return (String[])setters.clone();
    }

    public static BulkBean create(Class target, String[] getters, String[] setters, Class[] types) {
        Generator gen = new Generator();
        gen.setTarget(target);
        gen.setGetters(getters);
        gen.setSetters(setters);
        gen.setTypes(types);
        return gen.create();
    }

    public static class Generator extends AbstractClassGenerator {
        private static final Source SOURCE = new Source(BulkBean.class.getName(), true);
        private Class target;
        private String[] getters;
        private String[] setters;
        private Class[] types;

        public Generator() {
            super(SOURCE);
        }

        public void setTarget(Class target) {
            this.target = target;
        }

        public void setGetters(String[] getters) {
            this.getters = getters;
        }

        public void setSetters(String[] setters) {
            this.setters = setters;
        }

        public void setTypes(Class[] types) {
            this.types = types;
        }

        protected ClassLoader getDefaultClassLoader() {
            return target.getClassLoader();
        }

        public BulkBean create() {
            Object key = KEY_FACTORY.newInstance(target, getters, setters, types);
            return (BulkBean)super.create(key);
        }

        public void generateClass(ClassVisitor v) throws Exception {
            setNamePrefix(target.getName());
            new BulkBeanEmitter(v, getClassName(), target, getters, setters, types);
        }

        protected Object firstInstance(Class type) {
            BulkBean instance = (BulkBean)ReflectUtils.newInstance(type);
            instance.target = target;
                    
            int length = getters.length;
            instance.getters = new String[length];
            System.arraycopy(getters, 0, instance.getters, 0, length);
                    
            instance.setters = new String[length];
            System.arraycopy(setters, 0, instance.setters, 0, length);
                    
            instance.types = new Class[types.length];
            System.arraycopy(types, 0, instance.types, 0, types.length);

            return instance;
        }

        protected Object nextInstance(Object instance) {
            return instance;
        }
    }
}
