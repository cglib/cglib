/*
 * Signature.java
 *
 * Created on Antradienis, 2003, Rugsëjo 9, 10.38
 */

package net.sf.cglib.transform;

import java.util.*;

import org.objectweb.asm.Type;

/**
 *
 * @author  baliuka
 */
public class Signature {
    
    public static final String READ_WRITE_CALLBACK = "$CGLIB_READ_WRITE_CALLBACK";
    
    private static Map MAP_BY_DESCRIPTOR = new Hashtable();
    
    static {
        Type sType = Type.getType(String.class);
        Type oType = Type.getType(Object.class);
        
        MAP_BY_DESCRIPTOR.put( Type.getDescriptor(int.class), new Object[]{
            "readInt",  Type.getMethodDescriptor( Type.INT_TYPE, new Type[]{ oType,sType, Type.INT_TYPE  } ),
            "writeInt", Type.getMethodDescriptor( Type.INT_TYPE, new Type[]{ oType,sType, Type.INT_TYPE, Type.INT_TYPE  } )
          }  
        );
        
         MAP_BY_DESCRIPTOR.put( Type.getDescriptor(char.class), new Object[]{
            "readChar",  Type.getMethodDescriptor( Type.CHAR_TYPE, new Type[]{ oType,sType, Type.CHAR_TYPE  } ),
            "writeChar", Type.getMethodDescriptor( Type.CHAR_TYPE, new Type[]{ oType,sType, Type.CHAR_TYPE, Type.CHAR_TYPE } )
          }  
        );
     
        MAP_BY_DESCRIPTOR.put( Type.getDescriptor(boolean.class),  new Object[]{
            "readBoolean"  , Type.getMethodDescriptor( Type.BOOLEAN_TYPE, new Type[]{ oType,sType, Type.BOOLEAN_TYPE  } ),
            "writeBoolean" , Type.getMethodDescriptor( Type.BOOLEAN_TYPE, new Type[]{ oType,sType, Type.BOOLEAN_TYPE, Type.BOOLEAN_TYPE  } )
         }  
        );
        MAP_BY_DESCRIPTOR.put( Type.getDescriptor(byte.class), new Object[]{
            "readByte",  Type.getMethodDescriptor( Type.BYTE_TYPE, new Type[]{ oType,sType, Type.BYTE_TYPE  } ),
            "writeByte", Type.getMethodDescriptor( Type.BYTE_TYPE, new Type[]{ oType,sType, Type.BYTE_TYPE,Type.BYTE_TYPE  } )
         }  
        );
        MAP_BY_DESCRIPTOR.put( Type.getDescriptor(short.class), new Object[]{
            "readShort" , Type.getMethodDescriptor( Type.SHORT_TYPE, new Type[]{ oType,sType, Type.SHORT_TYPE  } ),
            "writeShort", Type.getMethodDescriptor( Type.SHORT_TYPE, new Type[]{ oType,sType, Type.SHORT_TYPE, Type.SHORT_TYPE  } )
         }  
        );
        MAP_BY_DESCRIPTOR.put( Type.getDescriptor(float.class), new Object[]{
            "readFloat" , Type.getMethodDescriptor( Type.FLOAT_TYPE, new Type[]{ oType,sType, Type.FLOAT_TYPE  } ),
            "writeFloat", Type.getMethodDescriptor( Type.FLOAT_TYPE, new Type[]{ oType,sType, Type.FLOAT_TYPE, Type.FLOAT_TYPE } )
        }  
        );
        MAP_BY_DESCRIPTOR.put( Type.getDescriptor(double.class), new Object[]{
            "readDouble" , Type.getMethodDescriptor( Type.DOUBLE_TYPE, new Type[]{ oType,sType, Type.DOUBLE_TYPE  } ),
            "writeDouble", Type.getMethodDescriptor( Type.DOUBLE_TYPE, new Type[]{ oType,sType, Type.DOUBLE_TYPE,Type.DOUBLE_TYPE   } )
          }  
        );
        MAP_BY_DESCRIPTOR.put( Type.getDescriptor(long.class), new Object[]{
            "readLong" , Type.getMethodDescriptor( Type.LONG_TYPE, new Type[]{ oType,sType, Type.LONG_TYPE  } ),
            "writeLong", Type.getMethodDescriptor( Type.LONG_TYPE, new Type[]{ oType,sType, Type.LONG_TYPE, Type.LONG_TYPE  } )
         }  
        );
        
         MAP_BY_DESCRIPTOR.put( Type.getDescriptor(Object.class), new Object[]{
            "readObject" , Type.getMethodDescriptor( oType, new Type[]{ oType,sType, oType  } ),
            "writeObject", Type.getMethodDescriptor( oType, new Type[]{ oType,sType, oType, oType  } )
         }  
        );
    
    
    
    }
    
