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
package net.sf.cglib.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import junit.framework.*;

public class TestReflectPerf extends net.sf.cglib.CodeGenTestCase {
    public interface IndexOf {
        int indexOf(String s, int start);
    }

    public void testReflectPerf() throws Throwable {
        int iterations = 1000000;
        System.out.println();
        System.out.println("iteration count: " + iterations);

        String test = "abcabcabc";

        Class[] types = new Class[]{ String.class, Integer.TYPE };
        Method indexOf = String.class.getDeclaredMethod("indexOf", types);
        FastClass fc = FastClass.create(String.class);
        FastMethod fm = fc.getMethod("indexOf", types);
        int fidx = fm.getIndex();
        Object[] args = new Object[]{ "ab", new Integer(1) };

        IndexOf fast = (IndexOf)MethodDelegate.create(test, "indexOf", IndexOf.class);
        
        int result;
        long t1  = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            result = ((Integer)fc.invoke("indexOf", types, test, args)).intValue();
        }
        long t2  = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            args = new Object[]{ "ab", new Integer(1) };
            result = ((Integer)indexOf.invoke(test, args)).intValue();
        }
        long t3  = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            result = ((Integer)indexOf.invoke(test, args)).intValue();
        }
        long t4  = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            args = new Object[]{ "ab", new Integer(1) };
            result = ((Integer)fm.invoke(test, args)).intValue();
        }
        long t5  = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            result = ((Integer)fm.invoke(test, args)).intValue();
        }
        long t6  = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            result = ((Integer)fc.invoke(fidx, test, args)).intValue();
        }
        long t7  = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            result = fast.indexOf("ab", 1);
        }
        long t8  = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            result = test.indexOf("ab", 1);
        }
        long t9  = System.currentTimeMillis();

        System.out.println("fc           = " + (t2 - t1)
                           + "\n" + "reflect+args = " + (t3 - t2) 
                           + "\n" + "reflect      = " + (t4 - t3)
                           + "\n" + "fm+args      = " + (t5 - t4)
                           + "\n" + "fm           = " + (t6 - t5)
                           + "\n" + "fc w/idx     = " + (t7 - t6)
                           + "\n" + "delegate     = " + (t8 - t7)
                           + "\n" + "raw          = " + (t9 - t8));
    }
    


    public TestReflectPerf(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestReflectPerf.class);
    }
    
    public void perform(ClassLoader loader) throws Throwable {
    }
    
    public void testFailOnMemoryLeak() throws Throwable {
    }
    
}
