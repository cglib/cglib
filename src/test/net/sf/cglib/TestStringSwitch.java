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
package net.sf.cglib;

import java.lang.reflect.Method;
import java.util.*;
import net.sf.cglib.util.*;
import junit.framework.*;

public class TestStringSwitch extends CodeGenTestCase {
    private static int index = 0;

    public static interface Indexed {
        int getIndex(String word);
    }

    public void testSimple() {
        String[] keys = new String[]{ "foo", "bar", "baz", "quud", "quick", "quip" };

        Class created = create(keys);
        Indexed test = (Indexed)ReflectUtils.newInstance(created);
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

    public static Class create(String[] keys) {
        return new Generator(keys).define();
    }

    private static class Generator extends CodeGenerator {
        private String[] keys;

        public Generator(String[] keys) {
            super("TestStringSwitch" + index++,
                  Object.class,
                  TestStringSwitch.class.getClassLoader());
            this.keys = keys;
            addInterface(Indexed.class);
        }

        protected void generate() throws Exception {
            null_constructor();

            Method method = Indexed.class.getMethod("getIndex", new Class[]{ String.class });
            begin_method(method);
            load_arg(0);
            string_switch(keys, new StringSwitchCallback() {
                    public void processCase(String key, Label end) {
                        push((int)key.charAt(key.length() - 1));
                        goTo(end);
                    }
                    public void processDefault() {
                        push(0);
                    }
                });
            return_value();
            end_method();
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
