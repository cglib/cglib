/*
 * MetaClassTest.java
 * JUnit based test
 *
 * Created on Sekmadienis, 2002, Lapkrièio 3, 13.15
 */

package net.sf.cglib.proxy;

import junit.framework.*;
import java.util.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

/**
 *
 * @author baliuka
 */
public class TestMetaClass extends TestCase {
    
    public TestMetaClass(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(TestMetaClass.class);
        
        return suite;
    }
    
    
    
    
    /** Test of getInstance method, of class net.sf.cglib.proxy.MetaClass. */
    public void testGetInstance() throws Throwable{
    
       String getters[] = {"getIntP","getLongP","getByteP",
                           "getShortP","getFloatP","isBooleanP","getCharP","getDoubleP",
                           "getStringP","getId", "getName" } ;
                           
       String setters[] = {"setIntP","setLongP","setByteP",
                           "setShortP","setFloatP","setBooleanP","setCharP","setDoubleP",
                           "setStringP","setId", "setName" } ;
                           
       Class types[] = {int.class,long.class,byte.class,
                           short.class,float.class,
                           boolean.class,
                           char.class,double.class,
                           String.class,Long.class, String.class } ;
                           
       Object values[] = { new Integer(2) , new Long(4) , new Byte((byte)8),
                           new Short((short)4),new Float(1.2),
                           Boolean.TRUE,
                           new Character('S'),new Double(5.6),
                           "test",new Long(88), "test2" } ;
        
      MetaClass mClass = MetaClass.getInstance( this.getClass().getClassLoader(),
                                                A.class,getters,setters,types );  
      
      A bean = (A)mClass.newInstance();
      
      mClass.setPropertyValues( bean, values );
      Object values1[] = mClass.getPropertyValues( bean );
      
      for( int i = 0; i < types.length; i++  ){
        assertEquals(" property " + getters[i] + "/" + setters[i] , values[i] , values1[i] );
      }  
      
      
    }
    
    /** Generated implementation of abstract class net.sf.cglib.proxy.MetaClass. Please fill dummy bodies of generated methods. */
    private class MetaClassImpl extends MetaClass {
        
        public MetaClassImpl(Class target, String[] getters, String[] setters, Class[] types) {
            super(target, getters, setters, types);
        }
        
        public Object newInstance() {
            //fill the body in order to provide useful implementation
            
            return null;
        }
        
        public Object[] getPropertyValues(Object bean) {
            //fill the body in order to provide useful implementation
            
            return null;
        }
        
        public void setPropertyValues(Object bean, Object[] values) {
            //fill the body in order to provide useful implementation
        }
        
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    
}
