/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package net.sf.cglib.core;

import java.io.*;
import java.util.*;
import org.objectweb.asm.*;

/**
 * @author Juozas Baliuka, Chris Nokleberg
 */
public class Emitter2 {
    public static final int OP_ADD = Constants.IADD;
    public static final int OP_MUL = Constants.IMUL;
    public static final int OP_XOR = Constants.IXOR;
    public static final int OP_USHR = Constants.IUSHR;
    public static final int OP_SUB = Constants.ISUB;
    public static final int OP_DIV = Constants.IDIV;
    public static final int OP_NEG = Constants.INEG;
    public static final int OP_REM = Constants.IREM;
    public static final int OP_AND = Constants.IAND;
    public static final int OP_OR = Constants.IOR;

    private static final Type OBJECT_TYPE = Type.getType("Ljava.lang.Object;");
    private static final Type[] EMPTY_PARAMS = {};

    // current class
    private ClassVisitor classv;
    private Type classType;
    private Type superType;
    private Map finalizeCallbacks = new HashMap();
    private Map fieldInfo = new HashMap();    

    // current method
    private CodeVisitor rawcodev;
    private CodeVisitor codev;
    private int methodAccess;
    private String methodName;
    private Type returnType;
    private Type[] parameterTypes;
    private boolean isStatic;
    private Map remap = new HashMap();
    private int firstLocal;
    private int nextLocal;

    // current block
    private Block2 curBlock;

    public Emitter2(ClassVisitor v) {
        this.classv = v;
    }

    public CodeVisitor getCodeVisitor() {
        return codev;
    }

    public Type getSuperType() {
        return superType;
    }

    public void begin_class(int access, Type classType, Type superType, Type[] interfaces, String sourceFile) {
        this.classType = classType;
        this.superType = superType;
        classv.visit(access,
                     classType.getInternalName(),
                     superType.getInternalName(),
                     getInternalNames(interfaces),
                     sourceFile);
    }

    public void begin_class(int access, String name, String superName, String[] interfaces, String sourceFile) {
        classType = Type.getType("L" + name + ";");
        superType = Type.getType("L" + superName + ";");
        classv.visit(access, name, superName, interfaces, sourceFile);
    }

    public void end_class() {
        for (Iterator it = finalizeCallbacks.values().iterator(); it.hasNext();) {
            ((FinalizeCallback)it.next()).process();
        }
        classv.visitEnd();

        closeMethod();
        classv = null; // for safety
    }

    public void register(Object key, FinalizeCallback callback) {
        finalizeCallbacks.put(key, callback);
    }

    private static boolean isStatic(int access) {
        return (Constants.ACC_STATIC & access) != 0;
    }

    private static boolean isAbstract(int access) {
        return (Constants.ACC_ABSTRACT & access) != 0;
    }

    public void begin_method(int access, String name, Type returnType, Type[] parameterTypes, Type[] exceptions) {
        begin_method(access,
                     name,
                     Type.getMethodDescriptor(returnType, parameterTypes),
                     getInternalNames(exceptions));
    }

    public void begin_method(int access, String name, String desc, String[] exceptions) {
        closeMethod();

        methodAccess = access;
        methodName = name;
        returnType = Type.getReturnType(desc);
        parameterTypes = Type.getArgumentTypes(desc);
        isStatic = isStatic(access);
        remap.clear();
        firstLocal = nextLocal = getLocalOffset() + getStackSize(parameterTypes);

        rawcodev = classv.visitMethod(access, name, desc, exceptions);
        codev = new CodeAdapter(rawcodev) {
            public void visitMaxs(int maxStack, int maxLocals) {
                // ignore
                // System.err.println("Ignoring visitMaxs(" + maxStack + ", " + maxLocals + ")");
            }

            public void visitVarInsn(int opcode, int var) {
                Type type;
                switch (opcode) {
                case Constants.RET: // is this correct?
                case Constants.ILOAD:
                case Constants.ISTORE:
                    type = Type.INT_TYPE;
                    break;
                case Constants.FLOAD:
                case Constants.FSTORE:
                    type = Type.FLOAT_TYPE;
                    break;
                case Constants.LLOAD:
                case Constants.LSTORE:
                    type = Type.LONG_TYPE;
                    break;
                case Constants.DLOAD:
                case Constants.DSTORE:
                    type = Type.DOUBLE_TYPE;
                    break;
                default:
                    type = OBJECT_TYPE;
                }
                cv.visitVarInsn(opcode, remapLocal(var, type));
            }

            public void visitIincInsn(int var, int increment) {
                cv.visitIincInsn(remapLocal(var, Type.INT_TYPE), increment);
            }

            public void visitLocalVariable(String name, String desc, org.objectweb.asm.Label start, org.objectweb.asm.Label end, int index) {
                cv.visitLocalVariable(name, desc, start, end, remapLocal(index, null));
            }
        };
    }

