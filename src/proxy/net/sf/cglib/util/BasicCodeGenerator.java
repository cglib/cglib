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
        int modifiers = method.getModifiers();
        return Modifier.FINAL
        | (modifiers
        & ~Modifier.ABSTRACT
        & ~Modifier.NATIVE
        & ~Modifier.SYNCHRONIZED);
    }
    
    protected void begin_method(Method method) {
        begin_method(method, getDefaultModifiers(method));
    }
    
    protected void begin_method(Method method, int modifiers) {
        begin_method(modifiers, method.getReturnType(), method.getName(),
        method.getParameterTypes(), method.getExceptionTypes());
    }
    
    protected void begin_constructor(Constructor constructor) {
        begin_constructor(constructor.getParameterTypes());
    }
    
    protected void begin_constructor() {
        begin_constructor(Constants.TYPES_EMPTY);
    }
    
    protected void begin_constructor(Class[] parameterTypes) {
        checkInMethod();
        this.returnType = Void.TYPE;
        this.parameterTypes = parameterTypes;
        backend.begin_constructor(parameterTypes);
        setNextLocal();
    }
    
    protected void begin_static() {
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
    
    protected void end_method() {
        backend.end_method();
        parameterTypes = null;
        returnType = null;
        methodName = null;
        if (curBlock != null) {
            throw new IllegalStateException("unclosed exception block");
        }
        inMethod = false;
    }

    protected Block begin_block() {
        Block newBlock = new Block(curBlock, backend.mark());
        curBlock = newBlock;
        return curBlock;
    }

    protected void end_block() {
        if (curBlock == null) {
            throw new IllegalStateException("mismatched block boundaries");
        }
        curBlock.setEnd(backend.mark());
        curBlock = curBlock.getParent();
    }
    
    protected void catch_exception(Block block, Class exceptionType) {
        // TODO
        // throw new IllegalArgumentException("block belongs to a different method");

        if (block.getEnd() == null) {
            throw new IllegalStateException("end of block is unset");
        }
        backend.catch_exception(block, exceptionType);
    }
    
    protected void ifeq(Label label) { backend.emit(Opcodes.IFEQ, label); }
    protected void ifne(Label label) { backend.emit(Opcodes.IFNE, label); }
    protected void iflt(Label label) { backend.emit(Opcodes.IFLT, label); }
    protected void ifge(Label label) { backend.emit(Opcodes.IFGE, label); }
    protected void ifgt(Label label) { backend.emit(Opcodes.IFGT, label); }
    protected void ifle(Label label) { backend.emit(Opcodes.IFLE, label); }
    protected void goTo(Label label) { backend.emit(Opcodes.GOTO, label); }
    protected void ifnull(Label label) { backend.emit(Opcodes.IFNULL, label); }
    protected void ifnonnull(Label label) { backend.emit(Opcodes.IFNONNULL, label); }
    protected void if_icmplt(Label label) { backend.emit(Opcodes.IF_ICMPLT, label); }
    protected void if_icmpne(Label label) { backend.emit(Opcodes.IF_ICMPNE, label); }
    protected void if_icmpeq(Label label) { backend.emit(Opcodes.IF_ICMPEQ, label); }
    protected void if_acmpeq(Label label) { backend.emit(Opcodes.IF_ACMPEQ, label); }
    protected void if_acmpne(Label label) { backend.emit(Opcodes.IF_ACMPNE, label); }

    protected void imul() { backend.emit(Opcodes.IMUL); }
    protected void iadd() { backend.emit(Opcodes.IADD); }
    protected void lushr() { backend.emit(Opcodes.LUSHR); }
    protected void lxor() { backend.emit(Opcodes.LXOR); }
    protected void ixor() { backend.emit(Opcodes.IXOR); }
    protected void l2i() { backend.emit(Opcodes.L2I); }
    protected void dcmpg() { backend.emit(Opcodes.DCMPG); }
    protected void fcmpg() { backend.emit(Opcodes.FCMPG); }
    protected void lcmp() { backend.emit(Opcodes.LCMP); }
    protected void pop() { backend.emit(Opcodes.POP); }
    protected void pop2() { backend.emit(Opcodes.POP2); }
    protected void dup() { backend.emit(Opcodes.DUP); }
    protected void dup2() { backend.emit(Opcodes.DUP2); }
    protected void dup_x1() { backend.emit(Opcodes.DUP_X1); }
    protected void dup_x2() { backend.emit(Opcodes.DUP_X2); }
    protected void swap() { backend.emit(Opcodes.SWAP); }
    protected void aconst_null() { backend.emit(Opcodes.ACONST_NULL); }
    
    protected void push(int i) {
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
    
    protected void push(long value) {
        if (value == 0L || value == 1L) {
            backend.emit(Opcodes.lconst(value));
        } else {
            backend.emit_ldc(new Long(value));
        }
    }
    
    protected void push(float value) {
        if (value == 0f || value == 1f || value == 2f) {
            backend.emit(Opcodes.fconst(value));
        } else {
            backend.emit_ldc(new Float(value));
        }
    }
    protected void push(double value) {
        if (value == 0d || value == 1d) {
            backend.emit(Opcodes.dconst(value));
        } else {
            backend.emit_ldc(new Double(value));
        }
    }
    
    protected void push(String value) {
        backend.emit_ldc(value);
    }

    protected void newarray() {
        newarray(Object.class);
    }
    
    protected void newarray(Class type) {
        if (type.isPrimitive()) {
            backend.emit_int(Opcodes.NEWARRAY, Opcodes.newarray(type));
        } else {
            backend.emit_type(Opcodes.ANEWARRAY, type.getName());
        }
    }
    
    protected void arraylength() {
        backend.emit(Opcodes.ARRAYLENGTH);
    }
    
    protected void array_load(Class type) {
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

    protected void array_store(Class type) {
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
    
    protected void load_this() {
        if (isStatic) {
            throw new IllegalStateException("no 'this' pointer within static method");
        }
        backend.emit_var(Opcodes.ALOAD, 0);
    }
    
    /**
     * Pushes all of the arguments of the current method onto the stack.
     */
    protected void load_args() {
        load_args(0, parameterTypes.length);
    }
    
    /**
     * Pushes the specified argument of the current method onto the stack.
     * @param index the zero-based index into the argument list
     */
    protected void load_arg(int index) {
        load_local(parameterTypes[index], getLocalOffset() + skipArgs(index));
    }
    
    // zero-based (see load_this)
    protected void load_args(int fromArg, int count) {
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
    
    protected void iinc(Local local, int amount) {
        backend.emit_iinc(local.getIndex(), amount);
    }
    
    protected void store_local(Local local) {
        store_local(local.getType(), local.getIndex());
    }
    
    protected void load_local(Local local) {
        load_local(local.getType(), local.getIndex());
    }
    
    protected void return_value() {
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

    protected void declare_field(int modifiers, Class type, String name) {
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
    
    protected void getfield(String name) {
        FieldInfo info = getFieldInfo(name);
        int opcode = info.isStatic() ? Opcodes.GETSTATIC : Opcodes.GETFIELD;
        backend.emit_field(opcode, className, name, info.getType());
    }
    
    protected void putfield(String name) {
        FieldInfo info = getFieldInfo(name);
        int opcode = info.isStatic() ? Opcodes.PUTSTATIC : Opcodes.PUTFIELD;
        backend.emit_field(opcode, className, name, info.getType());
    }
    
    protected void super_getfield(String name) throws NoSuchFieldException {
        // TODO: search up entire superclass chain?
        getfield(superclass.getDeclaredField(name));
    }
    
    protected void super_putfield(String name) throws NoSuchFieldException {
        putfield(superclass.getDeclaredField(name));
    }

    protected void getfield(Field field) {
        int opcode = isStatic(field) ? Opcodes.GETSTATIC : Opcodes.GETFIELD;
        fieldHelper(opcode, field);
    }
    
    protected void putfield(Field field) {
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

    protected void invoke(Method method) {
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
    
    protected void super_invoke(Method method) {
        backend.emit_invoke(Opcodes.INVOKESPECIAL,
                            superclass.getName(),
                            method.getName(),
                            method.getReturnType(),
                            method.getParameterTypes());
    }

    protected void invoke_virtual_this(String methodName, Class returnType, Class[] parameterTypes) {
        backend.emit_invoke(Opcodes.INVOKEVIRTUAL, className, methodName, returnType, parameterTypes);
    }

    protected void invoke_static_this(String methodName, Class returnType, Class[] parameterTypes) {
        backend.emit_invoke(Opcodes.INVOKESTATIC, className, methodName, returnType, parameterTypes);
    }

    protected void super_invoke() {
        backend.emit_invoke(Opcodes.INVOKESPECIAL,
                            superclass.getName(),
                            methodName,
                            returnType,
                            parameterTypes);
    }
    
    protected void invoke_constructor(String className, Class[] parameterTypes) {
        backend.emit_invoke(Opcodes.INVOKESPECIAL,
                            className,
                            Constants.CONSTRUCTOR_NAME,
                            Void.TYPE,
                            parameterTypes);
    }
    
    protected void invoke_constructor(Class type) {
        invoke_constructor(type, Constants.TYPES_EMPTY);
    }
    
    protected void invoke(Constructor constructor) {
        invoke_constructor(constructor.getDeclaringClass(), constructor.getParameterTypes());
    }
    
    protected void invoke_constructor(Class type, Class[] parameterTypes) {
        invoke_constructor(type.getName(), parameterTypes);
    }
    
    protected static int getStackSize(Class type) {
        return (type.equals(Double.TYPE) || type.equals(Long.TYPE)) ? 2 : 1;
    }

    private static int getStackSize(Class[] classes) {
        int size = 0;
        for (int i = 0; i < classes.length; i++) {
            size += getStackSize(classes[i]);
        }
        return size;
    }

    protected void super_invoke(Constructor constructor) {
        super_invoke_constructor(constructor.getParameterTypes());
    }
    
    protected void super_invoke_constructor() {
        invoke_constructor(superclass.getName(), Constants.TYPES_EMPTY);
    }
    
    protected void super_invoke_constructor(Class[] parameterTypes) {
        invoke_constructor(superclass.getName(), parameterTypes);
    }
    
    protected void invoke_constructor_this() {
        invoke_constructor_this(Constants.TYPES_EMPTY);
    }
    
    protected void invoke_constructor_this(Class[] parameterTypes) {
        invoke_constructor(className, parameterTypes);
    }
    
    protected void new_instance_this() {
        backend.emit_type(Opcodes.NEW, className);
    }
    
    protected void new_instance(String className) {
        backend.emit_type(Opcodes.NEW, className);
    }
    
    protected void new_instance(Class type) {
        new_instance(type.getName());
    }
    
    protected void aaload(int index) {
        push(index);
        aaload();
    }
    
    protected void aaload() { backend.emit(Opcodes.AALOAD); }
    protected void aastore() { backend.emit(Opcodes.AASTORE); }
    protected void athrow() { backend.emit(Opcodes.ATHROW); }
    
    protected Label make_label() {
        return backend.make_label();
    }
    
    protected Local make_local() {
        return make_local(null);
    }
    
    protected Local make_local(Class type) {
        int index = nextLocal;
        nextLocal += (type == null) ? 1 : getStackSize(type);
        return new Local(type, index);
    }

    protected void checkcast_this() {
        backend.emit_type(Opcodes.CHECKCAST, className);
    }
    
    protected void checkcast(Class type) {
        // TODO: necessary?
        //         if (type.isArray()) {
        //             append(new CHECKCAST(cp.addArrayClass((ArrayType)type)));
        if (type.equals(Object.class)) {
            // ignore
        } else {
            backend.emit_type(Opcodes.CHECKCAST, type.getName());
        }
    }
    
    protected void instance_of(Class type) {
        backend.emit_type(Opcodes.INSTANCEOF, type.getName());
    }
    
    protected void instance_of_this() {
        backend.emit_type(Opcodes.INSTANCEOF, className);
    }

    protected void mark(Label label) {
        backend.emit(label);
    }
}
