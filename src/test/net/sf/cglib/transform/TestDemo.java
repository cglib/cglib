
package net.sf.cglib.transform;

import junit.framework.*;

/**
 *
 * @author  baliuka
 */
public class TestDemo extends TestCase{
    
     /** Creates a new instance of AbstractTransformTest */
    public TestDemo(String s) {
       super(s);
    }
    
    public void test()throws Exception{
    
        TransformDemo.main( null );
    
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestDemo.class);
    }
    
}
