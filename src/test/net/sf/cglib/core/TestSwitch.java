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

import net.sf.cglib.CodeGenTestCase;
import net.sf.cglib.TestGenerator;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import junit.framework.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

public class TestSwitch extends CodeGenTestCase {
    private static int index = 0;

    public static interface Alphabet {
        String getLetter(int index);
    }

    public void testAlphabet() {
        int[] keys = new int[26];
    
        for (int i = 0; i < 26; i++) {
            keys[i] = i + 1;
        }
        String[] letters = new String[] {
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "k", "j", "l", "m",
            "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
        };
        
        Alphabet alpha = (Alphabet)new Generator(keys, letters).create();
        assertTrue("c".equals(alpha.getLetter(3)));
        assertTrue("f".equals(alpha.getLetter(6)));
        assertTrue("!".equals(alpha.getLetter(27)));
    }

    private static class Generator extends TestGenerator {
        private static final Source SOURCE = new Source(TestSwitch.class.getName());
        private int[] keys;
        private String[] values;

        public Generator(int[] keys, String[] values) {
            super(SOURCE);
            this.keys = keys;
            this.values = values;
        }

        public void generateClass(ClassVisitor v) throws Exception {
            ClassEmitter ce = new ClassEmitter(v);
            ce.begin_class(Constants.ACC_PUBLIC,
                           getClassName(),
                           null,
                           new Type[]{ Type.getType(Alphabet.class) },
                           Constants.SOURCE_FILE);
            ComplexOps.null_constructor(ce);
            Method method = Alphabet.class.getMethod("getLetter", new Class[]{ Integer.TYPE });
            final CodeEmitter e = ce.begin_method(Constants.ACC_PUBLIC,
                                                  ReflectUtils.getSignature(method),
                                                  ReflectUtils.getExceptionTypes(method));
            e.load_arg(0);
            e.process_switch(keys, new ProcessSwitchCallback() {
                    public void processCase(int index, Label end) {
                        e.push(values[index - 1]);
                        e.goTo(end);
                    }
                    public void processDefault() {
                        e.push("!");
                    }
                });
            e.return_value();
            e.end_method();
            ce.end_class();
        }
    }

    public TestSwitch(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestSwitch.class);
    }
}
