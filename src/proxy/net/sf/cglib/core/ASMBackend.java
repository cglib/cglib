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
package net.sf.cglib.core;

import java.lang.reflect.Modifier;
import java.util.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;

public class ASMBackend implements EmitterBackend {
    private ClassWriter cw;
    private CodeVisitor cv;

    public void init(Emitter gen) {
        cw = new ClassWriter(true);
        cw.visit(gen.getClassModifiers(),
                 getInternalName(gen.getClassName()),
                 getInternalName(gen.getSuperclass()),
                 getInternalNames(gen.getInterfaces()),
                 Constants.SOURCE_FILE);
    }
    
    public byte[] getBytes() {
        cw.visitEnd();
        return cw.toByteArray();
    }

    public void emit(int opcode) {
        cv.visitInsn(opcode);
    }

    public void emit(Label label) {
        cv.visitLabel(convertLabel(label));
    }

    public void emit(int opcode, Label label) {
        cv.visitJumpInsn(opcode, convertLabel(label));
    }
    
    public void emit_var(int opcode, int index) {
        cv.visitVarInsn(opcode, index);
    }
    
    public void emit_type(int opcode, String className) {
        cv.visitTypeInsn(opcode, getInternalName(className));
    }
    
    public void emit_int(int opcode, int value) {
        cv.visitIntInsn(opcode, value);
    }
    
    public void emit_field(int opcode, String className, String fieldName, Class type) {
        cv.visitFieldInsn(opcode,
                          getInternalName(className),
                          fieldName,
                          getInternalName(ReflectUtils.getDescriptor(type)));
    }

    public Label make_label() {
        return new ASMLabel();
    }

    private static class ASMLabel implements Label {
        org.objectweb.asm.Label label = new org.objectweb.asm.Label();
    }

    public Label mark() {
        Label label = make_label();
        emit(label);
        return label;
    }

    public void catch_exception(Block block, Class exceptionType) {
        cv.visitTryCatchBlock(convertLabel(block.getStart()),
                              convertLabel(block.getEnd()),
                              convertLabel(mark()),
                              getInternalName(exceptionType));
    }

    public void begin_constructor(Class[] parameterTypes) {
        cv = cw.visitMethod(Modifier.PUBLIC,
                            Constants.CONSTRUCTOR_NAME,
                            getInternalName(ReflectUtils.getMethodDescriptor(Void.TYPE, parameterTypes)),
                            null);
    }

    public void begin_method(int modifiers, Class returnType, String name, Class[] parameterTypes, Class[] exceptionTypes) {
        cv = cw.visitMethod(modifiers, // ASM modifier constants are same as JDK
                            name,
                            getInternalName(ReflectUtils.getMethodDescriptor(returnType, parameterTypes)),
                            getInternalNames(exceptionTypes));
    }

    public void begin_static() {
        cv = cw.visitMethod(Modifier.STATIC,
                            Constants.STATIC_NAME,
                            getInternalName(ReflectUtils.getMethodDescriptor(Void.TYPE, Constants.TYPES_EMPTY)),
                            null);
    }

    public void end_method() {
        cv.visitMaxs(0, 0); // values are ignored
    }

    public void emit_invoke(int opcode,
                            String className,
                            String methodName,
                            Class returnType,
                            Class[] parameterTypes) {
        cv.visitMethodInsn(opcode,
                           getInternalName(className),
                           methodName,
                           getInternalName(ReflectUtils.getMethodDescriptor(returnType, parameterTypes)));
    }

    public void declare_field(int modifiers, Class type, String name) {
        cw.visitField(modifiers, name, getInternalName(ReflectUtils.getDescriptor(type)), null);
    }

    public void emit_iinc(int index, int amount) {
        cv.visitIincInsn(index, amount);
    }

    public void emit_ldc(Object value) {
        cv.visitLdcInsn(value);
    }

    public void emit_switch(int[] keys, Label[] labels, Label def) {
        cv.visitLookupSwitchInsn(convertLabel(def),
                                 keys,
                                 convertLabels(labels));
    }

    public void emit_switch(int min, int max, Label[] labels, Label def) {
        cv.visitTableSwitchInsn(min, max,
                                convertLabel(def),
                                convertLabels(labels));
    }

    private org.objectweb.asm.Label convertLabel(Label label) {
        return ((ASMLabel)label).label;
    }

    private org.objectweb.asm.Label[] convertLabels(Label[] labels) {
        org.objectweb.asm.Label[] converted = new org.objectweb.asm.Label[labels.length];
        for (int i = 0; i < labels.length; i++) {
            converted[i] = convertLabel(labels[i]);
        }
        return converted;
    }

    private static String getInternalName(Class type) {
        return getInternalName(type.getName());
    }

    private static String getInternalName(String className) {
        return (className == null) ? null : className.replace('.', '/');
    }

    private static String[] getInternalNames(Class[] classes) {
        if (classes == null)
            return null;
        String[] copy = new String[classes.length];
        for (int i = 0; i < copy.length; i++) {
            copy[i] = getInternalName(classes[i]);
        }
        return copy;
    }
}
