/*
 * MetaClassTest.java
 * JUnit based test
 *
 * Created on Sekmadienis, 2002, Lapkrièio 3, 13.15
 */

package net.sf.cglib.metaclass;

import junit.framework.*;
import java.util.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;



/**
 *
 * @author baliuka
 */
public class TestMetaClass extends TestCase {
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
       
    
    
    public TestMetaClass(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(TestMetaClass.class);
        
        return suite;
    }
    
    
    
    
    /** Test of getInstance method, of class net.sf.cglib.proxy.MetaClass. */
    public void testGetInstance() throws Throwable{
    
        
      MetaClass mClass = MetaClass.getInstance( this.getClass().getClassLoader(),
                                                A.class,getters,setters,types );  
      
      A bean = (A)mClass.newInstance();
      
      mClass.setPropertyValues( bean, values );
      Object values1[] = mClass.getPropertyValues( bean );
      
      for( int i = 0; i < types.length; i++  ){
        assertEquals(" property " + getters[i] + "/" + setters[i] , values[i] , values1[i] );
      }  
      
      
    }
    
    public void testMetaClassPerformance()throws Throwable{
    
       int iterations = 100000;
       
       System.out.println(); 
       System.out.println("iteration count: " + iterations);
       System.out.println(); 
       
       MetaClass mClass = new MetaClassReflectImpl( A.class,getters,setters,types );
       
       System.out.println( mClass.getClass().getName() + ": " );
       int b = performanceTest( mClass, iterations );
       System.out.println( b + " ms.   "  + ( b/(float)iterations)  + " per iteration" );
       System.out.println(); 
       
       
        mClass = MetaClass.getInstance( this.getClass().getClassLoader(),
                                                A.class,getters,setters,types );  
       
       
        
      
       System.out.println( mClass.getClass().getName() + ": " );
       int a = performanceTest( mClass, iterations );
       System.out.println( a + " ms.   " + ( a/(float)iterations)  + " per iteration" );
       
       
       System.out.println( "factor: " + b/(float)a );
       
    
    }
    
     public int performanceTest( MetaClass mc, int iterations ) throws Throwable{
       
         
         
       long start = System.currentTimeMillis();
      for( int i = 0; i< iterations; i++   ){
         A bean = (A)mc.newInstance();
         mc.setPropertyValues( bean, values );
         
      }
       
      return (int)( System.currentTimeMillis() - start );
     }
    
    /** Generated implementation of abstract class net.sf.cglib.proxy.MetaClass. Please fill dummy bodies of generated methods. */
    private static class MetaClassReflectImpl extends MetaClass {
        
     private   java.lang.reflect.Method gets[];
     private   java.lang.reflect.Method sets[];
     private   int size ;    
        public MetaClassReflectImpl(Class target, String[] getters, String[] setters, Class[] types) {
            super(target, getters, setters, types);
            size = this.types.length;        
            gets = new java.lang.reflect.Method [size];
            sets = new java.lang.reflect.Method [size];
               
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
        
        public Object newInstance() {
         try{            
            return target.newInstance();
          }catch( Exception e ){
             throw new Error(e.getMessage());
         }   
       }
        
        public Object[] getPropertyValues(Object bean) {
            
            try{
                
            Object[] result = new Object[ size ];
           
            for( int i = 0; i < size ; i++  ){
              if( this.gets[i] != null ){
               result[i] = gets[i].invoke(bean, null );             
              }
             }
            return result;
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
