package net.sf.cglib.util;

import junit.framework.*;
import java.util.*;

public class TestKeyFactory extends TestCase {    
    public void setUp() {
        // CodeGenerator.setDebugLocation("/tmp/");
    }

    public interface MyKey {
        public Object newInstance(int a, int[] b, boolean flag);
    }

    public interface MyKey2 {
        public Object newInstance(int[][] a);
    }

    public interface MethodKey {
        public Object newInstance(Class returnType, Class[] parameterTypes);
    }
    
    public void testSimple() throws Exception {
        MyKey mykey = (MyKey)KeyFactory.makeFactory(MyKey.class, null);
        assertTrue(mykey.newInstance(5, new int[]{ 6, 7 }, false).hashCode() ==
                   mykey.newInstance(5, new int[]{ 6, 7 }, false).hashCode());
    }

    public void testNested() throws Exception {
        MyKey2 mykey2 = (MyKey2)KeyFactory.makeFactory(MyKey2.class, null);
        Object instance = mykey2.newInstance(new int[][]{ { 1, 2 }, { 3, 4 } });
        int code1 = instance.hashCode();
        int result = ((KeyFactory)instance).getHashConstant();
        int mult = ((KeyFactory)instance).getHashMultiplier();
        int code2 = ((((result * mult + 1) * mult + 2) * mult + 3) * mult + 4) * mult;
        assertTrue(code1 == code2);
    }

    public void testMethodKey() throws Exception {
        MethodKey factory = (MethodKey)KeyFactory.makeFactory(MethodKey.class, null);
        Set methodSet = new HashSet();
        methodSet.add(factory.newInstance(Number.class, new Class[]{ int.class }));
        assertTrue(methodSet.contains(factory.newInstance(Number.class, new Class[]{ int.class })));
        assertTrue(!methodSet.contains(factory.newInstance(Number.class, new Class[]{ Integer.class })));
    }

    public TestKeyFactory(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestKeyFactory.class);
    }
}
