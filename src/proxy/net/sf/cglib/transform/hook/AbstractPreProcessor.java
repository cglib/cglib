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
package net.sf.cglib.transform.hook;

import java.util.Hashtable;
import net.sf.cglib.core.*;
import net.sf.cglib.transform.*;
import org.codehaus.aspectwerkz.hook.ClassPreProcessor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

abstract public class AbstractPreProcessor implements ClassPreProcessor {
    public void initialize(Hashtable hashtable) {
    }

    public byte[] preProcess(String name, byte[] abyte, ClassLoader caller) {
        try {
            ClassTransformer t = getClassTransformer(name);
            if (t == null)
                return abyte;
            ClassWriter w = new DebuggingClassWriter(true);
            ClassGenerator gen = new ClassReaderGenerator(new ClassReader(abyte), false);
            gen = new TransformingClassGenerator(gen, t);
            gen.generateClass(w);
            return w.toByteArray();
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }
    }

    abstract protected ClassTransformer getClassTransformer(String name);
}
