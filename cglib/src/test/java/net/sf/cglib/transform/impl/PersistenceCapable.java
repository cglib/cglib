package net.sf.cglib.transform.impl;

/**
 * @author baliuka
 */
public interface PersistenceCapable {

    Object getPersistenceManager();

    void setPersistenceManager(Object manager);
}
