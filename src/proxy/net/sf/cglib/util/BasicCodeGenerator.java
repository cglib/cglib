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
package net.sf.cglib.util;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Abstract base class for code generators, basic operations only.
 * @author Juozas Baliuka, Chris Nokleberg
 */
abstract public class BasicCodeGenerator {
    private static String debugLocation;
    private static RuntimePermission DEFINE_CGLIB_CLASS_IN_JAVA_PACKAGE_PERMISSION =
      new RuntimePermission("defineCGLIBClassInJavaPackage");

    private boolean inited;
    private String className;
    private String namePrefix;
    private String nameSuffix = "";
    private Class superclass;
    private ClassLoader classLoader;
    private int modifiers = Modifier.PUBLIC;
    private CodeGeneratorBackend backend;
    private boolean debug;
    private List interfaces = new LinkedList();

    private String methodName;
    private Class returnType;
    private Class[] parameterTypes;
    private boolean inMethod;
    private boolean isStatic;
    private int nextLocal;

    private Map fieldInfo = new HashMap();
    private Block curBlock;

    public static void setDebugLocation(String debugLocation) {
        BasicCodeGenerator.debugLocation = debugLocation;
    }

    protected BasicCodeGenerator() {
    }

    public String getNamePrefix() {
        return namePrefix;
    }
    
    public void setNamePrefix(String value) {
        checkInit(false);
        namePrefix = value;
    }
    
    public String getNameSuffix() {
        return nameSuffix;
    }
    
    public void setNameSuffix(String value) {
        checkInit(false);
        nameSuffix = value;
    }

    public String getClassName() {
        return className;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }
    
    public void setClassLoader(ClassLoader value) {
        classLoader = value;
    }

    public Class getSuperclass() {
        return superclass;
    }
    
