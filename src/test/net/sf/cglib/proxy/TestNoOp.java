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
import java.lang.reflect.*;
import java.util.*;
import junit.framework.*;

public class TestNoOp extends CodeGenTestCase {
    private static class Foo {
        public Foo() { }
        public String toString() {
            return "foo";
        }
    }
    
    public void testNoOp() {
        Object obj = Enhancer.create(Foo.class, NoOp.INSTANCE);
        assertTrue("foo".equals(obj.toString()));
    }

    public TestNoOp(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestNoOp.class);
    }
    
    public void perform(ClassLoader loader) throws Throwable {
    }
    
    public void testFailOnMemoryLeak() throws Throwable {
    }
    
}
