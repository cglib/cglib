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
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.proxysample.ProxySampleInterface_ReturnsBasic;

import junit.framework.*;

/**
 * @author Chris Nokleberg <a href="mailto:chris@nokleberg.com">chris@nokleberg.com</a>
 * @version $Id: TestProxy.java,v 1.1 2003/01/29 16:51:12 nemecec Exp $
 */
public class TestProxy extends CodeGenTestCase {

    private class SimpleInvocationHandler implements InvocationHandler {
        Object o = null;
        public SimpleInvocationHandler(Object o) {
            this.o = o;
        }
        public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
            System.out.println("invoking " + m + " on " + o + " with " + args);
            Object r = m.invoke(o, args);
            System.out.println("done: " + m + " on " + o + " with " + args + ", result is " + r);
            return r; 
        }
     }

    public void testGetProxyClassAndConstructor() throws Exception {
        HashMap map = new HashMap();
        map.put("test", "test");
        InvocationHandler handler = new SimpleInvocationHandler(map);
        Class proxyClass = Proxy.getProxyClass(TestProxy.class.getClassLoader(), new Class[] { Map.class });
        Map proxyMap = (Map) proxyClass.getConstructor(new Class[] { InvocationHandler.class }).
            newInstance(new Object[] { handler });
        assertEquals("proxy delegation not correct", 
                            map.get("test"), proxyMap.get("test"));
    }

    public void testGetProxyInstance() throws Exception {
        HashMap map = new HashMap();
        map.put("test", "test");
        InvocationHandler handler = new SimpleInvocationHandler(map);
        Map proxyMap = (Map) Proxy.newProxyInstance(TestProxy.class.getClassLoader(), new Class[] { Map.class }, handler);
        assertEquals("proxy delegation not correct", map.get("test"), proxyMap.get("test"));
    }

    public void testIsProxyClass() throws Exception {
        HashMap map = new HashMap();
        map.put("test", "test");
        InvocationHandler handler = new SimpleInvocationHandler(map);
        Map proxyMap = (Map) Proxy.newProxyInstance(TestProxy.class.getClassLoader(), new Class[] { Map.class }, handler);
        assertTrue("real proxy not accepted", Proxy.isProxyClass(proxyMap.getClass()));
    }

    private class FakeProxy extends Proxy {
        public FakeProxy(InvocationHandler ih) {
            super(ih);
        }
    }

    public void testIsNotProxyClass() throws Exception {
        assertTrue("fake proxy accepted as real", 
                        !Proxy.isProxyClass(FakeProxy.class));
    }

    private static class ReturnNullHandler implements InvocationHandler {
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return null;
        }
    }

    public void testReturnNull() throws Exception {
        System.err.println("hello");
        ProxySampleInterface_ReturnsBasic rb =
            (ProxySampleInterface_ReturnsBasic)
            Proxy.newProxyInstance(null,
                                   new Class[]{ ProxySampleInterface_ReturnsBasic.class },
                                   new ReturnNullHandler());
        try {
            int result = rb.getKala(11);
            fail("must throw an exception, but returned " + result);
        } catch (NullPointerException ignore) { }
    }

    public void testGetInvocationHandler() throws Exception {
        HashMap map = new HashMap();
        map.put("test", "test");
        InvocationHandler handler = new InvocationHandler() {
            public Object invoke(Object o, Method method, Object[] args) throws Exception {
                throw new Exception("test!");
            }
        };
        Map proxyMap = (Map) Proxy.newProxyInstance(TestProxy.class.getClassLoader(), new Class[] { Map.class }, handler);
        assertSame("should be the same handler", handler, Proxy.getInvocationHandler(proxyMap));
    }

    public void testException() throws Exception {
        HashMap map = new HashMap();
        map.put("test", "test");
        InvocationHandler handler = new InvocationHandler() {
            public Object invoke(Object o, Method method, Object[] args) throws Exception {
                throw new Exception("test!");
            }
        };
        Map proxyMap = (Map) Proxy.newProxyInstance(TestProxy.class.getClassLoader(), new Class[] { Map.class }, handler);
        try {
            proxyMap.get("test"); //should throw exception
            fail("proxy exception handling not correct, should throw exception");
        } catch (UndeclaredThrowableException e) {
            System.out.println("exception: " + e);
        } catch (Exception e) {
            fail("proxy exception handling not correct, threw wrong exception: " + e);
        }
    }

    public TestProxy(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestProxy.class);
    }
}
