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


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.lang.reflect.*;
import java.io.*;


/**
 *@author     Juozas Baliuka <a href="mailto:baliuka@mwm.lt">
 *      baliuka@mwm.lt</a>
 *@version    $Id: TestEnhancer.java,v 1.12 2002/09/27 15:50:06 baliuka Exp $
 */
public class TestEnhancer extends TestCase {
    
    private static final MethodInterceptor NOOP_INTERCEPTOR = new NoOpInterceptor();
    
    private static final Class [] EMPTY_ARG = new Class[]{};
    
    private boolean invokedProtectedMethod = false;
    
    private boolean invokedPackageMethod   = false;
    
    private boolean invokedAbstractMethod  = false;
    
    public TestEnhancer(String testName) {
        super(testName);
    }
    
    
    
    public static Test suite() {
        return new TestSuite(TestEnhancer.class);
    }
    
    public static void main(String args[]) {
        String[] testCaseName = {TestEnhancer.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }
    
    
    public void testEnhance()throws Throwable{
        
        
        java.util.Vector vector1 = (java.util.Vector)Enhancer.enhance(
        java.util.Vector.class,
        new Class[]{java.util.List.class}, NOOP_INTERCEPTOR );
        
        java.util.Vector vector2  = (java.util.Vector)Enhancer.enhance(
        java.util.Vector.class,
        new Class[]{java.util.List.class}, NOOP_INTERCEPTOR );
        
        
        
        
        assertTrue("Cache failed",vector1.getClass() == vector2.getClass());
        
    }
    
    
    public void testMethods()throws Throwable{
        
        MethodInterceptor interceptor =
        new NoOpInterceptor(){
            
            public Object afterReturn(  Object obj, Method method,
            Object args[],
            boolean invokedSuper, Object retValFromSuper,
            java.lang.Throwable e )throws java.lang.Throwable{
                
                int mod =  method.getModifiers();
                
                if( Modifier.isProtected( mod ) ){
                    invokedProtectedMethod = true;
                }
                
                if( Modifier.isAbstract(mod) ){
                    invokedAbstractMethod = true;
                }
                
                
                if( ! ( Modifier.isProtected( mod ) || Modifier.isPublic( mod ) )){
                    invokedPackageMethod = true;
                }
                
                return retValFromSuper;//return the same as supper
            }
            
        };
        
        
        Source source =  (Source)Enhancer.enhance(
        Source.class,
        null,interceptor );
        
        source.callAll();
        assertTrue("protected", invokedProtectedMethod );
        assertTrue("package", invokedPackageMethod );
        assertTrue("abstract", invokedAbstractMethod );
    }
    
    public void testEnhanced()throws Throwable{
        
        Source source =  (Source)Enhancer.enhance(
        Source.class,
        null, NOOP_INTERCEPTOR );
        
        
        TestCase.assertTrue("enhance", Source.class != source.getClass() );
        
    }
    
    public void testTypes()throws Throwable{
        
        Source source =  (Source)Enhancer.enhance(
        Source.class,
        null, NOOP_INTERCEPTOR );
        
        
        
        TestCase.assertTrue("intType",   1   == source.intType(1));
        TestCase.assertTrue("longType",  1L  == source.longType(1L));
        TestCase.assertTrue("floatType", 1.1f  == source.floatType(1.1f));
        TestCase.assertTrue("doubleType",1.1 == source.doubleType(1.1));
        TestCase.assertEquals("objectType","1", source.objectType("1") );
        
        
        
    }
    
    
    public void testModifiers()throws Throwable{
        
        Source source =  (Source)Enhancer.enhance(
        Source.class,
        null, NOOP_INTERCEPTOR );
        
        Class enhancedClass = source.getClass();
        
        assertTrue("isProtected" , Modifier.isProtected( enhancedClass.getDeclaredMethod("protectedMethod", EMPTY_ARG ).getModifiers() ));
        int mod =  enhancedClass.getDeclaredMethod("packageMethod", EMPTY_ARG ).getModifiers() ;
        assertTrue("isPackage" , !( Modifier.isProtected(mod)|| Modifier.isPublic(mod) ) );
        
        //not sure about this (do we need it for performace ?)
        assertTrue("isFinal" ,  Modifier.isFinal( mod ) );
        
        mod =  enhancedClass.getDeclaredMethod("synchronizedMethod", EMPTY_ARG ).getModifiers() ;
        assertTrue("isSynchronized" ,  !Modifier.isSynchronized( mod ) );
        
        
    }
    
    public void testObject()throws Throwable{
        Object source =  Enhancer.enhance(
        null,
        null, NOOP_INTERCEPTOR );
        
        assertTrue("parent is object",
        source.getClass().getSuperclass() == Object.class  );
        
    }
    
    public void testSystemClassLoader()throws Throwable{
        Object source =  Enhancer.enhance(
        null,
        null, NOOP_INTERCEPTOR , ClassLoader.getSystemClassLoader());
        source.toString();
        assertTrue("SystemClassLoader",
        source.getClass().getClassLoader()
        == ClassLoader.getSystemClassLoader()  );
        
    }
    
    
    public void testCustomClassLoader()throws Throwable{
        
        ClassLoader custom = new ClassLoader(){};
        
        Object source =  Enhancer.enhance(
        null,
        null, NOOP_INTERCEPTOR, custom);
        source.toString();
        assertTrue("Custom classLoader",
        source.getClass().getClassLoader()
        == custom  );
        
        
    }
    
    public void testCheckedException()throws Throwable{
        
        Source source =  (Source)Enhancer.enhance(
        Source.class,
        null, NOOP_INTERCEPTOR );
        
        try{
            
            source.throwChecked();
            fail("lost exeption");
            
        }catch( Source.CheckedException e  ){
            
        }
        
    }
    
    public void testUndeclaredException()throws Throwable{
        
        MethodInterceptor interceptor =
        new NoOpInterceptor(){
            
            public Object afterReturn(  Object obj, Method method,
            Object args[],
            boolean invokedSuper, Object retValFromSuper,
            java.lang.Throwable e )throws java.lang.Throwable{
                throw new Source.UndeclaredException();
            }
        };
        
        Source source =  (Source)Enhancer.enhance(
        Source.class,
        null, interceptor );
        
        try{
            
            source.throwChecked();
            fail("must throw exeption");
            
        }catch( Exception cnse  ){
            
            if( !( cnse instanceof java.lang.reflect.UndeclaredThrowableException ) ){
                
                fail("invalid exeption");
            }
            
            if( !( ((java.lang.reflect.UndeclaredThrowableException)cnse).getUndeclaredThrowable()
            instanceof Source.UndeclaredException ) ){
                
                fail("invalid exeption");
            }
            
            
        }
        
        
    }
    
    public void testSerializable()throws Throwable{
        
        String testValue = "test";
        
        Source source =  (Source)Enhancer.enhance(
        Source.class,
        null, new NoOpInterceptor(testValue) );
        
        
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream    out  = new ObjectOutputStream( bout );
        
        out.writeObject( source );
        
        ObjectInputStream in = new ObjectInputStream(
        new ByteArrayInputStream(bout.toByteArray()) );
        
        Object ser = in.readObject();
        
        assertTrue("type",  ser instanceof Source );
        assertTrue("interceptor",
        Enhancer.getMethodInterceptor(ser) instanceof NoOpInterceptor );
        
        NoOpInterceptor interceptor = (NoOpInterceptor)Enhancer.getMethodInterceptor(ser);
        
        assertEquals("testValue", testValue, interceptor.getValue()  );
    }
    
    
}