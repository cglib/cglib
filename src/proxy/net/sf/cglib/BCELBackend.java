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
import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.AccessFlags;

class BCELBackend extends CodeGeneratorBackend implements org.apache.bcel.Constants {
    private final ClassGen cg;
    private final InstructionList il = new InstructionList();
    private final ConstantPoolGen cp;
    private MethodGen mg;
    private Map branches = new HashMap();
    private Map labels = new HashMap();
    

    public BCELBackend(String className, Class superclass) {
        super(className, superclass);
        cg = new ClassGen(className, superclass.getName(), Constants.SOURCE_FILE, ACC_PUBLIC, null); 
        cp = cg.getConstantPool();
    }

    public void setInterface(boolean flag) {
        if (flag) {
            cg.setAccessFlags(cg.getAccessFlags() | Modifier.INTERFACE);
        } else {
            cg.setAccessFlags(cg.getAccessFlags() & ~Modifier.INTERFACE);
        }
    }

    public byte[] getBytes() {
        return cg.getJavaClass().getBytes();
    }

    public void ifeq(Object label) { append(new IFEQ(null), label); }
    public void ifne(Object label) { append(new IFNE(null), label); }
    public void iflt(Object label) { append(new IFLT(null), label); }
    public void ifge(Object label) { append(new IFGE(null), label); }
    public void ifgt(Object label) { append(new IFGT(null), label); }
    public void ifle(Object label) { append(new IFLE(null), label); }
    public void goTo(Object label) { append(new GOTO(null), label); }
    public void ifnull(Object label) { append(new IFNULL(null), label); }
    public void ifnonnull(Object label) { append(new IFNONNULL(null), label); }
    public void if_icmplt(Object label) { append(new IF_ICMPLT(null), label); }
    public void if_icmpne(Object label) { append(new IF_ICMPNE(null), label); }
    public void if_icmpeq(Object label) { append(new IF_ICMPEQ(null), label); }
    public void nop(Object label) { append(label, new NOP()); }
    public void imul() { append(new IMUL()); }
    public void iadd() { append(new IADD()); }
    public void lushr() { append(new LUSHR()); }
    public void lxor() { append(new LXOR()); }
    public void ixor() { append(new IXOR()); }
    public void l2i() { append(new L2I()); }
    public void dcmpg() { append(new DCMPG()); }
    public void fcmpg() { append(new FCMPG()); }
    public void lcmp() { append(new LCMP()); }
    public void aconst_null() { append(new ACONST_NULL()); }
    public void arraylength() { append(new ARRAYLENGTH()); }
    public void newarray(Class clazz) { append(new NEWARRAY((BasicType)getType(clazz))); }
    public void anewarray(Class clazz) { append(new ANEWARRAY(cp.addClass(clazz.getName()))); }
    public void new_instance(String className) { append(new NEW(cp.addClass(className))); }
    public void checkcast(String className) { append(new CHECKCAST(cp.addClass(className))); }
    public void instance_of(String className) { append(new INSTANCEOF(cp.addClass(className))); }
    public void aaload() { append(new AALOAD()); }
    public void aastore() { append(new AASTORE()); }
    public void athrow() { append(new ATHROW()); }
    public void pop() { append(new POP()); }
    public void pop2() { append(new POP2()); }
    public void dup() { append(new DUP()); }
    public void dup2() { append(new DUP2()); }
    public void dup_x1() { append(new DUP_X1()); }
    public void dup_x2() { append(new DUP_X2()); }
    public void swap() { append(new SWAP()); }

    public void invoke_interface(String className, String methodName, Class returnType, Class[] parameterTypes) {
        append(new INVOKEINTERFACE(cp.addInterfaceMethodref(className, methodName, getSignature(returnType, parameterTypes)),
                                   1 + CodeGenerator.getStackSize(parameterTypes)));
    }

    public void invoke_virtual(String className, String methodName, Class returnType, Class[] parameterTypes) {
        append(new INVOKEVIRTUAL(cp.addMethodref(className, methodName, getSignature(returnType, parameterTypes))));
    }

    public void invoke_static(String className, String methodName, Class returnType, Class[] parameterTypes) {
        append(new INVOKESTATIC(cp.addMethodref(className, methodName, getSignature(returnType, parameterTypes))));
    }

