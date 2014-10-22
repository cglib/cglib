/*
 * Copyright 2003,2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.cglib.core;

import junit.framework.*;
import java.util.*;

/**
 * @author Chris Nokleberg <a href="mailto:chris@nokleberg.com">chris@nokleberg.com</a>
 * @version $Id: TestKeyFactory.java,v 1.7 2012/07/27 16:02:50 baliuka Exp $
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
    
    
}
