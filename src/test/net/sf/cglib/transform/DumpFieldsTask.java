package net.sf.cglib.transform;



import java.io.*;

import org.apache.tools.ant.BuildException;

import org.objectweb.asm.ClassReader;

import org.objectweb.asm.ClassVisitor;

import org.objectweb.asm.CodeVisitor;



public class DumpFieldsTask extends AbstractProcessTask {

    private File outfile;

    private PrintStream out;



    public void setOutputFile(File outfile) {

        this.outfile = outfile;

    }

    

    public void execute() throws BuildException {

        try {

            out = new PrintStream(new FileOutputStream(outfile));
            try{
                
             super.execute();
             
            }finally{ 
               out.close();
            }

        } catch (IOException e) {

            throw new BuildException(e);

        }

    }



    protected void processFile(File file) throws Exception {

        InputStream in = new BufferedInputStream(new FileInputStream(file));

        ClassReader r = new ClassReader(in);

        r.accept(new ClassVisitor() {

            private String className;



            public void visitEnd() { }

            public void visitInnerClass(String name, String outerName, String innerName, int access) { }



            public CodeVisitor visitMethod(int access, String name, String desc, String[] exceptions) {

                return null;

            }



            public void visit(int access, String name, String superName, String[] interfaces, String sourceFile) {

                className = name.replace('/', '.');

            }



            public void visitField(int access, String name, String desc, Object value) {

                out.println("class=" + className + ", field=" + name);

            }

        }, true);

    }

}