    public void setSuperclass(Class value) {
        checkInit(false);
        superclass = value;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean getDebug() {
        return debug;
    }
    
    public void setClassModifiers(int modifiers) {
        checkInit(false);
        this.modifiers = modifiers;
    }
    
    public int getClassModifiers() {
        return modifiers;
    }

    protected void init() {
        if (superclass == null) {
            superclass = Object.class;
        }
        if (namePrefix == null) {
            namePrefix = superclass.getName();
        }
        className = namePrefix + nameSuffix;
        backend = new ASMBackend();
        backend.init(this);
        inited = true;
    }

    private void checkInit(boolean flag) {
        if (inited ^ flag) {
            throw new IllegalStateException("cannot change value after initialization");
        }
    }

    protected void ensureLoadable(String className) throws ClassNotFoundException {
        if (className != null) {
            classLoader.loadClass(className);
        }
    }

    protected void ensureLoadable(Class type) throws ClassNotFoundException {
        if (type != null) {
            ensureLoadable(type.getName());
        }
    }

    protected void ensureLoadable(Class[] types) throws ClassNotFoundException {
        if (types != null) {
            for (int i = 0; i < types.length; i++) {
                ensureLoadable(types[i]);
            }
        }
    }

    protected CodeGeneratorBackend getBackend() {
        return backend;
    }
        
    /**
     * method used to generate code
     */
    abstract protected void generate() throws Exception;
    protected void postGenerate() throws Exception { }
    protected void postDefine(Class type) throws Exception { }

    public final Class define() {
        try {
            init();
            generate();
            postGenerate();
            byte[] bytes = backend.getBytes();                
            
            if (debugLocation != null) {
                OutputStream out = new FileOutputStream(new File(new File(debugLocation), className + ".class"));
                out.write(bytes);
                out.close();
            }
            Class type = defineClass(className, bytes, classLoader);
            postDefine(type);
            return type;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        } catch (Error e) {
            throw e;
        } catch (Throwable t) {
            // almost impossible
            throw new CodeGenerationException(t);
        }
    }

    private static Class defineClass(String className, byte b[],  ClassLoader loader)
    throws Exception {

        Method m = MethodConstants.DEFINE_CLASS;
        // protected method invocaton
        boolean flag = m.isAccessible();
        m.setAccessible(true);
        
        SecurityManager sm = System.getSecurityManager();
        if ( className != null && className.startsWith("java.") && sm != null  ) {
            sm.checkPermission( DEFINE_CGLIB_CLASS_IN_JAVA_PACKAGE_PERMISSION );
        }
        
        //way depricated in jdk to define classes,
        // doe's not throws SecurityException if class name starts with "java."
        Object[] args = new Object[]{ b, new Integer(0), new Integer(b.length) };
        Class result = (Class)m.invoke(loader, args);
        m.setAccessible(flag);
        
        return result;
    }

    public void addInterfaces(Class[] interfaces) {
        for (int i = 0; i < interfaces.length; i++) {
            addInterface(interfaces[i]);
        }
    }

    public void addInterface(Class type) {
        checkInit(false);
        if (!type.isInterface()) {
            throw new IllegalArgumentException(type + " is not an interface");
        }
        interfaces.add(type);
    }

    public Class[] getInterfaces() {
        return (Class[])interfaces.toArray(new Class[interfaces.size()]);
    }

    public Class[] getParameterTypes() {
        return parameterTypes;
    }

    public Class getReturnType() {
        return returnType;
    }
    
    public void begin_method(int modifiers, Class returnType, String methodName,
    Class[] parameterTypes, Class[] exceptionTypes) {
        checkInMethod();
        this.methodName = methodName;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        isStatic = Modifier.isStatic(modifiers);
        backend.begin_method(modifiers, returnType, methodName, parameterTypes, exceptionTypes);
        setNextLocal();
    }
    
    public static int getDefaultModifiers(Method method) {
        return Modifier.FINAL
            | (method.getModifiers()
               & ~Modifier.ABSTRACT
               & ~Modifier.NATIVE
               & ~Modifier.SYNCHRONIZED);
    }
    
    public void begin_method(Method method) {
        begin_method(method, getDefaultModifiers(method));
    }
    
    public void begin_method(Method method, int modifiers) {
        begin_method(modifiers, method.getReturnType(), method.getName(),
        method.getParameterTypes(), method.getExceptionTypes());
    }
    
    public void begin_constructor(Constructor constructor) {
        begin_constructor(constructor.getParameterTypes());
    }
    
    public void begin_constructor() {
        begin_constructor(Constants.TYPES_EMPTY);
    }
    
    public void begin_constructor(Class[] parameterTypes) {
        checkInMethod();
        this.returnType = Void.TYPE;
        this.parameterTypes = parameterTypes;
        backend.begin_constructor(parameterTypes);
        setNextLocal();
    }
    
    public void begin_static() {
        checkInMethod();
        this.returnType = Void.TYPE;
        this.parameterTypes = Constants.TYPES_EMPTY;
        backend.begin_static();
        setNextLocal();
    }
    
    private void checkInMethod() {
        if (inMethod) {
            throw new IllegalStateException("cannot nest methods");
        }
        inMethod = true;
    }
    
    private void setNextLocal() {
        nextLocal = getLocalOffset() + getStackSize(parameterTypes);
    }
    
    public void end_method() {
        backend.end_method();
        parameterTypes = null;
        returnType = null;
        methodName = null;
        if (curBlock != null) {
            throw new IllegalStateException("unclosed exception block");
        }
        inMethod = false;
    }

    public Block begin_block() {
        Block newBlock = new Block(curBlock, backend.mark());
        curBlock = newBlock;
        return curBlock;
    }

    public void end_block() {
        if (curBlock == null) {
            throw new IllegalStateException("mismatched block boundaries");
        }
        curBlock.setEnd(backend.mark());
        curBlock = curBlock.getParent();
    }
    
    public void catch_exception(Block block, Class exceptionType) {
        // TODO
        // throw new IllegalArgumentException("block belongs to a different method");

        if (block.getEnd() == null) {
            throw new IllegalStateException("end of block is unset");
        }
        backend.catch_exception(block, exceptionType);
    }
    
    public void ifeq(Label label) { backend.emit(Opcodes.IFEQ, label); }
    public void ifne(Label label) { backend.emit(Opcodes.IFNE, label); }
    public void iflt(Label label) { backend.emit(Opcodes.IFLT, label); }
    public void ifge(Label label) { backend.emit(Opcodes.IFGE, label); }
    public void ifgt(Label label) { backend.emit(Opcodes.IFGT, label); }
    public void ifle(Label label) { backend.emit(Opcodes.IFLE, label); }
    public void goTo(Label label) { backend.emit(Opcodes.GOTO, label); }
    public void ifnull(Label label) { backend.emit(Opcodes.IFNULL, label); }
    public void ifnonnull(Label label) { backend.emit(Opcodes.IFNONNULL, label); }
    public void if_icmplt(Label label) { backend.emit(Opcodes.IF_ICMPLT, label); }
    public void if_icmpgt(Label label) { backend.emit(Opcodes.IF_ICMPGT, label); }
    public void if_icmpne(Label label) { backend.emit(Opcodes.IF_ICMPNE, label); }
    public void if_icmpeq(Label label) { backend.emit(Opcodes.IF_ICMPEQ, label); }
    public void if_acmpeq(Label label) { backend.emit(Opcodes.IF_ACMPEQ, label); }
    public void if_acmpne(Label label) { backend.emit(Opcodes.IF_ACMPNE, label); }

    public void pop() { backend.emit(Opcodes.POP); }
    public void pop2() { backend.emit(Opcodes.POP2); }
    public void dup() { backend.emit(Opcodes.DUP); }
    public void dup2() { backend.emit(Opcodes.DUP2); }
    public void dup_x1() { backend.emit(Opcodes.DUP_X1); }
    public void dup_x2() { backend.emit(Opcodes.DUP_X2); }
    public void swap() { backend.emit(Opcodes.SWAP); }
    public void aconst_null() { backend.emit(Opcodes.ACONST_NULL); }

    public void monitorenter() { backend.emit(Opcodes.MONITORENTER); }
    public void monitorexit() { backend.emit(Opcodes.MONITOREXIT); }

    public void if_cmpeq(Class type, Label label) {
        cmpHelper(type, label, Opcodes.IF_ICMPEQ, Opcodes.IFEQ);
    }
    public void if_cmpne(Class type, Label label) {
        cmpHelper(type, label, Opcodes.IF_ICMPNE, Opcodes.IFNE);
    }
    public void if_cmplt(Class type, Label label) {
        cmpHelper(type, label, Opcodes.IF_ICMPLT, Opcodes.IFLT);
    }
    public void if_cmpgt(Class type, Label label) {
        cmpHelper(type, label, Opcodes.IF_ICMPGT, Opcodes.IFGT);
    }
    
    private void cmpHelper(Class type, Label label, int intOp, int numOp) {
        if (type == Long.TYPE) {
            backend.emit(Opcodes.LCMP);
        } else if (type == Double.TYPE) {
            backend.emit(Opcodes.DCMPG);
        } else if (type == Float.TYPE) {
            backend.emit(Opcodes.FCMPG);
        } else {
            backend.emit(intOp, label);
            return;
        }
        backend.emit(numOp, label);
    }

    public void add(Class type) {
        if (type == Long.TYPE) {
            backend.emit(Opcodes.LADD);
        } else if (type == Double.TYPE) {
            backend.emit(Opcodes.DADD);
        } else if (type == Float.TYPE) {
            backend.emit(Opcodes.FADD);
        } else if (type == Integer.TYPE) {
            backend.emit(Opcodes.IADD);
        }
    }

    public void mul(Class type) {
        if (type == Long.TYPE) {
            backend.emit(Opcodes.LMUL);
        } else if (type == Double.TYPE) {
            backend.emit(Opcodes.DMUL);
        } else if (type == Float.TYPE) {
            backend.emit(Opcodes.FMUL);
        } else {
            backend.emit(Opcodes.IMUL);
        }
    }

    public void xor(Class type) {
        if (type == Long.TYPE) {
            backend.emit(Opcodes.LXOR);
        } else {
            backend.emit(Opcodes.IXOR);
        }
    }

    public void ushr(Class type) {
        if (type == Long.TYPE) {
            backend.emit(Opcodes.LUSHR);
        } else {
            backend.emit(Opcodes.IUSHR);
        }
    }

    public void sub(Class type) {
        if (type == Long.TYPE) {
            backend.emit(Opcodes.LSUB);
        } else if (type == Double.TYPE) {
            backend.emit(Opcodes.DSUB);
        } else if (type == Float.TYPE) {
            backend.emit(Opcodes.FSUB);
        } else {
            backend.emit(Opcodes.ISUB);
        }
    }

    public void div(Class type) {
        if (type == Long.TYPE) {
            backend.emit(Opcodes.LDIV);
        } else if (type == Double.TYPE) {
            backend.emit(Opcodes.DDIV);
        } else if (type == Float.TYPE) {
            backend.emit(Opcodes.FDIV);
        } else {
            backend.emit(Opcodes.IDIV);
        }
    }

    public void neg(Class type) {
        if (type == Long.TYPE) {
            backend.emit(Opcodes.LNEG);
        } else if (type == Double.TYPE) {
            backend.emit(Opcodes.DNEG);
        } else if (type == Float.TYPE) {
            backend.emit(Opcodes.FNEG);
        } else {
            backend.emit(Opcodes.INEG);
        }
    }

    public void rem(Class type) {
        if (type == Long.TYPE) {
            backend.emit(Opcodes.LREM);
        } else if (type == Double.TYPE) {
            backend.emit(Opcodes.DREM);
        } else if (type == Float.TYPE) {
            backend.emit(Opcodes.FREM);
        } else {
            backend.emit(Opcodes.IREM);
        }
    }

    public void and(Class type) {
        if (type == Long.TYPE) {
            backend.emit(Opcodes.LAND);
        } else {
            backend.emit(Opcodes.IAND);
        }
    }
    
    public void or(Class type) {
        if (type == Long.TYPE) {
            backend.emit(Opcodes.LOR);
        } else {
            backend.emit(Opcodes.IOR);
        }
    }

    /**
     * Casts from one primitive numeric type to another
     */
    public void cast_numeric(Class from, Class to) {
        if (from != to) {
            if (from == Double.TYPE) {
                if (to == Float.TYPE) {
                    backend.emit(Opcodes.D2F);
                } else if (to == Long.TYPE) {
                    backend.emit(Opcodes.D2L);
                } else {
                    backend.emit(Opcodes.D2I);
                    cast_numeric(Integer.TYPE, to);
                }
            } else if (from == Float.TYPE) {
                if (to == Double.TYPE) {
                    backend.emit(Opcodes.F2D);
                } else if (to == Long.TYPE) {
                    backend.emit(Opcodes.F2L);
                } else {
                    backend.emit(Opcodes.F2I);
                    cast_numeric(Integer.TYPE, to);
                }
            } else if (from == Long.TYPE) {
                if (to == Double.TYPE) {
                    backend.emit(Opcodes.L2D);
                } else if (to == Float.TYPE) {
                    backend.emit(Opcodes.L2F);
                } else {
                    backend.emit(Opcodes.L2I);
                    cast_numeric(Integer.TYPE, to);
                }
            } else {
                if (to == Byte.TYPE) {
                    backend.emit(Opcodes.I2B);
                } else if (to == Character.TYPE) {
                    backend.emit(Opcodes.I2C);
                } else if (to == Double.TYPE) {
                    backend.emit(Opcodes.I2D);
                } else if (to == Float.TYPE) {
                    backend.emit(Opcodes.I2F);
                } else if (to == Long.TYPE) {
                    backend.emit(Opcodes.I2L);
                } else if (to == Short.TYPE) {
                    backend.emit(Opcodes.I2S);
                }
            }
        }
    }

    public void push(int i) {
        if (i < -1) {
            backend.emit_ldc(new Integer(i));
        } else if (i <= 5) {
            backend.emit(Opcodes.iconst(i));
        } else if (i <= Byte.MAX_VALUE) {
            backend.emit_int(Opcodes.BIPUSH, i);
        } else if (i <= Short.MAX_VALUE) {
            backend.emit_int(Opcodes.SIPUSH, i);
        } else {
            backend.emit_ldc(new Integer(i));
        }
    }
    
    public void push(long value) {
        if (value == 0L || value == 1L) {
            backend.emit(Opcodes.lconst(value));
        } else {
            backend.emit_ldc(new Long(value));
        }
    }
    
    public void push(float value) {
        if (value == 0f || value == 1f || value == 2f) {
            backend.emit(Opcodes.fconst(value));
        } else {
            backend.emit_ldc(new Float(value));
        }
    }
    public void push(double value) {
        if (value == 0d || value == 1d) {
            backend.emit(Opcodes.dconst(value));
        } else {
            backend.emit_ldc(new Double(value));
        }
    }
    
    public void push(String value) {
        backend.emit_ldc(value);
    }

    public void newarray() {
        newarray(Object.class);
    }
    
    public void newarray(Class type) {
        if (type.isPrimitive()) {
            backend.emit_int(Opcodes.NEWARRAY, Opcodes.newarray(type));
        } else {
            backend.emit_type(Opcodes.ANEWARRAY, type.getName());
        }
    }
    
    public void arraylength() {
        backend.emit(Opcodes.ARRAYLENGTH);
    }
    
    public void array_load(Class type) {
        if (type.isPrimitive()) {
            if (type.equals(Long.TYPE)) {
                backend.emit(Opcodes.LALOAD);
            } else if (type.equals(Double.TYPE)) {
                backend.emit(Opcodes.DALOAD);
            } else if (type.equals(Float.TYPE)) {
                backend.emit(Opcodes.FALOAD);
            } else if (type.equals(Short.TYPE)) {
                backend.emit(Opcodes.SALOAD);
            } else if (type.equals(Character.TYPE)) {
                backend.emit(Opcodes.CALOAD);
            } else if (type.equals(Integer.TYPE)) {
                backend.emit(Opcodes.IALOAD);
            } else {
                backend.emit(Opcodes.BALOAD);
            }
        } else {
            backend.emit(Opcodes.AALOAD);
        }
    }

    public void array_store(Class type) {
        if (type.isPrimitive()) {
            if (type.equals(Long.TYPE)) {
                backend.emit(Opcodes.LASTORE);
            } else if (type.equals(Double.TYPE)) {
                backend.emit(Opcodes.DASTORE);
            } else if (type.equals(Float.TYPE)) {
                backend.emit(Opcodes.FASTORE);
            } else if (type.equals(Short.TYPE)) {
                backend.emit(Opcodes.SASTORE);
            } else if (type.equals(Character.TYPE)) {
                backend.emit(Opcodes.CASTORE);
            } else if (type.equals(Integer.TYPE)) {
                backend.emit(Opcodes.IASTORE);
            } else {
                backend.emit(Opcodes.BASTORE);
            }
        } else {
            backend.emit(Opcodes.AASTORE);
        }
    }
    
    public void load_this() {
        if (isStatic) {
            throw new IllegalStateException("no 'this' pointer within static method");
        }
        backend.emit_var(Opcodes.ALOAD, 0);
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
            Class t = parameterTypes[fromArg + i];
            load_local(t, pos);
            pos += getStackSize(t);
        }
    }

