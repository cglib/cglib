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

public class TestBeanMap extends CodeGenTestCase {
    public static class TestBean {
        private String foo;
        private String bar = "x";
        private String baz;
        private int quud;
        private int quick = 42;
        private int quip;

        public String getFoo() {
            return foo;
        }
        
        public void setFoo(String value) {
            foo = value;
        }

        public String getBar() {
            return bar;
        }
        
        public void setBaz(String value) {
            baz = value;
        }

        public int getQuud() {
            return quud;
        }
        
        public void setQuud(int value) {
            quud = value;
        }

        public int getQuick() {
            return quick;
        }
        
        public void setQuip(int value) {
            quip = value;
        }
    }

    public void testBeanMap() {
        TestBean bean = new TestBean();
        Map map = BeanMap.create(bean, null);
        assertTrue(map.get("foo") == null);
        map.put("foo", "FOO");
        assertTrue("FOO".equals(map.get("foo")));
        assertTrue(bean.getFoo().equals("FOO"));
        assertTrue("x".equals(map.get("bar")));
        assertTrue(((Integer)map.get("quick")).intValue() == 42);
        map.put("quud", new Integer(13));
        assertTrue(bean.getQuud() == 13);
    }

    /*
    public void testPerformance() throws Throwable {
        int iterations = 500000;
        System.out.println();
        System.out.println("iteration count: " + iterations);

        TestBean bean = new TestBean();
        bean.setFoo("herby");
        bean.setQuud(20);

        Map m1 = BeanMap.create(bean, null);
        Map m2 = new org.apache.commons.collections.BeanMap(bean);
        Integer wrapped = new Integer(30);
        
        long t1  = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            bean.getFoo();
        }
        long t2  = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            m1.get("foo");
        }
        long t3  = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            m2.get("foo");
        }
        long t4  = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            bean.getQuud();
        }
        long t5  = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            m1.get("quud");
        }
        long t6  = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            m2.get("quud");
        }
        long t7  = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            bean.setFoo("derby");
        }
        long t8  = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            m1.put("foo", "derby");
        }
        long t9  = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            m2.put("foo", "derby");
        }
        long t10 = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            bean.setQuud(30);
        }
        long t11 = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            m1.put("quud", wrapped);
        }
        long t12 = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            m2.put("quud", wrapped);
        }
        long t13 = System.currentTimeMillis();

        System.out.println("raw  get object    = " + (t2 - t1) 
                           + "\n" + "fast get object    = " + (t3 - t2) 
                           + "\n" + "slow get object = " + (t4 - t3)
                           + "\n" + "raw  get primitive = " + (t5 - t4)
                           + "\n" + "fast get primitive = " + (t6 - t5)
                           + "\n" + "slow get primitive = " + (t7 - t6)
                           + "\n" + "raw  put object    = " + (t8 - t7)
                           + "\n" + "fast put object    = " + (t9 - t8)
                           + "\n" + "slow put object    = " + (t10 - t9)
                           + "\n" + "raw  put primitive = " + (t11 - t10)
                           + "\n" + "fast put primitive = " + (t12 - t11)
                           + "\n" + "slow put primitive = " + (t13 - t12));
    }
    */

    public TestBeanMap(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestBeanMap.class);
    }
}