    private int remapLocal(int index, Type type) {
        if (index < firstLocal) {
            // System.err.println("remap keeping " + index);
            return index;
        }
        Integer key = new Integer(index);
        Local2 local = (Local2)remap.get(key);
        if (local == null) {
            local = make_local(type);
            remap.put(key, local);
        }
        if (!local.getType().equals(type)) {
            throw new IllegalStateException("Remapped local has different type");
        }
        // System.err.println("remapping " + index + " --> " + local.getIndex());
        return local.getIndex();
    }

    private void closeMethod() {
        if (codev != null) {
            if (!isAbstract(methodAccess)) {
                // System.err.println("Calling visitMaxs(0, 0)");
                rawcodev.visitMaxs(0, 0);
            }
            
            if (curBlock != null) {
                throw new IllegalStateException("unclosed exception block");
            }
            codev = null;
        }
    }

    private int getLocalOffset() {
        return isStatic ? 0 : 1;
    }

    private static int getStackSize(Type[] types) {
        int size = 0;
        for (int i = 0; i < types.length; i++) {
            size += types[i].getSize();
        }
        return size;
    }

    public void begin_constructor(int access, Type[] parameterTypes, Type[] exceptions) {
        begin_method(access,
                     Constants.CONSTRUCTOR_NAME,
                     Type.getMethodDescriptor(Type.VOID_TYPE, parameterTypes),
                     getInternalNames(exceptions));
    }

