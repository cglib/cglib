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

import java.lang.reflect.Constructor;
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
 * multi-dimension arrays of either.
 * <p>
 * Once you have made a <code>KeyFactory</code>, you generate a new key by calling
 * the <code>newInstance</code> method defined by your interface.
 * <p>
 * This example should print <code>true</code> followed by <code>false</code>:
 * <p><pre>
 *   import net.sf.cglib.core.KeyFactory;
 *   public class KeySample {
 *       private interface MyFactory {
 *           public Object newInstance(int a, char[] b, String c);
 *       }
 *       public static void main(String[] args) {
 *           MyFactory f = (MyFactory)KeyFactory.create(MyFactory.class, null);
 *           Object key1 = f.newInstance(20, new char[]{ 'a', 'b' }, "hello");
 *           Object key2 = f.newInstance(20, new char[]{ 'a', 'b' }, "hello");
 *           Object key3 = f.newInstance(20, new char[]{ 'a', '_' }, "hello");
 *           System.out.println(key1.equals(key2));
 *           System.out.println(key2.equals(key3));
 *       }
 *   }
 * </pre>
 * <p>
 * <b>Note:</b>
 * <code>hashCode</code> equality between two keys <code>key1</code> and <code>key2</code> is guaranteed if
 * <code>key1.equals(key2)</code> <i>and</i> the keys were produced by the same factory.
 *
 * @version $Id: KeyFactory.java,v 1.10 2003/10/03 19:25:07 herbyderby Exp $
 */
public class KeyFactory extends AbstractClassGenerator {
    private static final Source SOURCE = new Source(KeyFactory.class.getName());
    private static final Signature GET_NAME =
      TypeUtils.parseSignature("String getName()");
    private static final Signature GET_CLASS =
      TypeUtils.parseSignature("Class getClass()");

    private Class keyInterface;
    private Customizer customizer;

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

    public static Object create(Class keyInterface) {
        return create(keyInterface, null);
    }

    public static Object create(Class keyInterface, Customizer customizer) {
        KeyFactory gen = new KeyFactory();
        gen.setInterface(keyInterface);
        gen.setCustomizer(customizer);
        return gen.create();
    }

    public KeyFactory() {
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

    public void generateClass(ClassVisitor v) throws Exception {
        new KeyFactoryEmitter(v, getClassName(), keyInterface, customizer).emit();
    }

    protected Object firstInstance(Class type) {
        return ReflectUtils.newInstance(type);
    }

    protected Object nextInstance(Object instance) {
        return instance;
    }
}
