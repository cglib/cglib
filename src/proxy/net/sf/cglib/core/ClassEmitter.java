/*
 * Copyright 2003 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.cglib.core;

import java.io.*;
import java.util.*;
import org.objectweb.asm.*;

/**
 * @author Juozas Baliuka, Chris Nokleberg
 */
public class ClassEmitter extends ClassAdapter {
    private ClassInfo classInfo;
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

    public ClassInfo getClassInfo() {
        return classInfo;
    }

    public void begin_class(final int access, String className, final Type superType, final Type[] interfaces, String sourceFile) {
        final Type classType = Type.getType("L" + className.replace('.', '/') + ";");
        classInfo = new ClassInfo() {
            public Type getType() {
                return classType;
            }
            public Type getSuperType() {
                return (superType != null) ? superType : Constants.TYPE_OBJECT;
            }
            public Type[] getInterfaces() {
                return interfaces;
            }
            public int getModifiers() {
                return access;
            }
        };
        cv.visit(access,
                 classInfo.getType().getInternalName(),
                 classInfo.getSuperType().getInternalName(),
                 TypeUtils.toInternalNames(interfaces),
                 sourceFile);
        init();
    }

    public CodeEmitter getStaticHook() {
         if (TypeUtils.isInterface(getAccess())) {
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
        return classInfo.getModifiers();
    }

    public Type getClassType() {
        return classInfo.getType();
    }

    public Type getSuperType() {
        return classInfo.getSuperType();
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
        if (classInfo == null)
            throw new IllegalStateException("classInfo is null! " + this);
        CodeVisitor v = cv.visitMethod(access,
                                       sig.getName(),
                                       sig.getDescriptor(),
                                       TypeUtils.toInternalNames(exceptions),
                                       attrs);
        if (sig.equals(Constants.SIG_STATIC) && !TypeUtils.isInterface(getAccess())) {
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
            throw new IllegalArgumentException("Field " + name + " is not declared in " + getClassType().getClassName());
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
