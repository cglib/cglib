/*
 * Copyright 2003 The Apache Software Foundation
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

import java.lang.reflect.Method;
import junit.framework.*;

/**
 *
 * @author baliuka
 */
public class TestImmutableBean extends TestCase {

    public void testSimple() {
        MA bean = new MA();
        assertTrue(bean.getIntP() == 0);
        bean.setIntP(42);
        assertTrue(bean.getIntP() == 42);
        bean = (MA)ImmutableBean.create(bean);
        assertTrue(bean.getIntP() == 42);
        try {
            bean.setIntP(43);
            fail("expecting illegal state exception");
        } catch (IllegalStateException ignore) { }
    }
    
    public TestImmutableBean(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestImmutableBean.class);
    }
}
