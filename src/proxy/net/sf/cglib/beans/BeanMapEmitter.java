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
import java.util.*;
import net.sf.cglib.core.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

class BeanMapEmitter extends ClassEmitter {
    private static final Signature CSTRUCT_OBJECT =
      TypeUtils.parseConstructor("Object");
    private static final Signature CSTRUCT_STRING_ARRAY =
      TypeUtils.parseConstructor("String[]");
    private static final Signature MAP_GET =
      TypeUtils.parseSignature("Object get(Object)");
    private static final Signature MAP_PUT =
      TypeUtils.parseSignature("Object put(Object, Object)");
    private static final Signature KEY_SET =
      TypeUtils.parseSignature("java.util.Set keySet()");
    private static final Signature NEW_INSTANCE =
      TypeUtils.parseSignature("net.sf.cglib.beans.BeanMap newInstance(Object)");
    private static final Signature GET_PROPERTY_TYPE =
      TypeUtils.parseSignature("Class getPropertyType(String)");
    private static final Type BEAN_MAP =
      TypeUtils.parseType("net.sf.cglib.beans.BeanMap");
    private static final Type FIXED_KEY_SET =
      TypeUtils.parseType("net.sf.cglib.beans.FixedKeySet");

    public BeanMapEmitter(ClassVisitor v, String className, Class type, int switchStyle) {
        super(v);

        begin_class(Constants.ACC_PUBLIC, className, BEAN_MAP, null, Constants.SOURCE_FILE);
        ComplexOps.null_constructor(this);
        ComplexOps.factory_method(this, NEW_INSTANCE);
        generateConstructor();
            
        Map getters = makePropertyMap(ReflectUtils.getBeanGetters(type));
        Map setters = makePropertyMap(ReflectUtils.getBeanSetters(type));
        generateGet(type, switchStyle, getters);
        generatePut(type, switchStyle, setters);

        Map allProps = new HashMap();
        allProps.putAll(getters);
        allProps.putAll(setters);
        String[] allNames = getNames(allProps);
        generateKeySet(allNames);
        generateGetPropertyType(switchStyle, allProps, allNames);
        end_class();
    }

    private Map makePropertyMap(PropertyDescriptor[] props) {
        Map names = new HashMap();
        for (int i = 0; i < props.length; i++) {
            names.put(((PropertyDescriptor)props[i]).getName(), props[i]);
        }
        return names;
    }

    private String[] getNames(Map propertyMap) {
        return (String[])propertyMap.keySet().toArray(new String[propertyMap.size()]);
    }

    private void generateConstructor() {
        CodeEmitter e = begin_method(Constants.ACC_PUBLIC, CSTRUCT_OBJECT, null);
        e.load_this();
        e.load_arg(0);
        e.super_invoke_constructor(CSTRUCT_OBJECT);
        e.return_value();
        e.end_method();
    }
        
    private void generateGet(Class type, int switchStyle, final Map getters) {
        final CodeEmitter e = begin_method(Constants.ACC_PUBLIC, MAP_GET, null);
        e.load_this();
        e.super_getfield("bean", Constants.TYPE_OBJECT);
        e.checkcast(Type.getType(type));
        e.load_arg(0);
        e.checkcast(Constants.TYPE_STRING);
        ComplexOps.string_switch(e, getNames(getters), switchStyle, new ObjectSwitchCallback() {
            public void processCase(Object key, Label end) {
                PropertyDescriptor pd = (PropertyDescriptor)getters.get(key);
                e.invoke(pd.getReadMethod());
                e.box(Type.getType(pd.getReadMethod().getReturnType()));
                e.return_value();
            }
            public void processDefault() {
                e.aconst_null();
                e.return_value();
            }
        });
        e.end_method();
    }

    private void generatePut(Class type, int switchStyle, final Map setters) {
        final CodeEmitter e = begin_method(Constants.ACC_PUBLIC, MAP_PUT, null);
        e.load_this();
        e.super_getfield("bean", Constants.TYPE_OBJECT);
        e.checkcast(Type.getType(type));
        e.load_arg(0);
        e.checkcast(Constants.TYPE_STRING);
        ComplexOps.string_switch(e, getNames(setters), switchStyle, new ObjectSwitchCallback() {
            public void processCase(Object key, Label end) {
                PropertyDescriptor pd = (PropertyDescriptor)setters.get(key);
                if (pd.getReadMethod() == null) {
                    e.aconst_null();
                } else {
                    e.dup();
                    e.invoke(pd.getReadMethod());
                    e.box(Type.getType(pd.getReadMethod().getReturnType()));
                }
                e.swap(); // move old value behind bean
                e.load_arg(1); // new value
                e.unbox(Type.getType(pd.getWriteMethod().getParameterTypes()[0]));
                e.invoke(pd.getWriteMethod());
                e.return_value();
            }
            public void processDefault() {
                // fall-through
            }
        });
        e.aconst_null();
        e.return_value();
        e.end_method();
    }
            
    private void generateKeySet(String[] allNames) {
        // static initializer
        declare_field(Constants.ACC_STATIC | Constants.ACC_PRIVATE, "keys", FIXED_KEY_SET, null);

        CodeEmitter e = begin_static();
        e.new_instance(FIXED_KEY_SET);
        e.dup();
        ComplexOps.push_array(e, allNames);
        e.invoke_constructor(FIXED_KEY_SET, CSTRUCT_STRING_ARRAY);
        e.putfield("keys");
        e.return_value();
        e.end_method();

        // keySet
        e = begin_method(Constants.ACC_PUBLIC, KEY_SET, null);
        e.load_this();
        e.getfield("keys");
        e.return_value();
        e.end_method();
    }

    private void generateGetPropertyType(int switchStyle, final Map allProps, String[] allNames) {
        final CodeEmitter e = begin_method(Constants.ACC_PUBLIC, GET_PROPERTY_TYPE, null);
        e.load_arg(0);
        ComplexOps.string_switch(e, allNames, switchStyle, new ObjectSwitchCallback() {
            public void processCase(Object key, Label end) {
                PropertyDescriptor pd = (PropertyDescriptor)allProps.get(key);
                ComplexOps.load_class(e, Type.getType(pd.getPropertyType()));
                e.return_value();
            }
            public void processDefault() {
                e.aconst_null();
                e.return_value();
            }
        });
        e.end_method();
    }
}
