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

import net.sf.cglib.core.ClassGenerator;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

public class ClassReaderGenerator implements ClassGenerator {
    private ClassReader r;
    private Attribute[] attrs;
    private boolean skipDebug;
    
    public ClassReaderGenerator(ClassReader r, boolean skipDebug) {
        this(r, null, skipDebug);
    }

    public ClassReaderGenerator(ClassReader r, Attribute[] attrs, boolean skipDebug) {
        this.r = r;
        if (attrs == null)
            attrs = new Attribute[0];
        this.attrs = attrs;
        this.skipDebug = skipDebug;
    }
    
    public void generateClass(ClassVisitor v) {
        r.accept(v, attrs, skipDebug);
    }
}
