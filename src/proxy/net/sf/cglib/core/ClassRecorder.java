package net.sf.cglib.core;

import net.sf.cglib.core.ClassGenerator;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Label;
import java.util.*;

public class ClassRecorder implements ClassVisitor, ClassGenerator {
    private static final int VISIT = 0;
    private static final int VISIT_END = 1;
    private static final int VISIT_FIELD = 2;
    private static final int VISIT_INNER_CLASS = 3;
    private static final int VISIT_METHOD = 4;

    private static final int VISIT_FIELD_INSN = 5;
    private static final int VISIT_IINC_INSN = 6;
    private static final int VISIT_INSN = 7;
    private static final int VISIT_INT_INSN = 8;
    private static final int VISIT_JUMP_INSN = 9;
    private static final int VISIT_LABEL = 10;
    private static final int VISIT_LDC_INSN = 11;
    private static final int VISIT_LINE_NUMBER = 12;
    private static final int VISIT_LOCAL_VARIABLE = 13;
    private static final int VISIT_LOOKUPSWITCH_INSN = 14;
    private static final int VISIT_MAXS = 15;
    private static final int VISIT_METHOD_INSN = 16;
    private static final int VISIT_MULTIANEWARRAY_INSN = 17;
    private static final int VISIT_TABLESWITCH_INSN = 18;
    private static final int VISIT_TRY_CATCH_BLOCK = 19;
    private static final int VISIT_TYPE_INSN = 20;
    private static final int VISIT_VAR_INSN = 21;

    private static final int INITIAL_SIZE = 64;
    private static final float ENLARGE_FACTOR = 2;

    private int masterIndex;
    private int[] classIndexes = new int[INITIAL_SIZE];
    private int classIndex;
    
    private int[] types = new int[INITIAL_SIZE];
    private int[] i1 = new int[INITIAL_SIZE];
    private int[] i2 = new int[INITIAL_SIZE];
    private Object[] o1 = new Object[INITIAL_SIZE];
    private Object[] o2 = new Object[INITIAL_SIZE];
    private Object[] o3 = new Object[INITIAL_SIZE];
    private Object[] o4 = new Object[INITIAL_SIZE];

    public void generateClass(ClassVisitor v) {
        for (int i = 0, size = classIndexes.length; i < size; i++) {
            int r = classIndexes[i];
            switch (types[r]) {
            case VISIT:
                v.visit(i1[r],
                        (String)o1[r],
                        (String)o2[r],
                        (o3[r] != null) ? (String[])((String[])o3[r]).clone() : null,
                        (String)o4[r]);
                break;
            case VISIT_END:
                v.visitEnd();
                break;
            case VISIT_FIELD:
                v.visitField(i1[r],
                             (String)o1[r],
                             (String)o2[r],
                             o3[r]);
                break;
            case VISIT_INNER_CLASS:
                v.visitInnerClass((String)o1[r],
                                  (String)o2[r],
                                  (String)o3[r],
                                  i1[r]);
                break;
            case VISIT_METHOD:
                ((CodeRecorder)o4[r]).accept(v.visitMethod(i1[r],
                                                           (String)o1[r],
                                                           (String)o2[r],
                                                           (o3[r] != null) ? (String[])((String[])o3[r]).clone() : null));
                break;
            }
        }
    }

    public void generateCode(Signature sig, CodeVisitor v) {
        for (int i = 0, size = classIndexes.length; i < size; i++) {
            int r = classIndexes[i];
            if (types[r] == VISIT_METHOD) {
                Signature compare = new Signature((String)o1[r], (String)o2[r]);
                if (sig.getName().equals(o1[r]) && sig.getDescriptor().equals(o2[r])) {
                    ((CodeRecorder)o4[r]).accept(v);
                    return;
                }
            }
        }
        throw new IllegalArgumentException("Did not record method with signature " + sig);
    }

    private int classNextIndex() {
        if (classIndex == classIndexes.length) {
            classIndexes = enlarge(classIndexes);
        }
        return classIndexes[classIndex++] = masterNextIndex();
    }

    synchronized private int masterNextIndex() {
        if (masterIndex == types.length) {
            types = enlarge(types);
            i1 = enlarge(i1);
            i2 = enlarge(i2);
            o1 = enlarge(o1);
            o2 = enlarge(o2);
            o3 = enlarge(o3);
            o4 = enlarge(o4);
        }
        return masterIndex++;
    }

