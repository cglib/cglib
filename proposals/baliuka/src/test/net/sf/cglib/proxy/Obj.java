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
    
    public int ifStatement(){
    int i = (int)System.currentTimeMillis();
        if(  i > 0 ){
        
           i = (int)( i/50.0 );
        }
        return i;
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
