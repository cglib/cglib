/*
 * Copyright 2004 The Apache Software Foundation
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
package net.sf.cglib.transform.impl;

import net.sf.cglib.proxy.*;
import net.sf.cglib.transform.*;
import junit.framework.*;
import org.objectweb.asm.*;

/**
 * @author Chris Nokleberg
 */
public class TestEnhancerTransform extends AbstractTransformTest {
    
    public TestEnhancerTransform() {
    }
    
    public TestEnhancerTransform(String name) {
        super(name);
    }

    public void test() {
        assertTrue(foo() == 0);

        Enhancer.registerCallbacks(getClass(), new Callback[]{new MethodInterceptor() {
            public Object intercept(Object obj, java.lang.reflect.Method method, Object[] args, MethodProxy proxy) throws Throwable {
                assertTrue(new Integer(0).equals(proxy.invokeSuper(obj, args)));
                return new Integer(1);
            }
        }});
        TestEnhancerTransform another = new TestEnhancerTransform();
        assertTrue(another.foo() == 1);
        
        TestEnhancerTransform proxied =
            (TestEnhancerTransform)Enhancer.create(getClass(), new MethodInterceptor() {
                public Object intercept(Object obj, java.lang.reflect.Method method, Object[] args, MethodProxy proxy) throws Throwable {
                    assertTrue(new Integer(1).equals(proxy.invokeSuper(obj, args)));
                    return new Integer(2);
                }
            });
        assertTrue(proxied.foo() == 2);
    }

    public int foo() {
        return 0;
    }

    protected ClassTransformerFactory getTransformer() throws Exception {
        Enhancer e = new Enhancer();
        e.setUseFactory(false);
        e.setCallbackType(MethodInterceptor.class);
        final ClassTransformer t = new ClassFilterTransformer(new ClassFilter() {
            public boolean accept(String className) {
                return className.equals("net.sf.cglib.transform.impl.TestEnhancerTransform");
            }
        }, e.createTransformer());
        return new ClassTransformerFactory() {
            public ClassTransformer newInstance() {
                return t;
            }
        };
    }

    public static void main(String[] args) throws Exception{
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() throws Exception{
        try {
            return new TestSuite(new TestEnhancerTransform().transform());
        } catch (Error e) {
            e.printStackTrace(System.err);
            throw e;
        }
    }
}
