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

import java.beans.*;
import java.lang.reflect.Method;
import junit.framework.*;
import net.sf.cglib.core.ReflectUtils;

/**
 * @author Juozas Baliuka, Chris Nokleberg
 */
public class TestBeanGenerator extends TestCase {
    
    public void testSimple() throws Exception {
        BeanGenerator bg = new BeanGenerator();
        bg.addProperty("sin", Double.TYPE);
        Object bean = bg.create();
        
        PropertyDescriptor[] pds = ReflectUtils.getBeanProperties(bean.getClass());
        assertTrue(pds.length == 1);
        assertTrue(pds[0].getName().equals("sin"));
        assertTrue(pds[0].getPropertyType().equals(Double.TYPE));
    }

    public void testSuperclass() throws Exception {
        BeanGenerator bg = new BeanGenerator();
        bg.setSuperclass(MA.class);
        bg.addProperty("sin", Double.TYPE);
        Object bean = bg.create();

        assertTrue(bean instanceof MA);
        assertTrue(BeanMap.create(bean).keySet().contains("sin"));
    }

    public TestBeanGenerator(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestBeanGenerator.class);
    }
}
