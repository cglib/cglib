/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
package net.sf.cglib;

import java.lang.reflect.Modifier;
import java.util.*;
import org.objectweb.asm.*;

class ASMBackend extends CodeGeneratorBackend implements org.objectweb.asm.Constants {
    private ClassWriter cw;
    private CodeVisitor cv;
    private boolean interfaceFlag;
    private Set interfaces = new HashSet();
    private Map labels = new HashMap();

    public ASMBackend(String className, Class superclass) {
         super(className, superclass);
    }

    private boolean inited()
    {
        return cw != null;
    }

    private void init() {
        if (cw == null) {
            cw = new ClassWriter(true);
            String[] interfaceNames = null;
            if (interfaces.size() > 0) {
                interfaceNames = getInternalNames((Class[])interfaces.toArray(new Class[interfaces.size()]));
            }
            cw.visit(ACC_PUBLIC | (interfaceFlag ? ACC_INTERFACE : 0),
                     getInternalName(className),
                     Type.getInternalName(superclass),
                     interfaceNames,
                     Constants.SOURCE_FILE);
        }
    }

    public void setInterface(boolean flag) {
        if (inited()) {
            throw new RuntimeException("Cannot set interface status after ClassWriter has been initialized");
        }
        interfaceFlag = flag;
    }

    public byte[] getBytes() {
        cw.visitEnd();
        return cw.toByteArray();
    }

    private Label make_label(Object maybeLabel) {
        if (maybeLabel instanceof Label) {
            return (Label)maybeLabel;
        } else {
            Label label = (Label)labels.get(maybeLabel);
            if (label == null) {
                labels.put(maybeLabel, label = (Label)make_label());
            }
            return label;
        }
    }

    public Object make_label() {
        return new Label();
    }

    public void ifeq(Object label) { cv.visitJumpInsn(IFEQ, make_label(label)); }
    public void ifne(Object label) { cv.visitJumpInsn(IFNE, make_label(label)); }
    public void iflt(Object label) { cv.visitJumpInsn(IFLT, make_label(label)); }
    public void ifge(Object label) { cv.visitJumpInsn(IFGE, make_label(label)); }
    public void ifgt(Object label) { cv.visitJumpInsn(IFGT, make_label(label)); }
    public void ifle(Object label) { cv.visitJumpInsn(IFLE, make_label(label)); }
    public void goTo(Object label) { cv.visitJumpInsn(GOTO, make_label(label)); }
    public void ifnull(Object label) { cv.visitJumpInsn(IFNULL, make_label(label)); }
    public void ifnonnull(Object label) { cv.visitJumpInsn(IFNONNULL, make_label(label)); }
    public void if_icmplt(Object label) { cv.visitJumpInsn(IF_ICMPLT, make_label(label)); }
    public void if_icmpne(Object label) { cv.visitJumpInsn(IF_ICMPNE, make_label(label)); }
    public void if_icmpeq(Object label) { cv.visitJumpInsn(IF_ICMPEQ, make_label(label)); }

    public void nop(Object label) {
        cv.visitLabel(make_label(label));
        cv.visitInsn(NOP);
    }

    public void imul() { cv.visitInsn(IMUL); }
    public void iadd() { cv.visitInsn(IADD); }
    public void lushr() { cv.visitInsn(LUSHR); }
    public void lxor() { cv.visitInsn(LXOR); }
    public void ixor() { cv.visitInsn(IXOR); }
    public void l2i() { cv.visitInsn(L2I); }
    public void dcmpg() { cv.visitInsn(DCMPG); }
    public void fcmpg() { cv.visitInsn(FCMPG); }
    public void lcmp() { cv.visitInsn(LCMP); }
    public void aconst_null() { cv.visitInsn(ACONST_NULL); }
    public void arraylength() { cv.visitInsn(ARRAYLENGTH); }

    public void newarray(Class clazz) {
        // TODO: find this lookup in ASM
        int v = 0;
        switch (clazz.getName().charAt(0)) {
        case 'B': v = 8; break;
        case 'C': v = 5; break;
        case 'D': v = 7; break;
        case 'F': v = 6; break;
        case 'I': v = 10; break;
        case 'J': v = 11; break;
        case 'S': v = 9; break;
        case 'Z': v = 4; break;
        }
        cv.visitIntInsn(NEWARRAY, v);
    }
    
    public void anewarray(Class clazz) {
        String desc;
        if (clazz.isArray()) {
            desc = Type.getDescriptor(clazz);
        } else {
            desc = Type.getInternalName(clazz);
        }
        cv.visitTypeInsn(ANEWARRAY, desc);
    }
    