    /** Holds value of property returnType. */
    private String returnType;
    
    /** Holds value of property name. */
    private String name;
    
    /** Holds value of property parameterTypes. */
    private String[] parameterTypes;
    
    /** Creates a new instance of Signature */
    public Signature() {
    }
    
    /** Creates a new instance of Signature */
    public Signature(String returnType, String name, String parameterTypes[]) {
        this.returnType     = returnType;
        this.name           = name;
        this.parameterTypes = parameterTypes;
    }
    
    
    /** Getter for property returnType.
     * @return Value of property returnType.
     *
     */
    public String getReturnType() {
        return this.returnType;
    }
    
    /** Setter for property returnType.
     * @param returnType New value of property returnType.
     *
     */
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }
    
    /** Getter for property name.
     * @return Value of property name.
     *
     */
    public String getName() {
        return this.name;
    }
    
    /** Setter for property name.
     * @param name New value of property name.
     *
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /** Getter for property parameterTypes.
     * @return Value of property parameterTypes.
     *
     */
    public String[] getParameterTypes() {
        return this.parameterTypes;
    }
    
    /** Setter for property parameterTypes.
     * @param parameterTypes New value of property parameterTypes.
     *
     */
    public void setParameterTypes(String[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }



  public static String readMethod(String field){
      return "$cglib_read_" + field;
    }

    
  public static String writeMethod(String field){
     return "$cglib_write_" + field;
    }

  public static String readMethodSignature(String desc){
  
      return Type.getMethodDescriptor( Type.getType(desc), new Type[]{ } );
  }
  
   public static String writeMethodSignature(String desc){
  
      return Type.getMethodDescriptor( Type.VOID_TYPE, new Type[]{ Type.getType(desc) } );
  }
 
   public static String readCallbackSignature(String desc){
     if(isObject(desc)){
       return (String)((Object[])MAP_BY_DESCRIPTOR.get(Type.getDescriptor(Object.class)))[1];
     }else{
       return (String)((Object[])MAP_BY_DESCRIPTOR.get(desc))[1];
     }
   }
   
   public static String writeCallbackSignature(String desc){
     if(isObject(desc)){
       return (String)((Object[])MAP_BY_DESCRIPTOR.get(Type.getDescriptor(Object.class)))[3];
     }else{
       return (String)((Object[])MAP_BY_DESCRIPTOR.get(desc))[3];
     }
   }


   public static String readCallbackName(String desc){

     Object[] value =  (Object[])MAP_BY_DESCRIPTOR.get(desc);
      if(value == null){
        return "readObject";
      }else{
        return value[0].toString();
      }
     
   }

   public static boolean isObject(String desc){
    
      return ! MAP_BY_DESCRIPTOR.containsKey(desc);
   }
   
   public static String writeCallbackName(String desc){
       
     Object[] value =  (Object[])MAP_BY_DESCRIPTOR.get(desc);
      if(value == null){
        return "writeObject";
      }else{
        return value[2].toString();
      }

   
   }
   
   
   public static String getInternalName(Class cls){
     return cls.getName().replace('.','/');
   }
  
}
