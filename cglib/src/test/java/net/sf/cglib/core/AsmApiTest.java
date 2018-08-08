package net.sf.cglib.core;

import org.junit.Test;
import org.objectweb.asm.Opcodes;

import static org.junit.Assert.*;

public class AsmApiTest {

    @Test
    public void testValue() {
        assertEquals(Opcodes.ASM6, AsmApi.value());
    }

    @Test
    public void testValueWithSystemPropertyTrue() {
        int asmApi = setSystemPropertyAndGetValue("true");
        assertEquals(Opcodes.ASM7_EXPERIMENTAL, asmApi);
    }

    @Test
    public void testValueWithSystemPropertyEmptyString() {
        int asmApi = setSystemPropertyAndGetValue("");
        assertEquals(Opcodes.ASM6, asmApi);
    }

    @Test
    public void testValueWithSystemPropertyFalse() {
        int asmApi = setSystemPropertyAndGetValue("false");
        assertEquals(Opcodes.ASM6, asmApi);
    }

    private int setSystemPropertyAndGetValue(String value) {
        String propName = "net.sf.cglib.experimental_asm7";
        System.setProperty(propName, value);
        try {
            return AsmApi.value();
        } finally {
            System.clearProperty(propName);
        }
    }
}
