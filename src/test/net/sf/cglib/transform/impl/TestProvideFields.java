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

import net.sf.cglib.transform.*;
import junit.framework.*;

/**
 *
 * @author  baliuka
 */
public class TestProvideFields extends AbstractTransformTest{
    
    String field = "test";
    
    /** Creates a new instance of TestProvideFields */
    public TestProvideFields() {
    }
    
    /** Creates a new instance of TestProvideFields */
    public TestProvideFields(String name) {
        super(name);
    }
    
    
    public void test(){
    
       FieldProvider provider = (FieldProvider)this;
       assertEquals(field,provider.getField("field"));
       String value = "tst2";
       provider.setField("field", value );
       assertEquals(field,value);
       
       
        
    }
    
    protected ClassTransformerFactory getTransformer() throws Exception {
       
        return new ClassTransformerFactory (){
        
           public ClassTransformer newInstance(){
       
                return   new FieldProviderTransformer();  
           }
        }; 
    }
    
    
    public static void main(String[] args) throws Exception{
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() throws Exception{
        return new TestSuite( new TestProvideFields( ).transform() );
    }
    

    
    
}
