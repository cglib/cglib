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
package net.sf.cglib.reflect;

import java.lang.reflect.*;
import java.util.*;
import junit.framework.*;
import net.sf.cglib.CodeGenTestCase;
import net.sf.cglib.TestGenerator;
import net.sf.cglib.core.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

public class TestMemberSwitch extends CodeGenTestCase {
    private static int index = 0;

    private static final Constructor C0 =
      ReflectUtils.findConstructor("net.sf.cglib.reflect.MemberSwitchBean()");
    private static final Constructor C1 =
      ReflectUtils.findConstructor("net.sf.cglib.reflect.MemberSwitchBean(double)");
    private static final Constructor C2 =
      ReflectUtils.findConstructor("net.sf.cglib.reflect.MemberSwitchBean(int)");
    private static final Constructor C3 =
      ReflectUtils.findConstructor("net.sf.cglib.reflect.MemberSwitchBean(int, String, String)");
    private static final Constructor C4 =
      ReflectUtils.findConstructor("net.sf.cglib.reflect.MemberSwitchBean(int, String, double)");
    private static final Constructor C5 =
      ReflectUtils.findConstructor("net.sf.cglib.reflect.MemberSwitchBean(int, short, long)");
    private static final Constructor C6 =
      ReflectUtils.findConstructor("net.sf.cglib.reflect.MemberSwitchBean(int, String)");

    public static interface Indexed {
        int getIndex(Class[] types);
    }

    public void testSimple() {
        Indexed test = (Indexed)new Generator(new Constructor[]{ C0, C1, C2, C3, C4, C5, C6 }).create();
        assertTrue(test.getIndex(C0.getParameterTypes()) == 0);
        assertTrue(test.getIndex(C1.getParameterTypes()) == 1);
        assertTrue(test.getIndex(C2.getParameterTypes()) == 2);
        assertTrue(test.getIndex(C3.getParameterTypes()) == 3);
        assertTrue(test.getIndex(C4.getParameterTypes()) == 4);
        assertTrue(test.getIndex(C5.getParameterTypes()) == 5);
        assertTrue(test.getIndex(C6.getParameterTypes()) == 6);
        assertTrue(test.getIndex(new Class[]{ Integer.TYPE, Integer.TYPE }) == -1);
    }

    private static class Generator extends TestGenerator {
        private static final Source SOURCE = new Source(TestMemberSwitch.class, false);
        private Constructor[] constructors;

        public Generator(Constructor[] constructors) {
            super(SOURCE);
            this.constructors = constructors;
        }

        public void generateClass(ClassVisitor v) throws Exception {
            final List clist = Arrays.asList(constructors);
            final Emitter e = new Emitter(v);
            e.begin_class(Constants.ACC_PUBLIC, getClassName(), null, new Type[]{ Type.getType(Indexed.class) }, Constants.SOURCE_FILE);
            e.null_constructor();
            Method method = Indexed.class.getMethod("getIndex", new Class[]{ Class[].class });
            e.begin_method(Constants.ACC_PUBLIC,
                           ReflectUtils.getSignature(method),
                           ReflectUtils.getExceptionTypes(method));
            e.load_arg(0);
            ReflectOps.constructor_switch(e, constructors, new ObjectSwitchCallback() {
                    public void processCase(Object key, Label end) {
                        e.push(clist.indexOf(key));
                        e.goTo(end);
                    }
                    public void processDefault() {
                        e.push(-1);
                    }
                });
            e.return_value();
            e.end_class();
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
