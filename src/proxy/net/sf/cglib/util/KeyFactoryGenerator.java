/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * 4. The names "Apache Cocoon" and "Apache Software Foundation" must
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

package net.sf.cglib.util;

import java.lang.reflect.*;

class KeyFactoryGenerator extends CodeGenerator {
    private static final Method hashCode;
    private static final Method floatToIntBits;
    private static final Method doubleToLongBits;

    static {
        try {
            hashCode = Object.class.getDeclaredMethod("hashCode", new Class[]{});
            floatToIntBits = Float.class.getDeclaredMethod("floatToIntBits", new Class[]{ float.class });
            doubleToLongBits = Double.class.getDeclaredMethod("doubleToLongBits", new Class[]{ double.class });
        } catch (Exception e) {
            throw new ImpossibleError(e);
        }
    }

    private Class keyInterface;
    private Method newInstance;
    private Class[] parameterTypes;
    private int numArgs;

    private final ProcessArrayCallback hashCallback = new ProcessArrayCallback() {
            public void processElement(Class type) {
                hash_code(type);
            }
        };

    public KeyFactoryGenerator(String className, Class keyInterface, ClassLoader loader) {
        super(className, KeyFactory.class, loader);
        this.keyInterface = keyInterface;
    }

    protected Class define() throws CodeGenerationException {
        return super.define();
    }

    protected void generate() throws NoSuchMethodException, NoSuchFieldException {
        if (!keyInterface.isInterface()) {
            throw new IllegalArgumentException(keyInterface + " is not an interface");
        }
        Method[] methods = keyInterface.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals("newInstance")) {
                if (newInstance != null) {
                    throw new IllegalArgumentException("Multiple newInstance methods");
                }
                newInstance = methods[i];
            }
        }
        if (newInstance == null) {
            throw new IllegalArgumentException("Missing newInstance method");
        }
        if (!newInstance.getReturnType().equals(Object.class)) {
            throw new IllegalArgumentException("newInstance method must return Object");
        }
        parameterTypes = newInstance.getParameterTypes();
        numArgs = parameterTypes.length;
        declare_interface(keyInterface);
        generateNullConstructor();
        generateConstructor();
        generateFactory();
        generateEquals();
    }

    private void generateNullConstructor() {
        begin_constructor();
        load_this();
        super_invoke_constructor();
        return_value();
        end_constructor();
    }

    private void generateConstructor() throws NoSuchFieldException {
        begin_constructor(parameterTypes);
        load_this();
        super_invoke_constructor();
        load_this();
        for (int i = 0; i < numArgs; i++) {
            String fieldName = "FIELD_" + i;
            declare_field(Modifier.PRIVATE | Modifier.FINAL, parameterTypes[i], fieldName);
            dup();
            load_arg(i);
            putfield(fieldName);
        }
        loadAndStoreConstant("hashMultiplier");
        dup();
        loadAndStoreConstant("hashConstant");
        imul();
        for (int i = 0; i < numArgs; i++) {
            load_arg(i);
            hash_code(parameterTypes[i]);
        }
        swap();
        pop();
        super_putfield("hash");
        return_value();
        end_constructor();
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
            String isNull = newLabel();
            String end = newLabel();
            dup();
            ifnull(isNull);
            process_array(clazz, hashCallback);
            goTo(end);
            nop(isNull);
            pop();
            nop(end);
        } else {
            if (clazz.isPrimitive()) {
                if (clazz.equals(Boolean.TYPE)) {
                    // f ? 0 : 1
                    push(1);
                    ixor();
                } else if (clazz.equals(Double.TYPE)) {
                    // Double.doubleToLongBits(f), hash_code(Long.TYPE)
                    invoke(doubleToLongBits);
                    hash_long();
                } else if (clazz.equals(Float.TYPE)) {
                    // Float.floatToIntBits(f)
                    invoke(floatToIntBits);
                } else if (clazz.equals(Long.TYPE)) {
                    hash_long();
                } else { // byte, char, short, int
                    // (int)f
                }
            } else {
                // (f == null) ? 0 : f.hashCode()
                String isNull = newLabel();
                String end = newLabel();
                dup();
                ifnull(isNull);
                invoke(hashCode);
                goTo(end);
                nop(isNull);
                pop();
                push(0);
                nop(end);
            }
            iadd();
            swap();
            dup_x1();
            imul();
        }
    }

    private void hash_long() {
        // (int)(f ^ (f >>> 32))
        push(32);
        lushr();
        lxor();
        l2i();
    }

    // TODO: use prime number?
    private int pickHashConstant() {
        return 2 * (int)(Math.random() * 100) + 1;
    }

    private void generateFactory() throws NoSuchMethodException {
        begin_method(newInstance);
        new_instance_this();
        dup();
        load_args();
        invoke_constructor_this(parameterTypes);
        return_value();
        end_method();
    }

    private void generateEquals() {
        begin_method(equalsMethod);
        for (int i = 0; i < numArgs; i++) {
            String fieldName = "FIELD_" + i;
            load_this();
            getfield(fieldName);
            load_arg(0);
            checkcast_this();
            getfield(fieldName);
            not_equals(parameterTypes[i], "failure");
        }
        push(1);
        return_value();
        nop("failure");
        push(0);
        return_value();
        end_method();
    }
}
