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

package net.sf.cglib.core;

import java.lang.reflect.*;
import org.objectweb.asm.ClassVisitor;

/**
 * @version $Id: KeyFactoryEmitter.java,v 1.2 2003/09/14 17:39:40 herbyderby Exp $
 * @author Chris Nokleberg
 */
class KeyFactoryEmitter extends Emitter {
    // private static final Method GET_ARGS = ReflectUtils.findMethod("KeyFactory.getArgs()");
    
    //generated numbers: 
    private final static int PRIMES[] = {
               11,         73,        179,       331,
              521,        787,       1213,      1823,
             2609,       3691,       5189,      7247,
            10037,      13931,      19289,     26627,
            36683,      50441,      69403,     95401,
           131129,     180179,     247501,    340057,
           467063,     641371,     880603,   1209107,
          1660097,    2279161,    3129011,   4295723,
          5897291,    8095873,   11114263,  15257791,
         20946017,   28754629,   39474179,  54189869,
         74391461,  102123817,  140194277, 192456917,
        264202273,  362693231,  497900099, 683510293,
        938313161, 1288102441, 1768288259  };

    public KeyFactoryEmitter(ClassVisitor v, String className, Class keyInterface) throws Exception {
        Method newInstance = ReflectUtils.findNewInstance(keyInterface);
        if (!newInstance.getReturnType().equals(Object.class)) {
            throw new IllegalArgumentException("newInstance method must return Object");
        }
        
        Class[] parameterTypes = newInstance.getParameterTypes();
        
        setClassVisitor(v);
        begin_class(Modifier.PUBLIC, className, KeyFactory.class, new Class[]{ keyInterface });
        Virt.null_constructor(this);
        Virt.factory_method(this, newInstance);
        generateConstructor(parameterTypes);
        generateEquals(parameterTypes);
        // generateGetArgs(parameterTypes);
        end_class();
    }

    // TODO: this doesn't exactly follow Effective Java recommendations
    // TODO: caching hashCode is a bad idea for mutable objects, at least document behavior
    private void generateConstructor(Class[] parameterTypes) throws NoSuchFieldException {
        begin_constructor(parameterTypes);
        load_this();
        super_invoke_constructor();
        load_this();
        for (int i = 0; i < parameterTypes.length; i++) {
            declare_field(Modifier.PRIVATE | Modifier.FINAL, parameterTypes[i], getFieldName(i));
            dup();
            load_arg(i);
            putfield(getFieldName(i));
        }
        loadAndStoreConstant("hashMultiplier");
        loadAndStoreConstant("hashConstant");
        for (int i = 0; i < parameterTypes.length; i++) {
            load_arg(i);
            hash_code(parameterTypes[i]);
        }
        swap();
        pop();
        super_putfield("hash");
        return_value();
        end_method();
    }

    private void loadAndStoreConstant(String fieldName) throws NoSuchFieldException {
        push(pickHashConstant());
        load_this();
        swap();
        dup_x1();
        super_putfield(fieldName);
    }

    private void hash_code(Class clazz) {
        if (clazz.isArray()) {
            hash_array(clazz);
        } else {
            if (clazz.isPrimitive()) {
                hash_primitive(clazz);
            } else {
                hash_object();
            }
            add(Integer.TYPE);
            swap();
            dup_x1();
            mul(Integer.TYPE);
        }
    }

    private void hash_array(Class clazz) {
        Label isNull = make_label();
        Label end = make_label();
        dup();
        ifnull(isNull);
        Virt.process_array(this, clazz, new Virt.ProcessArrayCallback() {
            public void processElement(Class type) {
                hash_code(type);
            }
        });
        goTo(end);
        mark(isNull);
        pop();
        mark(end);
    }

    private void hash_object() {
        // (f == null) ? 0 : f.hashCode();
        Label isNull = make_label();
        Label end = make_label();
        dup();
        ifnull(isNull);
        invoke(MethodConstants.HASH_CODE);
        goTo(end);
        mark(isNull);
        pop();
        push(0);
        mark(end);
    }

    private void hash_primitive(Class clazz) {
        if (clazz.equals(Boolean.TYPE)) {
            // f ? 0 : 1
            push(1);
            xor(Integer.TYPE);
        } else if (clazz.equals(Double.TYPE)) {
            // Double.doubleToLongBits(f), hash_code(Long.TYPE)
            invoke(MethodConstants.DOUBLE_TO_LONG_BITS);
            hash_long();
        } else if (clazz.equals(Float.TYPE)) {
            // Float.floatToIntBits(f)
            invoke(MethodConstants.FLOAT_TO_INT_BITS);
        } else if (clazz.equals(Long.TYPE)) {
            hash_long();
        } else { // byte, char, short, int
            // (int)f
        }
    }

    private void hash_long() {
        // (int)(f ^ (f >>> 32))
        push(32);
        ushr(Long.TYPE);
        xor(Long.TYPE);
        cast_numeric(Long.TYPE, Integer.TYPE);
    }

    // generates pseudo random prime number
    private int pickHashConstant() {
      return  PRIMES[ (int)(PRIMES.length*Math.random()) ];
    }

    private String getFieldName(int arg) {
        return "FIELD_" + arg;
    }

    private void generateEquals(Class[] parameterTypes) {
        Label fail = make_label();
        begin_method(MethodConstants.EQUALS);
        load_arg(0);
        instance_of_this();
        ifeq(fail);
        for (int i = 0; i < parameterTypes.length; i++) {
            load_this();
            getfield(getFieldName(i));
            load_arg(0);
            checkcast_this();
            getfield(getFieldName(i));
            Virt.not_equals(this, parameterTypes[i], fail);
        }
        push(1);
        return_value();
        mark(fail);
        push(0);
        return_value();
        end_method();
    }

//     private void generateGetArgs(Class[] parameterTypes) {
//         begin_method(GET_ARGS);
//         push(parameterTypes.length);
//         newarray();
//         for (int i = 0; i < parameterTypes.length; i++) {
//             dup();
//             push(i);
//             load_this();
//             getfield(getFieldName(i));
//             box(parameterTypes[i]);
//             aastore();
//         }
//         return_value();
//         end_method();
//     }
}
