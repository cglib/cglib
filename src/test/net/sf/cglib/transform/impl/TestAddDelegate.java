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
public class TestAddDelegate extends AbstractTransformTest {
    
    /** Creates a new instance of TestAddDelegate */
    public TestAddDelegate(String name) {
        super(name);
    }
    
    public interface Interface {
    
        Object getDelegte();
        
        Object getTarget();
    
    }
    
    public void test(){
        
     Interface i = (Interface)this;
     assertEquals(i.getTarget(),this);
     
    }
    
    public static class ImplExclude implements Interface {
    
        private Object target;
        
        public ImplExclude(Object target){
          this.target = target;
        }
        
        public Object getDelegte() {
          return this;
        }        
    
        public Object getTarget(){
           return  target;  
        }
    }
    
    public TestAddDelegate() {
        super(null);
    }
    
    
    protected ClassTransformerFactory getTransformer() throws Exception {

        return new ClassTransformerFactory(){
        
               public ClassTransformer newInstance(){
          
                   return new AddDelegateTransformer(new Class[]{Interface.class} , ImplExclude.class );
                   
               }
        
        };
        
        
    }
    
    
    public static void main(String[] args) throws Exception{
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() throws Exception{
        
        return new TestSuite( new TestAddDelegate().transform() );
        
    }
    
}
