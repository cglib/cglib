
package net.sf.cglib.transform;

/**
 *
 * @author  baliuka
 */
public class PersistenceCapableImpl {
    
    Object persistenceManager;
    /** Creates a new instance of PersistenceCapableImpl */
    public PersistenceCapableImpl() {
    }
    
   public void setPersistenceManager(Object _this, Object manager){
      persistenceManager = manager;
    }
    
   public Object getPersistenceManager(Object _this){
     return persistenceManager;
   }
    
}