    public void invoke_special(String className, String methodName, Class returnType, Class[] parameterTypes) {
        append(new INVOKESPECIAL(cp.addMethodref(className, methodName, getSignature(returnType, parameterTypes))));
    }

    private String getSignature(Class returnType, Class[] parameterTypes) {
        return Type.getMethodSignature(getType(returnType), getTypes(parameterTypes));
    }

    private String getSignature(Class type) {
        return getType(type).getSignature();
    }

    public void declare_field(int modifiers, Class typeClass, String fieldName) {
        Type type = getType(typeClass);
        FieldGen fg = new FieldGen(javaModifiersToBcel(modifiers), type, fieldName, cp);
        cg.addField(fg.getField());
        cp.addFieldref(className, fieldName, getSignature(typeClass));
    }

    public void getfield(String className, String fieldName, Class type) {
        append(new GETFIELD(cp.addFieldref(className, fieldName, getSignature(type))));
    }

    public void putfield(String className, String fieldName, Class type) {
        append(new PUTFIELD(cp.addFieldref(className, fieldName, getSignature(type))));
    }

    public void getstatic(String className, String fieldName, Class type) {
        append(new GETSTATIC(cp.addFieldref(className, fieldName, getSignature(type))));
    }

    public void putstatic(String className, String fieldName, Class type) {
        append(new PUTSTATIC(cp.addFieldref(className, fieldName, getSignature(type))));
    }

    public void begin_static() {
        mg = new MethodGen(ACC_STATIC, Type.VOID, new Type[0], null,
                           Constants.STATIC_NAME, className, il, cp);
    }

    public void declare_interface(Class iface) {
        cg.addInterface(iface.getName());
    }
    
    public void begin_method(int modifiers, Class returnType, String methodName,
                             Class[] parameterTypes, Class[] exceptionTypes) {
        mg = new MethodGen(javaModifiersToBcel(modifiers),
                           getType(returnType),
                           getTypes(parameterTypes),
                           null,
                           methodName,
                           className, il, cp);
        if (exceptionTypes != null) {
            for (int i = 0; i < exceptionTypes.length; i++) {
                mg.addException(exceptionTypes[i].getName());
            }
        }
    }

    public Object start_range() {
        return il.append(new NOP());
    }

    public Object end_range() {
        return il.getEnd().getPrev();        
    }

    public void handle_exception(Object start, Object end, Class exceptionType) {
        mg.addExceptionHandler((InstructionHandle)start,
                               (InstructionHandle)end,
                               il.append(new NOP()),
                               (ObjectType)getType(exceptionType));
    }

    public void begin_constructor(Class[] parameterTypes) {
        mg = new MethodGen(ACC_PUBLIC, Type.VOID, getTypes(parameterTypes), null,
                           Constants.CONSTRUCTOR_NAME, className, il, cp);
    }

    public void end_method() {
        setTargets();
        if (debug) {
            System.out.println(mg.getMethod());
            System.out.println(mg.getMethod().getCode());
        } 
        mg.removeNOPs();
        mg.stripAttributes(true);
        mg.setMaxLocals();
        mg.setMaxStack();
        cg.addMethod(mg.getMethod());
        il.dispose();
    }

