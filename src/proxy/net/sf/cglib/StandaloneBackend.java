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

import java.lang.reflect.*;
import java.io.*;

public class StandaloneBackend extends CodeGeneratorBackend {
    private ByteArrayOutputStream methodBytes;
    private DataOutputStream out;
    
    public StandaloneBackend(String className, Class superclass) {
        super(className, superclass);
    }

    public byte[] getBytes() {
        // TODO
        return null;
    }
    
    public void ifeq(Object label) { ifbranch(IFEQ, label); }
    public void ifne(Object label) { ifbranch(IFNE, label); }
    public void iflt(Object label) { ifbranch(IFLT, label); }
    public void ifge(Object label) { ifbranch(IFGE, label); }
    public void ifgt(Object label) { ifbranch(IFGT, label); }
    public void ifle(Object label) { ifbranch(IFLE, label); }
    public void ifnull(Object label) { ifbranch(IFNULL, label); }
    public void ifnonnull(Object label) { ifbranch(IFNONNULL, label); }
    public void if_icmplt(Object label) { ifbranch(IF_ICMPLT, label); }
    public void if_icmpne(Object label) { ifbranch(IF_ICMPNE, label); }
    public void if_icmpeq(Object label) { ifbranch(IF_ICMPEQ, label); }
    public void imul() { simple(IMUL); }
    public void iadd() { simple(IADD); }
    public void lushr() { simple(LUSHR); }
    public void lxor() { simple(LXOR); }
    public void ixor() { simple(IXOR); }
    public void l2i() { simple(L2I); }
    public void dcmpg() { simple(DCMPG); }
    public void fcmpg() { simple(FCMPG); }
    public void lcmp() { simple(LCMP); }
    public void aconst_null() { simple(ACONST_NULL); }
    public void arraylength() { simple(ARRAYLENGTH); }
    public void athrow() { simple(ATHROW); }
    public void pop() { simple(POP); }
    public void pop2() { simple(POP2); }
    public void dup() { simple(DUP); }
    public void dup2() { simple(DUP2); }
    public void dup_x1() { simple(DUP_X1); }
    public void dup_x2() { simple(DUP_X2); }
    public void swap() { simple(SWAP); }
    public void laload() { simple(LALOAD); }
    public void daload() { simple(DALOAD); }
    public void faload() { simple(FALOAD); }
    public void saload() { simple(SALOAD); }
    public void caload() { simple(CALOAD); }
    public void iaload() { simple(IALOAD); }
    public void baload() { simple(BALOAD); }
    public void aaload() { simple(AALOAD); }
    public void lastore() { simple(LASTORE); }
    public void dastore() { simple(DASTORE); }
    public void fastore() { simple(FASTORE); }
    public void sastore() { simple(SASTORE); }
    public void castore() { simple(CASTORE); }
    public void iastore() { simple(IASTORE); }
    public void bastore() { simple(BASTORE); }
    public void aastore() { simple(AASTORE); }
    public void iconst(int value) { simple(ICONST_0 + value); }
    public void bipush(byte value) { pushBS(BIPUSH, value); }
    public void sipush(short value) { pushBS(SIPUSH, value); }
    public void lconst(long value) { simple(LCONST_0 + (int)value); }
    public void fconst(float value) { simple(FCONST_0 + (int)value); }
    public void dconst(double value) { simple(DCONST_0 + (int)value); }
    public void lload(int index) { load(LLOAD, index); }
    public void dload(int index) { load(DLOAD, index); }
    public void fload(int index) { load(FLOAD, index); }
    public void iload(int index) { load(ILOAD, index); }
    public void aload(int index) { load(ALOAD, index); }
    public void lstore(int index) { store(LSTORE, index); }
    public void dstore(int index) { store(DSTORE, index); }
    public void fstore(int index) { store(FSTORE, index); }
    public void istore(int index) { store(ISTORE, index); }
    public void astore(int index) { store(ASTORE, index); }
    public void lreturn() { simple(LRETURN); }
    public void dreturn() { simple(DRETURN); }
    public void freturn() { simple(FRETURN); }
    public void ireturn() { simple(IRETURN); }
    public void areturn() { simple(ARETURN); }
    public void nop(Object label) { simple(label, NOP); }

    public void goTo(Object label) {
        // TODO
    }

    public void newarray(Class clazz) {
        // TODO
    }

    public void anewarray(Class clazz) {
        // TODO
    }

    public void new_instance(String className) {
        // TODO
    }

    public void checkcast(String className) {
        // TODO
    }

    public void instance_of(String className) {
        // TODO
    }

    public void invoke_interface(String className, String methodName, Class returnType, Class[] parameterTypes) {
        // TODO
    }

    public void invoke_virtual(String className, String methodName, Class returnType, Class[] parameterTypes) {
        // TODO
    }

    public void invoke_static(String className, String methodName, Class returnType, Class[] parameterTypes) {
        // TODO
    }

