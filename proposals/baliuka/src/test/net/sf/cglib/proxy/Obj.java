/*
 * Obj.java
 *
 * Created on Pirmadienis, 2002, Rugsëjo 23, 21.24
 */

package net.sf.cglib.proxy;
import java.io.*;
import org.apache.bcel.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;


/**
 *
 * @author  user
 */

public class Obj implements Serializable{
    
     private static java.lang.reflect.Method  METHOD_0;
     private static java.lang.reflect.Method  METHOD_1;
     private static java.lang.reflect.Method  METHOD_2;
      private static java.lang.reflect.Method METHOD_3;
    
     public Object invokePublic( Object src, 
                                   java.lang.reflect.Method method,
                                   Object[] args )
                           throws java.lang.reflect.InvocationTargetException,
                                  java.lang.NoSuchMethodException{
     
     Obj obj =  (Obj)src;                          
                               
     try{
       
      if( METHOD_3.equals( method ) ){
            method( ((Number)args[0]).intValue() , ((Number)args[2]).longValue() );
           return null;   
       }   
        
         
         
      if( METHOD_0.equals( method ) )
          return new Integer( obj.hashCode() );
      
      if( METHOD_1.equals( method ) )
          return new Boolean( obj.equals( args[0] ) );
     
       if( METHOD_2.equals( method ) )
          return  obj.toString();
     
       
      
     }catch( Exception e ){
       throw new java.lang.reflect.InvocationTargetException(e);
     } 
     
        throw new java.lang.NoSuchMethodException( method.toString() );
     
     }
       
   public void method( int a1,long a2 ){
   
   } 
    
    
    public static void main( String args[] ) throws Exception{
    
      JavaClass jc =  Repository.lookupClass( Obj.class.getName() );
      
      Field fields[] = jc.getFields();
       for( int i = 0; i < fields.length; i++ ){
        System.out.println(fields[i]);
        
      }
      
      
      Method m[] = jc.getMethods();
      for( int i = 0; i < m.length; i++ ){
       // if( m[i].getName().equals("<init>") ){  
        System.out.println(m[i]);
        System.out.println(m[i].getCode());
      //  }
      }
     
     System.out.println(Class.forName("[I"));
        
    }

}
