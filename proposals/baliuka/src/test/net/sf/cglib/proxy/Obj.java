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
class ObjS{
  ObjS(int i, int j, float d){}

}
public class Obj extends ObjS implements Serializable{
    
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
     //       method( ((Number)args[0]).intValue() , ((Number)args[2]).longValue() );
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
       
  public Object   newInstance(){
    return new String();
  }
     
   public Object [] method( Object bean ){

        String  str = (String)bean;
        Object [] result = new Object[3];
        result[0] = str.getBytes();
        result[1] = str.getBytes();
        result[2] = new Integer(str.length());
        
        
        return result;
        
   } 
   static abstract class Bean {
     abstract public void set(int i);
     abstract public void setO(Object i);
     abstract public void setS(String i);
     
   }
   
   void throwexeption(){
     throw new RuntimeException("");
   }
   public void method1( Object bean, Object[] values ){
         
        Bean src = ( Bean)bean;
        src.set( ((Number)values[0]).intValue() );
        src.setO(values[1]);
        src.set( ((Number)values[2]).intValue() );
        src.set( ((Number)values[3]).intValue() );
        src.setS( (String)values[4] );
        src.setS( (String)values[5] );
        src.setS( (String)values[6] );
   
  }   
    
    public static void main( String args[] ) throws Exception{
    
        
      Class cls = ClassLoader.class;
      
//      ClassLoader loader = (ClassLoader) Trace.newInstance(cls);
        
//      loader.loadClass(Obj.class.getName());
        
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

    Obj(int i, int j, float d){
        super(i,j,d);
    }
    
}
