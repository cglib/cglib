package net.sf.cglib.transform.impl;

public interface FieldProvider {
    
    String[] getFieldNames();
    
    Class[]  getFieldTypes();
    
    void setField(int index, Object value);
    
    Object getField(int index);
    
    
    void setField(String name, Object value);
    
    Object getField(String name);
    
    
}
