/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * 4. The names "Apache Cocoon" and "Apache Software Foundation" must
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

package net.sf.cglib.proxy;

import junit.framework.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import java.beans.*;
import java.util.*;

/**
 *
 * @author baliuka
 */
public class TestBeanGenerator extends TestCase {
    
    public TestBeanGenerator(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(TestBeanGenerator.class);
        return suite;
    }
    
    
    /** Test of addMethod method, of class net.sf.cglib.proxy.BeanGenerator. */
    public void testAddMethod() throws Exception{
       
        final String sinus = "sin";
        
       BeanGenerator bg = new BeanGenerator();
       bg.addMethod( Math.class.getMethod(sinus,
                                    new Class[]{ double.class }), null );
       Object bean = bg.getBeanClass().newInstance();
       java.lang.reflect.Method sin = 
            bean.getClass().getMethod( sinus, new Class[]{ double.class } );
            
       assertEquals( " bean method " + sinus, new Double(0), 
                                  sin.invoke(bean,new Object[]{new Double(0)}) );
       
    }
    
    
    /** Test of addProperty method, of class net.sf.cglib.proxy.BeanGenerator. */
    public void testAddProperty()throws Exception {
      
      final String name = "test";
        
       BeanGenerator bg = new BeanGenerator();
       bg.addProperty( name , String.class, Collections.singletonMap("name","value") );
       
       Object bean = bg.getBeanClass().newInstance();
       
       java.beans.BeanInfo info = java.beans.Introspector.getBeanInfo( bean.getClass() );
       java.beans.PropertyDescriptor descriptors [] =  info.getPropertyDescriptors();
       java.beans.PropertyDescriptor  descriptor = null;
       
       for( int i = 0; i< descriptors.length; i++ ){
           if( name.equals(descriptors[i].getName()) ){
             descriptor = descriptors[i]; 
           }
       }
       
       assertTrue( name + " property ",
                               descriptor != null );
       assertEquals( name + " property type ",
                               descriptor.getPropertyType(), String.class  );
       assertEquals( name + " attribute ", descriptor.getValue("name") , "value");
    
       final String value = name + "value";
       
       descriptor.getWriteMethod().invoke( bean, new Object[]{ value }  );
       
       
       assertEquals( name + " value", value , 
                            descriptor.getReadMethod().invoke( bean, null ) );
        
    }
    
    
}
