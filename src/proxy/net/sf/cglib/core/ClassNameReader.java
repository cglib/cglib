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
package net.sf.cglib.core;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;

// TODO: optimize (ClassReader buffers entire class before accept)
public class ClassNameReader {
    private ClassNameReader() {
    }

    private static final EarlyExitException EARLY_EXIT = new EarlyExitException();
    private static class EarlyExitException extends RuntimeException { }
    
    public static String getClassName(ClassReader r) {
        final String[] array = new String[1];
        try {
            r.accept(new ClassAdapter(null) {
                public void visit(int access,
                                  String name,
                                  String superName,
                                  String[] interfaces,
                                  String sourceFile) {
                    array[0] = name.replace('/', '.');
                    throw EARLY_EXIT;
                }
            }, true);
        } catch (EarlyExitException e) { }
        return array[0];
    }
}
