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

public class TestStringSwitch extends CodeGenTestCase {
    private static int index = 0;

    public static interface Indexed {
        int getIndex(String word);
    }

    public void testSimple() {
        simpleHelper(Ops.SWITCH_STYLE_HASH);
        simpleHelper(Ops.SWITCH_STYLE_TRIE);
    }

    private void simpleHelper(int switchStyle) {
        String[] keys = new String[]{ "foo", "bar", "baz", "quud", "quick", "quip" };
        Indexed test = (Indexed)new Generator(keys, switchStyle).create();
        assertTrue(test.getIndex("foo") == 'o');
        assertTrue(test.getIndex("bar") == 'r');
        assertTrue(test.getIndex("baz") == 'z');
        assertTrue(test.getIndex("quud") == 'd');
        assertTrue(test.getIndex("quick") == 'k');
        assertTrue(test.getIndex("quip") == 'p');
        assertTrue(test.getIndex("q") == 0);
        assertTrue(test.getIndex("qu") == 0);
        assertTrue(test.getIndex("fop") == 0);
        assertTrue(test.getIndex("quicker") == 0);
        assertTrue(test.getIndex("herby") == 0);
        assertTrue(test.getIndex("herbyderby") == 0);
        assertTrue(test.getIndex("") == 0);
    }

    public void testEqualHashCodes() {
        String[] keys = new String[]{ "ABC", "AAb", "foo" };
        assertTrue("ABC".hashCode() == "AAb".hashCode());
        assertTrue("ABC".hashCode() != "foo".hashCode());
        Indexed test = (Indexed)new Generator(keys, Ops.SWITCH_STYLE_HASH).create();
        assertTrue(test.getIndex("foo") == 'o');
        assertTrue(test.getIndex("ABC") == 'C');
        assertTrue(test.getIndex("AAb") == 'b');
    }

    private static class Generator extends TestGenerator {
        private static final Source SOURCE = new Source(TestStringSwitch.class, false);
        private String[] keys;
        private int switchStyle;

        public Generator(String[] keys, int switchStyle) {
            super(SOURCE);
            this.keys = keys;
            this.switchStyle = switchStyle;
        }

        public void generateClass(ClassVisitor v) throws Exception {
            final Emitter e = new Emitter(v);
            Ops.begin_class(e, Modifier.PUBLIC, getClassName(), null, new Class[]{ Indexed.class }, Constants.SOURCE_FILE);
            Ops.null_constructor(e);
            Method method = Indexed.class.getMethod("getIndex", new Class[]{ String.class });
            Ops.begin_method(e, method);
            e.load_arg(0);
            Ops.string_switch(e, keys, switchStyle, new ObjectSwitchCallback() {
                    public void processCase(Object key, Label end) {
                        String string = (String)key;
                        e.push((int)string.charAt(string.length() - 1));
                        e.goTo(end);
                    }
                    public void processDefault() {
                        e.push(0);
                    }
                });
            e.return_value();
            e.end_class();
        }
    }

    public TestStringSwitch(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestStringSwitch.class);
    }
}
