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
import java.security.PrivilegedAction;
import java.util.*;
import org.apache.bcel.generic.*;

/**
 * Abstract base class for code generators
 * @author  baliuka
 */
public abstract class CodeGenerator implements Constants {
    private static final String CONSTRUCTOR_NAME = "<init>";
    private static final String SOURCE_FILE = "<generated>";
    private static final String STATIC_NAME = "<clinit>";
    private static final String FIND_CLASS = "CGLIB$findClass";
    private static final String FIND_CLASS_SIG = getMethodSignature(Class.class, TYPES_STRING);
    private static final String PRIVATE_PREFIX = "PRIVATE_";
    private static final Map primitiveMethods = new HashMap();
    private static final Map primitiveToWrapper = new HashMap();
    private static String debugLocation;

	private final ClassGen cg;
	private final InstructionList il = new InstructionList();
	private final ConstantPoolGen cp;
    private final ClassLoader loader;
	private MethodGen mg;
	private Class returnType;
    private Class superclass;
    private boolean needsFindClass;
    
	private Map branches;  
	private Map labels;
    private int nextPrivateLabel;
    private int nextPrivateLocal;
	
	private Map locals = new HashMap();
    private Map localTypes = new HashMap();
    private int nextLocal;
    private boolean inMethod;

    private LinkedList handlerStack = new LinkedList();
    private LinkedList handlerList = new LinkedList();

    private Map fields = new HashMap();
    private Set staticFields = new HashSet();

	protected CodeGenerator(String className, Class superclass, ClassLoader loader) {
        if (loader == null) {
            throw new IllegalArgumentException("ClassLoader is required");
        }
        this.loader = loader;
        cg = new ClassGen(className, superclass.getName(), SOURCE_FILE, ACC_PUBLIC, null); 
        cp = cg.getConstantPool();
        this.superclass = superclass;
	}

