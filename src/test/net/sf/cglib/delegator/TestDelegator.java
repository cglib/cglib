package net.sf.cglib.delegator;

import junit.framework.*;
import org.apache.bcel.generic.*;
import java.io.*;

public class TestDelegator extends TestCase {
    /*
    public void testOutput() throws Exception {
        ClassGen cg = Delegator.makeClassGen(new Class[]{ I1.class, I2.class });
        OutputStream out = new FileOutputStream("/tmp/" + cg.getClassName() + ".class");
        out.write(cg.getJavaClass().getBytes());
        out.close();
    }
    */
    
    public void testSimple() throws Exception {
        Object obj = Delegator.makeDelegator(new Class[]{ I1.class, I2.class },
                                             new Object[]{ new C1(), new C2() },
                                             null);
        assertTrue(((I1)obj).herby().equals("C1"));
        assertTrue(((I2)obj).derby().equals("C2"));
    }

    public void testDetermineInterfaces() throws Exception {
        Object obj = Delegator.makeDelegator(new Object[]{ new C1(), new C2() }, null);
        assertTrue(((I1)obj).herby().equals("C1"));
        assertTrue(((I2)obj).derby().equals("C2"));
    }
 
    public void testOverride() throws Exception {
        Object obj = Delegator.makeDelegator(new Object[]{ new C1(), new C4() }, null);
        assertTrue(((I1)obj).herby().equals("C1"));
        assertTrue(((I2)obj).derby().equals("C4"));
    }

    public void testNonOverride() throws Exception {
        Object obj = Delegator.makeDelegator(new Object[]{ new C4(), new C1() }, null);
        assertTrue(((I1)obj).herby().equals("C4"));
        assertTrue(((I2)obj).derby().equals("C4"));
    }

    public void testSubclass() throws Exception {
        Object obj = Delegator.makeDelegator(new Object[]{ new C3B(), new C1() }, null);
        assertTrue(((I1)obj).herby().equals("C1"));
        assertTrue(((I2)obj).derby().equals("C2"));
        assertTrue(((I3)obj).extra().equals("C3B"));
    }

    public TestDelegator(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestDelegator.class);
    }
}
