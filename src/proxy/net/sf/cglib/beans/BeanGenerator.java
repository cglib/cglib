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
import java.util.*;
import net.sf.cglib.core.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

/**
 * @author Juozas Baliuka, Chris Nokleberg
 */
public class BeanGenerator extends AbstractClassGenerator
{
    private static final Source SOURCE = new Source(BeanGenerator.class.getName());
    private static final BeanGeneratorKey KEY_FACTORY =
      (BeanGeneratorKey)KeyFactory.create(BeanGeneratorKey.class);
    
    interface BeanGeneratorKey {
        public Object newInstance(Class superclass, Map props);
    }

    private Class superclass;
    private Map props = new HashMap();
    private boolean classOnly;

    public BeanGenerator() {
        super(SOURCE);
    }

    /**
     * Set the class which the generated class will extend. The class
     * must not be declared as final, and must have a non-private
     * no-argument constructor.
     * @param superclass class to extend, or null to extend Object
     */
    public void setSuperclass(Class superclass) {
        if (superclass != null && superclass.equals(Object.class)) {
            superclass = null;
        }
        this.superclass = superclass;
    }

    public void addProperty(String name, Class type) {
        if (props.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate property name \"" + name + "\"");
        }
        props.put(name, Type.getType(type));
    }

    protected ClassLoader getDefaultClassLoader() {
        if (superclass != null) {
            return superclass.getClassLoader();
        } else {
            return null;
        }
    }

    public Object create() {
        classOnly = false;
        return createHelper();
    }

    public Object createClass() {
        classOnly = true;
        return createHelper();
    }

    private Object createHelper() {
        if (superclass != null) {
            setNamePrefix(superclass.getName());
        }
        Object key = KEY_FACTORY.newInstance(superclass, props);
        return super.create(key);
    }

    public void generateClass(ClassVisitor v) throws Exception {
        int size = props.size();
        String[] names = (String[])props.keySet().toArray(new String[size]);
        Type[] types = new Type[size];
        for (int i = 0; i < size; i++) {
            types[i] = (Type)props.get(names[i]);
        }
        ClassEmitter ce = new ClassEmitter(v);
        ce.begin_class(Constants.ACC_PUBLIC,
                       getClassName(),
                       superclass != null ? Type.getType(superclass) : Constants.TYPE_OBJECT,
                       null,
                       null);
        EmitUtils.null_constructor(ce);
        EmitUtils.add_properties(ce, names, types);
        ce.end_class();
    }

    protected Object firstInstance(Class type) {
        if (classOnly) {
            return type;
        } else {
            return ReflectUtils.newInstance(type);
        }
    }

    protected Object nextInstance(Object instance) {
        Class protoclass = (instance instanceof Class) ? (Class)instance : instance.getClass();
        if (classOnly) {
            return protoclass;
        } else {
            return ReflectUtils.newInstance(protoclass);
        }
    }

    public static void addProperties(BeanGenerator gen, Map props) {
        for (Iterator it = props.keySet().iterator(); it.hasNext();) {
            String name = (String)it.next();
            gen.addProperty(name, (Class)props.get(name));
        }
    }

    public static void addProperties(BeanGenerator gen, Class type) {
        addProperties(gen, ReflectUtils.getBeanProperties(type));
    }

    public static void addProperties(BeanGenerator gen, PropertyDescriptor[] descriptors) {
        for (int i = 0; i < descriptors.length; i++) {
            gen.addProperty(descriptors[i].getName(), descriptors[i].getPropertyType());
        }
    }
}
