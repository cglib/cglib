package net.sf.cglib.transform;

public interface FieldProvider {
    void setField(String name, Object value);
    Object getField(String name);
}