    private int[] enlarge(int[] a) {
        int newsize = (int)(a.length * ENLARGE_FACTOR);
        int[] a2 = new int[newsize];
        System.arraycopy(a, 0, a2, 0, a.length);
        return a2;
    }

    private Object[] enlarge(Object[] a) {
        int newsize = (int)(a.length * ENLARGE_FACTOR);
        Object[] a2 = new Object[newsize];
        System.arraycopy(a, 0, a2, 0, a.length);
        return a2;
    }
    
    public void visit(int access, String name, String superName, String[] interfaces, String sourceFile) {
        int r = classNextIndex();
        types[r] = VISIT;
        i1[r] = access;
        o1[r] = name;
        o2[r] = superName;
        o3[r] = (interfaces != null) ? interfaces.clone() : null;
        o4[r] = sourceFile;
    }

    public void visitEnd() {
        int r = classNextIndex();
        types[r] = VISIT_END;
        // TODO: shrink arrays to minimum size?
    }

    public void visitField(int access, String name, String desc, Object value) {
        int r = classNextIndex();
        types[r] = VISIT_FIELD;
        i1[r] = access;
        o1[r] = name;
        o2[r] = desc;
        o3[r] = value;
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        int r = classNextIndex();
        types[r] = VISIT_INNER_CLASS;
        o1[r] = name;
        o2[r] = outerName;
        o3[r] = innerName;
        i1[r] = access;
    }

    public CodeVisitor visitMethod(int access, String name, String desc, String[] exceptions) {
        int r = classNextIndex();
        types[r] = VISIT_METHOD;
        i1[r] = access;
        o1[r] = name;
        o2[r] = desc;
        o3[r] = (exceptions != null) ? exceptions.clone() : null;

        CodeRecorder rec = new CodeRecorder();
        o4[r] = rec;
        return rec;
    }

    private static Label remap(Map cache, Object value) {
        Label label = (Label)cache.get(value);
        if (label == null) {
            cache.put(value, label = new Label());
        }
        return label;
    }

    private static Label[] remap(Map cache, Object[] values) {
        Label[] labels = new Label[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = remap(cache, values[i]);
        }
        return labels;
    }
    
    private class CodeRecorder implements CodeVisitor {
        private int[] indexes = new int[INITIAL_SIZE];
        private int index;

        private int methodNextIndex() {
            if (index == indexes.length) {
                indexes = enlarge(indexes);
            }
            return indexes[index++] = masterNextIndex();
        }
        
        public void accept(CodeVisitor v) {
            Map labels = new HashMap();
            for (int i = 0, size = indexes.length; i < size; i++) {
                int r = indexes[i];
                switch (types[r]) {
                case VISIT_FIELD_INSN:
                    v.visitFieldInsn(i1[r],
                                     (String)o1[r],
                                     (String)o2[r],
                                     (String)o3[r]);
                    break;
                case VISIT_IINC_INSN:
                    v.visitIincInsn(i1[r], i2[r]);
                    break;
                case VISIT_INSN:
                    v.visitInsn(i1[r]);
                    break;
                case VISIT_INT_INSN:
                    v.visitIntInsn(i1[r], i2[r]);
                    break;
                case VISIT_JUMP_INSN:
                    v.visitJumpInsn(i1[r], remap(labels, o1[r]));
                    break;
                case VISIT_LABEL:
                    v.visitLabel(remap(labels, o1[r]));
                    break;
                case VISIT_LDC_INSN:
                    v.visitLdcInsn(o1[r]);
                    break;
                case VISIT_LINE_NUMBER:
                    v.visitLineNumber(i1[r], remap(labels, o1[r]));
                    break;
                case VISIT_LOCAL_VARIABLE:
                    v.visitLocalVariable((String)o1[r],
                                         (String)o2[r],
                                         remap(labels, o3[r]),
                                         remap(labels, o4[r]), i1[r]);
                    break;
                case VISIT_LOOKUPSWITCH_INSN:
                    v.visitLookupSwitchInsn(remap(labels, o1[r]),
                                            (int[])((int[])o2[r]).clone(),
                                            remap(labels, (Object[])o3[r]));
                    break;
                case VISIT_MAXS:
                    v.visitMaxs(i1[r], i2[r]);
                    break;
                case VISIT_METHOD_INSN:
                    v.visitMethodInsn(i1[r],
                                      (String)o1[r],
                                      (String)o2[r],
                                      (String)o3[r]);
                    break;
                case VISIT_MULTIANEWARRAY_INSN:
                    v.visitMultiANewArrayInsn((String)o1[r], i1[r]);
                    break;
                case VISIT_TABLESWITCH_INSN:
                    v.visitTableSwitchInsn(i1[r],
                                           i2[r],
                                           remap(labels, o1[r]),
                                           remap(labels, (Object[])o2[r]));
                    break;
                case VISIT_TRY_CATCH_BLOCK:
                    v.visitTryCatchBlock(remap(labels, o1[r]),
                                         remap(labels, o2[r]),
                                         remap(labels, o3[r]),
                                         (String)o4[r]);
                    break;
                case VISIT_TYPE_INSN:
                    v.visitTypeInsn(i1[r], (String)o1[r]);
                    break;
                case VISIT_VAR_INSN:
                    v.visitVarInsn(i1[r], i2[r]);
                    break;
                }
            }
        }
        
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            int r = methodNextIndex();
            types[r] = VISIT_FIELD_INSN;
            i1[r] = opcode;
            o1[r] = owner;
            o2[r] = name;
            o3[r] = desc;
        }
        
