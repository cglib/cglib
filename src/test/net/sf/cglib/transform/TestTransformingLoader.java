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
package net.sf.cglib.transform;

import net.sf.cglib.beans.*;
import java.util.*;
import junit.framework.*;

/**
 * @version $Id: TestTransformingLoader.java,v 1.3 2003/09/19 23:31:00 herbyderby Exp $
 */
public class TestTransformingLoader extends net.sf.cglib.CodeGenTestCase {

    private static final ClassFilter TEST_FILTER = new ClassFilter() {
        public boolean accept(String name) {
            System.err.println("Loading " + name);
            return name.startsWith("net.sf.cglib.");
        }
    };

    private ClassTransformer getExampleTransformer(String name, Class type) {
        return new ExampleTransformer(new String[]{ name }, new Class[]{ type });
    }

    public void testExample() throws Exception {
        ClassTransformer t1 = getExampleTransformer("herby", String.class);
        ClassTransformer t2 = getExampleTransformer("derby", Double.TYPE);
        ClassTransformer chain = new TransformerChain(new ClassTransformer[]{ t1, t2 });
        System.err.println("chain=" + chain);

        Class loaded = loadHelper(chain, Example.class);
        Object obj = loaded.newInstance();
        String value = "HELLO";
        loaded.getMethod("setHerby", new Class[]{ String.class }).invoke(obj, new Object[]{ value });
        assertTrue(value.equals(loaded.getMethod("getHerby", null).invoke(obj, null)));

        loaded.getMethod("setDerby", new Class[]{ Double.TYPE }).invoke(obj, new Object[]{ new Double(1.23456789d) });
    }

    private static Class loadHelper(ClassTransformer t, Class target) throws ClassNotFoundException {
        ClassLoader parent = TestTransformingLoader.class.getClassLoader();
        TransformingLoader loader = new TransformingLoader(parent, TEST_FILTER, t);
        return loader.loadClass(target.getName());
    }

    /*
    private static class PatternTest1 {
        public int foo = 1;
        public int afoo = 2;
        public int bar = 3;
        public double dfoo = 4;

        public static void main(String[] args) {
            PatternTest1 p = new PatternTest1();
            System.out.println(p.afoo + p.bar + p.foo + p.dfoo);
        }
    }

    public void testPattern() throws Exception {
        final List names = new ArrayList(3);
        PatternTransformer t = new PatternTransformer(new CodeTransformer() {
            public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                names.add(name);
                super.visitFieldInsn(opcode, owner, name, desc);
            }
        });
        t.addGetFieldPattern("int *foo");
        loadHelper(t, PatternTest1.class);
        assertTrue(names.size() == 2);
        assertTrue(names.get(0).equals("afoo"));
        assertTrue(names.get(0).equals("foo"));
    }
    */

    public TestTransformingLoader(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestTransformingLoader.class);
    }

}
