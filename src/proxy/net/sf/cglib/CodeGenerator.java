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
package net.sf.cglib;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
/**
 * Abstract base class for code generators
 * @author  baliuka
 */
/* package */ abstract class CodeGenerator {
    private static final String FIND_CLASS = "CGLIB$findClass";
    private static final String PRIVATE_PREFIX = "CGLIB$PRIV";
    private static final Map primitiveMethods = new HashMap();
    private static final Map primitiveToWrapper = new HashMap();
    private static String debugLocation;
    private static RuntimePermission DEFINE_CGLIB_CLASS_IN_JAVA_PACKAGE_PERMISSION = 
                         new RuntimePermission("defineCGLIBClassInJavaPackage");

    private final ClassLoader loader;
    private String methodName;
    private Class returnType;
    private Class[] parameterTypes;
    private Class superclass;
    private boolean needsFindClass;
    
    private int nextPrivateLabel;
    private int nextPrivateLocal;

	private Set labels = new HashSet();
    private Map locals = new HashMap();
    private Map localTypes = new HashMap();
    private int nextLocal;
    private boolean inMethod;

    private LinkedList handlerStack = new LinkedList();
    private LinkedList handlerList = new LinkedList();

    private Map fieldInfo = new HashMap();
    private String className;

    private CodeGeneratorBackend backend;
    private boolean debug = false;
    
    protected CodeGenerator(String className, Class superclass, ClassLoader loader) {
        if (loader == null) {
            throw new IllegalArgumentException("ClassLoader is required");
        }
        this.loader = loader;
        this.className = className;
        this.superclass = superclass;
        backend = new BCELBackend(className, superclass);
	}

    protected void setDebug(boolean debug) {
        this.debug = debug;
        backend.setDebug(debug);
    }

    protected String getClassName() {
        return className;
    }

    protected Class getSuperclass() {
        return superclass;
    }

    public static void setDebugLocation(String debugLocation) {
        CodeGenerator.debugLocation = debugLocation;
    }

	/**
	 * method used to generate code  
     */
    abstract protected void generate() throws Exception;

    public Class define() {
        try {
            generate();
            if (needsFindClass) {
                generateFindClass();
            }
            byte[] bytes = backend.getBytes();
            if (debugLocation != null) {
                OutputStream out = new FileOutputStream(new File(new File(debugLocation), className + ".cglib"));
                out.write(bytes);
                out.close();
            }
            return defineClass(className, bytes, loader);
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
    
    private static Class defineClass(String className, byte[] b, ClassLoader loader)
    throws Exception {
        Method m = MethodConstants.DEFINE_CLASS;
        // protected method invocaton
        boolean flag = m.isAccessible();
        m.setAccessible(true);
                   
        SecurityManager sm = System.getSecurityManager(); 
        if (className.startsWith("java.") && sm != null  ) {
            sm.checkPermission( DEFINE_CGLIB_CLASS_IN_JAVA_PACKAGE_PERMISSION );
        }
                    
        //way depricated in jdk to define classes, 
        // doe's not throws SecurityException if class name starts with "java."  
        Object[] args = new Object[]{ b, new Integer(0), new Integer(b.length) };
        Class result = (Class)m.invoke(loader, args);
        m.setAccessible(flag);

        return result;
    }
       
    static {
        primitiveMethods.put(Boolean.TYPE, MethodConstants.BOOLEAN_VALUE);
        primitiveMethods.put(Character.TYPE, MethodConstants.CHAR_VALUE);
        primitiveMethods.put(Long.TYPE, MethodConstants.LONG_VALUE);
        primitiveMethods.put(Double.TYPE, MethodConstants.DOUBLE_VALUE);
        primitiveMethods.put(Float.TYPE, MethodConstants.FLOAT_VALUE);
        primitiveMethods.put(Short.TYPE, MethodConstants.INT_VALUE);
        primitiveMethods.put(Integer.TYPE, MethodConstants.INT_VALUE);
        primitiveMethods.put(Byte.TYPE, MethodConstants.INT_VALUE);

        primitiveToWrapper.put(Boolean.TYPE, Boolean.class);
        primitiveToWrapper.put(Character.TYPE, Character.class);
        primitiveToWrapper.put(Long.TYPE, Long.class);
        primitiveToWrapper.put(Double.TYPE, Double.class);
        primitiveToWrapper.put(Float.TYPE, Float.class);
        primitiveToWrapper.put(Short.TYPE, Short.class);
        primitiveToWrapper.put(Integer.TYPE, Integer.class);
        primitiveToWrapper.put(Byte.TYPE, Byte.class);
    }

    protected void declare_interfaces(Class[] interfaces) {
        for (int i = 0; i < interfaces.length; i++) {
            declare_interface(interfaces[i]);
        }
    }

    protected void declare_interface(Class iface) {
        backend.declare_interface(iface);
    }

    protected void begin_method(int modifiers, Class returnType, String methodName,
                                Class[] parameterTypes, Class[] exceptionTypes) {
        checkInMethod();
        this.methodName = methodName;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        backend.begin_method(modifiers, returnType, methodName, parameterTypes, exceptionTypes);
        setNextLocal();
    }

    protected int getDefaultModifiers(Method method) {
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
    }

    private void setNextLocal() {
        nextLocal = 1 + getStackSize(parameterTypes);
    }

    protected void end_method() {
        backend.end_method();
        parameterTypes = null;
        returnType = null;
        methodName = null;
        labels.clear();
        locals.clear();
        localTypes.clear();
        if (handlerStack.size() > 0) {
            throw new IllegalStateException("unclosed exception handler");
        }
        handlerList.clear();
        inMethod = false;
    }

    /**
     * Allocates and fills an Object[] array with the arguments to the
     * current method. Primitive values are inserted as their boxed
     * (Object) equivalents.
     */
    protected void create_arg_array() {
        /* generates:
           Object args[] = new Object[]{ arg1, new Integer(arg2) };
        */
        push(parameterTypes.length);
        newarray();
        for (int i = 0; i < parameterTypes.length; i++) {
            dup();
            push(i);
            load_arg(i);
            box(parameterTypes[i]);
            aastore();
        }
    }

    protected Object begin_handler() {
        int ref = handlerList.size();
        Object[] range = new Object[]{ backend.start_range(), null };
        handlerList.add(range);
        handlerStack.add(range);
        return new Integer(ref);
    }

    protected void end_handler() {
        if (handlerStack.size() == 0) {
            throw new IllegalStateException("mismatched handler boundaries");
        }
        Object[] range = (Object[])handlerStack.removeLast();
        range[1] = backend.end_range();
    }

    protected void handle_exception(Object handler, Class exceptionType) {
        int ref = ((Integer)handler).intValue();
        if (handlerList.size() <= ref) {
            throw new IllegalArgumentException("unknown handler reference: " + ref);
        }
        Object[] range = (Object[])handlerList.get(ref);
        if (range[1] == null) {
            throw new IllegalStateException("end of handler is unset");
        }
        backend.handle_exception(range[0], range[1], exceptionType);
    }

    protected void ifeq(String label) { backend.ifeq(label); }
    protected void ifne(String label) { backend.ifne(label); }
    protected void iflt(String label) { backend.iflt(label); }
    protected void ifge(String label) { backend.ifge(label); }
    protected void ifgt(String label) { backend.ifgt(label); }
    protected void ifle(String label) { backend.ifle(label); }
    protected void goTo(String label) { backend.goTo(label); }
    protected void ifnull(String label) { backend.ifnull(label); }
    protected void ifnonnull(String label) { backend.ifnonnull(label); }
    protected void if_icmplt(String label) { backend.if_icmplt(label); }
    protected void if_icmpne(String label) { backend.if_icmpne(label); }
    protected void if_icmpeq(String label) { backend.if_icmpeq(label); }
    protected void nop(String label) { backend.nop(label); }
    protected void imul() { backend.imul(); }
    protected void iadd() { backend.iadd(); }
    protected void lushr() { backend.lushr(); }
    protected void lxor() { backend.lxor(); }
    protected void ixor() { backend.ixor(); }
    protected void l2i() { backend.l2i(); }
    protected void dcmpg() { backend.dcmpg(); }
    protected void fcmpg() { backend.fcmpg(); }
    protected void lcmp() { backend.lcmp(); }
    protected void pop() { backend.pop(); }
    protected void pop2() { backend.pop2(); }
    protected void dup() { backend.dup(); }
    protected void dup2() { backend.dup2(); }
    protected void dup_x1() { backend.dup_x1(); }
    protected void dup_x2() { backend.dup_x2(); }
    protected void swap() { backend.swap(); }
    protected  void aconst_null() { backend.aconst_null(); }
  
    protected void push(int i) {
        if (i < 0) {
            backend.ldc(i);
        } else if (i <= 5) {
            backend.iconst(i);
        } else if (i <= Byte.MAX_VALUE) {
            backend.bipush((byte)i);
        } else if (i <= Short.MAX_VALUE) {
            backend.sipush((short)i);
        } else {
            backend.ldc(i);
        }
    }
    
    protected void push (long value) {
        if (value == 0L || value == 1L) {
            backend.lconst(value);
        } else {
            backend.ldc(value);
        }
    }
    
    protected void push (float value) {
        if (value == 0f || value == 1f || value == 2f) {
            backend.fconst(value);
        } else {
            backend.ldc(value);
        }
    }
    protected void push (double value) {
        if (value == 0d || value == 1d) {
            backend.dconst(value);
        } else {
            backend.ldc(value);
        }
    }

    protected void push(String value) {
        backend.ldc(value);
    }

    protected void push(Object[] array) {
        push(array.length);
        newarray(array.getClass().getComponentType());
        for (int i = 0; i < array.length; i++) {
            dup();
            push(i);
            push_object(array[i]);
            aastore();
        }
    }

    protected void push_object(Object obj) {
        if (obj == null) {
            aconst_null();
        } else {
            Class type = obj.getClass();
            if (type.isArray()) {
                push((Object[])obj);
            } else if (obj instanceof String) {
                push((String)obj);
            } else if (obj instanceof Class) {
                load_class((Class)obj);
            } else if (obj.getClass().getSuperclass().equals(Number.class)) {
                throw new IllegalArgumentException("not implemented yet");
            } else {
                throw new IllegalArgumentException("unknown type: " + obj.getClass());
            }
        }
    }
  
    protected void newarray() {
        newarray(Object.class);
    }

    protected void newarray(Class clazz) {
        if (clazz.isPrimitive()) {
            backend.newarray(clazz);
        } else {
            backend.anewarray(clazz);
        }
    }

    protected void arraylength() {
        backend.arraylength();
    }

    protected void array_load(Class clazz) {
        if (clazz.isPrimitive()) {
            if (clazz.equals(Long.TYPE)) {
                backend.laload();
            } else if (clazz.equals(Double.TYPE)) {
                backend.daload();
            } else if (clazz.equals(Float.TYPE)) {
                backend.faload();
            } else if (clazz.equals(Short.TYPE)) {
                backend.saload();
            } else if (clazz.equals(Character.TYPE)) {
                backend.caload();
            } else if (clazz.equals(Integer.TYPE)) {
                backend.iaload();
            } else {
                backend.baload();
            }
        } else {
            backend.aaload();
        }
    }

    protected void array_store(Class clazz) {
        if (clazz.isPrimitive()) {
            if (clazz.equals(Long.TYPE)) {
                backend.lastore();
            } else if (clazz.equals(Double.TYPE)) {
                backend.dastore();
            } else if (clazz.equals(Float.TYPE)) {
                backend.fastore();
            } else if (clazz.equals(Short.TYPE)) {
                backend.sastore();
            } else if (clazz.equals(Character.TYPE)) {
                backend.castore();
            } else if (clazz.equals(Integer.TYPE)) {
                backend.iastore();
            } else {
                backend.bastore();
            }
        } else {
            backend.aastore();
        }
    }
    
    protected void load_this() {
        backend.aload(0);
    }

    protected void load_class_this() {
        load_class_helper(className);
    }

    protected void load_class(Class clazz) {
        if (clazz.isPrimitive()) {
            if (clazz.equals(Void.TYPE)) {
                throw new IllegalArgumentException("cannot load void type");
            }
            try {
                getfield(((Class)primitiveToWrapper.get(clazz)).getDeclaredField("TYPE"));
            } catch (NoSuchFieldException e) {
                throw new CodeGenerationException(e);
            }
        } else {
            load_class_helper(clazz.getName());
        }
    }

    private void load_class_helper(String className) {
        // System.err.println("findclass: " + className + " hasFindClass: " + hasFindClass);
        needsFindClass = true;
        push(className);
        invoke_static_this(FIND_CLASS, Class.class, Constants.TYPES_STRING);
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
        load_local(parameterTypes[index], 1 + skipArgs(index));
    }

    // zero-based (see load_this)
    protected void load_args(int fromArg, int count) {
        int pos = 1 + skipArgs(fromArg);
        for (int i = 0; i < count; i++) {
            Class t = parameterTypes[fromArg + i];
            load_local(t, pos);
            pos += getStackSize(t);
        }
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
                backend.lload(pos);
            } else if (t.equals(Double.TYPE)) {
                backend.dload(pos);
            } else if (t.equals(Float.TYPE)) {
                backend.fload(pos);
            } else {
                backend.iload(pos);
            }
        } else {
            backend.aload(pos);
        }
    }

    private void store_local(Class t, int index) {
        if (t != null && t.isPrimitive()) {
            if (t.equals(Long.TYPE)) {
                backend.lstore(index);
            } else if (t.equals(Double.TYPE)) {
                backend.dstore(index);
            } else if (t.equals(Float.TYPE)) {
                backend.fstore(index);
            } else {
                backend.istore(index);
            }
        } else {
            backend.astore(index);
        }
    }

    protected void iinc(Object local, int amount) {
        backend.iinc(getLocal(local), amount);
    }

    protected void store_local(Object local) {
        store_local((Class)localTypes.get(local), getLocal(local));
    }

    protected void load_local(Object local) {
        load_local((Class)localTypes.get(local), getLocal(local));
    }

    private int getLocal(Object local) {
        return ((Integer)locals.get(local)).intValue();
    }

    protected void return_value() {
        if (returnType.isPrimitive()) {
            if (returnType.equals(Void.TYPE)) {
                backend.returnVoid();
            } else if (returnType.equals(Long.TYPE)) {
                backend.lreturn();
            } else if (returnType.equals(Double.TYPE)) {
                backend.dreturn();
            } else if (returnType.equals(Float.TYPE)) {
                backend.freturn();
            } else {
                backend.ireturn();
            }
        } else {
            backend.areturn();
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
        if (info.isStatic()) {
            backend.getstatic(className, name, info.getType());
        } else {
            backend.getfield(className, name, info.getType());
        }
    }

    protected void putfield(String name) {
        FieldInfo info = getFieldInfo(name);
        if (info.isStatic()) {
            backend.putstatic(className, name, info.getType());
        } else {
            backend.putfield(className, name, info.getType());
        }
    }

    protected void super_getfield(String name) throws NoSuchFieldException {
        // TODO: search up entire superclass chain?
        getfield(superclass.getDeclaredField(name));
    }

    protected void super_putfield(String name) throws NoSuchFieldException {
        putfield(superclass.getDeclaredField(name));
    }

    protected void getfield(Field field) {
        if (Modifier.isStatic(field.getModifiers())) {
            backend.getstatic(field.getDeclaringClass().getName(),
                              field.getName(),
                              field.getType());
        } else {
            backend.getfield(field.getDeclaringClass().getName(),
                             field.getName(),
                             field.getType());
        }
    }

    protected void putfield(Field field) {
        if (Modifier.isStatic(field.getModifiers())) {
            backend.putstatic(field.getDeclaringClass().getName(),
                              field.getName(),
                              field.getType());
        } else {
            backend.putfield(field.getDeclaringClass().getName(),
                             field.getName(),
                             field.getType());
        }
    }

    protected void invoke(Method method) {
        if (method.getDeclaringClass().isInterface()) {
            backend.invoke_interface(method.getDeclaringClass().getName(), method.getName(),
                                     method.getReturnType(), method.getParameterTypes());
        } else if (Modifier.isStatic(method.getModifiers())) {
            backend.invoke_static(method.getDeclaringClass().getName(), method.getName(),
                                  method.getReturnType(), method.getParameterTypes());
        } else {
            backend.invoke_virtual(method.getDeclaringClass().getName(), method.getName(),
                                   method.getReturnType(), method.getParameterTypes());
        }
    }

    protected void super_invoke(Method method) {
        backend.invoke_special(superclass.getName(), method.getName(),
                               method.getReturnType(), method.getParameterTypes());
    }

    protected void invoke_virtual_this(String methodName, Class returnType, Class[] parameterTypes) {
        backend.invoke_virtual(className, methodName, returnType, parameterTypes);
    }

    protected void invoke_static_this(String methodName, Class returnType, Class[] parameterTypes) {
        backend.invoke_static(className, methodName, returnType, parameterTypes);
    }

    protected void super_invoke() {
        backend.invoke_special(superclass.getName(), methodName, returnType, parameterTypes);
    }

    private void invoke_constructor_helper(String className, Class[] parameterTypes) {
        backend.invoke_special(className, Constants.CONSTRUCTOR_NAME, Void.TYPE, parameterTypes);
    }
   
    protected void invoke_constructor(Class type) {
        invoke_constructor(type, Constants.TYPES_EMPTY);
    }

    protected void invoke(Constructor constructor) {
        invoke_constructor(constructor.getDeclaringClass(), constructor.getParameterTypes());
    }

    protected void invoke_constructor(Class type, Class[] parameterTypes) {
        invoke_constructor_helper(type.getName(), parameterTypes);
    }

    private static int getStackSize(Class type) {
        return (type.equals(Double.TYPE) || type.equals(Long.TYPE)) ? 2 : 1;
    }

    /* package */ static int getStackSize(Class[] classes) {
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
        invoke_constructor_helper(superclass.getName(), Constants.TYPES_EMPTY);
    }

    protected void super_invoke_constructor(Class[] parameterTypes) {
        invoke_constructor_helper(superclass.getName(), parameterTypes);
    }

    protected void invoke_constructor_this() {
        invoke_constructor_this(Constants.TYPES_EMPTY);
    }

    protected void invoke_constructor_this(Class[] parameterTypes) {
        invoke_constructor_helper(className, parameterTypes);
    }

    protected void new_instance_this() {
        backend.new_instance(className);
    }
  
    protected void new_instance(Class clazz) {
        backend.new_instance(clazz.getName());
    }
 
    protected void aaload(int index) {
        push(index);
        aaload();
    }
    
    protected void aaload() { backend.aaload(); }
    protected void aastore() { backend.aastore(); }
    protected void athrow() { backend.athrow(); }
  
    protected String anon_label() {
        String label;
        do {
            label = PRIVATE_PREFIX + nextPrivateLabel++;
        } while (labels.contains(label));
        return label;
    }

    protected Object make_local() {
        return make_local(null);
    }

    protected Object make_local(Class type) {
        Object local = new Object();
        locals.put(local, new Integer(nextLocal));
        localTypes.put(local, type);
        nextLocal += (type == null) ? 1 : getStackSize(type);
        return local;
    }

    /**
     * Pushes a zero onto the stack if the argument is a primitive class, or a null otherwise.
     */
    protected void zero_or_null(Class type) {
        if (type.isPrimitive()) {
            if (type.equals(Double.TYPE)) {
                push(0d);
            } else if (type.equals(Long.TYPE)) {
                push(0L);
            } else if (type.equals(Float.TYPE)) {
                push(0f);
            } else if (type.equals(Void.TYPE)) {
                // ignore
            } else {
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
    protected void unbox_or_zero(Class type) {
        if (type.isPrimitive()) {
            if (!type.equals(Void.TYPE)) {
                String nonNull = anon_label();
                String end = anon_label();
                dup();
                ifnonnull(nonNull);
                pop();
                zero_or_null(type);
                goTo(end);
                nop(nonNull);
                unbox(type);
                nop(end);
            }
        } else {
            checkcast(type);
        }
    }

    /**
     * If the argument is a primitive class, replaces the primitive value
     * on the top of the stack with the wrapped (Object) equivalent. For
     * example, char -> Character.
     * If the class is Void, a null is pushed onto the stack instead.
     * @param clazz the class indicating the current type of the top stack value
     */
    protected void box(Class clazz) {
        if (clazz.isPrimitive()) {
            if (clazz.equals(Void.TYPE)) {
                aconst_null();
            } else {
                Class wrapper = (Class)primitiveToWrapper.get(clazz);
                new_instance(wrapper);
                if (getStackSize(clazz) == 2) {
                    // Pp -> Ppo -> oPpo -> ooPpo -> ooPp -> o
                    dup_x2();
                    dup_x2();
                    pop();
                } else {
                    // p -> po -> opo -> oop -> o
                    dup_x1();
                    swap();
                }
                invoke_constructor(wrapper, new Class[]{ clazz });
            }
        }
    }

    /**
     * If the argument is a primitive class, replaces the object
     * on the top of the stack with the unwrapped (primitive)
     * equivalent. For example, Character -> char.
     * @param clazz the class indicating the desired type of the top stack value
     * @return true if the value was unboxed
     */
    protected void unbox(Class clazz) {
        if (clazz.isPrimitive()) {
            if (clazz.equals(Void.TYPE)) {
                // ignore
            }
            Class wrapper = (Class)primitiveToWrapper.get(clazz);
            Method convert = (Method)primitiveMethods.get(clazz);
            checkcast(wrapper);
            invoke(convert);
        } else {
            checkcast(clazz);
        }
    }

    protected void checkcast_this() {
        backend.checkcast(className);
    }

    protected void checkcast(Class clazz) {
        // TODO: necessary?
//         if (clazz.isArray()) {
//             append(new CHECKCAST(cp.addArrayClass((ArrayType)type)));
        if (clazz.equals(Object.class)) {
            // ignore
        } else {
            backend.checkcast(clazz.getName());
        }
    }
   
    protected void instance_of(Class clazz) {
        backend.instance_of(clazz.getName());
    }

    protected void instance_of_this() {
        backend.instance_of(className);
    }
    
    protected void generateNullConstructor() {
        begin_constructor();
        load_this();
        super_invoke_constructor();
        return_value();
        end_method();
    }

    private void generateFindClass() {
        /* generates:
           static private Class findClass(String name) throws Exception {
               try {
                   return Class.forName(name);
               } catch (java.lang.ClassNotFoundException cne) {
                   throw new java.lang.NoClassDefFoundError(cne.getMessage());
               }
           }
        */
        begin_method(Modifier.PRIVATE | Modifier.STATIC,
                     Class.class,
                     FIND_CLASS,
                     Constants.TYPES_STRING,
                     null);
        Object eh = begin_handler();
        load_this();
        invoke(MethodConstants.FOR_NAME);
        return_value();
        end_handler();

        handle_exception(eh, ClassNotFoundException.class);
        invoke(MethodConstants.THROWABLE_GET_MESSAGE);
        new_instance(NoClassDefFoundError.class);
        dup_x1();
        swap();
        invoke_constructor(NoClassDefFoundError.class, Constants.TYPES_STRING);
        athrow();
        end_method();
    }

    protected interface ProcessArrayCallback {
        public void processElement(Class type);
    }

    /**
     * Process an array on the stack. Assumes the top item on the stack
     * is an array of the specified type. For each element in the array,
     * puts the element on the stack and triggers the callback.
     * @param type the type of the array (type.isArray() must be true)
     * @param callback the callback triggered for each element
     */
    protected void process_array(Class type, ProcessArrayCallback callback) {
        Class compType = type.getComponentType();
        Object array = make_local();
        Object loopvar = make_local(Integer.TYPE);
        String loopbody = anon_label();
        String checkloop = anon_label();
        store_local(array);
        push(0);
        store_local(loopvar);
        goTo(checkloop);

        nop(loopbody);
        load_local(array);
        load_local(loopvar);
        array_load(compType);
        callback.processElement(compType);
        iinc(loopvar, 1);

        nop(checkloop);
        load_local(loopvar);
        load_local(array);
        arraylength();
        if_icmplt(loopbody);
    }

    /**
     * Process two arrays on the stack in parallel. Assumes the top two items on the stack
     * are arrays of the specified class. The arrays must be the same length. For each pair
     * of elements in the arrays, puts the pair on the stack and triggers the callback.
     * @param clazz the type of the arrays (clazz.isArray() must be true)
     * @param callback the callback triggered for each pair of elements
     */
    protected void process_arrays(Class clazz, ProcessArrayCallback callback) {
        Class compType = clazz.getComponentType();
        Object array1 = make_local();
        Object array2 = make_local();
        Object loopvar = make_local(Integer.TYPE);
        String loopbody = anon_label();
        String checkloop = anon_label();
        store_local(array1);
        store_local(array2);
        push(0);
        store_local(loopvar);
        goTo(checkloop);

        nop(loopbody);
        load_local(array1);
        load_local(loopvar);
        array_load(compType);
        load_local(array2);
        load_local(loopvar);
        array_load(compType);
        callback.processElement(compType);
        iinc(loopvar, 1);

        nop(checkloop);
        load_local(loopvar);
        load_local(array1);
        arraylength();
        if_icmplt(loopbody);
    }

    /**
     * Branches to the specified label if the top two items on the stack
     * are not equal. The items must both be of the specified
     * class. Equality is determined by comparing primitive values
     * directly and by invoking the <code>equals</code> method for
     * Objects. Arrays are recursively processed in the same manner.
     */
    protected void not_equals(Class clazz, final String notEquals) {
        (new ProcessArrayCallback() {
                public void processElement(Class type) {
                    not_equals_helper(type, notEquals, this);
                }
            }).processElement(clazz);
    }

    private void not_equals_helper(Class clazz, String notEquals, ProcessArrayCallback callback) {
        if (clazz.isPrimitive()) {
            if (returnType.equals(Double.TYPE)) {
                dcmpg();
                ifne(notEquals);
            } else if (returnType.equals(Long.TYPE)) {
                lcmp();
                ifne(notEquals);
            } else if (returnType.equals(Float.TYPE)) {
                fcmpg();
                ifne(notEquals);
            } else {
                if_icmpne(notEquals);
            }
        } else {
            String end = anon_label();
            nullcmp(notEquals, end);
            if (clazz.isArray()) {
                String checkContents = anon_label();
                dup2();
                arraylength();
                swap();
                arraylength();
                if_icmpeq(checkContents);
                pop2();
                goTo(notEquals);
                nop(checkContents);
                process_arrays(clazz, callback);
            } else {
                invoke(MethodConstants.EQUALS);
                ifeq(notEquals);
            }
            nop(end);
        }
    }
    
    protected void throw_exception(Class type, String msg) {
        new_instance(type);
        dup();
        push(msg);
        invoke_constructor(type, new Class[]{ String.class });
        athrow();
    }  

    /**
     * If both objects on the top of the stack are non-null, does nothing.
     * If one is null, or both are null, both are popped off and execution
     * branches to the respective label.
     * @param oneNull label to branch to if only one of the objects is null
     * @param bothNull label to branch to if both of the objects are null
     */
    protected void nullcmp(String oneNull, String bothNull) {
        dup2();
        String nonNull = anon_label();
        String oneNullHelper = anon_label();
        String end = anon_label();
        ifnonnull(nonNull);
        ifnonnull(oneNullHelper);
        pop2();
        goTo(bothNull);

        nop(nonNull);
        ifnull(oneNullHelper);
        goTo(end);

        nop(oneNullHelper);
        pop2();
        goTo(oneNull);

        nop(end);
    }

    protected void generateFactoryMethod(Method method) {
        begin_method(method);
        new_instance_this();
        dup();
        load_args();
        invoke_constructor_this(method.getParameterTypes());
        return_value();
        end_method();
    }
}