        public void visitIincInsn(int var, int increment) {
            int r = methodNextIndex();
            types[r] = VISIT_IINC_INSN;
            i1[r] = var;
            i2[r] = increment;
        }
        
        public void visitInsn(int opcode) {
            int r = methodNextIndex();
            types[r] = VISIT_INSN;
            i1[r] = opcode;
        }
        
        public void visitIntInsn(int opcode, int operand) {
            int r = methodNextIndex();
            types[r] = VISIT_INT_INSN;
            i1[r] = opcode;
            i2[r] = operand;
        }
        
        public void visitJumpInsn(int opcode, Label label) {
            int r = methodNextIndex();
            types[r] = VISIT_JUMP_INSN;
            i1[r] = opcode;
            o1[r] = label;
        }
        
        public void visitLabel(Label label) {
            int r = methodNextIndex();
            types[r] = VISIT_LABEL;
            o1[r] = label;
        }
        
        public void visitLdcInsn(Object cst) {
            int r = methodNextIndex();
            types[r] = VISIT_LDC_INSN;
            o1[r] = cst;
        }
        
        public void visitLineNumber(int line, Label start) {
            int r = methodNextIndex();
            types[r] = VISIT_LINE_NUMBER;
            i1[r] = line;
            o1[r] = start;
        }
        
        public void visitLocalVariable(String name, String desc, Label start, Label end, int index) {
            int r = methodNextIndex();
            types[r] = VISIT_LOCAL_VARIABLE;
            o1[r] = name;
            o2[r] = desc;
            o3[r] = start;
            o4[r] = end;
            i1[r] = index;
        }
        
        public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
            int r = methodNextIndex();
            types[r] = VISIT_LOOKUPSWITCH_INSN;
            o1[r] = dflt;
            o2[r] = keys.clone();
            o3[r] = labels;
        }
        
        public void visitMaxs(int maxStack, int maxLocals) {
            int r = methodNextIndex();
            types[r] = VISIT_MAXS;
            i1[r] = maxStack;
            i2[r] = maxLocals;
        }
        
        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            int r = methodNextIndex();
            types[r] = VISIT_METHOD_INSN;
            i1[r] = opcode;
            o1[r] = owner;
            o2[r] = name;
            o3[r] = desc;
        }
        
        public void visitMultiANewArrayInsn(String desc, int dims) {
            int r = methodNextIndex();
            types[r] = VISIT_MULTIANEWARRAY_INSN;
            o1[r] = desc;
            i1[r] = dims;
        }
        
        public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
            int r = methodNextIndex();
            types[r] = VISIT_TABLESWITCH_INSN;
            i1[r] = min;
            i2[r] = max;
            o1[r] = dflt;
            o2[r] = labels;
        }
        
        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
            int r = methodNextIndex();
            types[r] = VISIT_TRY_CATCH_BLOCK;
            o1[r] = start;
            o2[r] = end;
            o3[r] = handler;
            o4[r] = type;
        }
        
        public void visitTypeInsn(int opcode, String desc) {
            int r = methodNextIndex();
            types[r] = VISIT_TYPE_INSN;
            i1[r] = opcode;
            o1[r] = desc;
        }
        
        public void visitVarInsn(int opcode, int var) {
            int r = methodNextIndex();
            types[r] = VISIT_VAR_INSN;
            i1[r] = opcode;
            i2[r] = var;
        }
    }
}
