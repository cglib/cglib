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
package net.sf.cglib.util;

import java.util.*;
import net.sf.cglib.core.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

/**
 * This class implements a simple String->int mapping for a fixed set of keys.
 */
abstract public class StringSwitcher {
    private static final Type STRING_SWITCHER =
      TypeUtils.parseType("net.sf.cglib.util.StringSwitcher");
    private static final Signature INT_VALUE =
      TypeUtils.parseSignature("int intValue(String)");
    private static final StringSwitcherKey KEY_FACTORY =
      (StringSwitcherKey)KeyFactory.create(StringSwitcherKey.class);

    interface StringSwitcherKey {
        public Object newInstance(String[] strings, int[] ints, boolean fixedInput);
    }

    /**
     * Helper method to create a StringSwitcher.
     * For finer control over the generated instance, use a new instance of StringSwitcher.Generator
     * instead of this static method.
     * @param strings the array of String keys; must be the same length as the value array
     * @param ints the array of integer results; must be the same length as the key array
     * @param fixedInput if false, an unknown key will be returned from {@link #intValue} as <code>-1</code>; if true,
     * the result will be undefined, and the resulting code will be faster
     */
    public static StringSwitcher create(String[] strings, int[] ints, boolean fixedInput) {
        Generator gen = new Generator();
        gen.setStrings(strings);
        gen.setInts(ints);
        gen.setFixedInput(fixedInput);
        return gen.create();
    }

    protected StringSwitcher() {
    }

    /**
     * Return the integer associated with the given key.
     * @param s the key
     * @return the associated integer value, or <code>-1</code> if the key is unknown (unless
     * <code>fixedInput</code> was specified when this <code>StringSwitcher</code> was created,
     * in which case the return value for an unknown key is undefined)
     */
    abstract public int intValue(String s);

    public static class Generator extends AbstractClassGenerator {
        private static final Source SOURCE = new Source(StringSwitcher.class.getName());

        private String[] strings;
        private int[] ints;
        private boolean fixedInput;
        
        public Generator() {
            super(SOURCE);
        }

        /**
         * Set the array of recognized Strings.
         * @param strings the array of String keys; must be the same length as the value array
         * @see #setInts
         */
        public void setStrings(String[] strings) {
            this.strings = strings;
        }

        /**
         * Set the array of integer results.
         * @param ints the array of integer results; must be the same length as the key array
         * @see #setStrings
         */
        public void setInts(int[] ints) {
            this.ints = ints;
        }

        /**
         * Configure how unknown String keys will be handled.
         * @param fixedInput if false, an unknown key will be returned from {@link #intValue} as <code>-1</code>; if true,
         * the result will be undefined, and the resulting code will be faster
         */
        public void setFixedInput(boolean fixedInput) {
            this.fixedInput = fixedInput;
        }

        protected ClassLoader getDefaultClassLoader() {
            return getClass().getClassLoader();
        }

        /**
         * Generate the <code>StringSwitcher</code>.
         */
        public StringSwitcher create() {
            setNamePrefix(StringSwitcher.class.getName());
            Object key = KEY_FACTORY.newInstance(strings, ints, fixedInput);
            return (StringSwitcher)super.create(key);
        }

        public void generateClass(ClassVisitor v) throws Exception {
            ClassEmitter ce = new ClassEmitter(v);
            ce.begin_class(Constants.ACC_PUBLIC,
                           getClassName(),
                           STRING_SWITCHER,
                           null,
                           Constants.SOURCE_FILE);
            EmitUtils.null_constructor(ce);
            final CodeEmitter e = ce.begin_method(Constants.ACC_PUBLIC, INT_VALUE, null);
            e.load_arg(0);
            final List stringList = Arrays.asList(strings);
            int style = fixedInput ? Constants.SWITCH_STYLE_HASHONLY : Constants.SWITCH_STYLE_HASH;
            EmitUtils.string_switch(e, strings, style, new ObjectSwitchCallback() {
                public void processCase(Object key, Label end) {
                    e.push(ints[stringList.indexOf(key)]);
                    e.return_value();
                }
                public void processDefault() {
                    e.push(-1);
                    e.return_value();
                }
            });
            e.end_method();
            ce.end_class();
        }

        protected Object firstInstance(Class type) {
            return (StringSwitcher)ReflectUtils.newInstance(type);
        }

        protected Object nextInstance(Object instance) {
            return instance;
        }
    }
}
