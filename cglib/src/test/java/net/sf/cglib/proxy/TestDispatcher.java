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
import java.beans.*;
import java.lang.reflect.Method;
import java.util.*;
import junit.framework.*;

/**
 * @author Chris Nokleberg
 * @version $Id: TestDispatcher.java,v 1.6 2004/06/24 21:15:17 herbyderby Exp $
 */
public class TestDispatcher extends CodeGenTestCase {
    interface Foo {
        String foo();
    }

    interface Bar {
        String bar();
    }
    
    public void testSimple() throws Exception {
        final Object[] impls = new Object[]{
            new Foo() {
                public String foo() {
                    return "foo1";
                }
            },
            new Bar() {
                public String bar() {
                    return "bar1";
                }
            }
        };

        Callback[] callbacks = new Callback[]{
            new Dispatcher() {
                public Object loadObject() {
                    return impls[0];
                }
            },
            new Dispatcher() {
                public Object loadObject() {
                    return impls[1];
                }
            }
        };

        Enhancer e = new Enhancer();
        e.setInterfaces(new Class[]{ Foo.class, Bar.class });
        e.setCallbacks(callbacks);
        e.setCallbackFilter(new CallbackFilter() {
            public int accept(Method method) {
                return (method.getDeclaringClass().equals(Foo.class)) ? 0 : 1;
            }
        });
        Object obj = e.create();
        
        assertTrue(((Foo)obj).foo().equals("foo1"));
        assertTrue(((Bar)obj).bar().equals("bar1"));

        impls[0] = new Foo() {
            public String foo() {
                return "foo2";
            }
        };
        assertTrue(((Foo)obj).foo().equals("foo2"));
    }

    public TestDispatcher(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestDispatcher.class);
    }
    
    public void perform(ClassLoader loader) throws Throwable {
    }
    
    public void testFailOnMemoryLeak() throws Throwable {
    }
    
}