    private int getLocalOffset() {
        return isStatic ? 0 : 1;
    }
    
    private int skipArgs(int numArgs) {
        int amount = 0;
        for (int i = 0; i < numArgs; i++) {
            amount += getStackSize(parameterTypes[i]);
        }
        return amount;
    }
    
    private void load_local(Class t, int pos) {
        if (t != null && t.isPrimitive()) {
            if (t.equals(Long.TYPE)) {
                backend.emit_var(Opcodes.LLOAD, pos);
            } else if (t.equals(Double.TYPE)) {
                backend.emit_var(Opcodes.DLOAD, pos);
            } else if (t.equals(Float.TYPE)) {
                backend.emit_var(Opcodes.FLOAD, pos);
            } else {
                backend.emit_var(Opcodes.ILOAD, pos);
            }
        } else {
            backend.emit_var(Opcodes.ALOAD, pos);
        }
    }

    private void store_local(Class t, int pos) {
        if (t != null && t.isPrimitive()) {
            if (t.equals(Long.TYPE)) {
                backend.emit_var(Opcodes.LSTORE, pos);
            } else if (t.equals(Double.TYPE)) {
                backend.emit_var(Opcodes.DSTORE, pos);
            } else if (t.equals(Float.TYPE)) {
                backend.emit_var(Opcodes.FSTORE, pos);
            } else {
                backend.emit_var(Opcodes.ISTORE, pos);
            }
        } else {
            backend.emit_var(Opcodes.ASTORE, pos);
        }
    }
    
