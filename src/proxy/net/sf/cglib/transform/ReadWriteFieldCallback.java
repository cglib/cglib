
package net.sf.cglib.transform;

/**
 *
 * @author  baluka
 */
public interface ReadWriteFieldCallback {
    
    int     writeInt( Object _this, String name, int oldValue,int newValue);
    
    char    writeChar( Object _this, String name, char oldValue, char newValue);
    
    byte    writeByte(Object _this,  String name, byte oldValue, byte newValue );
    
    boolean writeBoolean(Object _this,  String name, boolean oldValue,boolean newValue );
    
    short   writeShort(Object _this,  String name, short oldValue,short newValue );
    
    float   writeFloat( Object _this,  String name, float oldValue, float newValue );
    
    double  writeDouble( Object _this,  String name, double oldValue, double newValue);
    
    long    writeLong( Object _this,  String name, long oldValue, long newValue);
    
    Object  writeObject( Object _this, String name, Object oldValue, Object newValue);
    
    
    int   readInt( Object _this, String name, int oldValue);
    
    char   readChar( Object _this, String name, char oldValue);
    
    byte   readByte(Object _this, String name, byte oldValue);
    
    boolean   readBoolean(Object _this, String name, boolean oldValue);
    
    short   readShort(Object _this, String name, short oldValue);
    
    float   readFloat( Object _this, String name, float oldValue);
    
    double   readDouble( Object _this, String name, double oldValue);
    
    long   readLong( Object _this, String name, long oldValue);
    
    Object   readObject( Object _this, String name, Object oldValue);
    
    
    
    
}
