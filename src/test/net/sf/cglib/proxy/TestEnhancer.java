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
package net.sf.cglib.proxy;

import java.io.*;
import java.lang.reflect.*;
import junit.framework.*;
import net.sf.cglib.CodeGenTestCase;
import net.sf.cglib.core.ReflectUtils;

/**
 *@author     Juozas Baliuka <a href="mailto:baliuka@mwm.lt">
 *      baliuka@mwm.lt</a>
 *@version    $Id: TestEnhancer.java,v 1.40 2004/03/28 04:12:14 herbyderby Exp $
 */
public class TestEnhancer extends CodeGenTestCase {
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
        
        java.util.Vector vector1 = (java.util.Vector)Enhancer.create(
        java.util.Vector.class,
        new Class[]{java.util.List.class}, TEST_INTERCEPTOR );
        
        java.util.Vector vector2  = (java.util.Vector)Enhancer.create(
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
        
        
        Source source =  (Source)Enhancer.create(
        Source.class,
        null,interceptor );
        
        source.callAll();
        assertTrue("protected", invokedProtectedMethod );
        assertTrue("package", invokedPackageMethod );
        assertTrue("abstract", invokedAbstractMethod );
    }
    
    public void testEnhanced()throws Throwable{
        
        Source source =  (Source)Enhancer.create(
        Source.class,
        null, TEST_INTERCEPTOR );
        
        
        TestCase.assertTrue("enhance", Source.class != source.getClass() );
        
    }

    public void testEnhanceObject() throws Throwable {
        EA obj = new EA();
        EA save = obj;
        obj.setName("herby");
        EA proxy = (EA)Enhancer.create( EA.class,  new DelegateInterceptor(save) );
     
        assertTrue(proxy.getName().equals("herby"));

        Factory factory = (Factory)proxy;
        assertTrue(((EA)factory.newInstance(factory.getCallbacks())).getName().equals("herby"));
    }

    class DelegateInterceptor implements MethodInterceptor {
      Object delegate;
        DelegateInterceptor(Object delegate){
          this.delegate = delegate;
        }
        public Object intercept(Object obj, java.lang.reflect.Method method, Object[] args, MethodProxy proxy) throws Throwable {
            return proxy.invoke(delegate,args);
        }
        
    }
    public void testEnhanceObjectDelayed() throws Throwable {
        
        DelegateInterceptor mi = new DelegateInterceptor(null);
        EA proxy = (EA)Enhancer.create( EA.class, mi);
        EA obj = new EA();
        obj.setName("herby");
        mi.delegate = obj;
       assertTrue(proxy.getName().equals("herby"));
    }
    
    
    public void testTypes()throws Throwable{
        
        Source source =  (Source)Enhancer.create(
        Source.class,
        null, TEST_INTERCEPTOR );
        TestCase.assertTrue("intType",   1   == source.intType(1));
        TestCase.assertTrue("longType",  1L  == source.longType(1L));
        TestCase.assertTrue("floatType", 1.1f  == source.floatType(1.1f));
        TestCase.assertTrue("doubleType",1.1 == source.doubleType(1.1));
        TestCase.assertEquals("objectType","1", source.objectType("1") );
        TestCase.assertEquals("objectType","",  source.toString() );
        source.arrayType( new int[]{} );    
        
    }
    

