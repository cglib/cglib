/*
 * Copyright 2004 The Apache Software Foundation
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

import org.objectweb.asm.*;

abstract public class AbstractClassFilterTransformer extends AbstractClassTransformer {
    private ClassTransformer pass;
    private ClassVisitor target;

    public void setTarget(ClassVisitor target) {
        super.setTarget(target);
        pass.setTarget(target);
    }

    protected AbstractClassFilterTransformer(ClassTransformer pass) {
        this.pass = pass;
    }

    abstract protected boolean accept(int version, int access, String name, String superName, String[] interfaces, String sourceFile);

    public void visit(int version, int access, String name, String superName, String[] interfaces, String sourceFile) {
        target = accept(version, access, name, superName, interfaces, sourceFile) ? pass : cv;
        target.visit(version, access, name, superName, interfaces, sourceFile);
    }

    public void visitEnd() {
        target.visitEnd();
        target = null; // just to be safe
    }

    public void visitField(int access, String name, String desc, Object value, Attribute attrs) {
        target.visitField(access, name, desc, value, attrs);
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        target.visitInnerClass(name, outerName, innerName, access);
    }

    public CodeVisitor visitMethod(int access, String name, String desc, String[] exceptions, Attribute attrs) {
        return target.visitMethod(access, name, desc, exceptions, attrs);
    }

    public void visitAttribute(Attribute attrs) {
        target.visitAttribute(attrs);
    }
}
