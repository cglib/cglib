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

import junit.framework.*;
import java.util.*;

/**
 * @author Chris Nokleberg <a href="mailto:chris@nokleberg.com">chris@nokleberg.com</a>
 * @version $Id: TestKeyFactory.java,v 1.5 2004/04/25 16:15:21 baliuka Exp $
 */
public class TestKeyFactory extends net.sf.cglib.CodeGenTestCase {
    public interface MyKey {
        public Object newInstance(int a, int[] b, boolean flag);
    }

    public interface MyKey2 {
        public Object newInstance(int[][] a);
    }

    public interface CharArrayKey {
        public Object newInstance(char[] a);
    }    

    public interface BooleanArrayKey {
        public Object newInstance(boolean[] a);
    }    

    public interface ClassArrayKey {
        public Object newInstance(Class[] a);
    }    

    public interface MethodKey {
        public Object newInstance(Class returnType, Class[] parameterTypes);
    }

    public interface PrimitivesKey {
        public Object newInstance(boolean b, double d, float f, int i, long l);
    }

    public interface IntegerKey {
        public Object newInstance(int i);
    }

    public interface LongKey {
        public Object newInstance(long l);
    }

    public interface FloatKey {
        public Object newInstance(float f);
    }
    
    public void testSimple() throws Exception {
        MyKey mykey = (MyKey)KeyFactory.create(MyKey.class);
        assertTrue(mykey.newInstance(5, new int[]{ 6, 7 }, false).hashCode() ==
                   mykey.newInstance(5, new int[]{ 6, 7 }, false).hashCode());
    }

    private Object helper(Class type) {
        KeyFactory.Generator gen = new KeyFactory.Generator();
        gen.setInterface(type);
        gen.setHashConstant(5);
        gen.setHashMultiplier(3);
        return gen.create();
    }

    public void testPrimitives() throws Exception {
        PrimitivesKey factory = (PrimitivesKey)helper(PrimitivesKey.class);
        Object instance = factory.newInstance(true, 1.234d, 5.678f, 100, 200L);
        assertTrue(instance.hashCode() == 1525582882);
    }

    public void testInteger() throws Exception {
        IntegerKey factory = (IntegerKey)helper(IntegerKey.class);
        Object instance = factory.newInstance(7);
        assertTrue(instance.hashCode() == 22);
    }

    public void testLong() throws Exception {
        LongKey factory = (LongKey)helper(LongKey.class);
        Object instance = factory.newInstance(7L);
        assertTrue(instance.hashCode() == 22);
    }

    public void testFloat() throws Exception {
        FloatKey factory = (FloatKey)helper(FloatKey.class);
        Object instance = factory.newInstance(7f);
        assertTrue(instance.hashCode() == 1088421903);
    }
    
    public void testNested() throws Exception {
        KeyFactory.Generator gen = new KeyFactory.Generator();
        gen.setInterface(MyKey2.class);
        gen.setHashConstant(17);
        gen.setHashMultiplier(37);
        MyKey2 mykey2 = (MyKey2)gen.create();
        Object instance = mykey2.newInstance(new int[][]{ { 1, 2 }, { 3, 4 } });
        assertTrue(instance.hashCode() == 31914243);
    }

    public void testCharArray() throws Exception {
        CharArrayKey f = (CharArrayKey)KeyFactory.create(CharArrayKey.class);
        Object key1 = f.newInstance(new char[]{ 'a', 'b' });
        Object key2 = f.newInstance(new char[]{ 'a', '_' });
        assertTrue(!key1.equals(key2));
    }

    public void testBooleanArray() throws Exception {
        BooleanArrayKey f = (BooleanArrayKey)KeyFactory.create(BooleanArrayKey.class);
        Object key1 = f.newInstance(new boolean[]{ true, false, true });
        Object key2 = f.newInstance(new boolean[]{ true, false, true });
        assertTrue(key1.equals(key2));
    }

    public void testMethodKey() throws Exception {
        MethodKey factory = (MethodKey)KeyFactory.create(MethodKey.class);
        Set methodSet = new HashSet();
        methodSet.add(factory.newInstance(Number.class, new Class[]{ int.class }));
        assertTrue(methodSet.contains(factory.newInstance(Number.class, new Class[]{ int.class })));
        assertTrue(!methodSet.contains(factory.newInstance(Number.class, new Class[]{ Integer.class })));
    }

    public void testEqualOtherClass() throws Exception {
        MyKey mykey = (MyKey)KeyFactory.create(MyKey.class);
        assertTrue(!mykey.newInstance(5, new int[]{ 6, 7 }, false).equals(new Object()));
    }
    
    
    
    public TestKeyFactory(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestKeyFactory.class);
    }
    
    public void perform(ClassLoader loader) throws Throwable {
        
        KeyFactory.create(loader, MyKey.class, null );
    }
    
    public void testFailOnMemoryLeak() throws Throwable {
        if(leaks()){
          fail("Memory Leak in KeyFactory");
        }
    }
    
}