    protected String getClassName() {
        return cg.getClassName();
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
            try {
                generate();
                if (needsFindClass) {
                    generateFindClass();
                }
                String name = cg.getClassName();
                byte[] bytes = cg.getJavaClass().getBytes();

                if (debugLocation != null) {
                    OutputStream out = new FileOutputStream(new File(new File(debugLocation), name + ".cglib"));
                    out.write(bytes);
                    out.close();
                }
                    
                PrivilegedAction action = getDefineClassAction(name, bytes, loader);
                return (Class)java.security.AccessController.doPrivileged(action);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
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

    private static PrivilegedAction getDefineClassAction(final String name,
                                                         final byte[] b,
                                                         final ClassLoader loader) {
        return new PrivilegedAction() {
            public Object run() {
                try {
                    Method m = MethodConstants.DEFINE_CLASS;

                    // protected method invocaton
                    boolean flag = m.isAccessible();
                    m.setAccessible(true);
                    Object[] args = new Object[]{ name, b, new Integer(0), new Integer(b.length) };
                    Class result = (Class)m.invoke(loader, args);
                    m.setAccessible(flag);

                    return result;
                } catch (Exception e) {
                    throw new CodeGenerationException(e);
                }
            }
        };
    }

    static {
        primitiveMethods.put(Boolean.TYPE, MethodConstants.BOOLEAN_VALUE);
        primitiveMethods.put(Character.TYPE, MethodConstants.CHAR_VALUE);
        primitiveMethods.put(Long.TYPE, MethodConstants.LONG_VALUE);
        primitiveMethods.put(Double.TYPE, MethodConstants.DOUBLE_VALUE);
        primitiveMethods.put(Float.TYPE, MethodConstants.FLOAT_VALUE);
        primitiveMethods.put(Short.TYPE, MethodConstants.SHORT_INT_VALUE);
        primitiveMethods.put(Integer.TYPE, MethodConstants.INT_VALUE);
        primitiveMethods.put(Byte.TYPE, MethodConstants.BYTE_INT_VALUE);

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
        cg.addInterface(iface.getName());
    }

    protected void declare_field(int modifiers, Class typeClass, String name) {
        if (fields.containsKey(name)) {
            throw new IllegalArgumentException("Field \"" + name + "\" already exists");
        }
        Type type = getType(typeClass);
        FieldGen fg = new FieldGen(javaModifiersToBcel(modifiers), type, name, cp);
        cg.addField(fg.getField());
        int ref = cp.addFieldref(cg.getClassName(), name, type.getSignature());
        if (Modifier.isStatic(modifiers)) {
            staticFields.add(name);
        }
        fields.put(name, new Integer(ref));
    }

    protected void begin_method(int modifiers, Class returnType, String methodName,
                                Class[] parameterTypes, Class[] exceptionTypes) {
        checkInMethod();
        this.returnType = returnType;
        mg = new MethodGen(javaModifiersToBcel(modifiers),
                           getType(returnType),
                           getTypes(parameterTypes),
                           null,
                           methodName,
                           cg.getClassName(), il, cp);
        if (exceptionTypes != null) {
            for (int i = 0; i < exceptionTypes.length; i++) {
                mg.addException(exceptionTypes[i].getName());
            }
        }
        setNextLocal();
    }

    protected void begin_method(Method method) {
        int modifiers = method.getModifiers();
        modifiers = Modifier.FINAL
            | (modifiers
               & ~Modifier.ABSTRACT
               & ~Modifier.NATIVE
               & ~Modifier.SYNCHRONIZED);
        begin_method(method, modifiers);
    }

    protected void begin_method(Method method, int modifiers) {
        begin_method(modifiers, method.getReturnType(), method.getName(),
                     method.getParameterTypes(), method.getExceptionTypes());
    }
    
    protected void begin_constructor(Constructor constructor) {
        begin_constructor(constructor.getParameterTypes());
    }

    protected void begin_constructor() {
        begin_constructor(TYPES_EMPTY);
    }

    protected void begin_constructor(Class[] parameterTypes) {
        checkInMethod();
        returnType = Void.TYPE;	
        mg = new MethodGen(ACC_PUBLIC, Type.VOID, getTypes(parameterTypes), null,
                           CONSTRUCTOR_NAME, cg.getClassName(), il, cp);
        setNextLocal();
    }

    protected void begin_static() {
        checkInMethod();
        returnType = Void.TYPE;
        mg = new MethodGen(ACC_STATIC, Type.VOID, new Type[0], null,
                           STATIC_NAME, cg.getClassName(), il, cp);
        setNextLocal();
    }

    private void checkInMethod() {
        if (inMethod) {
            throw new IllegalStateException("cannot nest methods");
        }
    }

    private void setNextLocal() {
        nextLocal = 1 + getStackSize(mg.getArgumentTypes());
    }

    // TODO: ensure there are matched pairs and there is no nesting
    protected void end_constructor() {
        end_method();
    }

    protected void end_static() {
        end_method();
    }
    
    protected void end_method() {
        setTargets();
        // TODO: PRINT DEBUG
        // System.out.print(mg.getMethod());
        // System.out.print(mg.getMethod().getCode());
        mg.removeNOPs();
        mg.stripAttributes(true);
        mg.setMaxLocals();
        mg.setMaxStack();
        cg.addMethod(mg.getMethod());
        il.dispose();

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
        Type[] types = mg.getArgumentTypes();
        push(types.length);
        newarray();
        for (int i = 0; i < types.length; i++) {
            dup();
            push(i);
            load_arg(i);
            box(types[i]);
            aastore();
        }
    }

    private InstructionHandle mark() {
        return il.append(new NOP());
    }

    // TODO: use labels instead?
    protected int begin_handler() {
        int ref = handlerList.size();
        InstructionHandle[] ih = new InstructionHandle[]{ mark(), null };
        handlerList.add(ih);
        handlerStack.add(ih);
        return ref;
    }

    protected void end_handler() {
        if (handlerStack.size() == 0) {
            throw new IllegalStateException("mismatched handler boundaries");
        }
        InstructionHandle[] ih = (InstructionHandle[])handlerStack.removeLast();
        ih[1] = il.getEnd().getPrev();
    }

    protected void handle_exception(int ref, Class exceptionType) {
        if (handlerList.size() <= ref) {
            throw new IllegalArgumentException("unknown handler reference: " + ref);
        }
        InstructionHandle[] ih = (InstructionHandle[])handlerList.get(ref);
        if (ih[1] == null) {
            throw new IllegalStateException("end of handler is unset");
        }
        mg.addExceptionHandler(ih[0], ih[1], mark(), (ObjectType)getType(exceptionType));
    }

    private void append(Instruction intruction) {
        il.append(intruction);
    }

    private void append(String label, Instruction instruction) {
        if (label != null) {
            initLabels();
            if (null != labels.put(label, il.append(instruction))) {
                throw new IllegalStateException("duplicated label " + label);
            }
        } else {
            il.append(instruction);
        }
    }

    private void initLabels() {
        if (labels == null) {
            labels = new HashMap();
        }
    }

    private void append(BranchInstruction instruction, String label) {
        if (branches == null) {
            branches = new HashMap();
        }

        List list = (List)branches.get(label);
        if (list == null) {
            branches.put(label, list = new LinkedList());
        }

        list.add(instruction);
        il.append(instruction);
    }
  
    private void setTargets() {
        if (labels != null && branches != null) {
            for( Iterator labelIterator = labels.entrySet().iterator(); labelIterator.hasNext(); ){
                Map.Entry label = (Map.Entry)labelIterator.next();          
                List branchInstructions = (List)branches.get(label.getKey());
                if( branchInstructions != null ){
                    for( Iterator instructions = branchInstructions.iterator(); instructions.hasNext();   ){
                        BranchInstruction instruction = (BranchInstruction)instructions.next();
                        instruction.setTarget( (InstructionHandle)label.getValue() );
                    }
                }
            }     
        }
        if( labels != null){
            labels.clear();
        } 
        if( branches != null ){
            branches.clear();
        }
    }
  
    //-------------- branch istructions:
  
    protected    void ifeq(String label ){ append( new IFEQ(null), label ); }
    protected    void ifne(String label){ append( new IFNE(null), label ); }
    protected    void iflt(String label){ append( new IFLT(null), label ); }
    protected    void ifge(String label){ append( new IFGE(null), label ); }
    protected    void ifgt(String label){ append( new IFGT(null), label ); }
    protected    void ifle(String label){ append( new IFLE(null), label ); }
    protected    void goTo(String label){ append( new GOTO(null), label ); }
    protected    void jsr(String label ){ append( new JSR(null), label ); }
    protected    void ifnull(String label){ append( new IFNULL(null), label ); }
    protected    void ifnonnull(String label){ append( new IFNONNULL(null), label ); }

    protected void if_icmplt(String label) {
        append(new IF_ICMPLT(null), label);
    }

    protected void if_icmpne(String label) {
        append(new IF_ICMPNE(null), label);
    }

    protected void if_icmpeq(String label) {
        append(new IF_ICMPEQ(null), label);
    }
    

    // math
    protected void imul()  { append(new IMUL());  }
    protected void iadd()  { append(new IADD());  }
    protected void lushr() { append(new LUSHR()); }
    protected void lxor()  { append(new LXOR());  }
    protected void ixor()  { append(new IXOR());  }
    protected void l2i()   { append(new L2I());   }
    protected void dcmpg() { append(new DCMPG()); }
    protected void fcmpg() { append(new FCMPG()); }
    protected void lcmp()  { append(new LCMP()); }
  

    protected  void nop(){ append( new NOP()); }
    protected  void nop(String label){ append( label, new NOP() ); }
  
    protected  void aconst_null(){ append( new ACONST_NULL() ); }
  
    protected void push(int i) {
        if (i < 0) {
            append(new LDC(cp.addInteger(i)));
        } else if (i <= 5) { 
            append(new ICONST(i));
        } else if (i <= Byte.MAX_VALUE) { // < or <= ?
            append(new BIPUSH((byte)i));
        } else if (i <= Short.MAX_VALUE) { // < or <= ?
            append(new SIPUSH((short)i));
        } else {
            append(new LDC(cp.addInteger(i)));
        }
    }
    
    protected  void push( long value){
        if (value == 0L || value == 1L) {
            append(new LCONST(value));
        } else {
            append( new LDC( cp.addLong(value) ) );
        }
    }
    protected  void push( float value ){
        if (value == 0f || value == 1f || value == 2f) {
            append(new FCONST(value));
        } else {
            append( new LDC( cp.addFloat(value) ) );
        }
    }
    protected  void push( double value){
        if (value == 0d || value == 1d) {
            append(new DCONST(value));
        } else {
            append( new LDC( cp.addDouble(value) ) ) ;
        }
    }

    protected void push(String value) {
        append(new LDC(cp.addString(value)));
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
            append(new NEWARRAY((BasicType)getType(clazz)));
        } else {
            append(new ANEWARRAY(cp.addClass(clazz.getName())));
        }
    }

    protected void arraylength() {
        append(new ARRAYLENGTH());
    }

    protected void array_load(Class clazz) {
        if (clazz.isPrimitive()) {
            if (clazz.equals(Long.TYPE)) {
                append(new LALOAD());
            } else if (clazz.equals(Double.TYPE)) {
                append(new DALOAD());
            } else if (clazz.equals(Float.TYPE)) {
                append(new FALOAD());
            } else if (clazz.equals(Short.TYPE)) {
                append(new SALOAD());
            } else if (clazz.equals(Character.TYPE)) {
                append(new CALOAD());
            } else if (clazz.equals(Integer.TYPE)) {
                append(new IALOAD());
            } else {
                append(new BALOAD());
            }
        } else {
            append(new AALOAD());
        }
    }

    protected void array_store(Class clazz) {
        if (clazz.isPrimitive()) {
            if (clazz.equals(Long.TYPE)) {
                append(new LASTORE());
            } else if (clazz.equals(Double.TYPE)) {
                append(new DASTORE());
            } else if (clazz.equals(Float.TYPE)) {
                append(new FASTORE());
            } else if (clazz.equals(Short.TYPE)) {
                append(new SASTORE());
            } else if (clazz.equals(Character.TYPE)) {
                append(new CASTORE());
            } else if (clazz.equals(Integer.TYPE)) {
                append(new IASTORE());
            } else {
                append(new BASTORE());
            }
        } else {
            append(new AASTORE());
        }
    }
    
    protected void load_this() {
        append(new ALOAD(0));
    }

    protected void load_class_this() {
        load_class_helper(getClassName());
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
        invoke_static_this(FIND_CLASS, Class.class, TYPES_STRING);
    }

    /**
     * Pushes all of the arguments of the current method onto the stack.
     */
    protected void load_args() {
        load_args(0, mg.getArgumentTypes().length);
    }

    /**
     * Pushes the specified argument of the current method onto the stack.
     * @param index the zero-based index into the argument list
     */
    protected void load_arg(int index) {
        int pos = 1 + skipArgs(index);
        load_local(mg.getArgumentType(index), pos);
    }

    // zero-based (see load_this)
    protected void load_args(int fromArg, int count) {
        int pos = 1 + skipArgs(fromArg);
        for (int i = 0; i < count; i++) {
            Type t = mg.getArgumentType(fromArg + i);
            load_local(t, pos);
            pos += t.getSize();
        }
    }

    private int skipArgs(int numArgs) {
        int amount = 0;
        for (int i = 0; i < numArgs; i++) {
            amount += mg.getArgumentType(i).getSize();
        }
        return amount;
    }

    private void load_local(Type t, int pos) {
        if (t instanceof BasicType) {
            if (t.equals(Type.LONG)) {
                append(new LLOAD(pos));
            } else if (t.equals(Type.DOUBLE)) {
                append(new DLOAD(pos));
            } else if (t.equals(Type.FLOAT)) {
                append(new FLOAD(pos));
            } else {
                append(new ILOAD(pos));
            }
        } else {
            append(new ALOAD(pos));
        }
    }

    private void store_local(Type t, int pos) {
        if (t instanceof BasicType) {
            if (t.equals(Type.LONG)) {
                append(new LSTORE(pos));
            } else if (t.equals(Type.DOUBLE)) {
                append(new DSTORE(pos));
            } else if (t.equals(Type.FLOAT)) {
                append(new FSTORE(pos));
            } else {
                append(new ISTORE(pos));
            }
        } else {
            append(new ASTORE(pos));
        }
    }

    protected void iinc(String local, int amount) {
        append(new IINC(getLocal(local), amount));
    }

    protected void local_type(String name, Class type) {
        localTypes.put(name, getType(type));
    }

    protected void store_local(String name) {
        Integer position = (Integer)locals.get(name);
        if (position == null) {
            position = new Integer(nextLocal);
            locals.put(name, position);
            Type type = (Type)localTypes.get(name);
            nextLocal += (type == null) ? 1 : type.getSize();
        }
        store_local((Type)localTypes.get(name), position.intValue());
    }

    protected void load_local(String name) {
        load_local((Type)localTypes.get(name), getLocal(name));
    }

    private int getLocal(String name) {
        Integer position = (Integer)locals.get(name);
        if (position == null) {
            throw new IllegalArgumentException("unknown local " + name);
        }
        return position.intValue();
    }

    protected    void pop() { append( new POP() ); }
    protected    void pop2() { append( new POP2() ); }
    protected    void dup() {  append( new DUP() ); }
    protected    void dup2() {  append( new DUP2() ); }
    protected    void dup_x1() {  append( new DUP_X1() ); }
    protected    void dup_x2() {  append( new DUP_X2() ); }
    protected    void swap(){ append( new SWAP() ); }
   
    protected    void pop(String  label) { append( label, new POP() ); }
    protected    void dup(String  label) { append( label, new DUP() ); }
    protected    void swap(String label) { append( label, new SWAP() ); }
  
    protected void return_value() {
        if (returnType.isPrimitive()) {
            if (returnType.equals(Void.TYPE)) {
                append(new RETURN());
            } else if (returnType.equals(Long.TYPE)) {
                append(new LRETURN());
            } else if (returnType.equals(Double.TYPE)) {
                append(new DRETURN());
            } else if (returnType.equals(Float.TYPE)) {
                append(new FRETURN());
            } else {
                append(new IRETURN());
            }
        } else {
            append(new ARETURN());
        }
    }
  
    // --------------- Field access ----------------

    private int getFieldref(Field field) {
        return cp.addFieldref(field.getDeclaringClass().getName(),
                              field.getName(),
                              getType(field.getType()).getSignature());
    }

    private int getFieldref(String name) {
        Integer ref = (Integer)fields.get(name);
        if (ref == null) {
            throw new IllegalArgumentException("No field named \"" + name + "\"");
        }
        return ref.intValue();
    }

    private int getInstanceField(String name) {
        int ref = getFieldref(name);
        if (staticFields.contains(name)) {
            throw new IllegalArgumentException("Field \"" + name + "\" is static");
        }
        return ref;
    }

    private int getStaticField(String name) {
        int ref = getFieldref(name);
        if (!staticFields.contains(name)) {
            throw new IllegalArgumentException("Field \"" + name + "\" is not static");
        }
        return ref;
    }

    protected void getfield(String name) {
        append(new GETFIELD(getInstanceField(name)));
    }

    protected void putfield(String name) {
        append(new PUTFIELD(getInstanceField(name)));
    }

    protected void getstatic(String name) {
        append(new GETSTATIC(getStaticField(name)));
    }

    protected void putstatic(String name) {
        append(new PUTSTATIC(getStaticField(name)));
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
            append(new GETSTATIC(getFieldref(field)));
        } else {
            append(new GETFIELD(getFieldref(field)));
        }
    }

