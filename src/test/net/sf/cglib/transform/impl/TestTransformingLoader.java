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
package net.sf.cglib.transform.impl;

import net.sf.cglib.transform.*;
import net.sf.cglib.core.Constants;
import net.sf.cglib.core.ReflectUtils;
import net.sf.cglib.beans.*;
import java.util.*;
import java.lang.reflect.Method;
import junit.framework.*;
import org.objectweb.asm.Type;

/**
 * @version $Id: TestTransformingLoader.java,v 1.4 2004/04/25 16:15:21 baliuka Exp $
 */
public class TestTransformingLoader extends net.sf.cglib.CodeGenTestCase {

    private static final ClassFilter TEST_FILTER = new ClassFilter() {
        public boolean accept(String name) {
            System.err.println("Loading " + name);
            return name.startsWith("net.sf.cglib.");
        }
    };

    private ClassTransformer getExampleTransformer(String name, Type type) {
        return new AddPropertyTransformer(new String[]{ name }, new Type[]{ type });
    }

    public void testExample() throws Exception {
        ClassTransformer t1 = getExampleTransformer("herby", Constants.TYPE_STRING);
        ClassTransformer t2 = getExampleTransformer("derby", Type.DOUBLE_TYPE);
        ClassTransformer chain = new ClassTransformerChain(new ClassTransformer[]{ t1, t2 });
        Class loaded = loadHelper(chain, Example.class);
        Object obj = loaded.newInstance();
        String value = "HELLO";
        loaded.getMethod("setHerby", new Class[]{ String.class }).invoke(obj, new Object[]{ value });
        assertTrue(value.equals(loaded.getMethod("getHerby", null).invoke(obj, null)));

        loaded.getMethod("setDerby", new Class[]{ Double.TYPE }).invoke(obj, new Object[]{ new Double(1.23456789d) });
    }

    private static Class inited;

    public static void initStatic(Class foo) {
        System.err.println("INITING: " + foo);
    }

    public void testAddStatic() throws Exception {
        Method m = ReflectUtils.findMethod("net.sf.cglib.transform.impl.TestTransformingLoader.initStatic(Class)");
        ClassTransformer t = new AddStaticInitTransformer(m);
        // t = new ClassTransformerChain(new ClassTransformer[]{ t, new ClassTransformerTee(new org.objectweb.asm.util.TraceClassVisitor(null, new java.io.PrintWriter(System.out))) });
        Class loaded = loadHelper(t, Example.class);
        Object obj = loaded.newInstance();
        // TODO
    }

    public void testInterceptField() throws Exception {
        ClassTransformer t = new InterceptFieldTransformer(new InterceptFieldFilter() {
            public boolean acceptRead(Type owner, String name) {
                return true;
            }
            public boolean acceptWrite(Type owner, String name) {
                return true;
            }
        });
        Class loaded = loadHelper(t, Example.class);
        // TODO
    }

    public void testFieldProvider() throws Exception {
        ClassTransformer t = new FieldProviderTransformer();
        Class loaded = loadHelper(t, Example.class);
        // TODO
//         FieldProvider fp = (FieldProvider)loaded.newInstance();
//         assertTrue(((Integer)fp.getField("example")).intValue() == 42);
//         fp.setField("example", new Integer(6));
//         assertTrue(((Integer)fp.getField("example")).intValue() == 6);
//         assertTrue(fp.getField("example") == null);
//         try {
//             fp.getField("dsfjkl");
//             fail("expected exception");
//         } catch (IllegalArgumentException ignore) { }
    }

    private static Class loadHelper( final ClassTransformer t, Class target) throws ClassNotFoundException {
        ClassLoader parent = TestTransformingLoader.class.getClassLoader();
        TransformingClassLoader loader = new TransformingClassLoader(parent, TEST_FILTER,
        
           new ClassTransformerFactory(){
                  public ClassTransformer  newInstance(){
                     return t;
                  }
        }
        
        );
        return loader.loadClass(target.getName());
    }

    public TestTransformingLoader(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestTransformingLoader.class);
    }

    public void perform(ClassLoader loader) throws Throwable {
    }    
    
    public void testFailOnMemoryLeak() throws Throwable {
    }
    
}
