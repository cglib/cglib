
package net.sf.cglib.transform;

/**
 * all transformed classes implement this interface to 
 * provide fields and to register callbacks
 * @author  baliuka
 */
public interface Transformed {
    
    void setReadWriteFieldCallback(ReadWriteFieldCallback callback);
    
    ReadWriteFieldCallback  getReadWriteFieldCallback();
    
    
    void setDelegate(Object delegate);
    
    Object getDelegate();
    
    //provides fields
    
    
    int     setInt(  String name, int newValue);
    
    char    setChar(  String name,  char newValue);
    
    byte    setByte(  String name,  byte newValue );
    
    boolean setBoolean( String name, boolean newValue );
    
    short   setShort( String name, short newValue );
    
    float   setFloat(   String name,  float newValue );
    
    double  setDouble(  String name,  double newValue);
    
    long    setLong(   String name, long newValue);
    
    Object  setObject( String name,  Object newValue);
    
    
    int       getInt(  String name );
    
    char      getChar(  String name );
    
    byte      getByte( String name );
    
    boolean   getBoolean( String name );
    
    short     getShort( String name );
    
    float     getFloat( String name );
    
    double    getDouble(  String name);
    
    long      getLong(  String name);
    
    Object    getObject(  String name);
    
    
    
}
