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

import java.lang.reflect.Method;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

/**
 * Generates classes to handle multi-valued keys, for use in things such as Maps and Sets.
 * Code for <code>equals</code> and <code>hashCode</code> methods follow the
 * the rules laid out in <i>Effective Java</i> by Joshua Bloch. 
 * <p>
 * To generate a <code>KeyFactory</code>, you need to supply an interface which
 * describes the structure of the key. The interface should have a
 * single method named <code>newInstance</code>, which returns an
 * <code>Object</code>. The arguments array can be
 * <i>anything</i>--Objects, primitive values, or single or
 * multi-dimension arrays of either. For example:
 * <p><pre>
 *     private interface IntStringKey {
 *         public Object newInstance(int i, String s);
 *     }
 * </pre><p>
 * Once you have made a <code>KeyFactory</code>, you generate a new key by calling
 * the <code>newInstance</code> method defined by your interface.
 * <p><pre>
 *     IntStringKey factory = (IntStringKey)KeyFactory.create(IntStringKey.class);
 *     Object key1 = factory.newInstance(4, "Hello");
 *     Object key2 = factory.newInstance(4, "World");
 * </pre><p>
 * <b>Note:</b>
 * <code>hashCode</code> equality between two keys <code>key1</code> and <code>key2</code> is only guaranteed if
 * <code>key1.equals(key2)</code> <i>and</i> the keys were produced by the same factory.
 *
 * @version $Id: KeyFactory.java,v 1.21 2004/05/08 07:30:29 herbyderby Exp $
 */
abstract public class KeyFactory {
    private static final Signature GET_NAME =
      TypeUtils.parseSignature("String getName()");
    private static final Signature GET_CLASS =
      TypeUtils.parseSignature("Class getClass()");
    private static final Signature HASH_CODE =
      TypeUtils.parseSignature("int hashCode()");
    private static final Signature EQUALS =
      TypeUtils.parseSignature("boolean equals(Object)");
    private static final Signature TO_STRING =
      TypeUtils.parseSignature("String toString()");
    private static final Signature APPEND_STRING =
      TypeUtils.parseSignature("StringBuffer append(String)");
    private static final Type KEY_FACTORY =
      TypeUtils.parseType("net.sf.cglib.core.KeyFactory");
    
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
    

    public static final Customizer CLASS_BY_NAME = new Customizer() {
        public void customize(CodeEmitter e, Type type) {
            if (type.equals(Constants.TYPE_CLASS)) {
                e.invoke_virtual(Constants.TYPE_CLASS, GET_NAME);
            }
        }
    };

    public static final Customizer OBJECT_BY_CLASS = new Customizer() {
        public void customize(CodeEmitter e, Type type) {
            e.invoke_virtual(Constants.TYPE_OBJECT, GET_CLASS);
        }
    };

    protected KeyFactory() {
    }

    public static KeyFactory create(Class keyInterface) {
        return create(keyInterface, null);
    }

    public static KeyFactory create(Class keyInterface, Customizer customizer) {
        return create(keyInterface.getClassLoader(), keyInterface,  customizer);
    }

    // TODO: add another constructor that includes attemptLoad
    
    public static KeyFactory create(ClassLoader loader, Class keyInterface, Customizer customizer) {
        Generator gen = new Generator();
        gen.setInterface(keyInterface);
        gen.setCustomizer(customizer);
        gen.setClassLoader(loader);
        return gen.create();
    }

    public static class Generator extends AbstractClassGenerator {
        private static final Source SOURCE = new Source(KeyFactory.class.getName());
        private Class keyInterface;
        private Customizer customizer;
        private int constant;
        private int multiplier;

        public Generator() {
            super(SOURCE);
        }

        protected ClassLoader getDefaultClassLoader() {
            return keyInterface.getClassLoader();
        }

        public void setCustomizer(Customizer customizer) {
            this.customizer = customizer;
        }

