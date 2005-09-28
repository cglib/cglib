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

import java.io.*;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

import net.sf.cglib.core.KeyFactory;
import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.*;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.reflect.FastClass;

import junit.framework.*;

/**
 * @author Chris Nokleberg <a href="mailto:chris@nokleberg.com">chris@nokleberg.com</a>
 * @version $Id: CodeGenTestCase.java,v 1.9 2005/09/28 09:37:32 baliuka Exp $
 */
abstract public class CodeGenTestCase extends TestCase {
    public CodeGenTestCase(String testName) {
        super(testName);
    }
    
    public   abstract void perform(ClassLoader loader)throws Throwable;
    
    
    public boolean leaks()throws Throwable{
        
        List classPath = new ArrayList();
        
        for( StringTokenizer tokenizer = new StringTokenizer(System.getProperty("java.class.path"),File.pathSeparator); tokenizer.hasMoreElements();  ){
            
            classPath.add( new File(tokenizer.nextToken()).toURL() );
            
        }
        
        
        final Set coreClasses = new HashSet();
        coreClasses.add(Factory.class.getName());
        coreClasses.add(Callback.class.getName());
        coreClasses.add(MethodInterceptor.class.getName());
        coreClasses.add(Mixin.class.getName());
        coreClasses.add(KeyFactory.class.getName());
        coreClasses.add(FastClass.class.getName());
        coreClasses.add(FastClass.Generator.class.getName());
        coreClasses.add(Signature.class.getName());
        
        
        
        
        
        
        URLClassLoader loader = new URLClassLoader((URL[]) classPath.toArray(new URL[classPath.size()])){
            
            public Class loadClass(String name) throws ClassNotFoundException {
                
                String res = name.replace('.','/') + ".class";
                
                if(name.startsWith("java") || name.startsWith("org.objectweb.asm") || name.startsWith("sun.")){
                    return super.loadClass(name);
                }
                
                
                
                if( coreClasses.contains(name) ){
                    return super.loadClass(name);
                }
                
                
                try{
                    
                    
                    InputStream is = getResourceAsStream(res);
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    try{
                        int b;
                        while( (b = is.read()) != -1 ){
                            bout.write((byte)b);
                        }
                        
                    }finally{
                        is.close();     
                    }
                    byte data [] = bout.toByteArray();
                    return defineClass(name,data,0,data.length );
                }catch(Exception e){
                    throw new ClassNotFoundException( name + ":" + e.toString());
                }
            }
            
        };
        
        perform(loader);
        
        java.lang.ref.Reference ref = new java.lang.ref.WeakReference(loader);
        
        loader = null;
        java.util.List list = new  java.util.ArrayList();
        
        for(int i = 0; i < 512; i++  ){
            
            System.gc();
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

