/*
 * Copyright 2011 The Apache Software Foundation
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

package net.sf.cglib.proxy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sf.cglib.core.Signature;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Uses bytecode reflection to figure out the targets of all bridge methods
 * that use invokespecial, so that we can later rewrite them to use invokevirtual.
 * 
 * @author sberlin@gmail.com (Sam Berlin)
 */
class BridgeMethodResolver {

    private final Map/* <Class, Set<Signature> */declToBridge;

    public BridgeMethodResolver(Map declToBridge) {
        this.declToBridge = declToBridge;
    }

    /**
     * Finds all bridge methods that are being called with invokespecial &
     * returns them.
     */
    public Map/*<Signature, Signature>*/resolveAll() {
        Map resolved = new HashMap();
        for (Iterator entryIter = declToBridge.entrySet().iterator(); entryIter.hasNext(); ) {
            Map.Entry entry = (Map.Entry)entryIter.next();
            Class owner = (Class)entry.getKey();
            Set bridges = (Set)entry.getValue();
            try {
                new ClassReader(owner.getName())
                  .accept(new BridgedFinder(bridges, resolved),
                          ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
            } catch(IOException ignored) {}
        }
        return resolved;
    }

    private static class BridgedFinder implements ClassVisitor, MethodVisitor {
        private Map/*<Signature, Signature>*/ resolved;
        private Set/*<Signature>*/ eligableMethods;
        
        private Signature currentMethod = null;

        BridgedFinder(Set eligableMethods, Map resolved) {
            this.resolved = resolved;
            this.eligableMethods = eligableMethods;
        }

        public void visit(int version, int access, String name,
                String signature, String superName, String[] interfaces) {
        }

        public MethodVisitor visitMethod(int access, String name, String desc,
                String signature, String[] exceptions) {
            Signature sig = new Signature(name, desc);
            if (eligableMethods.remove(sig)) {
                currentMethod = sig;
                return this;
            } else {
                return null;
            }
        }

        public void visitSource(String source, String debug) {
        }

        public void visitLineNumber(int line, Label start) {
        }

        public void visitFieldInsn(int opcode, String owner, String name,
                String desc) {
        }

        public void visitEnd() {
        }

        public void visitInnerClass(String name, String outerName,
                String innerName, int access) {
        }

        public void visitOuterClass(String owner, String name, String desc) {
        }

        public void visitAttribute(Attribute attr) {
        }

        public FieldVisitor visitField(int access, String name, String desc,
                String signature, Object value) {
            return null;
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return null;
        }

        public AnnotationVisitor visitAnnotationDefault() {
            return null;
        }

        public AnnotationVisitor visitParameterAnnotation(int parameter,
                String desc, boolean visible) {
            return null;
        }

        public void visitCode() {
        }

        public void visitFrame(int type, int nLocal, Object[] local,
                int nStack, Object[] stack) {
        }

        public void visitIincInsn(int var, int increment) {
        }

        public void visitInsn(int opcode) {
        }

        public void visitIntInsn(int opcode, int operand) {
        }

        public void visitJumpInsn(int opcode, Label label) {
        }

        public void visitLabel(Label label) {
        }

        public void visitLdcInsn(Object cst) {
        }

        public void visitLocalVariable(String name, String desc,
                String signature, Label start, Label end, int index) {
        }

        public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        }

        public void visitMaxs(int maxStack, int maxLocals) {
        }

        public void visitMethodInsn(int opcode, String owner, String name,
                String desc) {
            if (opcode == Opcodes.INVOKESPECIAL && currentMethod != null) {
                Signature target = new Signature(name, desc);
                // If the target signature is the same as the current,
                // we shouldn't change our bridge becaues invokespecial
                // is the only way to make progress (otherwise we'll
                // get infinite recursion).  This would typically
                // only happen when a bridge method is created to widen
                // the visibility of a superclass' method.
                if (!target.equals(currentMethod)) {
                    resolved.put(currentMethod, target);
                }
                currentMethod = null;
            }
        }

        public void visitMultiANewArrayInsn(String desc, int dims) {
        }

        public void visitTableSwitchInsn(int min, int max, Label dflt,
                Label[] labels) {
        }

        public void visitTryCatchBlock(Label start, Label end, Label handler,
                String type) {
        }

        public void visitTypeInsn(int opcode, String desc) {
        }

        public void visitVarInsn(int opcode, int var) {
        }
    }

}
