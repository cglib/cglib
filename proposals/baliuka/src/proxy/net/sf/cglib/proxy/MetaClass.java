/*
 * MetaClass.java
 *
 * Created on Sekmadienis, 2002, Lapkrièio 3, 09.45
 */

package net.sf.cglib.proxy;

import java.util.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

/**
 *
 * @author  baliuka
 */
public abstract class MetaClass implements ClassFileConstants{
    
    static Map cache = new Hashtable();
    
    private Class target;
    private String [] getters, setters;
    private Class[] types;
    
    /** Creates a new instance of MetaClass */
   protected MetaClass( Class target, String getters[], 
                        String setters[], Class types[] ) {
        validate( target, getters, setters, types );
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
   
   
   private static String generateKey( Class target, String getters[],
                                      String setters[], Class types[] ){
      return target.getName();
   }
  
   private static void validate( Class target, String getters[], 
                                 String setters[], Class types[] ){
   
   }
   
   public static MetaClass getInstance(ClassLoader loader, Class target, String getters[], 
                                        String setters[], Class types[] )
                                                            throws Throwable{
       
      
       String key = generateKey(target, getters, setters, types);
       MetaClass result = (MetaClass)cache.get(key);
       if( result != null ){
         return result;
       }
        String name = target.getName() + "MetaClass";
        ClassGen cg =
        new ClassGen( name, MetaClass.class.getName(), SOURCE_FILE,
        ACC_PUBLIC | ACC_FINAL , null );
    
        ConstantPoolGen cp = cg.getConstantPool();       
        InstructionList il = new InstructionList();
        //------------- Generate constructor -------------
        MethodGen constructor = new MethodGen( ACC_PUBLIC, Type.VOID, 
                  new Type[]{ CLASS_OBJECT, new ArrayType(Type.STRING,1),
                           new ArrayType(Type.STRING, 1),  
                           new ArrayType(CLASS_OBJECT,1)
        }, null, CONSTRUCTOR_NAME, cg.getClassName(), il, cp);           
        
        
        il.append( new ALOAD(0) );
        il.append( new ALOAD(1) );
        il.append( new ALOAD(2) );
        il.append( new ALOAD(3) );
        il.append( new ALOAD(4) );
        il.append( new INVOKESPECIAL( cp.addMethodref(
          MetaClass.class.getName(),"<init>",
          "(Ljava/lang/Class;[Ljava/lang/String;[Ljava/lang/String;[Ljava/lang/Class;)V"
        ) ) );
        il.append( new RETURN() );
        cg.addMethod( ClassFileUtils.getMethod( constructor ) );
        il.dispose();
        //------------- newInstance -------------------------
        MethodGen newInstance = ClassFileUtils.toMethodGen( 
                     MetaClass.class.getMethod("newInstance",new Class[]{}), 
                     cg.getClassName(), il, cp
        );
        
            il.append( new NEW(cp.addClass( 
                                 new ObjectType( target.getName()) )) 
                   );
            il.append( new DUP() );
            il.append( new INVOKESPECIAL( cp.addMethodref( 
                   target.getName(),"<init>","()V"  ) ) 
                  ) ;
            il.append( new ARETURN());
            cg.addMethod( ClassFileUtils.getMethod( newInstance ) );
            il.dispose(); 
            
        //------------- getPropertyValues -------------------------    
            
            MethodGen getPropertyValues = ClassFileUtils.toMethodGen( 
                     MetaClass.class.getMethod("getPropertyValues",
                                                new Class[]{ Object.class} ), 
                     cg.getClassName(), il, cp
                   );
        
           il.append( new ALOAD(1) ); // arg1
           il.append( new CHECKCAST( cp.addClass(target.getName()) ) );
           il.append( new ASTORE(2) ); // local1
           if( getters.length <= 5 ){ 
                il.append( new  ICONST( types.length ) );
               }else{
                 il.append( new  SIPUSH( (short)types.length ) ); 
               }
           il.append( new ANEWARRAY(cp.addClass(Type.OBJECT)));
           il.append( new ASTORE(3) ); // local2
           for( int i = 0; i < types.length; i++  ){
           if( setters[i] != null ){    
            il.append( new ALOAD(3) );// local2
            
               if( i<= 5 ){ 
                il.append( new  ICONST( i ) );
               }else{
                 il.append( new  SIPUSH( (short)i ) ); 
               } 
            
            Type returnType = ClassFileUtils.toType(types[i]);
            Instruction wrapper = ClassFileUtils.newWrapper( returnType,cp );
            if( wrapper != null ){
             il.append( wrapper );
             il.append( new DUP() );
            }
            il.append( new ALOAD(2)  );//local1
            
            il.append( new INVOKEVIRTUAL( 
                                cp.addMethodref( 
                                  target.getName() ,
                                  getters[i],"()" + returnType.getSignature() 
                                       ) 
                                  ) 
                     );
            if( wrapper != null ){
             il.append( ClassFileUtils.initWrapper( returnType, cp ) );
            }
            il.append( new AASTORE() );
            }
           }
           il.append( new ALOAD(3) );//local2
           il.append( new ARETURN() );
           cg.addMethod( ClassFileUtils.getMethod( getPropertyValues ) );
           il.dispose(); 
           
          //------------- setPropertyValues -------------------------     
           
           
       MethodGen setPropertyValues = ClassFileUtils.toMethodGen( 
                     MetaClass.class.getMethod("setPropertyValues",
                      new Class[]{ Object.class, Object[].class } ), 
                     cg.getClassName(), il, cp
                   );
           il.append( new ALOAD(1) ); // arg1
           il.append( new CHECKCAST( cp.addClass(target.getName()) ) );
           il.append( new ASTORE(3) ); // local1
           for( int i = 0; i < types.length; i++  ){
               
                il.append( new  ALOAD(3) ); // local1
                il.append( new  ALOAD(2) ); // arg2
               if( i<= 5 ){ 
                il.append( new  ICONST( i ) );
               }else{
                 il.append( new  SIPUSH((short)i ) ); 
               } 
                il.append( new  AALOAD() ); // arg2[i]
           
              Type returnType = ClassFileUtils.toType(types[i]);
              ClassFileUtils.castObject(cp, il, returnType);
             
               il.append( new INVOKEVIRTUAL( 
                    cp.addMethodref( target.getName(),setters[i],
                                    "("+ returnType.getSignature() +")V"  ) 
                    ) 
                  );
              
           }  
           il.append( new RETURN() );
          
          cg.addMethod( ClassFileUtils.getMethod( setPropertyValues ) );  
          
          //---------------- Create generated instance ---------------------
          
          
          Class clazz = ClassFileUtils.defineClass(
                                           loader, cg.getClassName(),
                                            cg.getJavaClass().getBytes() 
                       );
          
          
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
    
}
