
package net.sf.cglib.transform.impl;

/**
 *
 * @author  baliuka
 */
public class PersistenceCapableImpl implements PersistenceCapable {
    
    Object persistenceManager;
    Object pc;//transformed class instance
    
    /** Creates a new instance of PersistenceCapableImpl,
     transformed class uses this constructor */
    
    public PersistenceCapableImpl(Object _this) {
        this.pc = _this;
        
    }
    
   public void setPersistenceManager( Object manager){
        
      persistenceManager = manager;
      System.out.println("setPersistenceManager:" + manager);
    }
    
   public Object getPersistenceManager(){
     return persistenceManager;
   }
    
}
