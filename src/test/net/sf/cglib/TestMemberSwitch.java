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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import net.sf.cglib.util.*;
import junit.framework.*;

public class TestMemberSwitch extends CodeGenTestCase {
    private static int index = 0;

    private static final Constructor C0 =
      ReflectUtils.findConstructor("TestMemberSwitch$ConstructorBean()");
    private static final Constructor C1 =
      ReflectUtils.findConstructor("TestMemberSwitch$ConstructorBean(double)");
    private static final Constructor C2 =
      ReflectUtils.findConstructor("TestMemberSwitch$ConstructorBean(int)");
    private static final Constructor C3 =
      ReflectUtils.findConstructor("TestMemberSwitch$ConstructorBean(int, String, String)");
    private static final Constructor C4 =
      ReflectUtils.findConstructor("TestMemberSwitch$ConstructorBean(int, String, double)");
    private static final Constructor C5 =
      ReflectUtils.findConstructor("TestMemberSwitch$ConstructorBean(int, short, long)");
    private static final Constructor C6 =
      ReflectUtils.findConstructor("TestMemberSwitch$ConstructorBean(int, String)");

    
    static class ConstructorBean {
        public ConstructorBean() { }
        public ConstructorBean(double foo) { }
        public ConstructorBean(int foo) { }
        public ConstructorBean(int foo, String bar, String baz) { }
        public ConstructorBean(int foo, String bar, double baz) { }
        public ConstructorBean(int foo, short bar, long baz) { }
        public ConstructorBean(int foo, String bar) { }
    }

    public static interface Indexed {
        int getIndex(Class[] types);
    }

    public void testSimple() {
        Class created = new Generator(new Constructor[]{ C0, C1, C2, C3, C4, C5, C6 }).define();
        Indexed test = (Indexed)ReflectUtils.newInstance(created);
        assertTrue(test.getIndex(C0.getParameterTypes()) == 0);
        assertTrue(test.getIndex(C1.getParameterTypes()) == 1);
        assertTrue(test.getIndex(C2.getParameterTypes()) == 2);
        assertTrue(test.getIndex(C3.getParameterTypes()) == 3);
        assertTrue(test.getIndex(C4.getParameterTypes()) == 4);
        assertTrue(test.getIndex(C5.getParameterTypes()) == 5);
        assertTrue(test.getIndex(C6.getParameterTypes()) == 6);
        assertTrue(test.getIndex(new Class[]{ Integer.TYPE, Integer.TYPE }) == -1);
    }

    private static class Generator extends CodeGenerator {
        private Constructor[] constructors;
        private List clist;

        public Generator(Constructor[] constructors) {
            setNamePrefix("TestMemberSwitch");
            setNameSuffix(String.valueOf(index++));
            setClassLoader(TestMemberSwitch.class.getClassLoader());
            this.constructors = constructors;
            clist = Arrays.asList(constructors);
            addInterface(Indexed.class);
        }

        protected void generate() throws Exception {
            null_constructor();

            Method method = Indexed.class.getMethod("getIndex", new Class[]{ Class[].class });
            begin_method(method);
            load_arg(0);
            constructor_switch(constructors, new ObjectSwitchCallback() {
                    public void processCase(Object key, Label end) {
                        push(clist.indexOf(key));
                        goTo(end);
                    }
                    public void processDefault() {
                        push(-1);
                    }
                });
            return_value();
            end_method();
        }
    }

    public TestMemberSwitch(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestMemberSwitch.class);
    }
}
