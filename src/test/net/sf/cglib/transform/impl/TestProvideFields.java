
package net.sf.cglib.transform.impl;

import net.sf.cglib.transform.*;
import junit.framework.*;

/**
 *
 * @author  baliuka
 */
public class TestProvideFields extends AbstractTransformTest{
    
    String field = "test";
    
    /** Creates a new instance of TestProvideFields */
    public TestProvideFields() {
    }
    
    /** Creates a new instance of TestProvideFields */
    public TestProvideFields(String name) {
        super(name);
    }
    
    
    public void test(){
    
       FieldProvider provider = (FieldProvider)this;
       assertEquals(field,provider.getField("field"));
       String value = "tst2";
       provider.setField("field", value );
       assertEquals(field,value);
       
       
        
    }
    
    protected ClassTransformerFactory getTransformer() throws Exception {
       
        return new ClassTransformerFactory (){
        
           public ClassTransformer newInstance(){
       
                return   new FieldProviderTransformer();  
           }
        }; 
    }
    
    
    public static void main(String[] args) throws Exception{
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() throws Exception{
        return new TestSuite( new TestProvideFields( ).transform() );
    }
    

    
    
}
