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
package net.sf.cglib;

import junit.framework.*;
import java.lang.reflect.*;
import java.io.*;

/**
 *@author     Juozas Baliuka <a href="mailto:baliuka@mwm.lt">
 *      baliuka@mwm.lt</a>
 *@version    $Id: TestEnhancer.java,v 1.6 2002/12/01 08:40:09 baliuka Exp $
 */
public class TestEnhancer extends TestCase {
    public void setUp() {
        net.sf.cglib.CodeGenerator.setDebugLocation("/tmp/");
    }
    
    private static final MethodInterceptor TEST_INTERCEPTOR = new TestInterceptor();
    
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
        new Class[]{java.util.List.class}, TEST_INTERCEPTOR );
        
        java.util.Vector vector2  = (java.util.Vector)Enhancer.enhance(
        java.util.Vector.class,
        new Class[]{java.util.List.class}, TEST_INTERCEPTOR );
        
        
        
        
        assertTrue("Cache failed",vector1.getClass() == vector2.getClass());
    }
    
    
    public void testMethods()throws Throwable{
        
        MethodInterceptor interceptor =
        new TestInterceptor(){
            
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
        null, TEST_INTERCEPTOR );
        
        
        TestCase.assertTrue("enhance", Source.class != source.getClass() );
        
    }

    public void testEnhanceObject() throws Throwable {
        EA obj = new EA();
        obj.setName("herby");
        EA proxy = (EA)Enhancer.enhance(obj, null, null, TEST_INTERCEPTOR, null, null);
        assertTrue(proxy.getName().equals("herby"));
    }

    public void testEnhanceObjectDelayed() throws Throwable {
        EA proxy = (EA)Enhancer.enhance(null, EA.class, null, TEST_INTERCEPTOR, null, null);
        EA obj = new EA();
        obj.setName("herby");
        ((Factory)proxy).setDelegate(obj);
        assertTrue(proxy.getName().equals("herby"));
    }
    
    public void testTypes()throws Throwable{
        
        Source source =  (Source)Enhancer.enhance(
        Source.class,
        null, TEST_INTERCEPTOR );
        
        
        
        TestCase.assertTrue("intType",   1   == source.intType(1));
        TestCase.assertTrue("longType",  1L  == source.longType(1L));
        TestCase.assertTrue("floatType", 1.1f  == source.floatType(1.1f));
        TestCase.assertTrue("doubleType",1.1 == source.doubleType(1.1));
        TestCase.assertEquals("objectType","1", source.objectType("1") );
        TestCase.assertEquals("objectType","",  source.toString() );
        
        
    }
    
    
    public void testModifiers()throws Throwable{
        
        Source source =  (Source)Enhancer.enhance(
        Source.class,
        null, TEST_INTERCEPTOR );
        
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
        null, TEST_INTERCEPTOR );
        
        assertTrue("parent is object",
        source.getClass().getSuperclass() == Object.class  );
        
    }
    
    public void testSystemClassLoader()throws Throwable{
        
        Object source =  Enhancer.enhance(
        null,
        null, TEST_INTERCEPTOR , ClassLoader.getSystemClassLoader());
        source.toString();
        assertTrue("SystemClassLoader",
        source.getClass().getClassLoader()
        == ClassLoader.getSystemClassLoader()  );
        
    }
    
    
    public void testCustomClassLoader()throws Throwable{
        
        ClassLoader custom = new ClassLoader(){};
        
        Object source =  Enhancer.enhance(
        null,
        null, TEST_INTERCEPTOR, custom);
        source.toString();
        assertTrue("Custom classLoader",
        source.getClass().getClassLoader()
        == custom  );
        
        
    }
    
    public void testRuntimException()throws Throwable{
    
        Source source =  (Source)Enhancer.enhance(
        Source.class,
        null, TEST_INTERCEPTOR );
        
        try{
            
            source.throwIndexOutOfBoundsException();
            fail("must throw an exception");
            
        }catch( IndexOutOfBoundsException ok  ){
            
        }
    
    }
    
    public void testUndeclaredException()throws Throwable{
        
        MethodInterceptor interceptor =
        new TestInterceptor(){
            
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
            fail("must throw an exception");
            
        } catch(Throwable cnse) {
            
            if (!(cnse instanceof UndeclaredThrowableException)) {
                fail("invalid exception type: " + cnse);
            }

            cnse = ((UndeclaredThrowableException)cnse).getUndeclaredThrowable();
            if (!(cnse instanceof Source.UndeclaredException)) {
                fail("invalid exception type: " + cnse);
            }
        }
    }
    
    public void testSerializable()throws Throwable{
        
        String testValue = "test";
        
        Source source =  (Source)Enhancer.enhance(
        Source.class,
        new Class []{} , new TestInterceptor(testValue),
        this.getClass().getClassLoader(),
         Enhancer.InternalReplace.class.getMethod("writeReplace",new Class[]{Object.class})
        );
        
        
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream    out  = new ObjectOutputStream( bout );
        
        out.writeObject( source );
        
        ObjectInputStream in = new ObjectInputStream(
        new ByteArrayInputStream(bout.toByteArray()) );
        
        Object ser = in.readObject();
        
        assertTrue("type",  ser instanceof Source );
        assertTrue("interceptor",
        Enhancer.getMethodInterceptor(ser) instanceof TestInterceptor );
        
        TestInterceptor interceptor = (TestInterceptor)Enhancer.getMethodInterceptor(ser);
        
        assertEquals("testValue", testValue, interceptor.getValue()  );
    }
    
   public void testABC() throws Throwable{
       Enhancer.enhance(EA.class, null, TEST_INTERCEPTOR).toString();
       Enhancer.enhance(EC1.class, null, TEST_INTERCEPTOR).toString();
       ((EB)Enhancer.enhance(EB.class, null, TEST_INTERCEPTOR)).finalTest();
       Enhancer.enhance(ED.class, null, TEST_INTERCEPTOR).toString();
       Enhancer.enhance(ClassLoader.class, null, TEST_INTERCEPTOR).toString();
   }

    public static class AroundDemo {
        public String getFirstName() {
            return "Chris";
        }
        public String getLastName() {
            return "Nokleberg";
        }
    }

    public void testAround() throws Throwable {
        AroundDemo demo = (AroundDemo)Enhancer.enhance(AroundDemo.class, null, new MethodInterceptor() {
                public Object aroundAdvice(Object obj, Method method, Object[] args,
                                           MethodProxy proxy) throws Throwable {
                    if (method.getName().equals("getFirstName"))
                        return "Christopher";
                    return proxy.invokeSuper(obj, args);
                }
            });
        assertTrue(demo.getFirstName().equals("Christopher"));
        assertTrue(demo.getLastName().equals("Nokleberg"));
    }
  
    public void testSamples() throws Throwable{
        samples.Trace.main(new String[]{});
        samples.Beans.main(new String[]{});
    }
}
