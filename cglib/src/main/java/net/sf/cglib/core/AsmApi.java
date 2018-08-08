package net.sf.cglib.core;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.objectweb.asm.Opcodes;

final class AsmApi {

    private static final String EXPERIMENTAL_ASM7_PROPERTY_NAME = "net.sf.cglib.experimental_asm7";

    /**
     * Returns the latest stable ASM API value in {@link Opcodes} unless overridden via the
     * net.sf.cglib.experimental_asm7 property.
     */
    static int value() {
        boolean experimentalAsm7;
        try {
            experimentalAsm7 = Boolean.parseBoolean(AccessController.doPrivileged(
                    new PrivilegedAction<String>() {
                        public String run() {
                            return System.getProperty(EXPERIMENTAL_ASM7_PROPERTY_NAME);
                        }
                    }));
        } catch (Exception ignored) {
            experimentalAsm7 = false;
        }
        return experimentalAsm7 ? Opcodes.ASM7_EXPERIMENTAL : Opcodes.ASM6;
    }

    private AsmApi() {
    }
}
