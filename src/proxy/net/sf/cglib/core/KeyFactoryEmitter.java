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
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

/**
 * @version $Id: KeyFactoryEmitter.java,v 1.5 2003/09/20 09:22:22 herbyderby Exp $
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
        super(v);
        Method newInstance = ReflectUtils.findNewInstance(keyInterface);
        if (!newInstance.getReturnType().equals(Object.class)) {
            throw new IllegalArgumentException("newInstance method must return Object");
        }
        
        Class[] parameterTypes = newInstance.getParameterTypes();
        
        Ops.begin_class(this,
                        Modifier.PUBLIC,
                        className,
                        KeyFactory.class,
                        new Class[]{ keyInterface },
                        Constants.SOURCE_FILE);
        null_constructor();
        factory_method(new Signature(newInstance));
        generateConstructor(parameterTypes);
        generateEquals(parameterTypes);
        end_class();
    }

    // TODO: this doesn't exactly follow Effective Java recommendations
    // TODO: caching hashCode is a bad idea for mutable objects, at least document behavior
    private void generateConstructor(Class[] parameterTypes) throws NoSuchFieldException {
        begin_constructor(Constants.ACC_PUBLIC, Signature.getTypes(parameterTypes), null);
        load_this();
        super_invoke_constructor();
        load_this();
        for (int i = 0; i < parameterTypes.length; i++) {
            declare_field(Constants.ACC_PRIVATE | Constants.ACC_FINAL, getFieldName(i), Type.getType(parameterTypes[i]), null);
            dup();
            load_arg(i);
            putfield(getFieldName(i));
        }
        loadAndStoreConstant("hashMultiplier");
        loadAndStoreConstant("hashConstant");
        for (int i = 0; i < parameterTypes.length; i++) {
            load_arg(i);
            hash_code(Type.getType(parameterTypes[i]));
        }
        swap();
        pop();
        super_putfield("hash", Type.INT_TYPE);
        return_value();
    }

    private void loadAndStoreConstant(String fieldName) throws NoSuchFieldException {
        push(pickHashConstant());
        load_this();
        swap();
        dup_x1();
        super_putfield(fieldName, Type.INT_TYPE);
    }

    private void hash_code(Type type) {
        if (isArray(type)) {
            hash_array(type);
        } else {
            if (isPrimitive(type)) {
                hash_primitive(type);
            } else {
                hash_object();
            }
            math(OP_ADD, Type.INT_TYPE);
            swap();
            dup_x1();
            math(OP_MUL, Type.INT_TYPE);
        }
    }

    private void hash_array(Type type) {
        Label isNull = make_label();
        Label end = make_label();
        dup();
        ifnull(isNull);
        Ops.process_array(this, type, new ProcessArrayCallback() {
            public void processElement(Type type) {
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
        invoke_virtual(Types.OBJECT, Signatures.HASH_CODE);
        goTo(end);
        mark(isNull);
        pop();
        push(0);
        mark(end);
    }

    private void hash_primitive(Type type) {
        switch (type.getSort()) {
        case Type.BOOLEAN:
            // f ? 0 : 1
            push(1);
            math(OP_XOR, Type.INT_TYPE);
            break;
        case Type.DOUBLE:
            // Double.doubleToLongBits(f), hash_code(Long.TYPE)
            invoke_static(Types.DOUBLE, Signatures.DOUBLE_TO_LONG_BITS);
            hash_long();
        case Type.FLOAT:
            // Float.floatToIntBits(f)
            invoke_static(Types.FLOAT, Signatures.FLOAT_TO_INT_BITS);
        case Type.LONG:
            hash_long();
        default:
            // (int)f
        }
    }

    private void hash_long() {
        // (int)(f ^ (f >>> 32))
        push(32);
        math(OP_USHR, Type.LONG_TYPE);
        math(OP_XOR, Type.LONG_TYPE);
        cast_numeric(Type.LONG_TYPE, Type.INT_TYPE);
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
        begin_method(Constants.ACC_PUBLIC, Signatures.EQUALS, null);
        load_arg(0);
        instance_of_this();
        ifeq(fail);
        for (int i = 0; i < parameterTypes.length; i++) {
            load_this();
            getfield(getFieldName(i));
            load_arg(0);
            checkcast_this();
            getfield(getFieldName(i));
            Ops.not_equals(this, Type.getType(parameterTypes[i]), fail);
        }
        push(1);
        return_value();
        mark(fail);
        push(0);
        return_value();
    }
}
