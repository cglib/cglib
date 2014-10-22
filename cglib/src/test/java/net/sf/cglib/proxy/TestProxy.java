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
package net.sf.cglib.proxy;

import net.sf.cglib.CodeGenTestCase;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.proxysample.ProxySampleInterface_ReturnsBasic;

import junit.framework.*;

/**
 * @author Chris Nokleberg <a href="mailto:chris@nokleberg.com">chris@nokleberg.com</a>
 * @version $Id: TestProxy.java,v 1.6 2012/07/27 16:02:49 baliuka Exp $
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

    public void testEquals() throws Exception {
        final Object k1 = new Object();
        final Object k2 = new Object();
        InvocationHandler handler = new InvocationHandler() {
            public Object invoke(Object o, Method method, Object[] args) throws Exception {
                if (method.getName().equals("equals")) {
                    return (args[0] == k1) ? Boolean.TRUE : Boolean.FALSE;
                }
                return null;
            }
        };
        Object proxy = Proxy.newProxyInstance(TestProxy.class.getClassLoader(), new Class[] { Map.class }, handler);
        assertTrue(proxy.equals(k1));
        assertTrue(!proxy.equals(k2));
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
    
    public void perform(ClassLoader loader) throws Throwable {
         InvocationHandler handler = new InvocationHandler() {
            public Object invoke(Object o, Method method, Object[] args) throws Exception {
                throw new Exception("test!");
            }
        };
        Proxy.newProxyInstance(loader, new Class[] { Map.class }, handler);
       
    }
    
   
    
}
