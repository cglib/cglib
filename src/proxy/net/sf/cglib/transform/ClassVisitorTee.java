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

import org.objectweb.asm.Attribute;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.ClassVisitor;

public class ClassVisitorTee implements ClassVisitor {
    private ClassVisitor cv1, cv2;
    
    public ClassVisitorTee(ClassVisitor cv1, ClassVisitor cv2) {
        this.cv1 = cv1;
        this.cv2 = cv2;
    }

    public void visit(int access, String name, String superName, String[] interfaces, String sourceFile) {
        cv1.visit(access, name, superName, interfaces, sourceFile);
        cv2.visit(access, name, superName, interfaces, sourceFile);
    }

    public void visitEnd() {
        cv1.visitEnd();
        cv2.visitEnd();
        cv1 = cv2 = null;
    }

    public void visitField(int access, String name, String desc, Object value, Attribute attrs) {
        cv1.visitField(access, name, desc, value, attrs);
        cv2.visitField(access, name, desc, value, attrs);
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        cv1.visitInnerClass(name, outerName, innerName, access);
        cv2.visitInnerClass(name, outerName, innerName, access);
    }

    public CodeVisitor visitMethod(int access, String name, String desc, String[] exceptions, Attribute attrs) {
        CodeVisitor code1 = cv1.visitMethod(access, name, desc, exceptions, attrs);
        CodeVisitor code2 = cv2.visitMethod(access, name, desc, exceptions, attrs);
        if (code1 == null) {
            return code2;
        } else if (code2 == null) {
            return code1;
        } else {
            return new CodeVisitorTee(code1, code2);
        }
    }

    public void visitAttribute(Attribute attrs) {
        cv1.visitAttribute(attrs);
        cv2.visitAttribute(attrs);
    }
}