    public void iinc(Local local, int amount) {
        backend.emit_iinc(local.getIndex(), amount);
    }
    
    public void store_local(Local local) {
        store_local(local.getType(), local.getIndex());
    }
    
    public void load_local(Local local) {
        load_local(local.getType(), local.getIndex());
    }
    
    public void return_value() {
        if (returnType.isPrimitive()) {
            if (returnType.equals(Void.TYPE)) {
                backend.emit(Opcodes.RETURN);
            } else if (returnType.equals(Long.TYPE)) {
                backend.emit(Opcodes.LRETURN);
            } else if (returnType.equals(Double.TYPE)) {
                backend.emit(Opcodes.DRETURN);
            } else if (returnType.equals(Float.TYPE)) {
                backend.emit(Opcodes.FRETURN);
            } else {
                backend.emit(Opcodes.IRETURN);
            }
        } else {
            backend.emit(Opcodes.ARETURN);
        }
    }

    public void declare_field(int modifiers, Class type, String name) {
        if (getFieldInfo(name) != null) {
            throw new IllegalArgumentException("Field \"" + name + "\" already exists");
        }
        backend.declare_field(modifiers, type, name);
        fieldInfo.put(name, new FieldInfo(Modifier.isStatic(modifiers), type));
    }
    
    private FieldInfo getFieldInfo(String name) {
        return (FieldInfo)fieldInfo.get(name);
    }
    