    protected void putfield(Field field) {
        if (Modifier.isStatic(field.getModifiers())) {
            append(new PUTSTATIC(getFieldref(field)));
        } else {
            append(new PUTFIELD(getFieldref(field)));
        }
    }

    // --------------- Invoke method ----------------
  
    protected void invoke(Method method) {
        if (method.getDeclaringClass().isInterface()) {
            invoke_interface(method);
        } else if (Modifier.isStatic(method.getModifiers())) {
            invoke_static(method);
        } else {
            invoke_virtual(method);
        }
    }

    protected void super_invoke(Method method) {
        append(new INVOKESPECIAL(addMethodref(method)));
    }

    protected void invoke_virtual_this(String methodName, Class returnType, Class[] parameterTypes) {
        append(new INVOKEVIRTUAL(cp.addMethodref(cg.getClassName(),
                                                 methodName,
                                                 getMethodSignature(returnType, parameterTypes))));
    }

    protected void invoke_static_this(String methodName, Class returnType, Class[] parameterTypes) {
        append(new INVOKESTATIC(cp.addMethodref(cg.getClassName(),
                                                methodName,
                                                getMethodSignature(returnType, parameterTypes))));
    }

    protected void super_invoke() {
        append(new INVOKESPECIAL(cp.addMethodref(cg.getSuperclassName(), mg.getName(), mg.getSignature())));
    }
   
