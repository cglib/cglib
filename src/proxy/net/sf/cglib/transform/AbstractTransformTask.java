/*
 * Copyright 2003,2004 The Apache Software Foundation
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
        String name = ClassNameReader.getClassName(getClassReader(file));
        ClassTransformer t = getClassTransformer(name);
        if (t != null) {
            new TransformingClassGenerator(new ClassReaderGenerator(getClassReader(file), skipDebug()), t).generateClass(w);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(w.toByteArray());
            fos.close();
            if (verbose) {
                System.out.println("Enhancing class " + name);
            }
        }
    }

    protected boolean skipDebug() {
        return false;
    }

    private static ClassReader getClassReader(File file) throws Exception {
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        ClassReader r = new ClassReader(in);
        in.close();
        return r;
    }
}
