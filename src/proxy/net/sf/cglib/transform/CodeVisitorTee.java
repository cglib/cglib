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
import org.objectweb.asm.Label;

public class CodeVisitorTee implements CodeVisitor {
    private CodeVisitor cv1, cv2;
    
    public CodeVisitorTee(CodeVisitor cv1, CodeVisitor cv2) {
        this.cv1 = cv1;
        this.cv2 = cv2;
    }

    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        cv1.visitFieldInsn(opcode, owner, name, desc);
        cv2.visitFieldInsn(opcode, owner, name, desc);
    }
        
    public void visitIincInsn(int var, int increment) {
        cv1.visitIincInsn(var, increment);
        cv2.visitIincInsn(var, increment);
    }
        
    public void visitInsn(int opcode) {
        cv1.visitInsn(opcode);
        cv2.visitInsn(opcode);
    }
        
    public void visitIntInsn(int opcode, int operand) {
        cv1.visitIntInsn(opcode, operand);
        cv2.visitIntInsn(opcode, operand);
    }
        
    public void visitJumpInsn(int opcode, Label label) {
        cv1.visitJumpInsn(opcode, label);
        cv2.visitJumpInsn(opcode, label);
    }
        
    public void visitLabel(Label label) {
        cv1.visitLabel(label);
        cv2.visitLabel(label);
    }
        
    public void visitLdcInsn(Object cst) {
        cv1.visitLdcInsn(cst);
        cv2.visitLdcInsn(cst);
    }
        
    public void visitLineNumber(int line, Label start) {
        cv1.visitLineNumber(line, start);
        cv2.visitLineNumber(line, start);
    }
        
    public void visitLocalVariable(String name, String desc, Label start, Label end, int index) {
        cv1.visitLocalVariable(name, desc, start, end, index);
        cv2.visitLocalVariable(name, desc, start, end, index);
    }
        
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        cv1.visitLookupSwitchInsn(dflt, keys, labels);
        cv2.visitLookupSwitchInsn(dflt, keys, labels);
    }
        
    public void visitMaxs(int maxStack, int maxLocals) {
        cv1.visitMaxs(maxStack, maxLocals);
        cv2.visitMaxs(maxStack, maxLocals);
    }
        
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        cv1.visitMethodInsn(opcode, owner, name, desc);
        cv2.visitMethodInsn(opcode, owner, name, desc);
    }
        
    public void visitMultiANewArrayInsn(String desc, int dims) {
        cv1.visitMultiANewArrayInsn(desc, dims);
        cv2.visitMultiANewArrayInsn(desc, dims);
    }
        
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
        cv1.visitTableSwitchInsn(min, max, dflt, labels);
        cv2.visitTableSwitchInsn(min, max, dflt, labels);
    }
        
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        cv1.visitTryCatchBlock(start, end, handler, type);
        cv2.visitTryCatchBlock(start, end, handler, type);
    }
        
    public void visitTypeInsn(int opcode, String desc) {
        cv1.visitTypeInsn(opcode, desc);
        cv2.visitTypeInsn(opcode, desc);
    }
        
    public void visitVarInsn(int opcode, int var) {
        cv1.visitVarInsn(opcode, var);
        cv2.visitVarInsn(opcode, var);
    }

    public void visitAttribute(Attribute attrs) {
        cv1.visitAttribute(attrs);
        cv2.visitAttribute(attrs);
    }
}

