/*
 * Copyright 2003 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.cglib.transform;


import java.io.*;

import org.apache.tools.ant.BuildException;

import org.objectweb.asm.ClassReader;

import org.objectweb.asm.ClassVisitor;

import org.objectweb.asm.CodeVisitor;

import org.objectweb.asm.Attribute;


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

            public void visitAttribute(Attribute attrs) { }



            public CodeVisitor visitMethod(int access, String name, String desc, String[] exceptions, Attribute attrs) {

                return null;

            }



            public void visit(int access, String name, String superName, String[] interfaces, String sourceFile) {

                className = name.replace('/', '.');

            }



            public void visitField(int access, String name, String desc, Object value, Attribute attrs) {

                out.println("class=" + className + ", field=" + name);

            }

        }, true);

    }

}