    protected void invoke_constructor(Class type) {
        invoke_constructor(type, TYPES_EMPTY);
    }

    protected void invoke_constructor(Class type, Class[] parameterTypes) {
        invoke_constructor_helper(type.getName(), parameterTypes);
    }

    private void invoke_interface(Method method) {
        int ref = cp.addInterfaceMethodref(method.getDeclaringClass().getName(),
                                           method.getName(),
                                           getMethodSignature(method));
        append(new INVOKEINTERFACE(ref, 1 + getStackSize(method.getParameterTypes())));
    }

    private static int getStackSize(Class clazz) {
        return getType(clazz).getSize();
    }

    private static int getStackSize(Class[] classes) {
        int size = 0;
        for (int i = 0; i < classes.length; i++) {
            size += getStackSize(classes[i]);
        }
        return size;
    }

    private static int getStackSize(Type[] types) {
        int size = 0;
        for (int i = 0; i < types.length; i++) {
            size += types[i].getSize();
        }
        return size;
    }

    // this is taken from BCEL CVS: Type.getType(Class)
    public static Type getType(java.lang.Class cl) {
        if (cl == null) {
            throw new IllegalArgumentException("Class must not be null");
        }
        if (cl.isArray()) {
            return Type.getType(cl.getName());
        } else if (cl.isPrimitive()) {
            if (cl == Integer.TYPE) {
                return Type.INT;
            } else if (cl == Void.TYPE) {
                return Type.VOID;
            } else if (cl == Double.TYPE) {
                return Type.DOUBLE;
            } else if (cl == Float.TYPE) {
                return Type.FLOAT;
            } else if (cl == Boolean.TYPE) {
                return Type.BOOLEAN;
            } else if (cl == Byte.TYPE) {
                return Type.BYTE;
            } else if (cl == Short.TYPE) {
                return Type.SHORT;
            } else if (cl == Byte.TYPE) {
                return Type.BYTE;
            } else if (cl == Long.TYPE) {
                return Type.LONG;
            } else if (cl == Character.TYPE) {
                return Type.CHAR;
            } else {
                throw new IllegalStateException("Ooops, what primitive type is " + cl);
            }
        } else { // "Real" class
            return new ObjectType(cl.getName());
        }
    }

