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
import net.sf.cglib.reflect.*;
import net.sf.cglib.util.*;
import junit.framework.*;

public class TestFastClass extends CodeGenTestCase {
    public static class Simple {
    }
    
    public void testSimple() throws Throwable {
        FastClass.create(Simple.class).newInstance();
    }

    public void testComplex() throws Throwable {
        FastClass fc = FastClass.create(MemberSwitchBean.class);
        MemberSwitchBean bean = (MemberSwitchBean)fc.newInstance();
        assertTrue(bean.init == 0);
        assertTrue(fc.getName().equals("net.sf.cglib.MemberSwitchBean"));
        assertTrue(fc.getJavaClass() == MemberSwitchBean.class);

        Constructor c1 = MemberSwitchBean.class.getConstructor(new Class[0]);
        FastConstructor fc1 = fc.getConstructor(c1);
        assertTrue(((MemberSwitchBean)fc1.newInstance()).init == 0);
        assertTrue(fc1.toString().equals("public net.sf.cglib.MemberSwitchBean()"));

        Method m1 = MemberSwitchBean.class.getMethod("foo", new Class[]{ Integer.TYPE, String.class });
        assertTrue(fc.getMethod(m1).invoke(bean, new Object[]{ new Integer(0), "" }).equals(new Integer(6)));

        // TODO: should null be allowed here?
        Method m2 = MemberSwitchBean.class.getDeclaredMethod("pkg", null);
        assertTrue(fc.getMethod(m2).invoke(bean, null).equals(new Integer(9)));
    }

    /*
    public void testPerformance() throws Throwable {
        int iterations = 500000;
        System.out.println();
        System.out.println("iteration count: " + iterations);

        String test = "abcabcabc";
        Object[] args;

        Class[] types = new Class[]{ String.class, Integer.TYPE };
        Method indexOf = String.class.getDeclaredMethod("indexOf", types);
        Reflect proxy = Reflect.create(indexOf,indexOf);
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
    */
    
    public TestFastClass(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestFastClass.class);
    }
}