        public void setInterface(Class keyInterface) {
            this.keyInterface = keyInterface;
        }

        public KeyFactory create() {
            setNamePrefix(keyInterface.getName());
            return (KeyFactory)super.create(keyInterface.getName());
        }

        public void setHashConstant(int constant) {
            this.constant = constant;
        }

        public void setHashMultiplier(int multiplier) {
            this.multiplier = multiplier;
        }

        protected Object firstInstance(Class type) {
            return ReflectUtils.newInstance(type);
        }

        protected Object nextInstance(Object instance) {
            return instance;
        }

        public void generateClass(ClassVisitor v) {
            ClassEmitter ce = new ClassEmitter(v);
            
            Method newInstance = ReflectUtils.findNewInstance(keyInterface);
            if (!newInstance.getReturnType().equals(Object.class)) {
                throw new IllegalArgumentException("newInstance method must return Object");
            }

            Type[] parameterTypes = TypeUtils.getTypes(newInstance.getParameterTypes());
            ce.begin_class(Constants.ACC_PUBLIC,
                           getClassName(),
                           KEY_FACTORY,
                           new Type[]{ Type.getType(keyInterface) },
                           Constants.SOURCE_FILE);
            EmitUtils.null_constructor(ce);
            EmitUtils.factory_method(ce, ReflectUtils.getSignature(newInstance));

            int seed = 0;
            CodeEmitter e = ce.begin_method(Constants.ACC_PUBLIC,
                                            TypeUtils.parseConstructor(parameterTypes),
                                            null,
                                            null);
            e.load_this();
            e.super_invoke_constructor();
            e.load_this();
            for (int i = 0; i < parameterTypes.length; i++) {
                seed += parameterTypes[i].hashCode();
                ce.declare_field(Constants.ACC_PRIVATE | Constants.ACC_FINAL,
                                 getFieldName(i),
                                 parameterTypes[i],
                                 null,
                                 null);
                e.dup();
                e.load_arg(i);
                e.putfield(getFieldName(i));
            }
            e.return_value();
            e.end_method();
            
            // hash code
            e = ce.begin_method(Constants.ACC_PUBLIC, HASH_CODE, null, null);
            int hc = (constant != 0) ? constant : PRIMES[(int)(Math.abs(seed) % PRIMES.length)];
            int hm = (multiplier != 0) ? multiplier : PRIMES[(int)(Math.abs(seed * 13) % PRIMES.length)];
            e.push(hc);
            for (int i = 0; i < parameterTypes.length; i++) {
                e.load_this();
                e.getfield(getFieldName(i));
                EmitUtils.hash_code(e, parameterTypes[i], hm, customizer);
            }
            e.return_value();
            e.end_method();

            // equals
            e = ce.begin_method(Constants.ACC_PUBLIC, EQUALS, null, null);
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
                EmitUtils.not_equals(e, parameterTypes[i], fail, customizer);
            }
            e.push(1);
            e.return_value();
            e.mark(fail);
            e.push(0);
            e.return_value();
            e.end_method();

            // toString
            e = ce.begin_method(Constants.ACC_PUBLIC, TO_STRING, null, null);
            e.new_instance(Constants.TYPE_STRING_BUFFER);
            e.dup();
            e.invoke_constructor(Constants.TYPE_STRING_BUFFER);
            for (int i = 0; i < parameterTypes.length; i++) {
                if (i > 0) {
                    e.push(", ");
                    e.invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_STRING);
                }
                e.load_this();
                e.getfield(getFieldName(i));
                EmitUtils.append_string(e, parameterTypes[i], EmitUtils.DEFAULT_DELIMITERS, customizer);
            }
            e.invoke_virtual(Constants.TYPE_STRING_BUFFER, TO_STRING);
            e.return_value();
            e.end_method();

            ce.end_class();
        }

        private String getFieldName(int arg) {
            return "FIELD_" + arg;
        }
    }
}