    private static class FieldInfo {
        private boolean staticFlag;
        private Class type;
        
        public FieldInfo(boolean staticFlag, Class type) {
            this.staticFlag = staticFlag;
            this.type = type;
        }
        
        public boolean isStatic() {
            return staticFlag;
        }
        
        public Class getType() {
            return type;
        }
    }
    
    public void getfield(String name) {
        FieldInfo info = getFieldInfo(name);
        int opcode = info.isStatic() ? Opcodes.GETSTATIC : Opcodes.GETFIELD;
        backend.emit_field(opcode, className, name, info.getType());
    }
    
    public void putfield(String name) {
        FieldInfo info = getFieldInfo(name);
        int opcode = info.isStatic() ? Opcodes.PUTSTATIC : Opcodes.PUTFIELD;
        backend.emit_field(opcode, className, name, info.getType());
    }
    
    public void super_getfield(String name) throws NoSuchFieldException {
        // TODO: search up entire superclass chain?
        getfield(superclass.getDeclaredField(name));
    }
    
    public void super_putfield(String name) throws NoSuchFieldException {
        putfield(superclass.getDeclaredField(name));
    }

    public void getfield(Field field) {
        int opcode = isStatic(field) ? Opcodes.GETSTATIC : Opcodes.GETFIELD;
        fieldHelper(opcode, field);
    }
    
