
package net.sf.cglib.transform;

/**
 *
 * @author  baliuka
 */
public interface PersistenceCapable {
    
    void setPersistenceManager(Object manager);
    
    Object getPersistenceManager();
}
