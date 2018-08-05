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

import net.sf.cglib.core.Constants;
import org.apache.tools.ant.BuildException;
import org.objectweb.asm.*;

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

    static class EmptyVisitor extends ClassVisitor {
	AnnotationVisitor av = new AnnotationVisitor(Constants.ASM_API) {
	    public AnnotationVisitor visitAnnotation(
		    String name, String desc) {
		return this;
	    }

	    public AnnotationVisitor visitArray(String name) {
		return this;
	    }
	};

	public EmptyVisitor() {
	    super(Constants.ASM_API);
	}

	public AnnotationVisitor visitAnnotation(
		String desc, boolean visible) {
	    return av;
	}

	public FieldVisitor visitField(
		int access, String name, String desc,
		String signature, Object value) {
	    return new FieldVisitor(Constants.ASM_API) {
		public AnnotationVisitor visitAnnotation(
			String desc, boolean visible) {
		    return av;
		}
	    };
	}

	@Override
	public MethodVisitor visitMethod(
		int access, String name, String desc, String signature,
		String[] exceptions) {
	    return new MethodVisitor(Constants.ASM_API) {
		public AnnotationVisitor visitAnnotationDefault() {
		    return av;
		}

		public AnnotationVisitor visitAnnotation(
			String desc, boolean visible) {
		    return av;
		}

		public AnnotationVisitor visitParameterAnnotation(
			int parameter, String desc, boolean visible) {
		    return av;
		}
	    };
	}
    }

    protected void processFile(File file) throws Exception {
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        ClassReader r = new ClassReader(in);
        r.accept(new EmptyVisitor() {
            private String className;

            public void visit(int version,
                              int access,
                              String name,
                              String signature,
                              String superName,
                              String[] interfaces) {
                className = name.replace('/', '.');
            }

            public FieldVisitor visitField(int access,
                                           String name,
                                           String desc,
                                           String signature,
                                           Object value) {
                out.println("class=" + className + ", field=" + name);
                return null;
            }
        }, ClassReader.SKIP_DEBUG);
    }
}

