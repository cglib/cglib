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
 * @version $Id: KeyFactoryEmitter.java,v 1.9 2003/09/30 18:21:50 herbyderby Exp $
 * @author Chris Nokleberg
 */
class KeyFactoryEmitter extends Emitter {
    private static final Signature HASH_CODE =
      TypeUtils.parseSignature("int hashCode()");
    private static final Signature EQUALS =
      TypeUtils.parseSignature("boolean equals(Object)");
    private static final Signature TO_STRING =
      TypeUtils.parseSignature("String toString()");
    private static final Signature DOUBLE_TO_LONG_BITS =
      TypeUtils.parseSignature("long doubleToLongBits(double)");
    private static final Signature FLOAT_TO_INT_BITS =
      TypeUtils.parseSignature("int floatToIntBits(float)");
    private static final Type KEY_FACTORY =
      TypeUtils.parseType("net.sf.cglib.core.KeyFactory");
    private static final Signature APPEND_STRING =
      TypeUtils.parseSignature("StringBuffer append(String)");
    private static final Signature APPEND_INT =
      TypeUtils.parseSignature("StringBuffer append(int)");
    private static final Signature APPEND_DOUBLE =
      TypeUtils.parseSignature("StringBuffer append(double)");
    private static final Signature APPEND_FLOAT =
      TypeUtils.parseSignature("StringBuffer append(float)");
    private static final Signature APPEND_CHAR =
      TypeUtils.parseSignature("StringBuffer append(char)");
    private static final Signature APPEND_LONG =
      TypeUtils.parseSignature("StringBuffer append(long)");
    private static final Signature APPEND_BOOLEAN =
      TypeUtils.parseSignature("StringBuffer append(boolean)");
    private static final Signature LENGTH =
      TypeUtils.parseSignature("int length()");
    private static final Signature SET_LENGTH =
      TypeUtils.parseSignature("void setLength(int)");
    
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

    private String className;
    private Class keyInterface;
    private Type[] parameterTypes;
    private Customizer customizer;
    private Method newInstance;

    public KeyFactoryEmitter(ClassVisitor v,
                             String className,
                             Class keyInterface,
                             Customizer customizer) throws Exception {
        super(v);
        this.className = className;
        this.keyInterface = keyInterface;
        this.customizer = customizer;

        newInstance = ReflectUtils.findNewInstance(keyInterface);
        if (!newInstance.getReturnType().equals(Object.class)) {
            throw new IllegalArgumentException("newInstance method must return Object");
        }

        parameterTypes = TypeUtils.getTypes(newInstance.getParameterTypes());
    }
    
    public void emit() {
        begin_class(Constants.ACC_PUBLIC,
                    className,
                    KEY_FACTORY,
                    new Type[]{ Type.getType(keyInterface) },
                    Constants.SOURCE_FILE);
        null_constructor();
        factory_method(ReflectUtils.getSignature(newInstance));
        generateConstructor();
        generateEquals();
        generateToString();
        end_class();
    }

    // TODO: this doesn't exactly follow Effective Java recommendations
    // TODO: caching hashCode is a bad idea for mutable objects, at least document behavior
    private void generateConstructor() {
        begin_method(Constants.ACC_PUBLIC, TypeUtils.parseConstructor(parameterTypes), null);
        load_this();
        super_invoke_constructor();
        load_this();
        int seed = 0;
        for (int i = 0; i < parameterTypes.length; i++) {
            seed += parameterTypes[i].hashCode();
            declare_field(Constants.ACC_PRIVATE | Constants.ACC_FINAL,
                          getFieldName(i),
                          parameterTypes[i],
                          null);
            dup();
            load_arg(i);
            putfield(getFieldName(i));
        }
        push(PRIMES[(int)(seed % PRIMES.length)]);
        push(PRIMES[(int)((seed * 13) % PRIMES.length)]);
        for (int i = 0; i < parameterTypes.length; i++) {
            load_arg(i);
            hash_code(parameterTypes[i]);
        }
        swap();
        pop();
        super_putfield("hash", Type.INT_TYPE);
        return_value();
    }

    private void hash_code(Type type) {
        if (TypeUtils.isArray(type)) {
            hash_array(type);
        } else {
            if (TypeUtils.isPrimitive(type)) {
                hash_primitive(type);
            } else {
                hash_object(type);
            }
            math(ADD, Type.INT_TYPE);
            swap();
            dup_x1();
            math(MUL, Type.INT_TYPE);
        }
    }

    private void hash_array(Type type) {
        Label isNull = make_label();
        Label end = make_label();
        dup();
        ifnull(isNull);
        ComplexOps.process_array(this, type, new ProcessArrayCallback() {
            public void processElement(Type type) {
                hash_code(type);
            }
        });
        goTo(end);
        mark(isNull);
        pop();
        mark(end);
    }

