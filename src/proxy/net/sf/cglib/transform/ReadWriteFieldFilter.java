/*
 * ReadWriteFieldFilter.java
 *
 * Created on Antradienis, 2003, Rugsëjo 9, 09.56
 */

package net.sf.cglib.transform;

/**
 *
 * @author  baliuka
 */
public interface ReadWriteFieldFilter {
    
    boolean acceptRead(String clas, String name);
    
    boolean acceptWrite(String clas, String name);
    
}