    public void putfield(Field field) {
        int opcode = isStatic(field) ? Opcodes.PUTSTATIC : Opcodes.PUTFIELD;
        fieldHelper(opcode, field);
    }

    private boolean isStatic(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }

    private boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    private void fieldHelper(int opcode, Field field) {
        backend.emit_field(opcode,
                           field.getDeclaringClass().getName(),
                           field.getName(),
                           field.getType());
    }

    public void invoke(Method method) {
        int opcode;
        if (method.getDeclaringClass().isInterface()) {
            opcode = Opcodes.INVOKEINTERFACE;
        } else if (isStatic(method)) {
            opcode = Opcodes.INVOKESTATIC;
        } else {
            opcode = Opcodes.INVOKEVIRTUAL;
        }
        backend.emit_invoke(opcode,
                            method.getDeclaringClass().getName(),
                            method.getName(),
                            method.getReturnType(),
                            method.getParameterTypes());
    }
    
    public void super_invoke(Method method) {
        backend.emit_invoke(Opcodes.INVOKESPECIAL,
                            superclass.getName(),
                            method.getName(),
                            method.getReturnType(),
                            method.getParameterTypes());
    }

    public void invoke_virtual_this(String methodName, Class returnType, Class[] parameterTypes) {
        backend.emit_invoke(Opcodes.INVOKEVIRTUAL, className, methodName, returnType, parameterTypes);
    }

