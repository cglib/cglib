
package net.sf.cglib.transform;

/**
 *
 * @author  baliuka
 */
public interface Tranformed {
    
    void setReadWriteFieldCallback(ReadWriteFieldCallback callback);
    
    ReadWriteFieldCallback  getReadWriteFieldCallback();
    
}
