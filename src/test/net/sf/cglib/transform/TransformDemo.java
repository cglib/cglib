
package net.sf.cglib.transform;

import net.sf.cglib.*;

import java.util.*;

/**
 *
 * @author  baliuka
 */
public class TransformDemo {
    
    public static void register(Class cls){
      System.out.println("register " + cls);
    }
    
   
    public static void start(){
     
        MA ma = new MA();
        makePersistent(ma);
        ma.setCharP('A');
        ma.getCharP();
        ma.setDoubleP(554);
        ma.setDoubleP(1.2);
        ma.getFloatP();
        ma.setName("testName");
        ma.publicField = "set value";
        ma.publicField = ma.publicField + " append value";
    
    }
    
    public static void makePersistent(Object obj){
        System.out.println( "makePersistent " + obj.getClass() + " " +  Arrays.asList(obj.getClass().getInterfaces()) ); 
        Transformed t = (Transformed)obj;
        t.setReadWriteFieldCallback( new StateManager());
    
    }
    
    public static void main( String args [] )throws Exception{
    
        
        TransformClassVisitor transformation = new TransformClassVisitor( new Filter() );
        
        
        
        transformation.setDelegate( 
                                    new Class[]{PersistenceCapable.class},
                                    PersistenceCapableImpl.class
                           );
        
        transformation.setClassInit(TransformDemo.class.getMethod("register",new Class[]{Class.class}) );
        
        
        TransformingLoader loader = new TransformingLoader(
          TransformDemo.class.getClassLoader(),
          new ClassFilter(){
            public boolean accept(String name){
                System.out.println("load : "  + name);
                boolean f = 
                  MA.class.getName().equals(name) || 
                     TransformDemo.class.getName().equals(name);
                if(f){
                 System.out.println("transforming " + name);
                }
                return f;
            } 
         },
         transformation
        );
        
        
        loader.loadClass(TransformDemo.class.getName()).getMethod("start",new Class[]{}).invoke(null,null);
        
        
    
    }
    
    
   public static class Filter implements ReadWriteFieldFilter{
       
        public boolean acceptRead(String clas, String name){
                 
            return true;
        }
        
        public boolean acceptWrite(String clas, String name){
            
            return true;
        }
    };
    

  public   static class  StateManager implements ReadWriteFieldCallback{
        
       
        public boolean readBoolean(Object _this, String name, boolean oldValue) {
            System.out.println("read " +  name + " = " + oldValue);
            return oldValue;
        }        
       
        public byte readByte(Object _this, String name, byte oldValue) {
            System.out.println("read " +  name + " = " + oldValue);
            return oldValue;
        }
        
        public char readChar(Object _this, String name, char oldValue) {
            System.out.println("read " +  name + " = " + oldValue);
            return oldValue;
        }
        
        public double readDouble(Object _this, String name, double oldValue) {
            System.out.println("read " +  name + " = " + oldValue);
            return oldValue;
        }
        
        public float readFloat(Object _this, String name, float oldValue) {
            System.out.println("read " +  name + " = " + oldValue);
            return oldValue;
        }
        
        public int readInt(Object _this, String name, int oldValue) {
            System.out.println("read " +  name + " = " + oldValue);
            return oldValue;
        }
        
        public long readLong(Object _this, String name, long oldValue) {
            System.out.println("read " +  name + " = " + oldValue);
            return oldValue;
        }
        
        public Object readObject(Object _this, String name, Object oldValue) {
            System.out.println("read " +  name + " = " + oldValue);
            return oldValue;
        }
        
        public short readShort(Object _this, String name, short oldValue) {
            System.out.println("read " +  name + " = " + oldValue);
            return oldValue;
        }
        
        public boolean writeBoolean(Object _this, String name, boolean oldValue, boolean newValue) {
            System.out.println( "write " + name + " = " + newValue);
            return newValue;
        }
        
        public byte writeByte(Object _this, String name, byte oldValue, byte newValue) {
            System.out.println( "write " + name + " = " + newValue);
            return newValue;
        }
        
        public char writeChar(Object _this, String name, char oldValue, char newValue) {
            System.out.println( "write " + name + " = " + newValue);
            return newValue;
        }
        
        public double writeDouble(Object _this, String name, double oldValue, double newValue) {
            System.out.println( "write " + name + " = " + newValue);
            return newValue;
        }
        
        public float writeFloat(Object _this, String name, float oldValue, float newValue) {
            System.out.println( "write " + name + " = " + newValue);
            return newValue;
        }
        
        public int writeInt(Object _this, String name, int oldValue, int newValue) {
            System.out.println( "write " + name + " = " + newValue);
            return newValue;
        }
        
        public long writeLong(Object _this, String name, long oldValue, long newValue) {
            System.out.println( "write " + name + " = " + newValue);
            return newValue;
        }
        
        public Object writeObject(Object _this, String name, Object oldValue, Object newValue) {
            System.out.println( "write " + name + " = " + newValue);
            return newValue;
        }
        
        public short writeShort(Object _this, String name, short oldValue, short newValue) {
            System.out.println( "write " + name + " = " + newValue);
            return newValue;
        }
        
       }

    
    
}
