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
import java.util.*;
import net.sf.cglib.core.*;

/**
 *
 * @author  baliuka
 */
public class TestAddClassInit extends AbstractTransformTest{
    
   static Class registred;
   
   static int i = 0;
   
   static {  
    
       i = 11;  
   
   }
    
   public static void register(Class cls){
    
        registred = cls;
    
        
    }
   
   
    public TestAddClassInit(){  }
    
    public void testInitTransform(){
        assertEquals(i,11);   
    }
   
    public void testRegistred(){
        
       
        assertNotNull(registred);
        
    
    }
    
    public TestAddClassInit(String s){
       super(s);
    }
    
    protected ClassTransformerFactory getTransformer() throws Exception{
        
       return new ClassTransformerFactory (){
        
           public ClassTransformer newInstance(){
             try{  
               return new   AddStaticInitTransformer(
                 TestAddClassInit.class.
                        getMethod("register",new Class[]{Class.class}) 
               );          
             }catch(Exception e){
               throw new CodeGenerationException(e);
             }          
        }   
       };
        
    }
    
    public static void main(String[] args) throws Exception{
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() throws Exception{
        return new TestSuite( new TestAddClassInit( ).transform() );
    }
    
    
}
