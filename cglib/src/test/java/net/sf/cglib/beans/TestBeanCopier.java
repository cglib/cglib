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

import net.sf.cglib.core.Converter;
import java.lang.reflect.Method;
import junit.framework.*;

/**
 *
 * @author baliuka
 */
public class TestBeanCopier extends TestCase {

    public void testSimple() {
        BeanCopier copier = BeanCopier.create(MA.class, MA.class, false);
        MA bean1 = new MA();
        bean1.setIntP(42);
        MA bean2 = new MA();
        copier.copy(bean1, bean2, null);
        assertTrue(bean2.getIntP() == 42);
    }

    public void testConvert() {
        BeanCopier copier = BeanCopier.create(MA.class, MA.class, true);
        MA bean1 = new MA();
        bean1.setIntP(42);
        MA bean2 = new MA();
        copier.copy(bean1, bean2, new Converter() {
            public Object convert(Object value, Class target, Object context) {
                if (target.equals(Integer.TYPE)) {
                    return new Integer(((Number)value).intValue() + 1);
                }
                return value;
            }
        });
        assertTrue(bean2.getIntP() == 43);
    }
    
    public TestBeanCopier(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestBeanCopier.class);
    }
}
