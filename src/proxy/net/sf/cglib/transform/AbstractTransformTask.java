package net.sf.cglib.transform;

import java.io.*;
import java.util.*;
import net.sf.cglib.core.*;
import org.apache.tools.ant.BuildException;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

abstract public class AbstractTransformTask extends AbstractProcessTask {
    private boolean verbose;

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    abstract protected ClassTransformer getClassTransformer(String name);

    protected void processFile(File file) throws Exception {
        ClassWriter w = new DebuggingClassWriter(true);
        String name = getClassName(file);
        ClassTransformer t = getClassTransformer(name);
        if (t != null) {
            new TransformingGenerator(new ClassReaderGenerator(getClassReader(file), true), t).generateClass(w);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(w.toByteArray());
            fos.close();
            if (verbose) {
                System.out.println("Enhancing class " + name);
            }
        }
    }

    private static ClassReader getClassReader(File file) throws Exception {
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        ClassReader r = new ClassReader(in);
        in.close();
        return r;
    }

    private static final EarlyExitException EARLY_EXIT = new EarlyExitException();
    private static class EarlyExitException extends RuntimeException { }

    private static String getClassName(File file) throws Exception {
        final String[] array = new String[1];
        try {
            getClassReader(file).accept(new ClassAdapter(null) {
                public void visit(int access, String name, String superName, String[] interfaces, String sourceFile) {
                    array[0] = name.replace('/', '.');
                    throw EARLY_EXIT;
                }
            }, true);
        } catch (EarlyExitException e) { }
        return array[0];
    }
}
