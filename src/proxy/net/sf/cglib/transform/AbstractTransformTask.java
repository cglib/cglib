package net.sf.cglib.transform;

import java.io.*;
import java.util.*;
import net.sf.cglib.core.*;
import org.apache.tools.ant.BuildException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

abstract public class AbstractTransformTask extends AbstractProcessTask implements ClassFilter {
    private boolean verbose;

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    private static class EarlyExitException extends RuntimeException { }

    private class CaptureNameWriter extends ClassWriter {
        private String name;

        public CaptureNameWriter(boolean computeMaxs) {
            super(computeMaxs);
        }

        public void visit(int access, String name, String superName, String[] interfaces, String sourceFile) {
            this.name = name.replace('/', '.');
            if (!accept(this.name)) {
                throw new EarlyExitException();
            }
            super.visit(access, name, superName, interfaces, sourceFile);
        }

        public String getName() {
            return name;
        }
    }

    protected void processFile(File file) throws Exception {
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        ClassReader r = new ClassReader(in);
        CaptureNameWriter w = new CaptureNameWriter(true);
        ClassTransformer t = getClassTransformer();
        try {
            new TransformingGenerator(new ClassReaderGenerator(r, true), t).generateClass(w);
            in.close();
            byte[] b = w.toByteArray();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(b);
            fos.close();
            if (verbose) {
                System.out.println("Enhancing class " + w.getName());
            }
        } catch (EarlyExitException e) {
            // ignore this file
        }
    }
                
    abstract protected ClassTransformer getClassTransformer();
}
