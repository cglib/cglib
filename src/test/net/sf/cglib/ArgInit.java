/*
 * ArgInit.java
 *
 * Created on Antradienis, 2003, Sausio 14, 18.02
 */

package net.sf.cglib;

/**
 *
 * @author  user
 */
public class ArgInit {
  final private String value;
  //TODO: must be optional
  // protected ArgInit() {
  //  value = null;
  // }
   
    /** Creates a new instance of ArgInit */
    public ArgInit(String value) {
        this.value = value;
    }
    
    public String toString(){
      return value;
    }
}
