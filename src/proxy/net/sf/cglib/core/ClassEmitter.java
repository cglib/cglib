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
public class ClassEmitter extends ClassAdapter {
    private int access;
    private Type classType;
    private Type superType;
    private Map fieldInfo;

    private static int hookCounter;
    private CodeVisitor rawStaticInit;
    private CodeEmitter staticInit;
    private CodeEmitter staticHook;
    private Signature staticHookSig;

    public ClassEmitter(ClassVisitor cv) {
        super(null);
        setTarget(cv);
    }

    public ClassEmitter() {
        super(null);
    }

    public void setTarget(ClassVisitor cv) {
        this.cv = cv;
        fieldInfo = new HashMap();

        // just to be safe
        staticInit = staticHook = null;
        staticHookSig = null;
    }

    synchronized private static int getNextHook() {
        return ++hookCounter;
    }

    public void begin_class(int access, String className, Type superType, Type[] interfaces, String sourceFile) {
        this.access = access;
        this.classType = Type.getType("L" + className.replace('.', '/') + ";");
        this.superType = (superType != null) ? superType : Constants.TYPE_OBJECT;
        cv.visit(access,
                 this.classType.getInternalName(),
                 this.superType.getInternalName(),
                 TypeUtils.toInternalNames(interfaces),
                 sourceFile);
        init();
    }

    public CodeEmitter getStaticHook() {
         if (TypeUtils.isInterface(access)) {
             throw new IllegalStateException("static hook is invalid for this class");
         }
         if (staticHook == null) {
             staticHookSig = new Signature("CGLIB$STATICHOOK" + getNextHook(), "()V");
             staticHook = begin_method(Constants.ACC_STATIC,
                                       staticHookSig,
                                       null,
                                       null);
             if (staticInit != null) {
                 staticInit.invoke_static_this(staticHookSig);
             }
         }
         return staticHook;
    }

    protected void init() {
    }

    public int getAccess() {
        return access;
    }

    public Type getClassType() {
        return classType;
    }

    public Type getSuperType() {
        return superType;
    }

    public void end_class() {
        if (staticHook != null && staticInit == null) {
            // force creation of static init
            begin_static();
        }
        if (staticInit != null) {
            staticHook.return_value();
            staticHook.end_method();
            rawStaticInit.visitInsn(Constants.RETURN);
            rawStaticInit.visitMaxs(0, 0);
            staticInit = staticHook = null;
            staticHookSig = null;
        }
        cv.visitEnd();
    }

    public CodeEmitter begin_method(int access, Signature sig, Type[] exceptions, Attribute attrs) {
        if (classType == null)
            throw new IllegalStateException("classtype is null! " + this);
        CodeVisitor v = cv.visitMethod(access,
                                       sig.getName(),
                                       sig.getDescriptor(),
                                       TypeUtils.toInternalNames(exceptions),
                                       attrs);
        if (sig.equals(Constants.SIG_STATIC) && !TypeUtils.isInterface(this.access)) {
            rawStaticInit = v;
            CodeVisitor wrapped = new CodeAdapter(v) {
                public void visitMaxs(int maxStack, int maxLocals) {
                    // ignore
                }
                public void visitInsn(int insn) {
                    if (insn != Constants.RETURN) {
                        super.visitInsn(insn);
                    }
                }
            };
            staticInit = new CodeEmitter(this, wrapped, access, sig, exceptions);
            if (staticHook == null) {
                // force static hook creation
                getStaticHook();
            } else {
                staticInit.invoke_static_this(staticHookSig);
            }
            return staticInit;
        } else if (sig.equals(staticHookSig)) {
            return new CodeEmitter(this, v, access, sig, exceptions) {
                public boolean isStaticHook() {
                    return true;
                }
            };
        } else {
            return new CodeEmitter(this, v, access, sig, exceptions);
        }
    }

    public CodeEmitter begin_static() {
        return begin_method(Constants.ACC_STATIC, Constants.SIG_STATIC, null, null);
    }

    public void declare_field(int access, String name, Type type, Object value, Attribute attrs) {
        FieldInfo existing = (FieldInfo)fieldInfo.get(name);
        FieldInfo info = new FieldInfo(access, name, type, value);
        if (existing != null) {
            if (!info.equals(existing)) {
                throw new IllegalArgumentException("Field \"" + name + "\" has been declared differently");
            }
        } else {
            fieldInfo.put(name, info);
            cv.visitField(access, name, type.getDescriptor(), value, attrs);
        }
    }

    public void define_attribute(Attribute attrs) {
        cv.visitAttribute(attrs);
    }

    // TODO: make public?
    boolean isFieldDeclared(String name) {
        return fieldInfo.get(name) != null;
    }

    FieldInfo getFieldInfo(String name) {
        FieldInfo field = (FieldInfo)fieldInfo.get(name);
        if (field == null) {
            throw new IllegalArgumentException("Field " + name + " is not declared in " + classType.getClassName());
        }
        return field;
    }
    
    static class FieldInfo {
        int access;
        String name;
        Type type;
        Object value;
        
        public FieldInfo(int access, String name, Type type, Object value) {
            this.access = access;
            this.name = name;
            this.type = type;
            this.value = value;
        }

        public boolean equals(Object o) {
            if (o == null)
                return false;
            if (!(o instanceof FieldInfo))
                return false;
            FieldInfo other = (FieldInfo)o;
            if (access != other.access ||
                !name.equals(other.name) ||
                !type.equals(other.type)) {
                return false;
            }
            if ((value == null) ^ (other.value == null))
                return false;
            if (value != null && !value.equals(other.value))
                return false;
            return true;
        }

        public int hashCode() {
            return access ^ name.hashCode() ^ type.hashCode() ^ ((value == null) ? 0 : value.hashCode());
        }
    }

    public void visit(int access, String name, String superName, String[] interfaces, String sourceFile) {
        begin_class(access,
                    name.replace('/', '.'),
                    TypeUtils.fromInternalName(superName),
                    TypeUtils.fromInternalNames(interfaces),
                    sourceFile);
    }
    
    public void visitEnd() {
        end_class();
    }
    
    public void visitField(int access, String name, String desc, Object value, Attribute attrs) {
        declare_field(access, name, Type.getType(desc), value, attrs);
    }

    // TODO: handle visitInnerClass?
    
    public CodeVisitor visitMethod(int access, String name, String desc, String[] exceptions, Attribute attrs) {
        return begin_method(access,
                            new Signature(name, desc),
                            TypeUtils.fromInternalNames(exceptions),
                            attrs);
    }

    public void visitAttribute(Attribute attrs) {
        define_attribute(attrs);
    }
}
