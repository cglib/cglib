
package net.sf.cglib.transform;

/**
 *
 * @author  baliuka
 */
public interface Transformation {
    
    byte [] transformClassFile(byte [] data);
    
}