    public void testModifiers()throws Throwable{
        
        Source source =  (Source)Enhancer.create(
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
        
        Object source =  Enhancer.create(
        null,
        null, TEST_INTERCEPTOR );
        
        assertTrue("parent is object",
        source.getClass().getSuperclass() == Object.class  );
        
    }

    public void testSystemClassLoader()throws Throwable{
        
        Object source =  enhance(
        null,
        null, TEST_INTERCEPTOR , ClassLoader.getSystemClassLoader());
        source.toString();
        assertTrue("SystemClassLoader",
        source.getClass().getClassLoader()
        == ClassLoader.getSystemClassLoader()  );
        
    }
    
    public void testCustomClassLoader()throws Throwable{
        
        ClassLoader custom = new ClassLoader(this.getClass().getClassLoader()){};
        
        Object source =  enhance( null, null, TEST_INTERCEPTOR, custom);
        source.toString();
        assertTrue("Custom classLoader", source.getClass().getClassLoader() == custom  );
        
        custom = new ClassLoader(){};
        
        source =  enhance( null, null, TEST_INTERCEPTOR, custom);
        source.toString();
        assertTrue("Custom classLoader", source.getClass().getClassLoader() == custom  );
        
        
    }

    public void testRuntimException()throws Throwable{
    
        Source source =  (Source)Enhancer.create(
        Source.class,
        null, TEST_INTERCEPTOR );
        
        try{
            
            source.throwIndexOutOfBoundsException();
            fail("must throw an exception");
            
        }catch( IndexOutOfBoundsException ok  ){
            
        }
    
    }
    
  static abstract class CastTest{
     CastTest(){} 
    abstract int getInt();
  }
  
  class CastTestInterceptor implements MethodInterceptor{
     
      public Object intercept(Object obj, java.lang.reflect.Method method, Object[] args, MethodProxy proxy) throws Throwable {
          return new Short((short)0);
      }
      
  }  
  
    
  public void testCast()throws Throwable{
    
    CastTest castTest =  (CastTest)Enhancer.create(CastTest.class, null, new  CastTestInterceptor());
  
    assertTrue(castTest.getInt() == 0);
    
  }
  
   public void testABC() throws Throwable{
       Enhancer.create(EA.class, null, TEST_INTERCEPTOR);
       Enhancer.create(EC1.class, null, TEST_INTERCEPTOR).toString();
       ((EB)Enhancer.create(EB.class, null, TEST_INTERCEPTOR)).finalTest();
       assertTrue("abstract method",( (EC1)Enhancer.create(EC1.class,
                     null, TEST_INTERCEPTOR) ).compareTo( new EC1() ) == -1 );
       Enhancer.create(ED.class, null, TEST_INTERCEPTOR).toString();
       Enhancer.create(ClassLoader.class, null, TEST_INTERCEPTOR).toString();
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
        AroundDemo demo = (AroundDemo)Enhancer.create(AroundDemo.class, null, new MethodInterceptor() {
                public Object intercept(Object obj, Method method, Object[] args,
                                           MethodProxy proxy) throws Throwable {
                    if (method.getName().equals("getFirstName")) {
                        return "Christopher";
                    }
                    return proxy.invokeSuper(obj, args);
                }
            });
        assertTrue(demo.getFirstName().equals("Christopher"));
        assertTrue(demo.getLastName().equals("Nokleberg"));
    }
 
    
    public static interface TestClone extends Cloneable{
     public Object clone()throws java.lang.CloneNotSupportedException;

    }
    public static class TestCloneImpl implements TestClone{
     public Object clone()throws java.lang.CloneNotSupportedException{
         return super.clone();
     }
    }

    public void testClone() throws Throwable{
    
      TestClone testClone = (TestClone)Enhancer.create( TestCloneImpl.class,
                                                          TEST_INTERCEPTOR );
      assertTrue( testClone.clone() != null );  
      
            
      testClone = (TestClone)Enhancer.create( TestClone.class,
         new MethodInterceptor(){
      
           public Object intercept(Object obj, java.lang.reflect.Method method, Object[] args,
                        MethodProxy proxy) throws Throwable{
                     return  proxy.invokeSuper(obj, args);
           }
  
      
      } );

      assertTrue( testClone.clone() != null );  
      
      
    }
    
    public void testSamples() throws Throwable{
        samples.Trace.main(new String[]{});
        samples.Beans.main(new String[]{});
    }

    public static interface FinalA {
        void foo();
    }

    public static class FinalB implements FinalA {
        final public void foo() { }
    }

    public void testFinal() throws Throwable {
        ((FinalA)Enhancer.create(FinalB.class, TEST_INTERCEPTOR)).foo();
    }

    public static interface ConflictA {
        int foo();
    }

    public static interface ConflictB {
        String foo();
    }

    public void testConflict() throws Throwable {
        Object foo =
            Enhancer.create(Object.class, new Class[]{ ConflictA.class, ConflictB.class }, TEST_INTERCEPTOR);
        ((ConflictA)foo).foo();
        ((ConflictB)foo).foo();
    }

     public void testArgInit() throws Throwable{

         Enhancer e = new Enhancer();
         e.setSuperclass(ArgInit.class);
         e.setCallbackType(MethodInterceptor.class);
         Class f = e.createClass();
         ArgInit a = (ArgInit)ReflectUtils.newInstance(f,
                                                       new Class[]{ String.class },
                                                       new Object[]{ "test" });
         assertEquals("test", a.toString());
         ((Factory)a).setCallback(0, TEST_INTERCEPTOR);
         assertEquals("test", a.toString());

         Callback[] callbacks = new Callback[]{ TEST_INTERCEPTOR };
         ArgInit b = (ArgInit)((Factory)a).newInstance(new Class[]{ String.class },
                                                       new Object[]{ "test2" },
                                                       callbacks);
         assertEquals("test2", b.toString());
         try{
             ((Factory)a).newInstance(new Class[]{  String.class, String.class },
                                      new Object[]{"test"},
                                      callbacks);
             fail("must throw exception");
         }catch( IllegalArgumentException iae ){
         
         }
    }

    public static class Signature {
        public int interceptor() {
            return 42;
        }
    }

    public void testSignature() throws Throwable {
        Signature sig = (Signature)Enhancer.create(Signature.class, TEST_INTERCEPTOR);
        assertTrue(((Factory)sig).getCallback(0) == TEST_INTERCEPTOR);
        assertTrue(sig.interceptor() == 42);
    }

    public abstract static class AbstractMethodCallInConstructor {
	public AbstractMethodCallInConstructor() {
	    foo();
	}

	public abstract void foo();
    }

    public void testAbstractMethodCallInConstructor() throws Throwable {
	AbstractMethodCallInConstructor obj = (AbstractMethodCallInConstructor)
	    Enhancer.create(AbstractMethodCallInConstructor.class,
			     TEST_INTERCEPTOR);
	obj.foo();
    }

    public void testProxyIface() throws Throwable {
        final DI1 other = new DI1() {
                public String herby() {
                    return "boop";
                }
            };
        DI1 d = (DI1)Enhancer.create(DI1.class, new MethodInterceptor() {
                public Object intercept(Object obj, java.lang.reflect.Method method, Object[] args,
                                        MethodProxy proxy) throws Throwable {
                    return proxy.invoke(other, args);
                }
            });
        assertTrue("boop".equals(d.herby()));
    }

    public static Object enhance(Class cls, Class interfaces[], Callback callback, ClassLoader loader) {
        Enhancer e = new Enhancer();
        e.setSuperclass(cls);
        e.setInterfaces(interfaces);
        e.setCallback(callback);
        e.setClassLoader(loader);
        return e.create();
    }

    public interface PublicClone extends Cloneable {
        Object clone() throws CloneNotSupportedException;
    }

    public void testNoOpClone() throws Exception {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(PublicClone.class);
        enhancer.setCallback(NoOp.INSTANCE);
        ((PublicClone)enhancer.create()).clone();
    }

    public void testNoFactory() throws Exception {
        noFactoryHelper();
        noFactoryHelper();
    }

    private void noFactoryHelper() {
        MethodInterceptor mi = new MethodInterceptor() {
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                return "Foo";
            }
        };
        Enhancer enhancer = new Enhancer();
        enhancer.setUseFactory(false);
        enhancer.setSuperclass(AroundDemo.class);
        enhancer.setCallback(mi);
        AroundDemo obj = (AroundDemo)enhancer.create();
        assertTrue(obj.getFirstName().equals("Foo"));
        assertTrue(!(obj instanceof Factory));
    }