    public void invoke_static_this(String methodName, Class returnType, Class[] parameterTypes) {
        backend.emit_invoke(Opcodes.INVOKESTATIC, className, methodName, returnType, parameterTypes);
    }

    public void super_invoke() {
        backend.emit_invoke(Opcodes.INVOKESPECIAL,
                            superclass.getName(),
                            methodName,
                            returnType,
                            parameterTypes);
    }
    
    public void invoke_constructor(String className, Class[] parameterTypes) {
        backend.emit_invoke(Opcodes.INVOKESPECIAL,
                            className,
                            Constants.CONSTRUCTOR_NAME,
                            Void.TYPE,
                            parameterTypes);
    }
    
    public void invoke_constructor(Class type) {
        invoke_constructor(type, Constants.TYPES_EMPTY);
    }
    
    public void invoke(Constructor constructor) {
        invoke_constructor(constructor.getDeclaringClass(), constructor.getParameterTypes());
    }
    
    public void invoke_constructor(Class type, Class[] parameterTypes) {
        invoke_constructor(type.getName(), parameterTypes);
    }
    
    protected static int getStackSize(Class type) {
        return (type.equals(Double.TYPE) || type.equals(Long.TYPE)) ? 2 : 1;
    }

    private static int getStackSize(Class[] classes) {
        int size = 0;
        if (classes != null) {
            for (int i = 0; i < classes.length; i++) {
                size += getStackSize(classes[i]);
            }
        }
        return size;
    }

    public void super_invoke(Constructor constructor) {
        super_invoke_constructor(constructor.getParameterTypes());
    }
    
    public void super_invoke_constructor() {
        invoke_constructor(superclass.getName(), Constants.TYPES_EMPTY);
    }
    
    public void super_invoke_constructor(Class[] parameterTypes) {
        invoke_constructor(superclass.getName(), parameterTypes);
    }
    
    public void invoke_constructor_this() {
        invoke_constructor_this(Constants.TYPES_EMPTY);
    }
    
    public void invoke_constructor_this(Class[] parameterTypes) {
        invoke_constructor(className, parameterTypes);
    }
    
    public void new_instance_this() {
        backend.emit_type(Opcodes.NEW, className);
    }
    
    public void new_instance(String className) {
        backend.emit_type(Opcodes.NEW, className);
    }
    
    public void new_instance(Class type) {
        new_instance(type.getName());
    }
    
    public void aaload(int index) {
        push(index);
        aaload();
    }
    
    public void aaload() { backend.emit(Opcodes.AALOAD); }
    public void aastore() { backend.emit(Opcodes.AASTORE); }
    public void athrow() { backend.emit(Opcodes.ATHROW); }
    
    public Label make_label() {
        return backend.make_label();
    }
    
    public Local make_local() {
        return make_local(null);
    }
    
    public Local make_local(Class type) {
        int index = nextLocal;
        nextLocal += (type == null) ? 1 : getStackSize(type);
        return new Local(type, index);
    }

    public void checkcast_this() {
        backend.emit_type(Opcodes.CHECKCAST, className);
    }
    
    public void checkcast(Class type) {
        // TODO: necessary?
        //         if (type.isArray()) {
        //             append(new CHECKCAST(cp.addArrayClass((ArrayType)type)));
        if (type.equals(Object.class)) {
            // ignore
        } else {
            backend.emit_type(Opcodes.CHECKCAST, type.getName());
        }
    }
    
    public void instance_of(Class type) {
        backend.emit_type(Opcodes.INSTANCEOF, type.getName());
    }
    
    public void instance_of_this() {
        backend.emit_type(Opcodes.INSTANCEOF, className);
    }

    public void mark(Label label) {
        backend.emit(label);
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
                backend.emit_switch(min, max, labels, def);
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
                backend.emit_switch(keys, labels, def);
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

    public interface ProcessSwitchCallback {
        void processCase(int key, Label end) throws Exception;
        void processDefault() throws Exception;
    }

    private static boolean isSorted(int[] keys) {
        for (int i = 1; i < keys.length; i++) {
            if (keys[i] < keys[i - 1])
                return false;
        }
        return true;
    }
}
