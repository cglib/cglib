/*
 * MetaClass.java
 *
 * Created on Sekmadienis, 2002, Lapkrièio 3, 09.45
 */

package net.sf.cglib.metaclass;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import net.sf.cglib.util.*;
import net.sf.cglib.util.*;

/**
 *
 * @author  baliuka
 */

public abstract class MetaClass  {
    
    private static Map cache = new Hashtable();
    
    protected Class target;
    protected String [] getters, setters;
    protected Class[] types;
    
    
     /** Creates a new instance of MetaClass */
   protected MetaClass( Class target, String getters[], 
                        String setters[], Class types[] ) {
       
        this.target = target;
        this.getters = new String[getters.length];
        System.arraycopy(getters,0,this.getters,0,getters.length ); 
        this.setters = new String[setters.length];
        System.arraycopy(setters,0,this.setters,0,setters.length ); 
        this.types = new Class[types.length];
        System.arraycopy(types,0,this.types,0,types.length ); 
   
   
    }
   
   public Class[] getPropertyTypes(){
   
        return (Class[])types.clone();
   }
   
   
   public String[] getGetters(){
   
        return (String[])getters.clone();
   }
   
   public String[] getSetters(){
   
        return (String[])setters.clone();
   }


  static private Method findDeclaredMethod(Class clazz, String name, Class types[])
                        throws NoSuchMethodException{

      
         Class cl = clazz;
         
         while ( cl != null ){
         
         try{
         	
          return cl.getDeclaredMethod(name,types);  

          
         }catch(NoSuchMethodException nsme ){
         	
             cl = cl.getSuperclass();  
        }
                     
      }
      
        throw new NoSuchMethodException(name);
      
  
  }   
   
   private static String generateKey( Class target, String getters[],
                                      String setters[], Class types[] ){
      return target.getName();
   }
  
   private static void validate( Class target, String getters[], 
                                 String setters[], Class types[],
                                 Method getters_out[], Method setters_out[] ){


     if(target.getName().startsWith("java") ){
       
       throw new IllegalArgumentException("classes from java packge not supported");
       
     } 
    
    int length = types.length;
    
    if( setters.length != length || length != getters.length ){
    
               throw new IllegalArgumentException("accessor array length must be equal type array length");
    
    }
     String last = null;	                             	
   try{
    
    for(int i=0; i< types.length; i++ ){
    
      if( getters[i] != null ){
      	 last = getters[i];
      	 Method method = findDeclaredMethod(target,last,new Class[]{});  
      	 
         int mod = method.getModifiers();
         
         if(method.getReturnType() != types[i] || Modifier.isPrivate(mod) ){
           throw new IllegalArgumentException(last);
         }
         getters_out[i] = method;
      }
      
      if( setters[i] != null ){
      	   last = setters[i];
       Method method = findDeclaredMethod( target, last, new Class[]{ types[i] });  
       if( Modifier.isPrivate(method.getModifiers()) ){
           throw new IllegalArgumentException(last);
         }
         setters_out[i] = method;
      }
      
    
    }
                                 	
    }catch( NoSuchMethodException nesme ){
    	
    	throw new IllegalArgumentException(last);
    
    }                             	
   
   }
   
   public static MetaClass getInstance(ClassLoader loader, Class target, String getters[], 
                                        String setters[], Class types[] )
                                                            throws Throwable{

      if (loader == null) {
          loader = MetaClass.class.getClassLoader();
      }
       
      String key = generateKey(target, getters, setters, types);
      MetaClass result = (MetaClass)cache.get(key);
       
      if( result != null ){
       	   return result;
       }

      Method g[] = new Method[types.length] ;
      Method  s[] = new Method[types.length] ;
      String name = target.getName() + "MetaClass";
       
      validate(target, getters, setters, types, g, s );
      
      MetaClassGenerator generator = 
                       new MetaClassGenerator(name,loader,target,g,s);
                       
      Class clazz = generator.define();
       
      result = (MetaClass)clazz.getConstructor( new Class[]{ 
                                        Class.class,String[].class,
                                        String[].class,Class[].class 
                                        } ).newInstance( 
                                        new Object[]{ target,getters,
                                                      setters,types 
                                                     }
                                        );
                                     
       
      cache.put( key, result );  
          
    return result;
   
   }
    
    public abstract Object newInstance();
    
    public abstract Object[] getPropertyValues( Object bean );
    
    public abstract void setPropertyValues( Object bean, Object[] values );
    
   
    
 static private class MetaClassGenerator extends CodeGenerator{


    private Class target;
    private Method [] getters, setters;
   

   protected MetaClassGenerator( String className, 
                                  ClassLoader loader,
                                  Class target,
                                  Method [] getters,Method [] setters ){
    	super(className, MetaClass.class, loader );
    	this.target = target;
    	this.getters = getters;
    	this.setters = setters;
    	
  
    }  
       
    protected Class define() throws CodeGenerationException {
      return super.define();
    }
     public  void generate() throws NoSuchMethodException {
        
      //------------- Generate constructor -------------
        
        Constructor constructor = MetaClass.class.
                   getDeclaredConstructor( 
                      new Class[]{ Class.class,String[].class,
                      	           String[].class,Class[].class}
                      	           );
                      	           
        begin_constructor( constructor );
        
	        load_this();
	        load_args();
	        super_invoke_constructor(constructor);
            return_value();

        end_constructor();
        
        //------------- newInstance -------------------------
        
        Method newInstance = MetaClass.class.getMethod("newInstance",new Class[]{});
        
        begin_method( newInstance );
        
        	new_instance(target);
	        dup();
	        invoke_constructor(target);    
	        return_value();
        
        end_method();
            
        //------------- getPropertyValues -------------------------    
            
        Method getPropertyValues = 
            MetaClass.class.getMethod("getPropertyValues", new Class[]{ Object.class });
           
        begin_method(getPropertyValues);   
                   
        load_arg(0);
        checkcast(target);
        store_local("bean");
        push(getters.length);
        newarray();

        // store_local("values");

        for( int i = 0; i < getters.length; i++  ){
            if( getters[i] != null ){    
                dup();
                // load_local("values");
                push(i);
                load_local("bean");
                invoke(getters[i]);
                box(getters[i].getReturnType());
                aastore();
            }//write only
        }
        // load_local("values");
        return_value();

        end_method();   
        
     //------------- setPropertyValues -------------------------     
           
           
       Method setPropertyValues = 
                     MetaClass.class.getMethod("setPropertyValues",
                      new Class[]{ Object.class, Object[].class } );
                      
       begin_method(setPropertyValues);              
          
       load_arg(0);            
       checkcast(target);
       load_arg(1);
         
       for( int i = 0; i < setters.length; i++  ){
           	
           if(setters[i] != null){
               dup2();
               aaload(i);
               unbox_or_checkcast(setters[i].getParameterTypes()[0]);
               invoke(setters[i]);
           }//read only
       }  
       return_value();
          
       end_method(); 
   }
 
 
 
 }    
    
    
    
}