    interface MethDec {
        void foo();
    }
    
    abstract static class MethDecImpl implements MethDec {
    }

    public void testMethodDeclarer() throws Exception {
        final boolean[] result = new boolean[]{ false };
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(MethDecImpl.class);
        enhancer.setCallback(new MethodInterceptor() {
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                
                result[0] = method.getDeclaringClass().getName().equals(MethDec.class.getName());
                return null;
            }
        });
        ((MethDecImpl)enhancer.create()).foo();
        assertTrue(result[0]);
    }


    interface ClassOnlyX { }
    public void testClassOnlyFollowedByInstance() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(ClassOnlyX.class);
        enhancer.setCallbackType(NoOp.class);
        Class type = enhancer.createClass();

        enhancer = new Enhancer();
        enhancer.setSuperclass(ClassOnlyX.class);
        enhancer.setCallback(NoOp.INSTANCE);
        Object instance = enhancer.create();

        assertTrue(instance instanceof ClassOnlyX);
        assertTrue(instance.getClass().equals(type));
    }

     public void testSql() {
         Enhancer.create(null, new Class[]{ java.sql.PreparedStatement.class }, TEST_INTERCEPTOR);
     }

    public void testEquals() throws Exception {
        final boolean[] result = new boolean[]{ false };
        EqualsInterceptor intercept = new EqualsInterceptor();
        Object obj = Enhancer.create(null, intercept);
        obj.equals(obj);
        assertTrue(intercept.called);
    }

    public static class EqualsInterceptor implements MethodInterceptor {
        final static Method EQUALS_METHOD = ReflectUtils.findMethod("Object.equals(Object)");
        boolean called;

        public Object intercept(Object obj,
                                Method method,
                                Object[] args,
                                MethodProxy proxy) throws Throwable {
            if (method.equals(EQUALS_METHOD)) {
                return proxy.invoke(this, args);
            } else {
                return proxy.invokeSuper(obj, args);
            }
        }

        public boolean equals(Object other) {
            called = true;
            return super.equals(other);
        }
    }

    private static interface ExceptionThrower {
        void throwsThrowable(int arg) throws Throwable;
        void throwsException(int arg) throws Exception;
        void throwsNothing(int arg);
    }

    private static class MyThrowable extends Throwable { }
    private static class MyException extends Exception { }
    private static class MyRuntimeException extends RuntimeException { }
    
    public void testExceptions() {
        Enhancer e = new Enhancer();
        e.setSuperclass(ExceptionThrower.class);
        e.setCallback(new MethodInterceptor() {
            public Object intercept(Object obj,
                                    Method method,
                                    Object[] args,
                                    MethodProxy proxy) throws Throwable {
                switch (((Integer)args[0]).intValue()) {
                case 1:
                    throw new MyThrowable();
                case 2:
                    throw new MyException();
                case 3:
                    throw new MyRuntimeException();
                default:
                    return null;
                }
            }
        });
        ExceptionThrower et = (ExceptionThrower)e.create();
        try { et.throwsThrowable(1); } catch (MyThrowable t) { } catch (Throwable t) { fail(); }
        try { et.throwsThrowable(2); } catch (MyException t) { } catch (Throwable t) { fail(); }
        try { et.throwsThrowable(3); } catch (MyRuntimeException t) { } catch (Throwable t) { fail(); }

        try { et.throwsException(1); } catch (Throwable t) { assertTrue(t instanceof MyThrowable); }
        try { et.throwsException(2); } catch (MyException t) { } catch (Throwable t) { fail(); }
        try { et.throwsException(3); } catch (MyRuntimeException t) { } catch (Throwable t) { fail(); }
        try { et.throwsException(4); } catch (Throwable t) { fail(); }

        try { et.throwsNothing(1); } catch (Throwable t) { assertTrue(t instanceof MyThrowable); }
        try { et.throwsNothing(2); } catch (Exception t) { assertTrue(t instanceof MyException); }
        try { et.throwsNothing(3); } catch (MyRuntimeException t) { } catch (Throwable t) { fail(); }
        try { et.throwsNothing(4); } catch (Throwable t) { fail(); }
    }

    public void testUnusedCallback() {
        Enhancer e = new Enhancer();
        e.setCallbackTypes(new Class[]{ MethodInterceptor.class, NoOp.class });
        e.setCallbackFilter(new CallbackFilter() {
            public int accept(Method method) {
                return 0;
            }
        });
        e.createClass();
    }

    private static ArgInit newArgInit(Class clazz, String value) {
        return (ArgInit)ReflectUtils.newInstance(clazz,
                                                 new Class[]{ String.class },
                                                 new Object[]{ value });
    }
   
    public void testRegisterCallbacks() {
         Enhancer e = new Enhancer();
         e.setSuperclass(ArgInit.class);
         e.setCallbackType(MethodInterceptor.class);
         e.setUseFactory(false);
         Class clazz = e.createClass();

         assertTrue(!Factory.class.isAssignableFrom(clazz));
         assertEquals("test", newArgInit(clazz, "test").toString());

         Enhancer.registerCallbacks(clazz, new Callback[]{ new MethodInterceptor() {
             public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                 return "fizzy";
             }
         }});
         assertEquals("fizzy", newArgInit(clazz, "test").toString());
         assertEquals("fizzy", newArgInit(clazz, "test").toString());

         Enhancer.registerCallbacks(clazz, null);
         assertEquals("test", newArgInit(clazz, "test").toString());
         
    }
}
