
package net.sf.cglib.transform;

/**
 *
 * @author  baliuka
 */
public interface Transformed {
    
    void setReadWriteFieldCallback(ReadWriteFieldCallback callback);
    
    ReadWriteFieldCallback  getReadWriteFieldCallback();
    
    
    void setDelegate(Object delegate);
    
    Object getDelegate();
    
}
