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

import java.beans.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import net.sf.cglib.core.*;
import org.objectweb.asm.ClassVisitor;

/**
 * A <code>Map</code>-based view of a JavaBean.  The default set of keys is the
 * union of all property names (getters or setters). An attempt to set
 * a read-only property will be ignored, and write-only properties will
 * be returned as <code>null</code>. Removal of objects is not a
 * supported (the key set is fixed).
 * @author Chris Nokleberg
 */
abstract public class BeanMap implements Map {
    /**
     * Limit the properties reflected in the key set of the map
     * to readable properties.
     * @see BeanMap.Generator#setRequire
     */
    public static final int REQUIRE_GETTER = 1;

    /**
     * Limit the properties reflected in the key set of the map
     * to writable properties.
     * @see BeanMap.Generator#setRequire
     */
    public static final int REQUIRE_SETTER = 2;
    
    /**
     * Helper method to create a new <code>BeanMap</code>.  For finer
     * control over the generated instance, use a new instance of
     * <code>BeanMap.Generator</code> instead of this static method.
     * @param bean the JavaBean underlying the map
     * @return a new <code>BeanMap</code> instance
     */
    public static BeanMap create(Object bean) {
        Generator gen = new Generator();
        gen.setBean(bean);
        return gen.create();
    }

    public static class Generator extends AbstractClassGenerator {
        private static final Source SOURCE = new Source(BeanMap.class.getName());

        private static final BeanMapKey KEY_FACTORY =
          (BeanMapKey)KeyFactory.create(BeanMapKey.class, KeyFactory.CLASS_BY_NAME);

        interface BeanMapKey {
            public Object newInstance(Class type, int require);
        }
        
        private Object bean;
        private Class beanClass;
        private int require;
        
        public Generator() {
            super(SOURCE);
        }

        /**
         * Set the bean that the generated map should reflect. The bean may be swapped
         * out for another bean of the same type using {@link #setBean}.
         * Calling this method overrides any value previously set using {@link #setBeanClass}.
         * You must call either this method or {@link #setBeanClass} before {@link #create}.
         * @param bean the initial bean
         */
        public void setBean(Object bean) {
            this.bean = bean;
            if (bean != null)
                beanClass = bean.getClass();
        }

        /**
         * Set the class of the bean that the generated map should support.
         * You must call either this method or {@link #setBeanClass} before {@link #create}.
         * @param beanClass the class of the bean
         */
        public void setBeanClass(Class beanClass) {
            this.beanClass = beanClass;
        }

        /**
         * Limit the properties reflected by the generated map.
         * @param require any combination of {@link #REQUIRE_GETTER} and
         * {@link #REQUIRE_SETTER}; default is zero (any property allowed)
         */
        public void setRequire(int require) {
            this.require = require;
        }

        protected ClassLoader getDefaultClassLoader() {
            return beanClass.getClassLoader();
        }

        /**
         * Create a new instance of the <code>BeanMap</code>. An existing
         * generated class will be reused if possible.
         */
        public BeanMap create() {
            if (beanClass == null)
                throw new IllegalArgumentException("Class of bean unknown");
            setNamePrefix(beanClass.getName());
            return (BeanMap)super.create(KEY_FACTORY.newInstance(beanClass, require));
        }

        public void generateClass(ClassVisitor v) throws Exception {
            new BeanMapEmitter(v, getClassName(), beanClass, require);
        }

        protected Object firstInstance(Class type) {
            return ((BeanMap)ReflectUtils.newInstance(type)).newInstance(bean);
        }

        protected Object nextInstance(Object instance) {
            return ((BeanMap)instance).newInstance(bean);
        }
    }

    /**
     * Create a new <code>BeanMap</code> instance using the specified bean.
     * This is faster than using the {@link #create} static method.
     * @param bean the JavaBean underlying the map
     * @return a new <code>BeanMap</code> instance
     */
    abstract public BeanMap newInstance(Object bean);

    /**
     * Get the type of a property.
     * @param name the name of the JavaBean property
     * @return the type of the property, or null if the property does not exist
     */
    abstract public Class getPropertyType(String name);

    protected Object bean;

    protected BeanMap() {
    }

    protected BeanMap(Object bean) {
        setBean(bean);
    }

    public Object get(Object key) {
        return get(bean, key);
    }

    public Object put(Object key, Object value) {
        return put(bean, key, value);
    }

    /**
     * Get the property of a bean. This allows a <code>BeanMap</code>
     * to be used statically for multiple beans--the bean instance tied to the
     * map is ignored and the bean passed to this method is used instead.
     * @param bean the bean to query; must be compatible with the type of
     * this <code>BeanMap</code>
     * @param key must be a String
     * @return the current value, or null if there is no matching property
     */
    abstract public Object get(Object bean, Object key);

    /**
     * Set the property of a bean. This allows a <code>BeanMap</code>
     * to be used statically for multiple beans--the bean instance tied to the
     * map is ignored and the bean passed to this method is used instead.
     * @param key must be a String
     * @return the old value, if there was one, or null
     */
    abstract public Object put(Object bean, Object key, Object value);

    /**
     * Change the underlying bean this map should use.
     * @param bean the new JavaBean
     * @see #getBean
     */
    public void setBean(Object bean) {
        this.bean = bean;
    }

    /**
     * Return the bean currently in use by this map.
     * @return the current JavaBean
     * @see #setBean
     */
    public Object getBean() {
        return bean;
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public boolean containsKey(Object key) {
        return keySet().contains(key);
    }

    public boolean containsValue(Object value) {
        for (Iterator it = keySet().iterator(); it.hasNext();) {
            Object v = get(it.next());
            if (((value == null) && (v == null)) || value.equals(v))
                return true;
        }
        return false;
    }

    public int size() {
        return keySet().size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public Object remove(Object key) {
        throw new UnsupportedOperationException();
    }

    public void putAll(Map t) {
        for (Iterator it = t.keySet().iterator(); it.hasNext();) {
            Object key = it.next();
            put(key, t.get(key));
        }
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof Map)) {
            return false;
        }
        Map other = (Map)o;
        if (size() != other.size()) {
            return false;
        }
        for (Iterator it = keySet().iterator(); it.hasNext();) {
            Object key = it.next();
            if (!other.containsKey(key)) {
                return false;
            }
            Object v1 = get(key);
            Object v2 = other.get(key);
            if (!((v1 == null) ? v2 == null : v1.equals(v2))) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int code = 0;
        for (Iterator it = keySet().iterator(); it.hasNext();) {
            Object key = it.next();
            Object value = get(key);
            code += ((key == null) ? 0 : key.hashCode()) ^
                ((value == null) ? 0 : value.hashCode());
        }
        return code;
    }

    // TODO: optimize
    public Set entrySet() {
        return Collections.unmodifiableMap(new HashMap(this)).entrySet();
    }

    public Collection values() {
        Set keys = keySet();
        List values = new ArrayList(keys.size());
        for (Iterator it = keys.iterator(); it.hasNext();) {
            values.add(get(it.next()));
        }
        return Collections.unmodifiableCollection(values);
    }

    /*
     * @see java.util.AbstractMap#toString
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append('{');
        for (Iterator it = keySet().iterator(); it.hasNext();) {
            Object key = it.next();
            sb.append(key);
            sb.append('=');
            sb.append(get(key));
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append('}');
        return sb.toString();
    }
}
