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
 * @version $Id: KeyFactoryEmitter.java,v 1.11 2003/10/03 19:25:07 herbyderby Exp $
 * @author Chris Nokleberg
 */
class KeyFactoryEmitter extends ClassEmitter {
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
    private int seed;

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
        ComplexOps.null_constructor(this);
        ComplexOps.factory_method(this, ReflectUtils.getSignature(newInstance));
        generateConstructor();
        generateHashCode();
        generateEquals();
        generateToString();
        end_class();
    }

    // TODO: change to exactly follow Effective Java recommendations
    private void generateConstructor() {
        CodeEmitter e = begin_method(Constants.ACC_PUBLIC,
                                     TypeUtils.parseConstructor(parameterTypes),
                                     null);
        e.load_this();
        e.super_invoke_constructor();
        e.load_this();
        for (int i = 0; i < parameterTypes.length; i++) {
            seed += parameterTypes[i].hashCode();
            declare_field(Constants.ACC_PRIVATE | Constants.ACC_FINAL,
                          getFieldName(i),
                          parameterTypes[i],
                          null);
            e.dup();
            e.load_arg(i);
            e.putfield(getFieldName(i));
        }
        e.return_value();
        e.end_method();
    }

    private void generateHashCode() {
        CodeEmitter e = begin_method(Constants.ACC_PUBLIC, HASH_CODE, null);
        e.push(PRIMES[(int)(seed % PRIMES.length)]);
        e.push(PRIMES[(int)((seed * 13) % PRIMES.length)]);
        for (int i = 0; i < parameterTypes.length; i++) {
            e.load_this();
            e.getfield(getFieldName(i));
            hash_code(e, parameterTypes[i]);
        }
        e.swap();
        e.pop();
        e.return_value();
        e.end_method();
    }

    private void hash_code(CodeEmitter e, Type type) {
        if (TypeUtils.isArray(type)) {
            hash_array(e, type);
        } else {
            if (TypeUtils.isPrimitive(type)) {
                hash_primitive(e, type);
            } else {
                hash_object(e, type);
            }
            e.math(e.ADD, Type.INT_TYPE);
            e.swap();
            e.dup_x1();
            e.math(e.MUL, Type.INT_TYPE);
        }
    }

    private void hash_array(final CodeEmitter e, Type type) {
        Label isNull = e.make_label();
        Label end = e.make_label();
        e.dup();
        e.ifnull(isNull);
        ComplexOps.process_array(e, type, new ProcessArrayCallback() {
            public void processElement(Type type) {
                hash_code(e, type);
            }
        });
        e.goTo(end);
        e.mark(isNull);
        e.pop();
        e.mark(end);
    }

    private void hash_object(CodeEmitter e, Type type) {
        // (f == null) ? 0 : f.hashCode();
        Label isNull = e.make_label();
        Label end = e.make_label();
        e.dup();
        e.ifnull(isNull);
        if (customizer != null) {
            customizer.customize(e, type);
        }
        e.invoke_virtual(Constants.TYPE_OBJECT, HASH_CODE);
        e.goTo(end);
        e.mark(isNull);
        e.pop();
        e.push(0);
        e.mark(end);
    }

    private void hash_primitive(CodeEmitter e, Type type) {
        switch (type.getSort()) {
        case Type.BOOLEAN:
            // f ? 0 : 1
            e.push(1);
            e.math(e.XOR, Type.INT_TYPE);
            break;
        case Type.DOUBLE:
            // Double.doubleToLongBits(f), hash_code(Long.TYPE)
            e.invoke_static(Constants.TYPE_DOUBLE, DOUBLE_TO_LONG_BITS);
            hash_long(e);
        case Type.FLOAT:
            // Float.floatToIntBits(f)
            e.invoke_static(Constants.TYPE_FLOAT, FLOAT_TO_INT_BITS);
        case Type.LONG:
            hash_long(e);
        default:
            // (int)f
        }
    }

    private void hash_long(CodeEmitter e) {
        // (int)(f ^ (f >>> 32))
        e.push(32);
        e.math(e.USHR, Type.LONG_TYPE);
        e.math(e.XOR, Type.LONG_TYPE);
        e.cast_numeric(Type.LONG_TYPE, Type.INT_TYPE);
    }

    private String getFieldName(int arg) {
        return "FIELD_" + arg;
    }

    private void generateEquals() {
        CodeEmitter e = begin_method(Constants.ACC_PUBLIC, EQUALS, null);
        Label fail = e.make_label();
        e.load_arg(0);
        e.instance_of_this();
        e.if_jump(e.EQ, fail);
        for (int i = 0; i < parameterTypes.length; i++) {
            e.load_this();
            e.getfield(getFieldName(i));
            e.load_arg(0);
            e.checkcast_this();
            e.getfield(getFieldName(i));
            not_equals(e, parameterTypes[i], fail);
        }
        e.push(1);
        e.return_value();
        e.mark(fail);
        e.push(0);
        e.return_value();
        e.end_method();
    }

    /**
     * Branches to the specified label if the top two items on the stack
     * are not equal. The items must both be of the specified
     * class. Equality is determined by comparing primitive values
     * directly and by invoking the <code>equals</code> method for
     * Objects. Arrays are recursively processed in the same manner.
     */
    private void not_equals(final CodeEmitter e, Type type, final Label notEquals) {
        (new ProcessArrayCallback() {
            public void processElement(Type type) {
                not_equals_helper(e, type, notEquals, this);
            }
        }).processElement(type);
    }
    
    private void not_equals_helper(CodeEmitter e, Type type, Label notEquals, ProcessArrayCallback callback) {
        if (TypeUtils.isPrimitive(type)) {
            e.if_cmp(type, e.NE, notEquals);
        } else {
            Label end = e.make_label();
            nullcmp(e, notEquals, end);
            if (TypeUtils.isArray(type)) {
                Label checkContents = e.make_label();
                e.dup2();
                e.arraylength();
                e.swap();
                e.arraylength();
                e.if_icmp(e.EQ, checkContents);
                e.pop2();
                e.goTo(notEquals);
                e.mark(checkContents);
                ComplexOps.process_arrays(e, type, callback);
            } else {
                if (customizer != null) {
                    customizer.customize(e, type);
                    e.swap();
                    customizer.customize(e, type);
                }
                e.invoke_virtual(Constants.TYPE_OBJECT, EQUALS);
                e.if_jump(e.EQ, notEquals);
            }
            e.mark(end);
        }
    }

    /**
     * If both objects on the top of the stack are non-null, does nothing.
     * If one is null, or both are null, both are popped off and execution
     * branches to the respective label.
     * @param oneNull label to branch to if only one of the objects is null
     * @param bothNull label to branch to if both of the objects are null
     */
    private void nullcmp(CodeEmitter e, Label oneNull, Label bothNull) {
        e.dup2();
        Label nonNull = e.make_label();
        Label oneNullHelper = e.make_label();
        Label end = e.make_label();
        e.ifnonnull(nonNull);
        e.ifnonnull(oneNullHelper);
        e.pop2();
        e.goTo(bothNull);
        
        e.mark(nonNull);
        e.ifnull(oneNullHelper);
        e.goTo(end);
        
        e.mark(oneNullHelper);
        e.pop2();
        e.goTo(oneNull);
        
        e.mark(end);
    }

    private void generateToString() {
        final CodeEmitter e = begin_method(Constants.ACC_PUBLIC, TO_STRING, null);
        Label fail = e.make_label();
        e.new_instance(Constants.TYPE_STRING_BUFFER);
        e.dup();
        e.invoke_constructor(Constants.TYPE_STRING_BUFFER);
        for (int i = 0; i < parameterTypes.length; i++) {
            e.load_this();
            e.getfield(getFieldName(i));
            (new ProcessArrayCallback() {
                public void processElement(Type type) {
                    toStringHelper(e, type, this);
                }
            }).processElement(parameterTypes[i]);
        }
        if (parameterTypes.length > 0) {
            shrinkStringBuffer(e, 2);
        }
        e.invoke_virtual(Constants.TYPE_STRING_BUFFER, TO_STRING);
        e.return_value();
        e.end_method();
    }

    private void toStringHelper(CodeEmitter e, Type type, ProcessArrayCallback callback) {
        Label skip = e.make_label();
        Label end = e.make_label();
        if (TypeUtils.isPrimitive(type)) {
            switch (type.getSort()) {
            case Type.INT:
            case Type.SHORT:
            case Type.BYTE:
                e.invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_INT);
                break;
            case Type.DOUBLE:
                e.invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_DOUBLE);
                break;
            case Type.FLOAT:
                e.invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_DOUBLE);
                break;
            case Type.LONG:
                e.invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_LONG);
                break;
            case Type.BOOLEAN:
                e.invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_BOOLEAN);
                break;
            case Type.CHAR:
                e.invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_CHAR);
                break;
            }
        } else if (TypeUtils.isArray(type)) {
            e.dup();
            e.ifnull(skip);
            e.swap();
            e.push("{");
            e.invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_STRING);
            e.swap();
            ComplexOps.process_array(e, type, callback);
            shrinkStringBuffer(e, 2);
            e.push("}");
            e.invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_STRING);
        } else {
            e.dup();
            e.ifnull(skip);
            if (customizer != null) {
                customizer.customize(e, type);
            }
            e.invoke_virtual(Constants.TYPE_OBJECT, TO_STRING);
            e.invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_STRING);
        }
        e.goTo(end);
        e.mark(skip);
        e.pop();
        e.push("null");
        e.invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_STRING);
        e.mark(end);
        e.push(", ");
        e.invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_STRING);
    }

    private void shrinkStringBuffer(CodeEmitter e, int amt) {
        e.dup();
        e.dup();
        e.invoke_virtual(Constants.TYPE_STRING_BUFFER, LENGTH);
        e.push(amt);
        e.math(e.SUB, Type.INT_TYPE);
        e.invoke_virtual(Constants.TYPE_STRING_BUFFER, SET_LENGTH);
    }
}
