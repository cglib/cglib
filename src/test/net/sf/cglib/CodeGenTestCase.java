/*
 * Copyright 2002,2003,2004 The Apache Software Foundation
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
package net.sf.cglib;

import junit.framework.*;

/**
 * @author Chris Nokleberg <a href="mailto:chris@nokleberg.com">chris@nokleberg.com</a>
 * @version $Id: CodeGenTestCase.java,v 1.6 2004/06/24 21:15:18 herbyderby Exp $
 */
abstract public class CodeGenTestCase extends TestCase {
    public CodeGenTestCase(String testName) {
        super(testName);
    }
    
    public   abstract void perform(ClassLoader loader)throws Throwable;
    
    
    public boolean leaks()throws Throwable{
        
        ClassLoader loader = new ClassLoader(this.getClass().getClassLoader()){};
        
        perform(loader);
        
        java.lang.ref.Reference ref = new java.lang.ref.WeakReference(loader);
        
        loader = null;
        java.util.List list = new  java.util.ArrayList();
        
        for(int i = 0; i < 512; i++  ){
            
            System.gc();
            
            if(ref.get() == null ){
             
                return false;
                
            }
            
          byte[] garbage  =  new byte[ (i + 1)*1004 ];
          list.add(garbage);  
        
          
        }
        
        return true;
        
        
    }
    
    
    
}

