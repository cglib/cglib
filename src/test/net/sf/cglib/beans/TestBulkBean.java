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
package net.sf.cglib.beans;

import java.lang.reflect.Method;
import junit.framework.*;

/**
 *
 * @author baliuka
 */
public class TestBulkBean extends TestCase {
    private String getters[] = {
        "getIntP",
        "getLongP",
        "getByteP",
        "getShortP",
        "getFloatP",
        "isBooleanP",
        "getCharP",
        "getDoubleP",
        "getStringP",
        "getId",
        "getName",
        "getPrivateName"
    } ;
                           
    private String setters[] = {
        "setIntP",
        "setLongP",
        "setByteP",
        "setShortP",
        "setFloatP",
        "setBooleanP",
        "setCharP",
        "setDoubleP",
        "setStringP",
        "setId",
        "setName",
        "setPrivateName"
    };
                           
    private Class types[] = {
        int.class,
        long.class,
        byte.class,
        short.class,
        float.class,
        boolean.class,
        char.class,
        double.class,
        String.class,
        Long.class,
        String.class,
        String.class
    };
                           
    private Object values[] = {
        new Integer(2) ,
        new Long(4) ,
        new Byte((byte)8),
        new Short((short)4),
        new Float(1.2),
        Boolean.TRUE,
        new Character('S'),
        new Double(5.6),
        "test",
        new Long(88),
        "test2",
        "private"
    };
       
    
    
    public TestBulkBean(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestBulkBean.class);
    }
    
    /** Test of create method, of class net.sf.cglib.BulkBean. */
    public void testGetInstance() throws Throwable {
        BulkBean mClass = BulkBean.create(MA.class, getters, setters, types);
      
        MA bean = new MA();
      
        mClass.setPropertyValues( bean, values );
        Object values1[] = mClass.getPropertyValues( bean );
      
        for( int i = 0; i < types.length; i++  ){
            assertEquals(" property " + getters[i] + "/" + setters[i] , values[i] , values1[i] );
        }  
    }

    public void testEmpty() throws Throwable {
        BulkBean.create(MA.class, new String[0], new String[0], new Class[0]);
    }

    public void testBadTypes() throws Throwable {
        Class[] types2 = (Class[])types.clone();
        types2[2] = String.class;
        try {
            BulkBean.create(MA.class, getters, setters, types2);
            fail("expected exception");
        } catch (BulkBeanException e) {
            assertTrue(e.getIndex() == 2);
        }
    }

    public void testMismatchedLengths() throws Throwable {
        try {
            BulkBean.create(MA.class, getters, setters, new Class[0]);
            fail("expected exception");
        } catch (BulkBeanException e) {
            assertTrue(e.getIndex() == -1);
        }
    }

    public void testMissingProperty() throws Throwable {
        String[] getters2 = (String[])getters.clone();
        getters2[3] = "getChris";
        try {
            BulkBean.create(MA.class, getters2, setters, types);
            fail("expected exception");
        } catch (BulkBeanException e) {
            assertTrue(e.getIndex() == 3);
        }
    }

    public void testSetWrongType() throws Throwable {
        BulkBean mClass = BulkBean.create(MA.class, getters, setters, types);
        MA bean = new MA();
        Object[] values2 = (Object[])values.clone();
        values2[4] = new Object();
        try {
            mClass.setPropertyValues(bean, values2);
            fail("expected exception");
        } catch (BulkBeanException e) {
            assertTrue(e.getIndex() == 4);
        }
    }

    public void testBulkBeanPerformance() throws Throwable{
    
        int iterations = 100000;
       
        System.out.println(); 
        System.out.println("iteration count: " + iterations);
        System.out.println(); 
       
        BulkBean mClass = new BulkBeanReflectImpl( MA.class,getters,setters,types );
       
        System.out.println( mClass.getClass().getName() + ": " );
        int b = performanceTest( mClass, iterations );
        System.out.println( b + " ms.   "  + ( b/(float)iterations)  + " per iteration" );
        System.out.println(); 
       
       
        mClass = BulkBean.create(MA.class, getters, setters, types);
                                       
      
        System.out.println( mClass.getClass().getName() + ": " );
        int a = performanceTest( mClass, iterations );
        System.out.println( a + " ms.   " + ( a/(float)iterations)  + " per iteration" );
       
       
        System.out.println( "factor: " + b/(float)a );
    }
    
    public int performanceTest( BulkBean mc, int iterations ) throws Throwable{
       
         
         
        long start = System.currentTimeMillis();
        for( int i = 0; i< iterations; i++   ){
            MA bean = new MA(); // (MA)mc.newInstance();
            mc.setPropertyValues( bean, values );
            mc.getPropertyValues( bean, values );
        }
       
        return (int)( System.currentTimeMillis() - start );
    }
    
    /** Generated implementation of abstract class net.sf.cglib.BulkBean. Please fill dummy bodies of generated methods. */
    private static class BulkBeanReflectImpl extends BulkBean {
        
        private   Method gets[];
        private   Method sets[];
        private   int size ;    
        public BulkBeanReflectImpl(Class target, String[] getters, String[] setters, Class[] types) {
            this.target = target;
            this.types = types;
            this.getters = getters;
            this.setters = setters;

            size = this.types.length;        
            gets = new Method [size];
            sets = new Method [size];
               
            try{ 
               
                for( int i = 0; i< size; i++  ) {
                
                    if( getters[i] != null ){   
                        gets[i] =  target.getDeclaredMethod( getters[i], new Class[]{} );
                        gets[i].setAccessible(true);
                    } 
                    if( setters[i] != null ) {  
                        sets[i] =  target.getDeclaredMethod( setters[i], new Class[]{ types[i] } );
                        sets[i].setAccessible(true);
                    } 
               
              
                }
            }catch( Exception e ){
                throw new Error(e.getClass().getName() + ":" +  e.getMessage() );
            } 
        }
        
        public void getPropertyValues(Object bean, Object[] values) {
            
            try{
                for( int i = 0; i < size ; i++  ){
                    if( this.gets[i] != null ){
                        values[i] = gets[i].invoke(bean, null );             
                    }
                }
            }catch( Exception e ){
                throw new Error( e.getMessage() );     
            } 
        }
        
        public void setPropertyValues(Object bean, Object[] values) {
            try{
           
                for( int i = 0; i < size ; i++  ){
                    if( this.sets[i] != null ){
                        sets[i].invoke(bean, new Object[]{ values[i] } );             
                    }
                }
           
           
            }catch( Exception e ){
                e.printStackTrace();
                throw new Error( e.getMessage() );     
            }
        }
        
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    
}