    private static Type[] getTypes(Class[] classes) {
        Type[] types = new Type[classes.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = getType(classes[i]);
        }
        return types;
    }

    protected static String getMethodSignature(Method method) {
        return getMethodSignature(method.getReturnType(), method.getParameterTypes());
    }

    private static String getMethodSignature(Class returnType, Class[] parameterTypes) {
        return Type.getMethodSignature(getType(returnType), getTypes(parameterTypes));
    }

    private void invoke_virtual(Method method) {
        append(new INVOKEVIRTUAL(addMethodref(method)));
    }

    private void invoke_static(Method method) {
        append(new INVOKESTATIC(addMethodref(method)));
    }

    private int addMethodref(Method method) {
        return cp.addMethodref(method.getDeclaringClass().getName(),
                               method.getName(),
                               getMethodSignature(method));
    }

    protected void super_invoke_constructor(Constructor constructor) {
        super_invoke_constructor(constructor.getParameterTypes());
    }

    protected void super_invoke_constructor() {
        invoke_constructor_helper(cg.getSuperclassName(), TYPES_EMPTY);
    }

    protected void super_invoke_constructor(Class[] parameterTypes) {
        invoke_constructor_helper(cg.getSuperclassName(), parameterTypes);
    }

    protected void invoke_constructor_this() {
        invoke_constructor_this(TYPES_EMPTY);
    }

