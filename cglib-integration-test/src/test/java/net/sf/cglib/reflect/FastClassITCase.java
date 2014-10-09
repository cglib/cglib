package net.sf.cglib.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import net.sf.cglib.samples.SimpleClass;

import org.junit.Test;

public class FastClassITCase {

    @Test
    public void testSignedClassProtectionDomain() throws Throwable {
        // SimpleClass must be signed for this test to work
        assertNotNull(SimpleClass.class.getProtectionDomain().getCodeSource());
        assertNotNull("SimpleClass.class must be signed for this test to work",SimpleClass.class.getProtectionDomain().getCodeSource().getCertificates());
        assertTrue("SimpleClass.class must be signed for this test to work", SimpleClass.class.getProtectionDomain().getCodeSource().getCertificates().length > 0);

        // when
        Object instance = FastClass.create(SimpleClass.class).newInstance();

        // then
        assertNotNull(instance.getClass().getProtectionDomain().getCodeSource());
        assertNotNull(instance.getClass().getProtectionDomain().getCodeSource().getCertificates());
        assertTrue(instance.getClass().getProtectionDomain().getCodeSource().getCertificates().length > 0);
        assertEquals(SimpleClass.class.getProtectionDomain().getCodeSource().getCertificates().length, instance.getClass().getProtectionDomain().getCodeSource().getCertificates().length);
        for (int i = 0; i < SimpleClass.class.getProtectionDomain().getCodeSource().getCertificates().length; i++) {
            assertEquals(SimpleClass.class.getProtectionDomain().getCodeSource().getCertificates()[i], instance.getClass().getProtectionDomain().getCodeSource().getCertificates()[i]);
        }
    }

}
