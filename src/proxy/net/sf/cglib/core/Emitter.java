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
public class Emitter {
    private static final Signature BOOLEAN_VALUE =
      TypeUtils.parseSignature("boolean booleanValue()");
    private static final Signature CHAR_VALUE =
      TypeUtils.parseSignature("char charValue()");
    private static final Signature LONG_VALUE =
      TypeUtils.parseSignature("long longValue()");
    private static final Signature DOUBLE_VALUE =
      TypeUtils.parseSignature("double doubleValue()");
    private static final Signature FLOAT_VALUE =
      TypeUtils.parseSignature("float floatValue()");
    private static final Signature INT_VALUE =
      TypeUtils.parseSignature("int intValue()");
    private static final Signature STATIC =
      TypeUtils.parseSignature("void <clinit>()");
    private static final Signature CSTRUCT_NULL =
      TypeUtils.parseConstructor("");
    private static final Signature CSTRUCT_STRING =
      TypeUtils.parseConstructor("String");

    public static final int ADD = Constants.IADD;
    public static final int MUL = Constants.IMUL;
    public static final int XOR = Constants.IXOR;
    public static final int USHR = Constants.IUSHR;
    public static final int SUB = Constants.ISUB;
    public static final int DIV = Constants.IDIV;
    public static final int NEG = Constants.INEG;
    public static final int REM = Constants.IREM;
    public static final int AND = Constants.IAND;
    public static final int OR = Constants.IOR;

    public static final int GT = Constants.IFGT;
    public static final int LT = Constants.IFLT;
    public static final int GE = Constants.IFGE;
    public static final int LE = Constants.IFLE;
    public static final int NE = Constants.IFNE;
    public static final int EQ = Constants.IFEQ;

    // current class
    private ClassVisitor classv;
    private int classAccess;
    private Type classType;
    private Type superType;
    private Map fieldInfo = new HashMap();    

    // current method
    private EmitterMethod curMethod;
    private CodeVisitor codev;

    // current block
    private Block curBlock;

    public Emitter(ClassVisitor v) {
        this.classv = v;
    }

    public Type getClassType() {
        return classType;
    }

    public Type getSuperType() {
        return superType;
    }

    public Type getReturnType() {
        return curMethod.getReturnType();
    }

    protected void init() {
        // no-op
    }
    
    public void begin_class(int access, String className, Type superType, Type[] interfaces, String sourceFile) {
        init();
        this.classAccess = access;
        this.classType = Type.getType("L" + className.replace('.', '/') + ";");
        this.superType = (superType != null) ? superType : Constants.TYPE_OBJECT;
        classv.visit(access,
                     this.classType.getInternalName(),
                     this.superType.getInternalName(),
                     TypeUtils.toInternalNames(interfaces),
                     sourceFile);
    }

    public int getClassAccess() {
        return classAccess;
    }

    public void end_class() {
        closeMethod();
        classv.visitEnd();
        classv = null; // for safety
    }
    
    public CodeVisitor begin_method(int access, Signature sig, Type[] exceptions) {
        closeMethod();
        curMethod = new EmitterMethod(classv, access, sig, exceptions);
        codev = curMethod.getRaw();
        return curMethod;
    }

    private void closeMethod() {
        if (curBlock != null) {
            throw new IllegalStateException("unclosed exception block");
        }
        if (curMethod != null) {
            curMethod.close();
            curMethod = null;
            codev = null;
        }
    }

    public CodeVisitor begin_static() {
        return begin_method(Constants.ACC_STATIC, STATIC, null);
    }

    public Block begin_block() {
        return curBlock = new Block(curBlock, mark());
    }

    public void end_block() {
        if (curBlock == null) {
            throw new IllegalStateException("mismatched block boundaries");
        }
        curBlock.setEnd(mark());
        curBlock = curBlock.getParent();
    }

    public void catch_exception(Block block, Type exception) {
        if (block.getEnd() == null) {
            throw new IllegalStateException("end of block is unset");
        }
        codev.visitTryCatchBlock(block.getStart(),
                                 block.getEnd(),
                                 mark(),
                                 exception.getInternalName());
    }

    public void goTo(Label label) { codev.visitJumpInsn(Constants.GOTO, label); }
    public void ifnull(Label label) { codev.visitJumpInsn(Constants.IFNULL, label); }
    public void ifnonnull(Label label) { codev.visitJumpInsn(Constants.IFNONNULL, label); }

    public void if_jump(int mode, Label label) {
        codev.visitJumpInsn(mode, label);
    }

    public void if_icmp(int mode, Label label) {
        if_cmp(Type.INT_TYPE, mode, label);
    }

