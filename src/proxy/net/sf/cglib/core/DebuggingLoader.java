package net.sf.cglib.core;

import org.objectweb.asm.ClassWriter;
import java.io.*;

public class DebuggingLoader {
    private static String debugLocation;

    private DebuggingLoader() {
    }
    
    static {
        debugLocation = System.getProperty("cglib.debugLocation");
        if (debugLocation != null) {
            System.err.println("CGLIB debugging enabled, writing to '" + debugLocation + "'");
        }
    }

    public static byte[] getBytes(ClassGenerator gen) {
        try {
            ClassWriter w = new ClassWriter(true);
            String debugName = "foo"; // TODO
            gen.generateClass(w);
            byte[] data = w.toByteArray();
            
            if (debugLocation != null) {
                File file = new File(new File(debugLocation), debugName + ".class");
                // System.err.println("CGLIB writing " + file);
                OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                out.write(data);
                out.close();
            }

            return data;
        } catch (Error e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }
    }
}