    public void invoke_special(String className, String methodName, Class returnType, Class[] parameterTypes) {
        // TODO
    }

    public void declare_field(int modifiers, Class type, String name) {
        // TODO
    }

    public void getfield(String className, String fieldName, Class type) {
        // TODO
    }

    public void putfield(String className, String fieldName, Class type) {
        // TODO
    }

    public void getstatic(String className, String fieldName, Class type) {
        // TODO
    }

    public void putstatic(String className, String fieldName, Class type) {
        // TODO
    }

    public void begin_static() {
        // TODO
    }

    public void begin_constructor(Class[] parameterTypes) {
        // TODO
    }

    public void declare_interface(Class iface) {
        // TODO
    }

    public void begin_method(int modifiers, Class returnType, String name,
        Class[] parameterTypes, Class[] exceptionTypes) {
        methodBytes = new ByteArrayOutputStream();
        out = new DataOutputStream(methodBytes);
        // TODO
    }

    public Object start_range() {
        // TODO
        return null;
    }

    public Object end_range() {
        // TODO
        return null;
    }

    public void handle_exception(Object start, Object end, Class exceptionType) {
        // TODO
    }

    public void end_method() {
        // TODO
    }

    public void ldc(String value) {
        // TODO
    }

    public void ldc(double value) {
        // TODO
    }

    public void ldc(long value) {
        // TODO
    }

    public void ldc(int value) {
        // TODO
    }

    public void ldc(float value) {
        // TODO
    }

    public void returnVoid() {
        // TODO
    }

    public void iinc(int index, int amount) {
        // TODO
    }

    private void simple(int opcode) {
        try {
            out.writeByte(opcode);
        } catch (IOException e) {
            // TODO
        }
    }

    private void simple(Object label, int opcode) {
        // TODO
    }

    private void pushBS(int opcode, int value) {
        // TODO
    }

    private void load(int opcode, int index) {
        // TODO
    }

    private void store(int opcode, int index) {
        // TODO
    }

    private void ifbranch(int opcode, Object label) {
        // TODO
    }