    public void if_cmp(Type type, int mode, Label label) {
        int intOp = -1;
        int jumpmode = mode;
        switch (mode) {
        case GE: jumpmode = LT; break;
        case LE: jumpmode = GT; break;
        }
        switch (type.getSort()) {
        case Type.LONG:
            codev.visitInsn(Constants.LCMP);
            break;
        case Type.DOUBLE:
            codev.visitInsn(Constants.DCMPG);
            break;
        case Type.FLOAT:
            codev.visitInsn(Constants.FCMPG);
            break;
        case Type.ARRAY:
        case Type.OBJECT:
            switch (mode) {
            case EQ:
                codev.visitJumpInsn(Constants.IF_ACMPEQ, label);
                return;
            case NE:
                codev.visitJumpInsn(Constants.IF_ACMPNE, label);
                return;
            }
            throw new IllegalArgumentException("Bad comparison for type " + type);
        default:
            switch (mode) {
            case EQ: intOp = Constants.IF_ICMPEQ; break;
            case NE: intOp = Constants.IF_ICMPNE; break;
            case GE: swap(); /* fall through */
            case LT: intOp = Constants.IF_ICMPLT; break;
            case LE: swap(); /* fall through */
            case GT: intOp = Constants.IF_ICMPGT; break;
            }
            codev.visitJumpInsn(intOp, label);
            return;
        }
        if_jump(jumpmode, label);
    }

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
            codev.visitInsn(TypeUtils.ICONST(i));
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
            codev.visitInsn(TypeUtils.LCONST(value));
        } else {
            codev.visitLdcInsn(new Long(value));
        }
    }
    
    public void push(float value) {
        if (value == 0f || value == 1f || value == 2f) {
            codev.visitInsn(TypeUtils.FCONST(value));
        } else {
            codev.visitLdcInsn(new Float(value));
        }
    }
    public void push(double value) {
        if (value == 0d || value == 1d) {
            codev.visitInsn(TypeUtils.DCONST(value));
        } else {
            codev.visitLdcInsn(new Double(value));
        }
    }
    
    public void push(String value) {
        codev.visitLdcInsn(value);
    }

    public void newarray() {
        newarray(Constants.TYPE_OBJECT);
    }

    public void newarray(Type type) {
        if (TypeUtils.isPrimitive(type)) {
            codev.visitIntInsn(Constants.NEWARRAY, TypeUtils.NEWARRAY(type));
        } else {
            emit_type(Constants.ANEWARRAY, type);
        }
    }
    
    public void arraylength() {
        codev.visitInsn(Constants.ARRAYLENGTH);
    }
    
    public void load_this() {
        if (TypeUtils.isStatic(curMethod.getAccess())) {
            throw new IllegalStateException("no 'this' pointer within static method");
        }
        codev.visitVarInsn(Constants.ALOAD, 0);
    }
    
    /**
     * Pushes all of the arguments of the current method onto the stack.
     */
    public void load_args() {
        load_args(0, curMethod.getArgumentTypes().length);
    }

    /**
     * Pushes the specified argument of the current method onto the stack.
     * @param index the zero-based index into the argument list
     */
    public void load_arg(int index) {
        load_local(curMethod.getArgumentTypes()[index],
                   curMethod.getLocalOffset() + skipArgs(index));
    }

    // zero-based (see load_this)
    public void load_args(int fromArg, int count) {
        Type[] args = curMethod.getArgumentTypes();
        int pos = curMethod.getLocalOffset() + skipArgs(fromArg);
        for (int i = 0; i < count; i++) {
            Type t = args[fromArg + i];
            load_local(t, pos);
            pos += t.getSize();
        }
    }
    
    private int skipArgs(int numArgs) {
        Type[] args = curMethod.getArgumentTypes();
        int amount = 0;
        for (int i = 0; i < numArgs; i++) {
            amount += args[i].getSize();
        }
        return amount;
    }

    private void load_local(Type t, int pos) {
        // TODO: make t == null ok?
        codev.visitVarInsn(t.getOpcode(Constants.ILOAD), pos);
    }

    private void store_local(Type t, int pos) {
        // TODO: make t == null ok?
        codev.visitVarInsn(t.getOpcode(Constants.ISTORE), pos);
    }
    
    public void iinc(Local local, int amount) {
        codev.visitIincInsn(local.getIndex(), amount);
    }
    
    public void store_local(Local local) {
        store_local(local.getType(), local.getIndex());
    }
    
    public void load_local(Local local) {
        load_local(local.getType(), local.getIndex());
    }

    public void return_value() {
        codev.visitInsn(getReturnType().getOpcode(Constants.IRETURN));
    }

    public void declare_field(int access, String name, Type type, Object value) {
        if (fieldInfo.get(name) != null) {
            throw new IllegalArgumentException("Field \"" + name + "\" already exists");
        }
        fieldInfo.put(name, new FieldInfo(TypeUtils.isStatic(access), type));
        classv.visitField(access, name, type.getDescriptor(), value);
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

    // package-protected for ReflectOps, try to fix
    void emit_field(int opcode, Type ctype, String name, Type ftype) {
        codev.visitFieldInsn(opcode,
                             ctype.getInternalName(),
                             name,
                             ftype.getDescriptor());
    }

    public void super_invoke() {
        super_invoke(curMethod.getSignature());
    }

    public void super_invoke(Signature sig) {
        emit_invoke(Constants.INVOKESPECIAL, superType, sig);
    }

    public void invoke_constructor(Type type) {
        invoke_constructor(type, CSTRUCT_NULL);
    }

    public void super_invoke_constructor() {
        invoke_constructor(superType);
    }
    
    public void invoke_constructor_this() {
        invoke_constructor(classType);
    }

    private void emit_invoke(int opcode, Type type, Signature sig) {
        if (sig.getName().equals(Constants.CONSTRUCTOR_NAME) &&
            ((opcode == Constants.INVOKEVIRTUAL) ||
             (opcode == Constants.INVOKESTATIC))) {
            // TODO: error
        }
        codev.visitMethodInsn(opcode,
                              type.getInternalName(),
                              sig.getName(),
                              sig.getDescriptor());
    }
    
    public void invoke_interface(Type owner, Signature sig) {
        emit_invoke(Constants.INVOKEINTERFACE, owner, sig);
    }

    public void invoke_virtual(Type owner, Signature sig) {
        emit_invoke(Constants.INVOKEVIRTUAL, owner, sig);
    }

    public void invoke_static(Type owner, Signature sig) {
        emit_invoke(Constants.INVOKESTATIC, owner, sig);
    }

    public void invoke_virtual_this(Signature sig) {
        invoke_virtual(classType, sig);
    }

    public void invoke_static_this(Signature sig) {
        invoke_static(classType, sig);
    }

    public void invoke_constructor(Type type, Signature sig) {
        emit_invoke(Constants.INVOKESPECIAL, type, sig);
    }

    public void invoke_constructor_this(Signature sig) {
        invoke_constructor(classType, sig);
    }

    public void super_invoke_constructor(Signature sig) {
        invoke_constructor(superType, sig);
    }
    
    public void new_instance_this() {
        new_instance(classType);
    }

    public void new_instance(Type type) {
        emit_type(Constants.NEW, type);
    }

    private void emit_type(int opcode, Type type) {
        String desc;
        if (TypeUtils.isArray(type)) {
            desc = type.getDescriptor();
        } else {
            desc = type.getInternalName();
        }
        codev.visitTypeInsn(opcode, desc);
    }

    public void aaload(int index) {
        push(index);
        aaload();
    }

    public void aaload() { codev.visitInsn(Constants.AALOAD); }
    public void aastore() { codev.visitInsn(Constants.AASTORE); }
    public void athrow() { codev.visitInsn(Constants.ATHROW); }

    public Label make_label() {
        return new Label();
    }
    
    public Local make_local() {
        return make_local(Constants.TYPE_OBJECT);
    }
    
    public Local make_local(Type type) {
        return curMethod.make_local(type);
    }

    public void checkcast_this() {
        checkcast(classType);
    }
    
    public void checkcast(Type type) {
        if (!type.equals(Constants.TYPE_OBJECT)) {
            emit_type(Constants.CHECKCAST, type);
        }
    }

    public void instance_of(Type type) {
        emit_type(Constants.INSTANCEOF, type);
    }
    
    public void instance_of_this() {
        instance_of(classType);
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
        Label def = make_label();
        Label end = make_label();

        if (keys.length > 0) {
            int len = keys.length;
            int min = keys[0];
            int max = keys[len - 1];
            int range = max - min + 1;

            if (useTable) {
                Label[] labels = new Label[range];
                Arrays.fill(labels, def);
                for (int i = 0; i < len; i++) {
                    labels[keys[i] - min] = make_label();
                }
                codev.visitTableSwitchInsn(min, max, def, labels);
                for (int i = 0; i < range; i++) {
                    Label label = labels[i];
                    if (label != def) {
                        mark(label);
                        callback.processCase(i + min, end);
                    }
                }
            } else {
                Label[] labels = new Label[len];
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

    private static boolean isSorted(int[] keys) {
        for (int i = 1; i < keys.length; i++) {
            if (keys[i] < keys[i - 1])
                return false;
        }
        return true;
    }

    public void mark(Label label) {
        codev.visitLabel(label);
    }

    private Label mark() {
        Label label = make_label();
        codev.visitLabel(label);
        return label;
    }

    public void push(boolean value) {
        push(value ? 1 : 0);
    }

    /**
     * Toggles the integer on the top of the stack from 1 to 0 or vice versa
     */
    public void not() {
        push(1);
        math(XOR, Type.INT_TYPE);
    }

    public void throw_exception(Type type, String msg) {
        new_instance(type);
        dup();
        push(msg);
        invoke_constructor(type, CSTRUCT_STRING);
        athrow();
    }

    public void factory_method(Signature sig) {
        begin_method(Constants.ACC_PUBLIC, sig, null);
        new_instance_this();
        dup();
        load_args();
        invoke_constructor_this(TypeUtils.parseConstructor(sig.getArgumentTypes()));
        return_value();
    }

    public void null_constructor() {
        begin_method(Constants.ACC_PUBLIC, CSTRUCT_NULL, null);
        load_this();
        super_invoke_constructor();
        return_value();
    }
    
    /**
     * Allocates and fills an Object[] array with the arguments to the
     * current method. Primitive values are inserted as their boxed
     * (Object) equivalents.
     */
    public void create_arg_array() {
        /* generates:
           Object[] args = new Object[]{ arg1, new Integer(arg2) };
         */

        Type[] args = curMethod.getArgumentTypes();
        push(args.length);
        newarray();
        for (int i = 0; i < args.length; i++) {
            dup();
            push(i);
            load_arg(i);
            box(args[i]);
            aastore();
        }
    }

     /**
      * If the argument is a primitive class, replaces the primitive value
      * on the top of the stack with the wrapped (Object) equivalent. For
      * example, char -> Character.
      * If the class is Void, a null is pushed onto the stack instead.
      * @param type the class indicating the current type of the top stack value
      */
     public void box(Type type) {
         if (TypeUtils.isPrimitive(type)) {
             if (type == Type.VOID_TYPE) {
                 aconst_null();
             } else {
                 Type boxed = TypeUtils.getBoxedType(type);
                 new_instance(boxed);
                 if (type.getSize() == 2) {
                     // Pp -> Ppo -> oPpo -> ooPpo -> ooPp -> o
                     dup_x2();
                     dup_x2();
                     pop();
                 } else {
                     // p -> po -> opo -> oop -> o
                     dup_x1();
                     swap();
                 }
                 invoke_constructor(boxed, new Signature(Constants.CONSTRUCTOR_NAME, Type.VOID_TYPE, new Type[]{ type }));
             }
         }
     }
    
    /**
     * If the argument is a primitive class, replaces the object
     * on the top of the stack with the unwrapped (primitive)
     * equivalent. For example, Character -> char.
     * @param type the class indicating the desired type of the top stack value
     * @return true if the value was unboxed
     */
    public void unbox(Type type) {
        Type t = Constants.TYPE_NUMBER;
        Signature sig = null;
        switch (type.getSort()) {
        case Type.VOID:
            return;
        case Type.CHAR:
            t = Constants.TYPE_CHARACTER;
            sig = CHAR_VALUE;
            break;
        case Type.BOOLEAN:
            t = Constants.TYPE_BOOLEAN;
            sig = BOOLEAN_VALUE;
            break;
        case Type.DOUBLE:
            sig = DOUBLE_VALUE;
            break;
        case Type.FLOAT:
            sig = FLOAT_VALUE;
            break;
        case Type.LONG:
            sig = LONG_VALUE;
            break;
        case Type.INT:
        case Type.SHORT:
        case Type.BYTE:
            sig = INT_VALUE;
        }

        if (sig == null) {
            checkcast(type);
        } else {
            checkcast(t);
            invoke_virtual(t, sig);
        }
    }

    /**
     * Pushes a zero onto the stack if the argument is a primitive class, or a null otherwise.
     */
    public void zero_or_null(Type type) {
        if (TypeUtils.isPrimitive(type)) {
            switch (type.getSort()) {
            case Type.DOUBLE:
                push(0d);
                break;
            case Type.LONG:
                push(0L);
                break;
            case Type.FLOAT:
                push(0f);
                break;
            case Type.VOID:
                aconst_null();
            default:
                push(0);
            }
        } else {
            aconst_null();
        }
    }

    /**
     * Unboxes the object on the top of the stack. If the object is null, the
     * unboxed primitive value becomes zero.
     */
    public void unbox_or_zero(Type type) {
        if (TypeUtils.isPrimitive(type)) {
            if (type != Type.VOID_TYPE) {
                Label nonNull = make_label();
                Label end = make_label();
                dup();
                ifnonnull(nonNull);
                pop();
                zero_or_null(type);
                goTo(end);
                mark(nonNull);
                unbox(type);
                mark(end);
            }
        } else {
            checkcast(type);
        }
    }
}
