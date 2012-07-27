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
package net.sf.cglib.proxy;

import java.io.*;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import junit.framework.*;
import net.sf.cglib.CodeGenTestCase;
import net.sf.cglib.core.DefaultNamingPolicy;
import net.sf.cglib.core.ReflectUtils;
import net.sf.cglib.reflect.FastClass;

/**
 *@author     Juozas Baliuka <a href="mailto:baliuka@mwm.lt">
 *      baliuka@mwm.lt</a>
 *@version    $Id: TestEnhancer.java,v 1.58 2012/07/27 16:02:49 baliuka Exp $
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

    // TODO: make this work again
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

    static class NamingPolicyDummy {}

    public void testNamingPolicy() throws Throwable {
      Enhancer e = new Enhancer();
      e.setSuperclass(NamingPolicyDummy.class);
      e.setUseCache(false);
      e.setUseFactory(false);
      e.setNamingPolicy(new DefaultNamingPolicy() {
        public String getTag() {
          return "ByHerby";
        }
          public String toString() {
            return getTag();
          }
      });
      e.setCallbackType(MethodInterceptor.class);
      Class proxied = e.createClass();
      final boolean[] ran = new boolean[1];
      Enhancer.registerStaticCallbacks(proxied, new Callback[]{
        new MethodInterceptor() {
          public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            ran[0] = true;
            assertTrue(proxy.getSuperFastClass().getClass().getName().indexOf("$FastClassByHerby$") >= 0);
            return proxy.invokeSuper(obj, args);
          }
        }
      });
      NamingPolicyDummy dummy = (NamingPolicyDummy) proxied.newInstance();
      dummy.toString();
      assertTrue(ran[0]);
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

    private static class StringValue
    implements MethodInterceptor
    {
        private String value;

        public StringValue(String value) {
            this.value = value;
        }
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) {
            return value;
        }
    }

    public void testRegisterCallbacks()
    throws InterruptedException
    {
         Enhancer e = new Enhancer();
         e.setSuperclass(ArgInit.class);
         e.setCallbackType(MethodInterceptor.class);
         e.setUseFactory(false);
         final Class clazz = e.createClass();

         assertTrue(!Factory.class.isAssignableFrom(clazz));
         assertEquals("test", newArgInit(clazz, "test").toString());

         Enhancer.registerCallbacks(clazz, new Callback[]{ new StringValue("fizzy") });
         assertEquals("fizzy", newArgInit(clazz, "test").toString());
         assertEquals("fizzy", newArgInit(clazz, "test").toString());

         Enhancer.registerCallbacks(clazz, new Callback[]{ null });
         assertEquals("test", newArgInit(clazz, "test").toString());

         Enhancer.registerStaticCallbacks(clazz, new Callback[]{ new StringValue("soda") });
         assertEquals("test", newArgInit(clazz, "test").toString());

         Enhancer.registerCallbacks(clazz, null);
         assertEquals("soda", newArgInit(clazz, "test").toString());
         
         Thread thread = new Thread(){
             public void run() {
                 assertEquals("soda", newArgInit(clazz, "test").toString());
             }
         };
         thread.start();
         thread.join();
    }
    
   public void perform(ClassLoader loader) throws Exception{
    
           enhance( Source.class , null, TEST_INTERCEPTOR, loader);
    
    }
    
  
    public void testCallbackHelper() {
        final ArgInit delegate = new ArgInit("helper");
        Class sc = ArgInit.class;
        Class[] interfaces = new Class[]{ DI1.class, DI2.class };

        CallbackHelper helper = new CallbackHelper(sc, interfaces) {
            protected Object getCallback(final Method method) {
                return new FixedValue() {
                    public Object loadObject() {
                        return "You called method " + method.getName();
                    }
                };
            }
        };

        Enhancer e = new Enhancer();
        e.setSuperclass(sc);
        e.setInterfaces(interfaces);
        e.setCallbacks(helper.getCallbacks());
        e.setCallbackFilter(helper);

        ArgInit proxy = (ArgInit)e.create(new Class[]{ String.class }, new Object[]{ "whatever" });
        assertEquals("You called method toString", proxy.toString());
        assertEquals("You called method herby", ((DI1)proxy).herby());
        assertEquals("You called method derby", ((DI2)proxy).derby());
    }

   
    
    public void testSerialVersionUID() throws Exception {
        Long suid = new Long(0xABBADABBAD00L);

        Enhancer e = new Enhancer();
        e.setSerialVersionUID(suid);
        e.setCallback(NoOp.INSTANCE);
        Object obj = e.create();

        Field field = obj.getClass().getDeclaredField("serialVersionUID");
        field.setAccessible(true);
        assertEquals(suid, field.get(obj));
    }

    interface ReturnTypeA { int foo(String x); }
    interface ReturnTypeB { String foo(String x); }
    public void testMethodsDifferingByReturnTypeOnly() throws IOException {
        Enhancer e = new Enhancer();
        e.setInterfaces(new Class[]{ ReturnTypeA.class, ReturnTypeB.class });
        e.setCallback(new MethodInterceptor() {
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                if (method.getReturnType().equals(String.class))
                    return "hello";
                return new Integer(42);
            }
        });
        Object obj = e.create();
        assertEquals(42, ((ReturnTypeA)obj).foo("foo"));
        assertEquals("hello", ((ReturnTypeB)obj).foo("foo"));
        assertEquals(-1, FastClass.create(obj.getClass()).getIndex("foo", new Class[]{ String.class }));
    }


    public static class ConstructorCall
    {
        private String x;
        public ConstructorCall() {
            x = toString();
        }
        public String toString() {
            return "foo";
        }
    }
    
    public void testInterceptDuringConstruction() {
        FixedValue fixedValue = new FixedValue() {
            public Object loadObject() {
                return "bar";
            }
        };

        Enhancer e = new Enhancer();
        e.setSuperclass(ConstructorCall.class);
        e.setCallback(fixedValue);
        assertEquals("bar", ((ConstructorCall)e.create()).x);

        e = new Enhancer();
        e.setSuperclass(ConstructorCall.class);
        e.setCallback(fixedValue);
        e.setInterceptDuringConstruction(false);
        assertEquals("foo", ((ConstructorCall)e.create()).x);
    }
    
    
    
   void assertThreadLocalCallbacks(Class cls)throws Exception{
        
        Field field = cls.getDeclaredField("CGLIB$THREAD_CALLBACKS");
        field.setAccessible(true);
        
        assertNull(((ThreadLocal) field.get(null)).get());
    }
    
    public void testThreadLocalCleanup1()throws Exception{
        
        Enhancer e = new Enhancer();
        e.setUseCache(false);    
        e.setCallbackType(NoOp.class);
        Class cls = e.createClass();
        
        assertThreadLocalCallbacks(cls);
        
      
        

    }
    
    
    public void testThreadLocalCleanup2()throws Exception{
        
        Enhancer e = new Enhancer();
        e.setCallback(NoOp.INSTANCE);
        Object obj = e.create();
        
        assertThreadLocalCallbacks(obj.getClass());
        
        

    }
    
    public void testThreadLocalCleanup3()throws Exception{
        
        Enhancer e = new Enhancer();
        e.setCallback(NoOp.INSTANCE);
        Factory obj = (Factory) e.create();
        obj.newInstance(NoOp.INSTANCE);
        
        assertThreadLocalCallbacks(obj.getClass());
        
        

    }
    
    public void testBridgeForcesInvokeVirtual() {
        List<Class> retTypes = new ArrayList<Class>();
        List<Class> paramTypes = new ArrayList<Class>();
        Interceptor interceptor = new Interceptor(retTypes, paramTypes);

        Enhancer e = new Enhancer();
        e.setSuperclass(Impl.class);
        e.setCallbackFilter(new CallbackFilter() {
            public int accept(Method method) {
                return method.getDeclaringClass() != Object.class ? 0 : 1;
            }
        });
        e.setCallbacks(new Callback[] { interceptor, NoOp.INSTANCE });
        // We expect the bridge ('ret') to be called & forward us to the non-bridge 'erased'
        Interface intf = (Interface)e.create();
        intf.aMethod(null);
        // Make sure the right things got called in the right order:
        assertEquals(Arrays.asList(RetType.class, ErasedType.class), retTypes);
        
        // Validate calling the refined just gives us that.
        retTypes.clear();
        Impl impl = (Impl)intf;
        impl.aMethod((Refined)null);
        assertEquals(Arrays.asList(Refined.class), retTypes);
        
        // When calling from the impl, we are dispatched directly to the non-bridge,
        // because that's just how it works.
        retTypes.clear();
        impl.aMethod((RetType)null);
        assertEquals(Arrays.asList(ErasedType.class), retTypes);
        
        // Do a whole bunch of checks for the other methods too
        
        paramTypes.clear();
        intf.intReturn(null);
        assertEquals(Arrays.asList(RetType.class, ErasedType.class), paramTypes);
        
        paramTypes.clear();
        intf.voidReturn(null);
        assertEquals(Arrays.asList(RetType.class, ErasedType.class), paramTypes);
        
        paramTypes.clear();
        intf.widenReturn(null);
        assertEquals(Arrays.asList(RetType.class, ErasedType.class), paramTypes);
    }
    
    public void testBridgeForcesInvokeVirtualEvenWithoutInterceptingBridge() {
        List<Class> retTypes = new ArrayList<Class>();
        Interceptor interceptor = new Interceptor(retTypes);

        Enhancer e = new Enhancer();
        e.setSuperclass(Impl.class);
        e.setCallbackFilter(new CallbackFilter() {
            public int accept(Method method) {
              // Ideally this would be:
              // return !method.isBridge() && method.getDeclaringClass() != Object.class ? 0 : 1;
              // But Eclipse sometimes labels the wrong things as bridge methods, so we're more
              // explicit:
              return method.getDeclaringClass() != Object.class
                  && method.getReturnType() != RetType.class ? 0 : 1;
            }
        });
        e.setCallbacks(new Callback[] { interceptor, NoOp.INSTANCE });
        // We expect the bridge ('ret') to be called & forward us to non-bridge ('erased'),
        // and we only intercept on the non-bridge.
        Interface intf = (Interface)e.create();
        intf.aMethod(null);
        assertEquals(Arrays.asList(ErasedType.class), retTypes);
        
        // Validate calling the refined just gives us that.
        retTypes.clear();
        Impl impl = (Impl)intf;
        impl.aMethod((Refined)null);
        assertEquals(Arrays.asList(Refined.class), retTypes);
        
        // Make sure we still get our non-bride interception if we didn't intercept the bridge.
        retTypes.clear();
        impl.aMethod((RetType)null);
        assertEquals(Arrays.asList(ErasedType.class), retTypes);
    }

    public void testReverseBridge() {
        List<Class> retTypes = new ArrayList<Class>();
        Interceptor interceptor = new Interceptor(retTypes);

        Enhancer e = new Enhancer();
        e.setSuperclass(ReverseImpl.class);
        e.setCallbackFilter(new CallbackFilter() {
            public int accept(Method method) {
                return method.getDeclaringClass() != Object.class ? 0 : 1;
            }
        });
        e.setCallbacks(new Callback[] { interceptor, NoOp.INSTANCE });
        // We expect the bridge ('erased') to be called & forward us to 'ret' (non-bridge)
        ReverseSuper superclass = (ReverseSuper)e.create();
        superclass.aMethod(null, null, null, null);
        assertEquals(Arrays.asList(ErasedType.class, RetType.class), retTypes);
        
        // Calling the Refined type gives us just that.
        retTypes.clear();
        ReverseImpl impl2 = (ReverseImpl)superclass;
        impl2.aMethod(null, (Refined)null, null, null);
        assertEquals(Arrays.asList(Refined.class), retTypes);

        retTypes.clear();
        impl2.aMethod(null, (RetType)null, null, null);
        assertEquals(Arrays.asList(RetType.class), retTypes);
    }
    
    public void testBridgeForMoreViz() {
        List<Class> retTypes = new ArrayList<Class>();
        List<Class> paramTypes = new ArrayList<Class>();
        Interceptor interceptor = new Interceptor(retTypes, paramTypes);

        Enhancer e = new Enhancer();
        e.setSuperclass(PublicViz.class);
        e.setCallbackFilter(new CallbackFilter() {
            public int accept(Method method) {
                return method.getDeclaringClass() != Object.class ? 0 : 1;
            }
        });
        e.setCallbacks(new Callback[] { interceptor, NoOp.INSTANCE });

        VizIntf intf = (VizIntf)e.create();
        intf.aMethod(null);
        assertEquals(Arrays.asList(Concrete.class), paramTypes);
    }
    
    
    
    static class ErasedType {}
    static class RetType extends ErasedType {}
    static class Refined extends RetType {}
    
    static abstract class Superclass<T extends ErasedType> {
        // Check narrowing return value & parameters
        public T aMethod(T t) { return null; }
        // Check void return value
        public void voidReturn(T t) { }
        // Check primitive return value
        public int intReturn(T t) { return 1; }
        // Check widening return value
        public RetType widenReturn(T t) { return null; }
    }
    public interface Interface { // the usage of the interface forces the bridge
        RetType aMethod(RetType obj);
        void voidReturn(RetType obj);
        int intReturn(RetType obj);
        // a wider type than in superclass
        ErasedType widenReturn(RetType obj);
    }
    public static class Impl extends Superclass<RetType> implements Interface {
        // An even more narrowed type, just to make sure
        // it doesn't confuse us.
        public Refined aMethod(Refined obj) { return null; }
    }
    
    // Another set of classes -- this time with the bridging in reverse,
    // to make sure that if we define the concrete type, a bridge
    // is created to call it from an erased type.
    static abstract class ReverseSuper<T extends ErasedType> {
        // the various parameters are to make sure we only
        // change signature when we have to -- only 'c' goes
        // from ErasedType -> RetType
        public T aMethod(Concrete b, T c, RetType d, ErasedType e) { return null; }
    }
    static class Concrete {}
    static class ReverseImpl extends ReverseSuper<RetType> {
        public Refined aMethod(Concrete b, Refined c, RetType d, ErasedType e) { return null; }
        public RetType aMethod(Concrete b, RetType c, RetType d, ErasedType e) { return null; }
    }
    
    public interface VizIntf {
        public void aMethod(Concrete a);
    }
    static abstract class PackageViz implements VizIntf {
        public void aMethod(Concrete e) {  }
    }
    // inherits aMethod from PackageViz, but bridges to make it
    // publicly accessible.  the bridge here has the same
    // target signature, so it absolutely requires invokespecial,
    // otherwise we recurse forever.
    public static class PublicViz extends PackageViz implements VizIntf {}
    
    private static class Interceptor implements MethodInterceptor {
        private final List<Class> retList;
        private final List<Class> paramList;
        
        public Interceptor(List<Class> retList) {
            this(retList, new ArrayList<Class>());
        }
        
        public Interceptor(List<Class> retList, List<Class> paramList) {
            this.retList = retList;
            this.paramList = paramList;
        }

        public Object intercept(Object obj, Method method, Object[] args,
                MethodProxy proxy) throws Throwable {
            retList.add(method.getReturnType());
            if (method.getParameterTypes().length > 0) {
                paramList.add(method.getParameterTypes()[0]);
            }
            return proxy.invokeSuper(obj, args);
        }
    }
    
}
