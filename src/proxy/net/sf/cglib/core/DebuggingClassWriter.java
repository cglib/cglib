package net.sf.cglib.core;

import org.objectweb.asm.ClassWriter;
import java.io.*;

public class DebuggingClassWriter extends ClassWriter {
    private static String debugLocation;
    private String debugName;

    static {
        debugLocation = System.getProperty("cglib.debugLocation");
        if (debugLocation != null) {
            System.err.println("CGLIB debugging enabled, writing to '" + debugLocation + "'");
        }
    }

    public DebuggingClassWriter(boolean computeMaxs) {
        super(computeMaxs);
    }

    public void visit(int access, String name, String superName, String[] interfaces, String sourceFile) {
        debugName = name.replace('/', '.');
        super.visit(access, name, superName, interfaces, sourceFile);
    }

    public byte[] toByteArray() {
        byte[] b = super.toByteArray();
        if (debugLocation != null) {
            try {
                File file = new File(new File(debugLocation), debugName + ".class");
                OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                out.write(b);
                out.close();
            } catch (IOException e) {
                throw new CodeGenerationException(e);
            }
        }
        return b;
    }
}