    public void new_instance(String className) { cv.visitTypeInsn(NEW, getInternalName(className)); }
    public void checkcast(String className) { cv.visitTypeInsn(CHECKCAST, getInternalName(className)); }
    public void instance_of(String className) { cv.visitTypeInsn(INSTANCEOF, getInternalName(className)); }
    public void aaload() { cv.visitInsn(AALOAD); }
    public void aastore() { cv.visitInsn(AASTORE); }
    public void athrow() { cv.visitInsn(ATHROW); }

    private String getInternalName(String className) {
        return (className == null) ? null : className.replace('.', '/');
    }

    private String[] getInternalNames(Class[] classes) {
        if (classes == null)
            return null;
        String[] copy = new String[classes.length];
        for (int i = 0; i < copy.length; i++) {
            copy[i] = Type.getInternalName(classes[i]);
        }
        return copy;
    }

    public void pop() { cv.visitInsn(POP); }
    public void pop2() { cv.visitInsn(POP2); }
    public void dup() { cv.visitInsn(DUP); }
    public void dup2() { cv.visitInsn(DUP2); }
    public void dup_x1() { cv.visitInsn(DUP_X1); }
    public void dup_x2() { cv.visitInsn(DUP_X2); }
    public void swap() { cv.visitInsn(SWAP); }

    public void invoke_interface(String className, String methodName, Class returnType, Class[] parameterTypes) {
        methodHelper(INVOKEINTERFACE, className, methodName, returnType, parameterTypes);
    }

    public void invoke_virtual(String className, String methodName, Class returnType, Class[] parameterTypes) {
        methodHelper(INVOKEVIRTUAL, className, methodName, returnType, parameterTypes);
    }

    public void invoke_static(String className, String methodName, Class returnType, Class[] parameterTypes) {
        methodHelper(INVOKESTATIC, className, methodName, returnType, parameterTypes);
    }

    public void invoke_special(String className, String methodName, Class returnType, Class[] parameterTypes) {
        methodHelper(INVOKESPECIAL, className, methodName, returnType, parameterTypes);
    }

    private void methodHelper(int opcode, String className, String methodName, Class returnType, Class[] parameterTypes) {
        cv.visitMethodInsn(opcode,
                           getInternalName(className),
                           methodName,
                           Type.getMethodDescriptor(Type.getType(returnType), getTypes(parameterTypes)));
    }

    public void declare_field(int modifiers, Class typeClass, String fieldName) {
        init();
        cw.visitField(convertModifiers(modifiers), fieldName, Type.getDescriptor(typeClass), null);
    }

    public void getfield(String className, String fieldName, Class type) {
        fieldHelper(GETFIELD, className, fieldName, type);
    }

    public void putfield(String className, String fieldName, Class type) {
        fieldHelper(PUTFIELD, className, fieldName, type);
    }

    public void getstatic(String className, String fieldName, Class type) {
        fieldHelper(GETSTATIC, className, fieldName, type);
    }

    public void putstatic(String className, String fieldName, Class type) {
        fieldHelper(PUTSTATIC, className, fieldName, type);
    }

    private void fieldHelper(int opcode, String className, String fieldName, Class type) {
        cv.visitFieldInsn(opcode, getInternalName(className), fieldName, Type.getDescriptor(type));
    }

    public void begin_static() {
        init();
        cv = cw.visitMethod(ACC_STATIC,
                            Constants.STATIC_NAME,
                            Type.getMethodDescriptor(Type.VOID_TYPE, new Type[0]),
                            null);
    }

    public void declare_interface(Class iface) {
        if (inited()) {
            throw new RuntimeException("Cannot declare interface after ClassWriter has been initialized");
        }
        interfaces.add(iface);
    }
    
    public void begin_method(int modifiers, Class returnType, String methodName,
                             Class[] parameterTypes, Class[] exceptionTypes) {
        init();
        labels.clear();
        cv = cw.visitMethod(convertModifiers(modifiers),
                            methodName,
                            Type.getMethodDescriptor(Type.getType(returnType), getTypes(parameterTypes)),
                            getInternalNames(exceptionTypes));
    }

    private Label mark() {
        Object label = make_label();
        nop(label);
        return (Label)label;
    }

    public Object start_range() {
        return mark();
    }

    public Object end_range() {
        return mark();
    }

    public void handle_exception(Object start, Object end, Class exceptionType) {
        cv.visitTryCatchBlock(make_label(start), make_label(end), mark(), Type.getInternalName(exceptionType));
    }

    public void begin_constructor(Class[] parameterTypes) {
        init();
        cv = cw.visitMethod(ACC_PUBLIC,
                            Constants.CONSTRUCTOR_NAME,
                            Type.getMethodDescriptor(Type.VOID_TYPE, getTypes(parameterTypes)),
                            null);
    }

    public void end_method() {
        cv.visitMaxs(0, 0); // values are ignored
    }

    // backend only

