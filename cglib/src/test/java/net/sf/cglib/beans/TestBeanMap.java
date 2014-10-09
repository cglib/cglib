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
package net.sf.cglib.beans;

import net.sf.cglib.proxy.*;
import net.sf.cglib.core.Constants;
import net.sf.cglib.core.ReflectUtils;
import java.lang.reflect.Method;
import java.util.*;
import junit.framework.*;

public class TestBeanMap extends net.sf.cglib.CodeGenTestCase {
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
        BeanMap map = BeanMap.create(bean);
        assertTrue(map.size() == 6);
        assertTrue(map.get("foo") == null);
        map.put("foo", "FOO");
        assertTrue("FOO".equals(map.get("foo")));
        assertTrue(bean.getFoo().equals("FOO"));
        assertTrue("x".equals(map.get("bar")));
        assertTrue(((Integer)map.get("quick")).intValue() == 42);
        map.put("quud", new Integer(13));
        assertTrue(bean.getQuud() == 13);

        assertTrue(map.getPropertyType("foo").equals(String.class));
        assertTrue(map.getPropertyType("quud").equals(Integer.TYPE));
        assertTrue(map.getPropertyType("kdkkj") == null);
    }

    public void testEntrySet() {
        TestBean bean = new TestBean();
        BeanMap map = BeanMap.create(bean);
        assertTrue(map.entrySet().size() == map.size());
    }

    public void testNoUnderlyingBean() {
        BeanMap.Generator gen = new BeanMap.Generator();
        gen.setBeanClass(TestBean.class);
        BeanMap map = gen.create();

        TestBean bean = new TestBean();
        assertTrue(bean.getFoo() == null);
        assertTrue(map.put(bean, "foo", "FOO") == null);
        assertTrue(bean.getFoo().equals("FOO"));
        assertTrue(map.get(bean, "foo").equals("FOO"));
    }

    public void testMixinMapIntoBean() {
        Object bean = new TestBean();
        bean = mixinMapIntoBean(bean);
        ((TestBean)bean).setFoo("hello");
        assertTrue(bean instanceof Map);
        assertTrue(((Map)bean).get("foo").equals("hello"));
    }

    public void testRequire() {
        BeanMap.Generator gen = new BeanMap.Generator();
        gen.setBeanClass(TestBean.class);
        gen.setRequire(BeanMap.REQUIRE_GETTER);
        BeanMap map = gen.create();
        assertTrue(map.containsKey("foo"));
        assertTrue(map.containsKey("bar"));
        assertTrue(!map.containsKey("baz"));
    }

    public static Object mixinMapIntoBean(final Object bean) {
        Enhancer e = new Enhancer();
        e.setSuperclass(bean.getClass());
        e.setInterfaces(new Class[]{ Map.class });
        final Map map = BeanMap.create(bean);
        e.setCallbackFilter(new CallbackFilter() {
            public int accept(Method method) {
                return method.getDeclaringClass().equals(Map.class) ? 1 : 0;
            }
        });
        e.setCallbacks(new Callback[]{
            new Dispatcher() {
                public Object loadObject() {
                    return bean;
                }
            },
            new Dispatcher() {
                public Object loadObject() {
                    return map;
                }
            }
        });
        return e.create();
    }    

    // TODO: test different package
    // TODO: test change bean instance
    // TODO: test toString

    public TestBeanMap(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestBeanMap.class);
    }
    
    public void perform(ClassLoader loader) throws Throwable {
        //tested in enhancer test unit
    }
    
    public void testFailOnMemoryLeak() throws Throwable {
    }
    
}