    // backend only
    public void ldc(String value) { append(new LDC(cp.addString(value))); }
    public void ldc(double value) { append(new LDC(cp.addDouble(value))); }
    public void ldc(long value) { append(new LDC(cp.addLong(value))); }
    public void ldc(int value) { append(new LDC(cp.addInteger(value))); }
    public void ldc(float value) { append(new LDC(cp.addFloat(value))); }
    public void laload() { append(new LALOAD()); }
    public void daload() { append(new DALOAD()); }
    public void faload() { append(new FALOAD()); }
    public void saload() { append(new SALOAD()); }
    public void caload() { append(new CALOAD()); }
    public void iaload() { append(new IALOAD()); }
    public void baload() { append(new BALOAD()); }
    public void lastore() { append(new LASTORE()); }
    public void dastore() { append(new DASTORE()); }
    public void fastore() { append(new FASTORE()); }
    public void sastore() { append(new SASTORE()); }
    public void castore() { append(new CASTORE()); }
    public void iastore() { append(new IASTORE()); }
    public void bastore() { append(new BASTORE()); }
    public void iconst(int value) { append(new ICONST(value)); }
    public void bipush(byte value) { append(new BIPUSH(value)); }
    public void sipush(short value) { append(new SIPUSH(value)); }
    public void lconst(long value) { append(new LCONST(value)); }
    public void fconst(float value) { append(new FCONST(value)); }
    public void dconst(double value) { append(new DCONST(value)); }
    public void lload(int index) { append(new LLOAD(index)); }
    public void dload(int index) { append(new DLOAD(index)); }
    public void fload(int index) { append(new FLOAD(index)); }
    public void iload(int index) { append(new ILOAD(index)); }
    public void aload(int index) { append(new ALOAD(index)); }
    public void lstore(int index) { append(new LSTORE(index)); }
    public void dstore(int index) { append(new DSTORE(index)); }
    public void fstore(int index) { append(new FSTORE(index)); }
    public void istore(int index) { append(new ISTORE(index)); }
    public void astore(int index) { append(new ASTORE(index)); }
    public void returnVoid() { append(new RETURN()); }
    public void lreturn() { append(new LRETURN()); }
    public void dreturn() { append(new DRETURN()); }
    public void freturn() { append(new FRETURN()); }
    public void ireturn() { append(new IRETURN()); }
    public void areturn() { append(new ARETURN()); }
    public void iinc(int index, int amount) { append(new IINC(index, amount)); }
                                           
    private void append(Instruction intruction) {
        il.append(intruction);
    }

    private void append(Object label, Instruction instruction) {

        if (label != null) {
            if (null != labels.put(label, il.append(instruction))) {
                throw new IllegalStateException("duplicated label " + label);
            }
        } else {
            il.append(instruction);
        }
    }

    private void append(BranchInstruction instruction, Object label) {

        List list = (List)branches.get(label);
        if (list == null) {
            branches.put(label, list = new LinkedList());
        }

        list.add(instruction);
        il.append(instruction);
    }

    private Type[] getTypes(Class[] classes) {
        Type[] types = new Type[classes.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = getType(classes[i]);
        }
        return types;
    }

    private static int javaModifiersToBcel(int modifiers) {
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
      
    private void setTargets() {
        if (labels.size() > 0 && branches.size() > 0) {
            for (Iterator labelIterator = labels.entrySet().iterator(); labelIterator.hasNext();) {
                Map.Entry label = (Map.Entry)labelIterator.next();          
                List branchInstructions = (List)branches.get(label.getKey());
                if (branchInstructions != null) {
                    for (Iterator instructions = branchInstructions.iterator(); instructions.hasNext();) {
                        BranchInstruction instruction = (BranchInstruction)instructions.next();
                        instruction.setTarget((InstructionHandle)label.getValue());
                    }
                }
            }     
        }
        labels.clear();
        branches.clear();
    }
  
    // this is taken from BCEL CVS: Type.getType(Class)
    private  Type getType(Class clazz) {

        if (clazz == null) {
            throw new IllegalArgumentException("Class must not be null");
        }
        if (clazz.isArray()) {
            return Type.getType(clazz.getName());
        } else if (clazz.isPrimitive()) {
            if (clazz == Integer.TYPE) {
                return Type.INT;
            } else if (clazz == Void.TYPE) {
                return Type.VOID;
            } else if (clazz == Double.TYPE) {
                return Type.DOUBLE;
            } else if (clazz == Float.TYPE) {
                return Type.FLOAT;
            } else if (clazz == Boolean.TYPE) {
                return Type.BOOLEAN;
            } else if (clazz == Byte.TYPE) {
                return Type.BYTE;
            } else if (clazz == Short.TYPE) {
                return Type.SHORT;
            } else if (clazz == Byte.TYPE) {
                return Type.BYTE;
            } else if (clazz == Long.TYPE) {
                return Type.LONG;
            } else if (clazz == Character.TYPE) {
                return Type.CHAR;
            } else {
                throw new IllegalStateException("Ooops, what primitive type is " + clazz);
            }
        } else { // "Real" class
            return new ObjectType(clazz.getName());
        }
    }
}