    protected void invoke_constructor_this(Class[] parameterTypes) {
        invoke_constructor_helper(cg.getClassName(), parameterTypes);
    }

    private void invoke_constructor_helper(String className, Class[] parameterTypes) {
        int ref = cp.addMethodref(className,
                                  CONSTRUCTOR_NAME,
                                  getMethodSignature(Void.TYPE, parameterTypes));
        append(new INVOKESPECIAL(ref));
    }

    protected void new_instance_this() {
        append(new NEW(cp.addClass(cg.getClassName())));
    }
  
    protected void new_instance(Class clazz) {
        append(new NEW(cp.addClass(clazz.getName())));
    }
 
    protected void aaload( int index ){
        push(index);
        aaload();
    }
    
    protected void aaload() {
        append( new AALOAD() );
    }

    protected void aastore() {
        append(new AASTORE());
    }
  
    protected   void athrow(){ append( new ATHROW() ); }
    protected   void athrow(String label){ append(label,new ATHROW() ); }
  
    protected String newLabel() {
        initLabels();
        String label;
        do {
            label = PRIVATE_PREFIX + nextPrivateLabel++;
        } while (labels.containsKey(label));
        return label;
    }

    protected String newLocal() {
        String local;
        do {
            local = PRIVATE_PREFIX + nextPrivateLocal++;
        } while (locals.containsKey(local));
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
                String nonNull = newLabel();
                String end = newLabel();
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

    private Class getPrimitiveClass(BasicType type) {
        switch (type.getType()) {
        case T_BYTE:
            return Byte.TYPE;
        case T_BOOLEAN:
            return Boolean.TYPE;
        case T_CHAR:
            return Character.TYPE;
        case T_LONG:
            return Long.TYPE;
        case T_DOUBLE:
            return Double.TYPE;
        case T_FLOAT:
            return Float.TYPE;
        case T_SHORT:
            return Short.TYPE;
        case T_INT:
            return Integer.TYPE;
        case T_VOID:
            return Void.TYPE;
        default:
            return null; // impossible
        }
    }

    private void box(Type type) {
        if (type instanceof BasicType) {
            box(getPrimitiveClass((BasicType)type));
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
        append(new CHECKCAST(cp.addClass(cg.getClassName())));
    }

    protected void checkcast(Class clazz) {
        Type type = getType(clazz);
        if (clazz.isArray()) {
            append(new CHECKCAST(cp.addArrayClass((ArrayType)type)));
        } else if (clazz.equals(Object.class)) {
            // ignore
        } else {
            append(new CHECKCAST(cp.addClass((ObjectType)type)));
        }
    }
   
    protected void instance_of(Class clazz) { 
        append(new INSTANCEOF(cp.addClass(clazz.getName())));
    }

    protected void instance_of_this() {
        append(new INSTANCEOF(cp.addClass(cg.getClassName())));
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

    protected void generateNullConstructor() {
        begin_constructor();
        load_this();
        super_invoke_constructor();
        return_value();
        end_constructor();
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
                     TYPES_STRING,
                     null);
        int eh = begin_handler();
        load_this();
        invoke(MethodConstants.FOR_NAME);
        return_value();
        end_handler();

        handle_exception(eh, ClassNotFoundException.class);
        invoke(MethodConstants.THROWABLE_GET_MESSAGE);
        new_instance(NoClassDefFoundError.class);
        dup_x1();
        swap();
        invoke_constructor(NoClassDefFoundError.class, TYPES_STRING);
        athrow();
        end_method();
    }

    protected boolean isVisible(Member member, Package packageName) {
        int mod = member.getModifiers();
        if (Modifier.isPrivate(mod)) {
            return false;
        }
        if (Modifier.isProtected(mod) || Modifier.isPublic(mod)) {
            return true;
        }
        
        Package p = member.getDeclaringClass().getPackage();
        return ( null == packageName ? p == null : 
                                       packageName.equals(p) ); 
                                                    
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
        String array = newLocal();
        String loopvar = newLocal();
        String loopbody = newLabel();
        String checkloop = newLabel();
        local_type(loopvar, Integer.TYPE);
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
        String array1 = newLocal();
        String array2 = newLocal();
        String loopvar = newLocal();
        String loopbody = newLabel();
        String checkloop = newLabel();
        local_type(loopvar, Integer.TYPE);
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
            String end = newLabel();
            nullcmp(notEquals, end);
            if (clazz.isArray()) {
                String checkContents = newLabel();
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
    
  protected void throwException(Class type, String msg){
      
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
        String nonNull = newLabel();
        String oneNullHelper = newLabel();
        String end = newLabel();
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
}
