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
import junit.framework.*;

/**
 * @version $Id: TestMethodProxy.java,v 1.10 2003/02/01 19:44:51 baliuka Exp $
 */
public class TestMethodProxy extends CodeGenTestCase {

    public interface StringMaker {
        Object newInstance(char[] buf, int offset, int count);
    }

    public void testTypeCheckedConstructor() throws Throwable {
        StringMaker maker = (StringMaker)ConstructorProxy.create(StringMaker.class, String.class);
        assertTrue("nil".equals(maker.newInstance("vanilla".toCharArray(), 2, 3)));
    }

    public void testSimple() throws Throwable {
        Class[] types = new Class[]{ Integer.TYPE, Integer.TYPE };
        Method substring = String.class.getDeclaredMethod("substring", types);
        MethodProxy proxy = MethodProxy.create(substring,substring);
        Object[] args = new Object[]{ new Integer(2), new Integer(4) };
        assertTrue("LI".equals(proxy.invokeSuper("CGLIB", args)));
    }

    public interface Substring {
        String substring(int start, int end);
    }

    public interface Substring2 {
        Object anyNameAllowed(int start, int end);
    }

    public interface IndexOf {
        int indexOf(String str, int fromIndex);
    }

    public void testFancy() throws Throwable {
        Substring delegate = (Substring)MethodDelegate.create("CGLIB", "substring", Substring.class);
        assertTrue("LI".equals(delegate.substring(2, 4)));
    }

    public void testFancyNames() throws Throwable {
        Substring2 delegate = (Substring2)MethodDelegate.create("CGLIB", "substring", Substring2.class);
        assertTrue("LI".equals(delegate.anyNameAllowed(2, 4)));
    }

    public void testFancyTypes() throws Throwable {
        String test = "abcabcabc";
        IndexOf delegate = (IndexOf)MethodDelegate.create(test, "indexOf", IndexOf.class);
        assertTrue(delegate.indexOf("ab", 1) == test.indexOf("ab", 1));
    }

    public void testEquals() throws Throwable {
        String test = "abc";
        MethodDelegate mc1 = MethodDelegate.create(test, "indexOf", IndexOf.class);
        MethodDelegate mc2 = MethodDelegate.create(test, "indexOf", IndexOf.class);
        MethodDelegate mc3 = MethodDelegate.create("other", "indexOf", IndexOf.class);
        MethodDelegate mc4 = MethodDelegate.create(test, "substring", Substring.class);
        MethodDelegate mc5 = MethodDelegate.create(test, "substring", Substring2.class);
        assertTrue(mc1.equals(mc2));
        assertTrue(!mc1.equals(mc3));
        assertTrue(!mc1.equals(mc4));
        assertTrue(mc4.equals(mc5));
    }

    public static interface MainDelegate {
        int main(String[] args);
    }

    public static class MainTest {
        public static int alternateMain(String[] args) {
            return 7;
        }
    }

    public void testStaticDelegate() throws Throwable {
        MainDelegate start = (MainDelegate)MethodDelegate.createStatic(MainTest.class,
                                                                       "alternateMain",
                                                                       MainDelegate.class);
        assertTrue(start.main(null) == 7);
    }

    public static interface Listener {
        public void onEvent();
    }

    public static class Publisher {
        public int test = 0;
        private MulticastDelegate event = MulticastDelegate.create(Listener.class);
        public void addListener(Listener listener) {
            event = event.add(listener);
        }
        public void removeListener(Listener listener) {
            event = event.remove(listener);
        }
        public void fireEvent() {
            ((Listener)event).onEvent();
        }
    }

    public void testPublisher() throws Throwable {
        final Publisher p = new Publisher();
        Listener l1 = new Listener() {
                public void onEvent() {
                    p.test++;
                }
            };
        p.addListener(l1);
        p.addListener(l1);
        p.fireEvent();
        assertTrue(p.test == 2);
        p.removeListener(l1);
        p.fireEvent(); 
        assertTrue(p.test == 3);
    }

    public static interface SuperSimple {
        public int execute();
    }

    public void testMulticastReturnValue() throws Throwable {
        SuperSimple ss1 = new SuperSimple() {
                public int execute() {
                    return 1;
                }
            };
        SuperSimple ss2 = new SuperSimple() {
                public int execute() {
                    return 2;
                }
            };
        MulticastDelegate multi = MulticastDelegate.create(SuperSimple.class);
        multi = multi.add(ss1);
        multi = multi.add(ss2);
        assertTrue(((SuperSimple)multi).execute() == 2);
        multi = multi.remove(ss1);
        multi = multi.add(ss1);
        assertTrue(((SuperSimple)multi).execute() == 1);
    }

    public void testMethodProxyPerformance() throws Throwable {
        int iterations = 500000;
        System.out.println();
        System.out.println("iteration count: " + iterations);

        String test = "abcabcabc";
        Object[] args;

        Class[] types = new Class[]{ String.class, Integer.TYPE };
        Method indexOf = String.class.getDeclaredMethod("indexOf", types);
        MethodProxy proxy = MethodProxy.create(indexOf,indexOf);
        args = new Object[]{ "ab", new Integer(1) };

        IndexOf fast = (IndexOf)MethodDelegate.create(test, "indexOf", IndexOf.class);
        
        int result;
        long t1  = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            args = new Object[]{ "ab", new Integer(1) };
            result = ((Integer)indexOf.invoke(test, args)).intValue();
        }
        long t2  = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            result = ((Integer)indexOf.invoke(test, args)).intValue();
        }
        long t3  = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            args = new Object[]{ "ab", new Integer(1) };
            result = ((Integer)proxy.invokeSuper(test, args)).intValue();
        }
        long t4  = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            result = ((Integer)proxy.invokeSuper(test, args)).intValue();
        }
        long t5  = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            result = fast.indexOf("ab", 1);
        }
        long t6  = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            result = test.indexOf("ab", 1);
        }
        long t7  = System.currentTimeMillis();

        System.out.println("reflect+args = " + (t2 - t1) 
                           + "\n" + "reflect      = " + (t3 - t2)
                           + "\n" + "proxy+args   = " + (t4 - t3)
                           + "\n" + "proxy        = " + (t5 - t4)
                           + "\n" + "fast         = " + (t6 - t5)
                           + "\n" + "raw          = " + (t7 - t6));
    }
    
    public TestMethodProxy(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestMethodProxy.class);
    }

}
