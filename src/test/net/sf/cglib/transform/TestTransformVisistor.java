/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package net.sf.cglib.transform;

import net.sf.cglib.*;
import org.objectweb.asm.util.*;
import org.objectweb.asm.*;

import junit.framework.*;
import java.util.*;

/**
 * @author baliuka
 * @version $Id: TestTransformVisistor.java,v 1.2 2003/09/12 05:17:51 baliuka Exp $
 */
public class TestTransformVisistor extends TestCase {

   ReadWriteFieldFilter acceptAll = new ReadWriteFieldFilter(){
    public boolean acceptRead(String clas, String name){
     
      return true;
    }
    
     public boolean acceptWrite(String clas, String name){
     
      return true;
    }
   };
    
 static class  TransforClassLoader extends ClassLoader{
 
     byte data[];
     String name;
     
     TransforClassLoader(byte data[],String name){
       this.data = data;
       this.name = name;
     }
     public Class loadClass(String name)throws ClassNotFoundException{
      if(this.name.equals(name)){   
        return super.defineClass(name,data,0,data.length);  
      }else{
        return super.loadClass(name);  
      }
     }
 
 } 
    
    public TestTransformVisistor(String testName) {
        super(testName);
    }

     void print(byte data[]){
     
        ClassReader cr = new ClassReader(data);
        cr.accept(new TraceClassVisitor(null,new java.io.PrintWriter(System.out)), false);
      
     }
    void print(java.io.InputStream is)throws Exception{
     
        ClassReader cr = new ClassReader(is);
        cr.accept(new TraceClassVisitor(null,new java.io.PrintWriter(System.out)), false);
      
     } 
   
    public void testFormat( )throws Exception{
        
        formatTest(MA.class);  
       
    }
    public void formatTest( Class cls )throws Exception{
    
        
        java.io.InputStream is = this.getClass().getResourceAsStream( "/" + cls.getName().replace('.','/') + ".class");
        
        TransformClassVisitor tv = new TransformClassVisitor(is,acceptAll);
        byte data[] = tv.transform();
      
        TransforClassLoader loader = new TransforClassLoader(data,cls.getName());
        Class transformed = loader.loadClass(cls.getName());
        Object obj = transformed.newInstance();
        
        
      
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestTransformVisistor.class);
    }
    
}
