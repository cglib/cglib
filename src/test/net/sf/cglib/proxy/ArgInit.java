
package net.sf.cglib.proxy;

/**
 *
 * @author  baliuka
 */
public class ArgInit {
  final private String value;
   
    /** Creates a new instance of ArgInit */
    public ArgInit(String value) {
        this.value = value;
    }
    
    public String toString(){
      return value;
    }
}
