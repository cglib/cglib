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

import org.objectweb.asm.*;
import java.util.*;

class EmitterMethod extends CodeAdapter {
    private int access;
    private Signature sig;
    private Type returnType;
    private Type[] argumentTypes;
    private int localOffset;
    private int firstLocal;
    private int nextLocal;
    private Map remap = new HashMap();
        
    public EmitterMethod(ClassVisitor classv, int access, Signature sig, Type[] exceptions) {
        super(null);
        this.access = access;
        this.sig = sig;
        returnType = sig.getReturnType();
        argumentTypes = sig.getArgumentTypes();
        localOffset = TypeUtils.isStatic(access) ? 0 : 1;
        firstLocal = nextLocal = localOffset + TypeUtils.getStackSize(argumentTypes);
            
        cv = classv.visitMethod(access,
                                sig.getName(),
                                sig.getDescriptor(),
                                TypeUtils.toInternalNames(exceptions));
    }

    public int getAccess() {
        return access;
    }

    public Signature getSignature() {
        return sig;
    }

    public Type getReturnType() {
        return returnType;
    }

    public Type[] getArgumentTypes() {
        return argumentTypes;
    }

    public String getName() {
        return sig.getName();
    }

    public CodeVisitor getRaw() {
        return cv;
    }

    public int getLocalOffset() {
        return localOffset;
    }

    public Local make_local(Type type) {
        Local local = new Local(nextLocal, type);
        nextLocal += type.getSize();
        return local;
    }

    public void close() {
        if (!TypeUtils.isAbstract(access)) {
            cv.visitMaxs(0, 0);
        }
    }

    private int remapLocal(int index, Type type) {
        if (index < firstLocal) {
            // System.err.println("remap keeping " + index + " (type=" + type.getDescriptor() + ")");
            return index;
        }
        Integer key = new Integer((type.getSize() == 2) ? ~index : index);
        Local local = (Local)remap.get(key);
        if (local == null) {
            remap.put(key, local = make_local(type));
        }

        // System.err.println("remapping " + index + " --> " + local.getIndex()  + " (type=" + type.getDescriptor() + ")");
        if (local.getType().getSize() != type.getSize()) {
            throw new IllegalStateException("Remapped local (" + index + "->" + local.getIndex() + ") in method " + sig + " requires different opcode sizes: old=" + local.getType().getDescriptor() + " new=" + type.getDescriptor());
        }
        return local.getIndex();
    }

    public void visitMaxs(int maxStack, int maxLocals) {
        // ignore
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
            type = Constants.TYPE_OBJECT;
        }
        cv.visitVarInsn(opcode, remapLocal(var, type));
    }

    public void visitIincInsn(int var, int increment) {
        cv.visitIincInsn(remapLocal(var, Type.INT_TYPE), increment);
    }

    public void visitLocalVariable(String name, String desc, Label start, Label end, int index) {
        cv.visitLocalVariable(name, desc, start, end, remapLocal(index, null));
    }
}
