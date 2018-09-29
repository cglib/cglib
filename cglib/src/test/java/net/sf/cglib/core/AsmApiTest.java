package net.sf.cglib.core;

import org.junit.Test;
import org.objectweb.asm.Opcodes;

import static org.junit.Assert.*;

public class AsmApiTest {

    @Test
    public void testValue() {
        assertEquals(Opcodes.ASM7, AsmApi.value());
    }

    /**
     * With the release of ASM 7.0 beta, Opcodes.ASM7_EXPERIMENTAL
     * has been replaced by Opcodes.ASM7 so we simply ignore
     * the system property and default to the newest stable
     * version.
     */
    @Test
    public void testValueWithAsm7Experimental() {
        int asmApi = setAsm7ExperimentalAndGetValue("true");
        assertEquals(Opcodes.ASM7, asmApi);

        asmApi = setAsm7ExperimentalAndGetValue("");
        assertEquals(Opcodes.ASM7, asmApi);

        asmApi = setAsm7ExperimentalAndGetValue("false");
        assertEquals(Opcodes.ASM7, asmApi);
    }

    private int setAsm7ExperimentalAndGetValue(String value) {
        String propName = "net.sf.cglib.experimental_asm7";
        System.setProperty(propName, value);
        try {
            return AsmApi.value();
        } finally {
            System.clearProperty(propName);
        }
    }
}
