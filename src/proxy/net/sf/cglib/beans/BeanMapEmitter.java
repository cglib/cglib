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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import net.sf.cglib.core.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

class BeanMapEmitter extends Emitter {
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
    private static final Type BEAN_MAP =
      TypeUtils.parseType("net.sf.cglib.beans.BeanMap");
    private static final Type FIXED_KEY_SET =
      TypeUtils.parseType("net.sf.cglib.beans.FixedKeySet");

    public BeanMapEmitter(ClassVisitor v, String className, Class type, int switchStyle) throws Exception {
        super(v);

        begin_class(Constants.ACC_PUBLIC, className, BEAN_MAP, null, Constants.SOURCE_FILE);
        null_constructor();
        factory_method(NEW_INSTANCE);
        generateConstructor();
            
        Map getters = makePropertyMap(ReflectUtils.getBeanGetters(type));
        Map setters = makePropertyMap(ReflectUtils.getBeanSetters(type));
        generateGet(type, switchStyle, getters);
        generatePut(type, switchStyle, setters);
        generateKeySet(getters, setters);
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
        begin_method(Constants.ACC_PUBLIC, CSTRUCT_OBJECT, null);
        load_this();
        load_arg(0);
        super_invoke_constructor(CSTRUCT_OBJECT);
        return_value();
    }
        
    private void generateGet(Class type, int switchStyle, final Map getters) throws Exception {
        begin_method(Constants.ACC_PUBLIC, MAP_GET, null);
        load_this();
        super_getfield("bean", Constants.TYPE_OBJECT);
        checkcast(Type.getType(type));
        load_arg(0);
        checkcast(Constants.TYPE_STRING);
        ComplexOps.string_switch(this, getNames(getters), switchStyle, new ObjectSwitchCallback() {
            public void processCase(Object key, Label end) {
                PropertyDescriptor pd = (PropertyDescriptor)getters.get(key);
                ReflectOps.invoke(BeanMapEmitter.this, pd.getReadMethod());
                box(Type.getType(pd.getReadMethod().getReturnType()));
                return_value();
            }
            public void processDefault() {
                aconst_null();
                return_value();
            }
        });
    }

    private void generatePut(Class type, int switchStyle, final Map setters) throws Exception {
        begin_method(Constants.ACC_PUBLIC, MAP_PUT, null);
        load_this();
        super_getfield("bean", Constants.TYPE_OBJECT);
        checkcast(Type.getType(type));
        load_arg(0);
        checkcast(Constants.TYPE_STRING);
        ComplexOps.string_switch(this, getNames(setters), switchStyle, new ObjectSwitchCallback() {
            public void processCase(Object key, Label end) {
                PropertyDescriptor pd = (PropertyDescriptor)setters.get(key);
                if (pd.getReadMethod() == null) {
                    aconst_null();
                } else {
                    dup();
                    ReflectOps.invoke(BeanMapEmitter.this, pd.getReadMethod());
                    box(Type.getType(pd.getReadMethod().getReturnType()));
                }
                swap(); // move old value behind bean
                load_arg(1); // new value
                unbox(Type.getType(pd.getWriteMethod().getParameterTypes()[0]));
                ReflectOps.invoke(BeanMapEmitter.this, pd.getWriteMethod());
                return_value();
            }
            public void processDefault() {
                // fall-through
            }
        });
        aconst_null();
        return_value();
    }
            
    private void generateKeySet(Map getters, Map setters) {
        // static initializer
        declare_field(Modifier.STATIC | Modifier.PRIVATE, "keys", FIXED_KEY_SET, null);
        Set allNames = new HashSet();
        allNames.addAll(getters.keySet());
        allNames.addAll(setters.keySet());
        begin_static();
        new_instance(FIXED_KEY_SET);
        dup();
        ReflectOps.push(this, allNames.toArray(new String[allNames.size()]));
        invoke_constructor(FIXED_KEY_SET, CSTRUCT_STRING_ARRAY);
        putfield("keys");
        return_value();

        // keySet
        begin_method(Constants.ACC_PUBLIC, KEY_SET, null);
        load_this();
        getfield("keys");
        return_value();
    }
}
