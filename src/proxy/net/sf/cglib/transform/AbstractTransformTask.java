package net.sf.cglib.transform;

import java.io.*;
import java.util.*;
import net.sf.cglib.core.*;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

abstract public class AbstractTransformTask extends Task implements ClassFilter {
    private FileUtils FILE_UTILS = new FileUtils() { };
    private Vector filesets = new Vector();
    private File destdir;

    public void addFileset(FileSet set) {
        filesets.addElement(set);
    }
    
    private Iterator getFiles() {
        Map fileMap = new HashMap();
        Project p = getProject();
        for (int i = 0; i < filesets.size(); i++) {
            FileSet fs = (FileSet)filesets.elementAt(i);
            DirectoryScanner ds = fs.getDirectoryScanner(p);
            String[] srcFiles = ds.getIncludedFiles();
            File dir = fs.getDir(p);
            for (int j = 0; j < srcFiles.length; j++) {
                File src = new File(dir, srcFiles[j]);
                fileMap.put(src.getAbsolutePath(), src);
            }
        }
        return fileMap.values().iterator();
    }

    public void setDestdir(File value) {
        destdir = value;
    }

    protected void validateAttributes() throws BuildException {
        if (filesets.size() == 0) {
            throw new BuildException("Specify at least one source fileset.");
        }
        if (destdir == null) {
            throw new BuildException("destdir is not set");
        }
    }

    public void execute() throws BuildException {
         validateAttributes();
         for (Iterator it = getFiles(); it.hasNext();) {
             try {
                 processFile((File)it.next());
             } catch (Exception e) {
                 throw new BuildException(e);
             }
         }
    }

    private static class EarlyExitException extends RuntimeException { }

    private class CaptureNameWriter extends ClassWriter {
        private String name;

        public CaptureNameWriter(boolean computeMaxs) {
            super(computeMaxs);
        }

        public void visit(int access, String name, String superName, String[] interfaces, String sourceFile) {
            this.name = name;
            if (!accept(name)) {
                throw new EarlyExitException();
            }
            super.visit(access, name, superName, interfaces, sourceFile);
        }

        public String getName() {
            return name;
        }
    }

    private void processFile(File file) throws Exception {

        if (!file.getAbsolutePath().endsWith(".class")) {
            // is this right?
            return;
        }
        boolean modified = false;
        ClassReader r = new ClassReader(new BufferedInputStream(new FileInputStream(file)));
        CaptureNameWriter w = new CaptureNameWriter(true);
        ClassTransformer t = getClassTransformer();
        if (t != null) {
            try {
                new TransformingGenerator(new ClassReaderGenerator(r, true), t).generateClass(w);
                modified = true;
            } catch (EarlyExitException e) {
            }
        }
        String name = w.getName();
        File outFile = new File(destdir, w.getName() + ".class");
        outFile.getParentFile().mkdirs();
        if (modified) {
            byte[] b = w.toByteArray();
            FileOutputStream fos = new FileOutputStream(outFile);
            fos.write(b);
            fos.close();
        } else {
            FILE_UTILS.copyFile(file, outFile);
        }
    }

    abstract protected ClassTransformer getClassTransformer();
}