    private static String[] getInternalNames(Type[] types) {
        if (types == null) {
            return null;
        }
        String[] names = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            names[i] = types[i].getInternalName();
        }
        return names;
    }

    public void begin_static() {
        begin_method(Constants.ACC_STATIC, Constants.STATIC_NAME, "()V", null);
    }

    public Block2 begin_block() {
        return curBlock = new Block2(curBlock, mark());
    }

    public void end_block() {
        if (curBlock == null) {
            throw new IllegalStateException("mismatched block boundaries");
        }
        curBlock.setEnd(mark());
        curBlock = curBlock.getParent();
    }

    public void catch_exception(Block2 block, Type exception) {
        if (block.getEnd() == null) {
            throw new IllegalStateException("end of block is unset");
        }
        codev.visitTryCatchBlock(block.getStart(),
                                 block.getEnd(),
                                 mark(),
                                 exception.getInternalName());
    }

    public void ifeq(org.objectweb.asm.Label label) { codev.visitJumpInsn(Constants.IFEQ, label); }
    public void ifne(org.objectweb.asm.Label label) { codev.visitJumpInsn(Constants.IFNE, label); }
    public void iflt(org.objectweb.asm.Label label) { codev.visitJumpInsn(Constants.IFLT, label); }
    public void ifge(org.objectweb.asm.Label label) { codev.visitJumpInsn(Constants.IFGE, label); }
    public void ifgt(org.objectweb.asm.Label label) { codev.visitJumpInsn(Constants.IFGT, label); }
    public void ifle(org.objectweb.asm.Label label) { codev.visitJumpInsn(Constants.IFLE, label); }
    public void goTo(org.objectweb.asm.Label label) { codev.visitJumpInsn(Constants.GOTO, label); }
    public void ifnull(org.objectweb.asm.Label label) { codev.visitJumpInsn(Constants.IFNULL, label); }
    public void ifnonnull(org.objectweb.asm.Label label) { codev.visitJumpInsn(Constants.IFNONNULL, label); }
    public void if_icmplt(org.objectweb.asm.Label label) { codev.visitJumpInsn(Constants.IF_ICMPLT, label); }
    public void if_icmpgt(org.objectweb.asm.Label label) { codev.visitJumpInsn(Constants.IF_ICMPGT, label); }
    public void if_icmpne(org.objectweb.asm.Label label) { codev.visitJumpInsn(Constants.IF_ICMPNE, label); }
    public void if_icmpeq(org.objectweb.asm.Label label) { codev.visitJumpInsn(Constants.IF_ICMPEQ, label); }
    public void if_acmpeq(org.objectweb.asm.Label label) { codev.visitJumpInsn(Constants.IF_ACMPEQ, label); }
    public void if_acmpne(org.objectweb.asm.Label label) { codev.visitJumpInsn(Constants.IF_ACMPNE, label); }
    
    public void pop() { codev.visitInsn(Constants.POP); }
    public void pop2() { codev.visitInsn(Constants.POP2); }
    public void dup() { codev.visitInsn(Constants.DUP); }
    public void dup2() { codev.visitInsn(Constants.DUP2); }
    public void dup_x1() { codev.visitInsn(Constants.DUP_X1); }
    public void dup_x2() { codev.visitInsn(Constants.DUP_X2); }
    public void swap() { codev.visitInsn(Constants.SWAP); }
    public void aconst_null() { codev.visitInsn(Constants.ACONST_NULL); }

    public void monitorenter() { codev.visitInsn(Constants.MONITORENTER); }
    public void monitorexit() { codev.visitInsn(Constants.MONITOREXIT); }

    public void math(int op, Type type) { codev.visitInsn(type.getOpcode(op)); }

    public void array_load(Type type) { codev.visitInsn(type.getOpcode(Constants.IALOAD)); }
    public void array_store(Type type) { codev.visitInsn(type.getOpcode(Constants.IASTORE)); }

    public void if_cmpeq(Type type, org.objectweb.asm.Label label) {
        cmpHelper(type, label, Constants.IF_ICMPEQ, Constants.IFEQ);
    }
    public void if_cmpne(Type type, org.objectweb.asm.Label label) {
        cmpHelper(type, label, Constants.IF_ICMPNE, Constants.IFNE);
    }
    public void if_cmplt(Type type, org.objectweb.asm.Label label) {
        cmpHelper(type, label, Constants.IF_ICMPLT, Constants.IFLT);
    }
    public void if_cmpgt(Type type, org.objectweb.asm.Label label) {
        cmpHelper(type, label, Constants.IF_ICMPGT, Constants.IFGT);
    }

    private void cmpHelper(Type type, org.objectweb.asm.Label label, int intOp, int numOp) {
        if (type == Type.LONG_TYPE) {
            codev.visitInsn(Constants.LCMP);
        } else if (type == Type.DOUBLE_TYPE) {
            codev.visitInsn(Constants.DCMPG);
        } else if (type == Type.FLOAT_TYPE) {
            codev.visitInsn(Constants.FCMPG);
        } else {
            codev.visitJumpInsn(intOp, label);
            return;
        }
        codev.visitJumpInsn(numOp, label);
    }

    /**
     * Casts from one primitive numeric type to another
     */
    public void cast_numeric(Type from, Type to) {
        if (from != to) {
            if (from == Type.DOUBLE_TYPE) {
                if (to == Type.FLOAT_TYPE) {
                    codev.visitInsn(Constants.D2F);
                } else if (to == Type.LONG_TYPE) {
                    codev.visitInsn(Constants.D2L);
                } else {
                    codev.visitInsn(Constants.D2I);
                    cast_numeric(Type.INT_TYPE, to);
                }
            } else if (from == Type.FLOAT_TYPE) {
                if (to == Type.DOUBLE_TYPE) {
                    codev.visitInsn(Constants.F2D);
                } else if (to == Type.LONG_TYPE) {
                    codev.visitInsn(Constants.F2L);
                } else {
                    codev.visitInsn(Constants.F2I);
                    cast_numeric(Type.INT_TYPE, to);
                }
            } else if (from == Type.LONG_TYPE) {
                if (to == Type.DOUBLE_TYPE) {
                    codev.visitInsn(Constants.L2D);
                } else if (to == Type.FLOAT_TYPE) {
                    codev.visitInsn(Constants.L2F);
                } else {
                    codev.visitInsn(Constants.L2I);
                    cast_numeric(Type.INT_TYPE, to);
                }
            } else {
                if (to == Type.BYTE_TYPE) {
                    codev.visitInsn(Constants.I2B);
                } else if (to == Type.CHAR_TYPE) {
                    codev.visitInsn(Constants.I2C);
                } else if (to == Type.DOUBLE_TYPE) {
                    codev.visitInsn(Constants.I2D);
                } else if (to == Type.FLOAT_TYPE) {
                    codev.visitInsn(Constants.I2F);
                } else if (to == Type.LONG_TYPE) {
                    codev.visitInsn(Constants.I2L);
                } else if (to == Type.SHORT_TYPE) {
                    codev.visitInsn(Constants.I2S);
                }
            }
        }
    }

    public void push(int i) {
        if (i < -1) {
            codev.visitLdcInsn(new Integer(i));
        } else if (i <= 5) {
            codev.visitInsn(Constants.ICONST(i));
        } else if (i <= Byte.MAX_VALUE) {
            codev.visitIntInsn(Constants.BIPUSH, i);
        } else if (i <= Short.MAX_VALUE) {
            codev.visitIntInsn(Constants.SIPUSH, i);
        } else {
            codev.visitLdcInsn(new Integer(i));
        }
    }
    
    public void push(long value) {
        if (value == 0L || value == 1L) {
            codev.visitInsn(Constants.LCONST(value));
        } else {
            codev.visitLdcInsn(new Long(value));
        }
    }
    
    public void push(float value) {
        if (value == 0f || value == 1f || value == 2f) {
            codev.visitInsn(Constants.FCONST(value));
        } else {
            codev.visitLdcInsn(new Float(value));
        }
    }
    public void push(double value) {
        if (value == 0d || value == 1d) {
            codev.visitInsn(Constants.DCONST(value));
        } else {
            codev.visitLdcInsn(new Double(value));
        }
    }
    
    public void push(String value) {
        codev.visitLdcInsn(value);
    }

    public void newarray() {
        newarray(OBJECT_TYPE);
    }

    public void newarray(Type type) {
        if (isPrimitive(type)) {
            codev.visitIntInsn(Constants.NEWARRAY, Constants.NEWARRAY(type));
        } else {
            emit_type(Constants.ANEWARRAY, type);
        }
    }

    private static boolean isPrimitive(Type type) {
        switch (type.getSort()) {
        case Type.ARRAY:
        case Type.OBJECT:
        case Type.VOID:
            return false;
        default:
            return true;
        }
    }

    public void arraylength() {
        codev.visitInsn(Constants.ARRAYLENGTH);
    }
    
    public void load_this() {
        if (isStatic) {
            throw new IllegalStateException("no 'this' pointer within static method");
        }
        codev.visitVarInsn(Constants.ALOAD, 0);
    }
    
    /**
     * Pushes all of the arguments of the current method onto the stack.
     */
    public void load_args() {
        load_args(0, parameterTypes.length);
    }

    /**
     * Pushes the specified argument of the current method onto the stack.
     * @param index the zero-based index into the argument list
     */
    public void load_arg(int index) {
        load_local(parameterTypes[index], getLocalOffset() + skipArgs(index));
    }

    // zero-based (see load_this)
    public void load_args(int fromArg, int count) {
        int pos = getLocalOffset() + skipArgs(fromArg);
        for (int i = 0; i < count; i++) {
            Type t = parameterTypes[fromArg + i];
            load_local(t, pos);
            pos += t.getSize();
        }
    }
    
    private int skipArgs(int numArgs) {
        int amount = 0;
        for (int i = 0; i < numArgs; i++) {
            amount += parameterTypes[i].getSize();
        }
        return amount;
    }

    private void load_local(Type t, int pos) {
        // TODO: t == null ok?
        codev.visitVarInsn(t.getOpcode(Constants.ILOAD), pos);
    }

    private void store_local(Type t, int pos) {
        // TODO: t == null ok?
        codev.visitVarInsn(t.getOpcode(Constants.ILOAD), pos);
    }
    
    public void iinc(Local2 local, int amount) {
        codev.visitIincInsn(local.getIndex(), amount);
    }
    
    public void store_local(Local2 local) {
        store_local(local.getType(), local.getIndex());
    }
    
    public void load_local(Local2 local) {
        load_local(local.getType(), local.getIndex());
    }

    public void return_value() {
        codev.visitInsn(returnType.getOpcode(Constants.IRETURN));
    }

    public void declare_field(int access, String name, Type type, Object value) {
        declare_field(access, name, type.getDescriptor(), value);
    }

    public void declare_field(int access, String name, String desc, Object value) {
        // System.err.println("DECLARING FIELD name=" + name + " desc=" + desc + " value=" + value);
        closeMethod();
        if (fieldInfo.get(name) != null) {
            throw new IllegalArgumentException("Field \"" + name + "\" already exists");
        }
        fieldInfo.put(name, new FieldInfo(isStatic(access), Type.getType(desc)));
        classv.visitField(access, name, desc, value);
    }

    private FieldInfo getFieldInfo(String name) {
        FieldInfo field = (FieldInfo)fieldInfo.get(name);
        if (field == null) {
            throw new IllegalArgumentException("Field " + name + " is not declared");
        }
        return field;
    }
    
    private static class FieldInfo {
        boolean isStatic;
        Type type;
        
        public FieldInfo(boolean isStatic, Type type) {
            this.isStatic = isStatic;
            this.type = type;
        }
    }

    public void getfield(String name) {
        FieldInfo info = getFieldInfo(name);
        int opcode = info.isStatic ? Constants.GETSTATIC : Constants.GETFIELD;
        emit_field(opcode, classType, name, info.type);
    }
    
    public void putfield(String name) {
        FieldInfo info = getFieldInfo(name);
        int opcode = info.isStatic ? Constants.PUTSTATIC : Constants.PUTFIELD;
        emit_field(opcode, classType, name, info.type);
    }

    public void super_getfield(String name, Type type) {
        emit_field(Constants.GETFIELD, superType, name, type);
    }
    
    public void super_putfield(String name, Type type) {
        emit_field(Constants.PUTFIELD, superType, name, type);
    }

    public void super_getstatic(String name, Type type) {
        emit_field(Constants.GETSTATIC, superType, name, type);
    }
    
    public void super_putstatic(String name, Type type) {
        emit_field(Constants.PUTSTATIC, superType, name, type);
    }

    public void getfield(Type owner, String name, Type type) {
        emit_field(Constants.GETFIELD, owner, name, type);
    }
    
    public void putfield(Type owner, String name, Type type) {
        emit_field(Constants.PUTFIELD, owner, name, type);
    }

    public void getstatic(Type owner, String name, Type type) {
        emit_field(Constants.GETSTATIC, owner, name, type);
    }
    
    public void putstatic(Type owner, String name, Type type) {
        emit_field(Constants.PUTSTATIC, owner, name, type);
    }
    
    protected void emit_field(int opcode, Type ctype, String name, Type ftype) {
        codev.visitFieldInsn(opcode,
                             ctype.getInternalName(),
                             name,
                             ftype.getDescriptor());
    }

    public void invoke_virtual(Type owner, String methodName, Type returnType, Type[] parameterTypes) {
        emit_invoke(Constants.INVOKEVIRTUAL, owner, methodName, returnType, parameterTypes);
    }

    public void invoke_static(Type owner, String methodName, Type returnType, Type[] parameterTypes) {
        emit_invoke(Constants.INVOKESTATIC, owner, methodName, returnType, parameterTypes);
    }

    public void invoke_virtual_this(String methodName, Type returnType, Type[] parameterTypes) {
        emit_invoke(Constants.INVOKEVIRTUAL, classType, methodName, returnType, parameterTypes);
    }

    public void invoke_static_this(String methodName, Type returnType, Type[] parameterTypes) {
        emit_invoke(Constants.INVOKESTATIC, classType, methodName, returnType, parameterTypes);
    }

    public void super_invoke() {
        emit_invoke(Constants.INVOKESPECIAL,
                    superType,
                    methodName,
                    returnType,
                    parameterTypes);
    }
    
    public void invoke_constructor(Type type, Type[] parameterTypes) {
        emit_invoke(Constants.INVOKESPECIAL,
                    classType,
                    Constants.CONSTRUCTOR_NAME,
                    Type.VOID_TYPE,
                    parameterTypes);
    }

    public void invoke_constructor(Type type) {
        invoke_constructor(type, EMPTY_PARAMS);
    }

    protected void emit_invoke(int opcode,
                               Type type,
                               String methodName,
                               Type returnType,
                               Type[] parameterTypes) {
        codev.visitMethodInsn(opcode,
                              type.getInternalName(),
                              methodName,
                              Type.getMethodDescriptor(returnType, parameterTypes));
    }

    public void super_invoke_constructor() {
        invoke_constructor(superType, EMPTY_PARAMS);
    }
    
    public void super_invoke_constructor(Type[] parameterTypes) {
        invoke_constructor(superType, parameterTypes);
    }
    
    public void invoke_constructor_this() {
        invoke_constructor_this(EMPTY_PARAMS);
    }
    
    public void invoke_constructor_this(Type[] parameterTypes) {
        invoke_constructor(classType, parameterTypes);
    }
    
    public void new_instance_this() {
        emit_type(Constants.NEW, classType);
    }

    public void new_instance(Type type) {
        emit_type(Constants.NEW, type);
    }

    private void emit_type(int opcode, Type type) {
        codev.visitTypeInsn(opcode, type.getInternalName());
    }

    public void aaload(int index) {
        push(index);
        aaload();
    }

    public void aaload() { codev.visitInsn(Constants.AALOAD); }
    public void aastore() { codev.visitInsn(Constants.AASTORE); }
    public void athrow() { codev.visitInsn(Constants.ATHROW); }

    public org.objectweb.asm.Label make_label() {
        return new org.objectweb.asm.Label();
    }
    
    public Local2 make_local() {
        return make_local(OBJECT_TYPE);
    }
    
    public Local2 make_local(Type type) {
        Local2 local = new Local2(nextLocal, type);
        nextLocal += type.getSize();
        return local;
    }

    public void checkcast_this() {
        emit_type(Constants.CHECKCAST, classType);
    }
    
    public void checkcast(Type type) {
        if (!type.equals(OBJECT_TYPE)) {
            emit_type(Constants.CHECKCAST, type);
        }
    }

    public void instance_of(Type type) {
        emit_type(Constants.INSTANCEOF, type);
    }
    
    public void instance_of_this() {
        emit_type(Constants.INSTANCEOF, classType);
    }

    public void process_switch(int[] keys, ProcessSwitchCallback callback) throws Exception {
        float density;
        if (keys.length == 0) {
            density = 0;
        } else {
            density = (float)keys.length / (keys[keys.length - 1] - keys[0] + 1);
        }
        process_switch(keys, callback, density >= 0.5f);
    }

    public void process_switch(int[] keys, ProcessSwitchCallback callback, boolean useTable) throws Exception {
        if (!isSorted(keys))
            throw new IllegalArgumentException("keys to switch must be sorted ascending");
        org.objectweb.asm.Label def = make_label();
        org.objectweb.asm.Label end = make_label();

        if (keys.length > 0) {
            int len = keys.length;
            int min = keys[0];
            int max = keys[len - 1];
            int range = max - min + 1;

            if (useTable) {
                org.objectweb.asm.Label[] labels = new org.objectweb.asm.Label[range];
                Arrays.fill(labels, def);
                for (int i = 0; i < len; i++) {
                    labels[keys[i] - min] = make_label();
                }
                codev.visitTableSwitchInsn(min, max, def, labels);
                for (int i = 0; i < range; i++) {
                    org.objectweb.asm.Label label = labels[i];
                    if (label != def) {
                        mark(label);
                        callback.processCase(i + min, end);
                    }
                }
            } else {
                org.objectweb.asm.Label[] labels = new org.objectweb.asm.Label[len];
                for (int i = 0; i < len; i++) {
                    labels[i] = make_label();
                }
                codev.visitLookupSwitchInsn(def, keys, labels);
                for (int i = 0; i < len; i++) {
                    mark(labels[i]);
                    callback.processCase(keys[i], end);
                }
            }
        }

        mark(def);
        callback.processDefault();
        mark(end);
    }

    public interface FinalizeCallback {
        void process();
    }

    private static boolean isSorted(int[] keys) {
        for (int i = 1; i < keys.length; i++) {
            if (keys[i] < keys[i - 1])
                return false;
        }
        return true;
    }

    public void mark(org.objectweb.asm.Label label) {
        codev.visitLabel(label);
    }

    private org.objectweb.asm.Label mark() {
        org.objectweb.asm.Label label = make_label();
        codev.visitLabel(label);
        return label;
    }
}
