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
package net.sf.cglib.transform.impl;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Juozas
 *
 */
public class TestInterceptFieldsSubclass extends TestInterceptFields {

    private boolean readTest = false;
    private boolean writeTest = false;
    
    public TestInterceptFieldsSubclass() {
        super();
        
    }
    public TestInterceptFieldsSubclass(String name) {
        super(name);
      
    }
    public void testSubClass(){
        super.test();
        assertTrue( "super class read field", readTest  );
        assertTrue( "super class write field", readTest  );
    }
    public Object readObject(Object _this, String name, Object oldValue) {
       if(name.equals("field")){
           readTest = true;
       }
       return super.readObject(_this, name, oldValue);
    }
    
    public Object writeObject(Object _this, String name, Object oldValue,
            Object newValue) {
      
        if(name.equals("field")){
            writeTest = true;
        }
       
        return super.writeObject(_this, name, oldValue, newValue);
    }
    
    public static void main(String[] args) throws Exception{
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() throws Exception{
        return new TestSuite( new TestInterceptFieldsSubclass( ).transform() );
    }
}