    // Java VM opcodes.
     private static final short NOP              = 0; 
     private static final short ACONST_NULL      = 1; 
//     private static final short ICONST_M1        = 2; 
     private static final short ICONST_0         = 3; 
     private static final short LCONST_0         = 9; 
     private static final short FCONST_0         = 11; 
     private static final short DCONST_0         = 14; 
     private static final short BIPUSH           = 16; // length 2
     private static final short SIPUSH           = 17; // length 3
//     private static final short LDC              = 18; // length 3
//     private static final short LDC_W            = 19; // length 3
//     private static final short LDC2_W           = 20; // length 3
     private static final short ILOAD            = 21; 
     private static final short LLOAD            = 22; 
     private static final short FLOAD            = 23; 
     private static final short DLOAD            = 24; 
     private static final short ALOAD            = 25; 
//     private static final short ILOAD_0          = 26; 
//     private static final short LLOAD_0          = 30; 
//     private static final short FLOAD_0          = 34; 
//     private static final short DLOAD_0          = 38; 
//     private static final short ALOAD_0          = 42; 
     private static final short IALOAD           = 46; 
     private static final short LALOAD           = 47; 
     private static final short FALOAD           = 48; 
     private static final short DALOAD           = 49; 
     private static final short AALOAD           = 50; 
     private static final short BALOAD           = 51; 
     private static final short CALOAD           = 52; 
     private static final short SALOAD           = 53; 
     private static final short ISTORE           = 54; 
     private static final short LSTORE           = 55; 
     private static final short FSTORE           = 56; 
     private static final short DSTORE           = 57; 
     private static final short ASTORE           = 58; 
//     private static final short ISTORE_0         = 59; 
//     private static final short LSTORE_0         = 63; 
//     private static final short FSTORE_0         = 67; 
//     private static final short DSTORE_0         = 71; 
//     private static final short ASTORE_0         = 75; 
     private static final short IASTORE          = 79; 
     private static final short LASTORE          = 80; 
     private static final short FASTORE          = 81; 
     private static final short DASTORE          = 82; 
     private static final short AASTORE          = 83; 
     private static final short BASTORE          = 84; 
     private static final short CASTORE          = 85; 
     private static final short SASTORE          = 86; 
     private static final short POP              = 87; 
     private static final short POP2             = 88; // length 2
     private static final short DUP              = 89; 
     private static final short DUP_X1           = 90; 
     private static final short DUP_X2           = 91; 
     private static final short DUP2             = 92; // length 2
//     private static final short DUP2_X1          = 93; // length 2
//     private static final short DUP2_X2          = 94; // length 2
     private static final short SWAP             = 95; 
     private static final short IADD             = 96; 
//     private static final short LADD             = 97; 
//     private static final short FADD             = 98; 
//     private static final short DADD             = 99; 
//     private static final short ISUB             = 100; 
//     private static final short LSUB             = 101; 
//     private static final short FSUB             = 102; 
//     private static final short DSUB             = 103; 
     private static final short IMUL             = 104; 
//     private static final short LMUL             = 105; 
//     private static final short FMUL             = 106; 
//     private static final short DMUL             = 107; 
//     private static final short IDIV             = 108; 
//     private static final short LDIV             = 109; 
//     private static final short FDIV             = 110; 
//     private static final short DDIV             = 111; 
//     private static final short IREM             = 112; 
//     private static final short LREM             = 113; 
//     private static final short FREM             = 114; 
//     private static final short DREM             = 115; 
//     private static final short INEG             = 116; 
//     private static final short LNEG             = 117; 
//     private static final short FNEG             = 118; 
//     private static final short DNEG             = 119; 
//     private static final short ISHL             = 120; 
//     private static final short LSHL             = 121; 
//     private static final short ISHR             = 122; 
//     private static final short LSHR             = 123; 
//     private static final short IUSHR            = 124; 
     private static final short LUSHR            = 125; 
//     private static final short IAND             = 126; 
//     private static final short LAND             = 127; 
//     private static final short IOR              = 128; 
//     private static final short LOR              = 129; 
     private static final short IXOR             = 130; 
     private static final short LXOR             = 131; 
//     private static final short IINC             = 132; 
//     private static final short I2L              = 133; 
//     private static final short I2F              = 134; 
//     private static final short I2D              = 135; 
     private static final short L2I              = 136; 
//     private static final short L2F              = 137; 
//     private static final short L2D              = 138; 
//     private static final short F2I              = 139; 
//     private static final short F2L              = 140; 
//     private static final short F2D              = 141; 
//     private static final short D2I              = 142; 
//     private static final short D2L              = 143; 
//     private static final short D2F              = 144; 
//     private static final short I2B              = 145; 
//     private static final short I2C              = 146; 
//     private static final short I2S              = 147; 
     private static final short LCMP             = 148; 
//     private static final short FCMPL            = 149; 
     private static final short FCMPG            = 150; 
//     private static final short DCMPL            = 151; 
     private static final short DCMPG            = 152; 
     private static final short IFEQ             = 153; // length 3
     private static final short IFNE             = 154; // length 3
     private static final short IFLT             = 155; // length 3
     private static final short IFGE             = 156; // length 3
     private static final short IFGT             = 157; // length 3
     private static final short IFLE             = 158; // length 3
     private static final short IF_ICMPEQ        = 159; // length 3
     private static final short IF_ICMPNE        = 160; // length 3
     private static final short IF_ICMPLT        = 161; // length 3
//     private static final short IF_ICMPGE        = 162; // length 3
//     private static final short IF_ICMPGT        = 163; // length 3
//     private static final short IF_ICMPLE        = 164; // length 3
//     private static final short IF_ACMPEQ        = 165; // length 3
//     private static final short IF_ACMPNE        = 166; // length 3
//     private static final short GOTO             = 167; // length 3
//     private static final short JSR              = 168; // length 3
//     private static final short RET              = 169; // length 2
//     private static final short TABLESWITCH      = 170; // length ?
//     private static final short LOOKUPSWITCH     = 171; // length 3
     private static final short IRETURN          = 172; 
     private static final short LRETURN          = 173; 
     private static final short FRETURN          = 174; 
     private static final short DRETURN          = 175; 
     private static final short ARETURN          = 176; 
//      private static final short RETURN           = 177; 
//     private static final short GETSTATIC        = 178; // length 3
//     private static final short PUTSTATIC        = 179; // length 3
//     private static final short GETFIELD         = 180; // length 3
//     private static final short PUTFIELD         = 181; // length 3
//     private static final short INVOKEVIRTUAL    = 182; // length 3
//     private static final short INVOKESPECIAL    = 183; // length 3
//     private static final short INVOKESTATIC     = 184; // length 3
//     private static final short INVOKEINTERFACE  = 185; // length 3
//     private static final short NEW              = 187; // length 3
//     private static final short NEWARRAY         = 188; // length 2
//     private static final short ANEWARRAY        = 189; // length 3
     private static final short ARRAYLENGTH      = 190; 
     private static final short ATHROW           = 191; 
//     private static final short CHECKCAST        = 192; // length 3
//     private static final short INSTANCEOF       = 193; // length 3
//     private static final short MONITORENTER     = 194; 
//     private static final short MONITOREXIT      = 195; 
//     private static final short WIDE             = 196; 
//     private static final short MULTIANEWARRAY   = 197; // length 3
     private static final short IFNULL           = 198; // length 3
     private static final short IFNONNULL        = 199; // length 3
//     private static final short GOTO_W           = 200; // length 3
//     private static final short JSR_W            = 201; // length 3
}
