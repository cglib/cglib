package net.sf.cglib.transform;

import net.sf.cglib.core.CodeGenerationException;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Label;

abstract public class CodeTransformer implements CodeVisitor, Cloneable {   
    private CodeVisitor cv;

    public void setTarget(CodeVisitor target) {
        cv = target;
    }

    public CodeVisitor getTarget() {
        return cv;
    }

    public ClassTransformer asClassTransformer() {
        return new ClassTransformer() {
            public CodeVisitor visitMethod(int access, String name, String desc, String[] exceptions) {
                CodeVisitor inner = super.visitMethod(access, name, desc, exceptions);
                CodeTransformer ct = (CodeTransformer)CodeTransformer.this.clone();
                ct.setTarget(inner);
                return ct;
            }
        };
    }

    public Object clone() {
        try {
            CodeTransformer t = (CodeTransformer)super.clone();
            if (cv instanceof CodeTransformer) {
                t.cv = (CodeTransformer)((CodeTransformer)cv).clone();
            }
            return t;
        } catch (CloneNotSupportedException e) {
            throw new CodeGenerationException(e); // should be impossible
        }
    }

    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        cv.visitFieldInsn(opcode, owner, name, desc);
    }
    
    public void visitIincInsn(int var, int increment) {
        cv.visitIincInsn(var, increment);
    }
    
    public void visitInsn(int opcode) {
        cv.visitInsn(opcode);
    }
    
    public void visitIntInsn(int opcode, int operand) {
        cv.visitIntInsn(opcode, operand);
    }
    
    public void visitJumpInsn(int opcode, Label label) {
        cv.visitJumpInsn(opcode, label);
    }
    
    public void visitLabel(Label label) {
        cv.visitLabel(label);
    }
    
    public void visitLdcInsn(Object cst) {
        cv.visitLdcInsn(cst);
    }
    
    public void visitLineNumber(int line, Label start) {
        cv.visitLineNumber(line, start);
    }
    
    public void visitLocalVariable(String name, String desc, Label start, Label end, int index) {
        cv.visitLocalVariable(name, desc, start, end, index);
    }
    
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        cv.visitLookupSwitchInsn(dflt, keys, labels);
    }
    
    public void visitMaxs(int maxStack, int maxLocals) {
        cv.visitMaxs(maxStack, maxLocals);
    }
    
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        cv.visitMethodInsn(opcode, owner, name, desc);
    }
    
    public void visitMultiANewArrayInsn(String desc, int dims) {
        cv.visitMultiANewArrayInsn(desc, dims);
    }
    
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
        cv.visitTableSwitchInsn(min, max, dflt, labels);
    }
    
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        cv.visitTryCatchBlock(start, end, handler, type);
    }
    
    public void visitTypeInsn(int opcode, String desc) {
        cv.visitTypeInsn(opcode, desc);
    }
    
    public void visitVarInsn(int opcode, int var) {
        cv.visitVarInsn(opcode, var);
    }
}
