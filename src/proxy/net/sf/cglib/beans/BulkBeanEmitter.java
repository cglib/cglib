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
import org.objectweb.asm.Type;
    
class BulkBeanEmitter extends Emitter {
    private static final Signature GET_PROPERTY_VALUES =
      Signature.parse("void getPropertyValues(Object, Object[])");
    private static final Signature SET_PROPERTY_VALUES =
      Signature.parse("void setPropertyValues(Object, Object[])");
    private static final Type BULK_BEAN =
      Signature.parseType("net.sf.cglib.beans.BulkBean");
    private static final Type BULK_BEAN_EXCEPTION =
      Signature.parseType("net.sf.cglib.beans.BulkBeanException");
    private static final Type CLASS_CAST_EXCEPTION =
      Signature.parseType("ClassCastException");
    private static final Type[] EXCEPTION_TYPES =
      Signature.parseTypes("Throwable, int");
        
    public BulkBeanEmitter(ClassVisitor v,
                           String className,
                           Class target,
                           String[] getterNames,
                           String[] setterNames,
                           Class[] types) throws Exception {
        super(v);

        Method[] getters = new Method[getterNames.length];
        Method[] setters = new Method[setterNames.length];
        validate(target, getterNames, setterNames, types, getters, setters);

        begin_class(Constants.ACC_PUBLIC, className, BULK_BEAN, null, Constants.SOURCE_FILE);
        null_constructor();
        generateGet(target, getters);
        generateSet(target, setters);
        end_class();
    }

    private void generateGet(Class target, Method[] getters) {
        begin_method(Constants.ACC_PUBLIC, GET_PROPERTY_VALUES, null);
        load_arg(0);
        checkcast(Type.getType(target));
        Local bean = make_local();
        store_local(bean);
        for (int i = 0; i < getters.length; i++) {
            if (getters[i] != null) {
                load_arg(1);
                push(i);
                load_local(bean);
                ReflectOps.invoke(this, getters[i]);
                box(Type.getType(getters[i].getReturnType()));
                aastore();
            }
        }
        return_value();
    }

    private void generateSet(Class target, Method[] setters) {
        // setPropertyValues
        begin_method(Constants.ACC_PUBLIC, SET_PROPERTY_VALUES, null);
        Local index = make_local(Type.INT_TYPE);
        push(0);
        store_local(index);
        load_arg(0);
        checkcast(Type.getType(target));
        load_arg(1);
        Block handler = begin_block();
        int lastIndex = 0;
        for (int i = 0; i < setters.length; i++) {
            if (setters[i] != null) {
                int diff = i - lastIndex;
                if (diff > 0) {
                    iinc(index, diff);
                    lastIndex = i;
                }
                dup2();
                aaload(i);
                unbox(Type.getType(setters[i].getParameterTypes()[0]));
                ReflectOps.invoke(this, setters[i]);
            }
        }
        end_block();
        return_value();
        catch_exception(handler, CLASS_CAST_EXCEPTION);
        new_instance(BULK_BEAN_EXCEPTION);
        dup_x1();
        swap();
        load_local(index);
        invoke_constructor(BULK_BEAN_EXCEPTION, EXCEPTION_TYPES);
        athrow();
    }
    
    private static void validate(Class target,
                                 String[] getters,
                                 String[] setters,
                                 Class[] types,
                                 Method[] getters_out,
                                 Method[] setters_out) {
        int i = -1;
        if (setters.length != types.length || getters.length != types.length) {
            throw new BulkBeanException("accessor array length must be equal type array length", i);
        }
        try {
            for (i = 0; i < types.length; i++) {
                if (getters[i] != null) {
                    Method method = ReflectUtils.findDeclaredMethod(target, getters[i], null);
                    if (method.getReturnType() != types[i]) {
                        throw new BulkBeanException("Specified type " + types[i] +
                                                    " does not match declared type " + method.getReturnType(), i);
                    }
                    if (Modifier.isPrivate(method.getModifiers())) {
                        throw new BulkBeanException("Property is private", i);
                    }
                    getters_out[i] = method;
                }
                if (setters[i] != null) {
                    Method method = ReflectUtils.findDeclaredMethod(target, setters[i], new Class[]{ types[i] });
                    if (Modifier.isPrivate(method.getModifiers()) ){
                        throw new BulkBeanException("Property is private", i);
                    }
                    setters_out[i] = method;
                }
            }
        } catch (NoSuchMethodException e) {
            throw new BulkBeanException("Cannot find specified property", i);
        }
    }
}
