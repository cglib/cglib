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

import net.sf.cglib.core.*;
import net.sf.cglib.transform.*;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Type;

public class ExamplePreProcessor extends AbstractPreProcessor {
    private static final Type PRINT_STREAM =
      TypeUtils.parseType("java.io.PrintStream");
    private static final Signature PRINTLN =
      TypeUtils.parseSignature("void println(String)");
    
    protected ClassTransformer getClassTransformer(String name) {
        return new ClassEmitterTransformer() {
            public CodeEmitter begin_method(int access, Signature sig, Type[] exceptions, Attribute attrs) {
                CodeEmitter e = super.begin_method(access, sig, exceptions, attrs);
                if (!TypeUtils.isAbstract(access)) {
                    e.getstatic(Constants.TYPE_SYSTEM, "err", PRINT_STREAM);
                    e.push("Running " + sig.getName() + sig.getDescriptor());
                    e.invoke_virtual(PRINT_STREAM, PRINTLN);
                }
                return e;
            }
        };
    }
}