    public void ldc(String value) { cv.visitLdcInsn(value); }
    public void ldc(double value) { cv.visitLdcInsn(new Double(value)); }
    public void ldc(long value) { cv.visitLdcInsn(new Long(value)); }
    public void ldc(int value) { cv.visitLdcInsn(new Integer(value)); }
    public void ldc(float value) { cv.visitLdcInsn(new Float(value)); }

    public void laload() { cv.visitInsn(LALOAD); }
    public void daload() { cv.visitInsn(DALOAD); }
    public void faload() { cv.visitInsn(FALOAD); }
    public void saload() { cv.visitInsn(SALOAD); }
    public void caload() { cv.visitInsn(CALOAD); }
    public void iaload() { cv.visitInsn(IALOAD); }
    public void baload() { cv.visitInsn(BALOAD); }
    public void lastore() { cv.visitInsn(LASTORE); }
    public void dastore() { cv.visitInsn(DASTORE); }
    public void fastore() { cv.visitInsn(FASTORE); }
    public void sastore() { cv.visitInsn(SASTORE); }
    public void castore() { cv.visitInsn(CASTORE); }
    public void iastore() { cv.visitInsn(IASTORE); }
    public void bastore() { cv.visitInsn(BASTORE); }

    public void iconst(int value) {
        switch (value) {
        case -1: cv.visitInsn(ICONST_M1); break;
        case 0: cv.visitInsn(ICONST_0); break;
        case 1: cv.visitInsn(ICONST_1); break;
        case 2: cv.visitInsn(ICONST_2); break;
        case 3: cv.visitInsn(ICONST_3); break;
        case 4: cv.visitInsn(ICONST_4); break;
        case 5: cv.visitInsn(ICONST_5); break;
        }
    }
    
    public void bipush(byte value) { cv.visitIntInsn(BIPUSH, value); }
    public void sipush(short value) { cv.visitIntInsn(SIPUSH, value); }
    
    public void lconst(long value) {
        cv.visitInsn(value == 0L ? LCONST_0 : LCONST_1);
    }

    public void fconst(float value) {
        if (value == 0f) {
            cv.visitInsn(FCONST_0);
        } else if (value == 1f) {
            cv.visitInsn(FCONST_1);
        } else {
            cv.visitInsn(FCONST_2);
        }
    }

    public void dconst(double value) {
        cv.visitInsn(value == 0d ? DCONST_0 : DCONST_1);        
    }
    
    public void lload(int index) { cv.visitVarInsn(LLOAD, index); }
    public void dload(int index) { cv.visitVarInsn(DLOAD, index); }
    public void fload(int index) { cv.visitVarInsn(FLOAD, index); }
    public void iload(int index) { cv.visitVarInsn(ILOAD, index); }
    public void aload(int index) { cv.visitVarInsn(ALOAD, index); }
    public void lstore(int index) { cv.visitVarInsn(LSTORE, index); }
    public void dstore(int index) { cv.visitVarInsn(DSTORE, index); }
    public void fstore(int index) { cv.visitVarInsn(FSTORE, index); }
    public void istore(int index) { cv.visitVarInsn(ISTORE, index); }
    public void astore(int index) { cv.visitVarInsn(ASTORE, index); }
    public void returnVoid() { cv.visitInsn(RETURN); }
    public void lreturn() { cv.visitInsn(LRETURN); }
    public void dreturn() { cv.visitInsn(DRETURN); }
    public void freturn() { cv.visitInsn(FRETURN); }
    public void ireturn() { cv.visitInsn(IRETURN); }
    public void areturn() { cv.visitInsn(ARETURN); }
    public void iinc(int index, int amount) { cv.visitIincInsn(index, amount); }

    private static int convertModifiers(int modifiers) {
        int result = 0;
        if (Modifier.isAbstract(modifiers))
            result |= ACC_ABSTRACT;
        if (Modifier.isFinal(modifiers))
            result |= ACC_FINAL;
        if (Modifier.isInterface(modifiers))
            result |= ACC_INTERFACE;
        if (Modifier.isNative(modifiers))
            result |= ACC_NATIVE;
        if (Modifier.isPrivate(modifiers))
            result |= ACC_PRIVATE;
        if (Modifier.isProtected(modifiers))
            result |= ACC_PROTECTED;
        if (Modifier.isPublic(modifiers))
            result |= ACC_PUBLIC;
        if (Modifier.isStatic(modifiers))
            result |= ACC_STATIC;
        if (Modifier.isStrict(modifiers))
            result |= ACC_STRICT;
        if (Modifier.isSynchronized(modifiers))
            result |= ACC_SYNCHRONIZED;
        if (Modifier.isTransient(modifiers))
            result |= ACC_TRANSIENT;
        if (Modifier.isVolatile(modifiers))
            result |= ACC_VOLATILE;
        return result;
    }

    private Type[] getTypes(Class[] classes) {
        Type[] types = new Type[classes.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = Type.getType(classes[i]);
        }
        return types;
    }
}
