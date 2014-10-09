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

import junit.framework.*;
import java.util.*;

/**
 * @author Chris Nokleberg <a href="mailto:chris@nokleberg.com">chris@nokleberg.com</a>
 * @version $Id: TestBeanMapProxy.java,v 1.3 2004/06/24 21:15:17 herbyderby Exp $
 */
public class TestBeanMapProxy extends net.sf.cglib.CodeGenTestCase {
    public void testBeanMap() throws Exception {
        HashMap identity = new HashMap() {}; // use anonymous class for correct class loader
        Person person = (Person)BeanMapProxy.newInstance(identity, new Class[]{ Person.class });
        person.setName("Chris");
        assertTrue("Chris".equals(person.getName()));
        assertTrue("Chris".equals(identity.get("Name")));
    }

   public interface Person {
        public String getName();
        public void setName(String name);
    }

    public TestBeanMapProxy(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestBeanMapProxy.class);
    }
    
    public void perform(ClassLoader loader) throws Throwable {
      //nothing to test at this time
    }
    
    public void testFailOnMemoryLeak() throws Throwable {
    }
    
}