    private void hash_object(Type type) {
        // (f == null) ? 0 : f.hashCode();
        Label isNull = make_label();
        Label end = make_label();
        dup();
        ifnull(isNull);
        if (customizer != null) {
            customizer.customize(this, type);
        }
        invoke_virtual(Constants.TYPE_OBJECT, HASH_CODE);
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
            math(XOR, Type.INT_TYPE);
            break;
        case Type.DOUBLE:
            // Double.doubleToLongBits(f), hash_code(Long.TYPE)
            invoke_static(Constants.TYPE_DOUBLE, DOUBLE_TO_LONG_BITS);
            hash_long();
        case Type.FLOAT:
            // Float.floatToIntBits(f)
            invoke_static(Constants.TYPE_FLOAT, FLOAT_TO_INT_BITS);
        case Type.LONG:
            hash_long();
        default:
            // (int)f
        }
    }

    private void hash_long() {
        // (int)(f ^ (f >>> 32))
        push(32);
        math(USHR, Type.LONG_TYPE);
        math(XOR, Type.LONG_TYPE);
        cast_numeric(Type.LONG_TYPE, Type.INT_TYPE);
    }

    private String getFieldName(int arg) {
        return "FIELD_" + arg;
    }

    private void generateEquals() {
        Label fail = make_label();
        begin_method(Constants.ACC_PUBLIC, EQUALS, null);
        load_arg(0);
        instance_of_this();
        if_jump(EQ, fail);
        for (int i = 0; i < parameterTypes.length; i++) {
            load_this();
            getfield(getFieldName(i));
            load_arg(0);
            checkcast_this();
            getfield(getFieldName(i));
            not_equals(parameterTypes[i], fail);
        }
        push(1);
        return_value();
        mark(fail);
        push(0);
        return_value();
    }

    /**
     * Branches to the specified label if the top two items on the stack
     * are not equal. The items must both be of the specified
     * class. Equality is determined by comparing primitive values
     * directly and by invoking the <code>equals</code> method for
     * Objects. Arrays are recursively processed in the same manner.
     */
    private void not_equals(Type type, final Label notEquals) {
        (new ProcessArrayCallback() {
            public void processElement(Type type) {
                not_equals_helper(type, notEquals, this);
            }
        }).processElement(type);
    }
    
    private void not_equals_helper(Type type, Label notEquals, ProcessArrayCallback callback) {
        if (TypeUtils.isPrimitive(type)) {
            if_cmp(type, NE, notEquals);
        } else {
            Label end = make_label();
            nullcmp(notEquals, end);
            if (TypeUtils.isArray(type)) {
                Label checkContents = make_label();
                dup2();
                arraylength();
                swap();
                arraylength();
                if_icmp(EQ, checkContents);
                pop2();
                goTo(notEquals);
                mark(checkContents);
                ComplexOps.process_arrays(this, type, callback);
            } else {
                if (customizer != null) {
                    customizer.customize(this, type);
                    swap();
                    customizer.customize(this, type);
                }
                invoke_virtual(Constants.TYPE_OBJECT, EQUALS);
                if_jump(EQ, notEquals);
            }
            mark(end);
        }
    }

    /**
     * If both objects on the top of the stack are non-null, does nothing.
     * If one is null, or both are null, both are popped off and execution
     * branches to the respective label.
     * @param oneNull label to branch to if only one of the objects is null
     * @param bothNull label to branch to if both of the objects are null
     */
    private void nullcmp(Label oneNull, Label bothNull) {
        dup2();
        Label nonNull = make_label();
        Label oneNullHelper = make_label();
        Label end = make_label();
        ifnonnull(nonNull);
        ifnonnull(oneNullHelper);
        pop2();
        goTo(bothNull);
        
        mark(nonNull);
        ifnull(oneNullHelper);
        goTo(end);
        
        mark(oneNullHelper);
        pop2();
        goTo(oneNull);
        
        mark(end);
    }

    private void generateToString() {
        Label fail = make_label();
        begin_method(Constants.ACC_PUBLIC, TO_STRING, null);
        new_instance(Constants.TYPE_STRING_BUFFER);
        dup();
        invoke_constructor(Constants.TYPE_STRING_BUFFER);
        for (int i = 0; i < parameterTypes.length; i++) {
            load_this();
            getfield(getFieldName(i));
            (new ProcessArrayCallback() {
                public void processElement(Type type) {
                    toStringHelper(type, this);
                }
            }).processElement(parameterTypes[i]);
        }
        if (parameterTypes.length > 0) {
            shrinkStringBuffer(2);
        }
        invoke_virtual(Constants.TYPE_STRING_BUFFER, TO_STRING);
        return_value();
    }

    private void toStringHelper(Type type, ProcessArrayCallback callback) {
        Label skip = make_label();
        Label end = make_label();
        if (TypeUtils.isPrimitive(type)) {
            switch (type.getSort()) {
            case Type.INT:
            case Type.SHORT:
            case Type.BYTE:
                invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_INT);
                break;
            case Type.DOUBLE:
                invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_DOUBLE);
                break;
            case Type.FLOAT:
                invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_DOUBLE);
                break;
            case Type.LONG:
                invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_LONG);
                break;
            case Type.BOOLEAN:
                invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_BOOLEAN);
                break;
            case Type.CHAR:
                invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_CHAR);
                break;
            }
        } else if (TypeUtils.isArray(type)) {
            dup();
            ifnull(skip);
            swap();
            push("{");
            invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_STRING);
            swap();
            ComplexOps.process_array(this, type, callback);
            shrinkStringBuffer(2);
            push("}");
            invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_STRING);
        } else {
            dup();
            ifnull(skip);
            if (customizer != null) {
                customizer.customize(this, type);
            }
            invoke_virtual(Constants.TYPE_OBJECT, TO_STRING);
            invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_STRING);
        }
        goTo(end);
        mark(skip);
        pop();
        push("null");
        invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_STRING);
        mark(end);
        push(", ");
        invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_STRING);
    }

    private void shrinkStringBuffer(int amt) {
        dup();
        dup();
        invoke_virtual(Constants.TYPE_STRING_BUFFER, LENGTH);
        push(amt);
        math(SUB, Type.INT_TYPE);
        invoke_virtual(Constants.TYPE_STRING_BUFFER, SET_LENGTH);
    }
}
