/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * 4. The names "Apache Cocoon" and "Apache Software Foundation" must
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
package net.sf.cglib.proxy;

import java.io.*;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
/**
 *
 * this code returns Enhanced Vector to intercept  all methods for tracing
 *   <pre>
 *         java.util.Vector vector = (java.util.Vector)Enhancer.enhance(
 *        java.util.Vector.<b>class</b>,
 *        new Class[]{java.util.List.<b>class</b>},
 *
 *        new MethodInterceptor(){
 *
 *
 *            <b>public boolean invokeSuper</b>( Object obj,java.lang.reflect.Method method,
 *            Object args[])
 *            throws java.lang.Throwable{
 *                return true;
 *            }
 *
 *
 *        <b>public</b> Object <b>afterReturn</b>(  Object obj,     java.lang.reflect.Method method,
 *        Object args[],
 *        boolean invokedSuper, Object retValFromSuper,
 *        java.lang.Throwable e )throws java.lang.Throwable{
 *            System.out.println(method);
 *            return retValFromSuper;//return the same as supper
 *        }
 *
 *    });
 * </pre>
 *@author     Juozas Baliuka <a href="mailto:baliuka@mwm.lt">
 *      baliuka@mwm.lt</a>
 *@version    $Id: Enhancer.java,v 1.16 2002/09/30 06:11:51 baliuka Exp $
 */
public class Enhancer implements org.apache.bcel.Constants {
    
    
    
    static final String INTERCEPTOR_CLASS_NAME = MethodInterceptor.class.getName();
    static final ObjectType BOOLEAN_OBJECT =
    new ObjectType(Boolean.class.getName());
    static final ObjectType INTEGER_OBJECT =
    new ObjectType(Integer.class.getName());
    static final ObjectType CHARACTER_OBJECT =
    new ObjectType(Character.class.getName());
    static final ObjectType BYTE_OBJECT = new ObjectType(Byte.class.getName());
    static final ObjectType SHORT_OBJECT = new ObjectType(Short.class.getName());
    static final ObjectType LONG_OBJECT = new ObjectType(Long.class.getName());
    static final ObjectType DOUBLE_OBJECT = new ObjectType(Double.class.getName());
    static final ObjectType FLOAT_OBJECT = new ObjectType(Float.class.getName());
    static final ObjectType METHOD_OBJECT =
    new ObjectType(java.lang.reflect.Method.class.getName());
    static final ObjectType CLASS_OBJECT = new ObjectType(Class.class.getName());
    static final ObjectType NUMBER_OBJECT = new ObjectType(Number.class.getName());
    static final String CONSTRUCTOR_NAME = "<init>";
    static final String FIELD_NAME = "h";
    static final String SOURCE_FILE = "<generated>";
    static final String CLASS_SUFIX = "$$EnhancedByCGLIB$$";
    static final String CLASS_PREFIX = "net.sf.cglib.proxy";
    static int index = 0;
    static java.util.Map factories = new java.util.HashMap();
    
    private static int addAfterConstructionRef(ConstantPoolGen cp) {
        return cp.addMethodref(
        Enhancer.class.getName(),
        "handleConstruction",
        "(Ljava/lang/Object;[Ljava/lang/Object;)V");
    }
    
    private static int addNewInstanceRef(ConstantPoolGen cp,String name) {
        return cp.addMethodref(
        name,
        "<init>",
        "(L"+ INTERCEPTOR_CLASS_NAME.replace('.','/') +";)V");
    }
    
    private static int addWriteReplace(ConstantPoolGen cp){
        return cp.addMethodref( Enhancer.InternalReplace.class.getName(),
        "writeReplace",
        "(Ljava/lang/Object;)Ljava/lang/Object;");
    }
    
    private static int addAfterRef(ConstantPoolGen cp) {
        return cp.addInterfaceMethodref(
        INTERCEPTOR_CLASS_NAME,
        "afterReturn",
        "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;ZLjava/lang/Object;Ljava/lang/Throwable;)Ljava/lang/Object;");
    }
    private static int addInvokeSupperRef(ConstantPoolGen cp) {
        return cp.addInterfaceMethodref(
        INTERCEPTOR_CLASS_NAME,
        "invokeSuper",
        "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Z");
    }
    private static java.util.List costructionHandlers = new java.util.Vector();
    private static java.util.Map cache = new java.util.WeakHashMap();
    
    
    private Enhancer() {}
    
    public static MethodInterceptor getMethodInterceptor(Object enhanced){
        try{
            return (MethodInterceptor) enhanced.getClass().getField(FIELD_NAME).get(
            enhanced);
        }catch( NoSuchFieldException nsfe){
            throw new NoSuchFieldError(enhanced + " is not enhanced :" + nsfe.getMessage());
        }catch( java.lang.IllegalAccessException iae ){
            throw new IllegalAccessError(enhanced.getClass().getName() + ":" + iae.getMessage());
        }
    }
    
    
    
    /**
     *  implemented as
     * return enhance(cls,interfaces,ih, null);
     */
    public static Object enhance(
    Class cls,
    Class interfaces[],
    MethodInterceptor ih)
    throws Throwable {
        
        return enhance(
        cls,
        interfaces,
        ih,
        null);
    }
    /** enhances public not final class,
     * source class must have public no args constructor.
     * Code generated for protected and public not final methods,
     * package scope methods supported from source class package.
     * Defines new class in  source class package, if it not java*.
     * @param cls class to extend, uses Object.class if null
     * @param interfaces interfaces to implement, can be null
     * @param ih valid interceptor implementation
     * @param loader classloater for enhanced class, uses "current" if null
     * @throws Throwable on error
     * @return instanse of enhanced  class
     */
    public synchronized static Object enhance(
    Class cls,
    Class interfaces[],
    MethodInterceptor ih,
    ClassLoader loader)
    throws Throwable {
        if (cls == null) {
            cls = Object.class;
        }
        if( loader == null ){
            loader = Enhancer.class.getClassLoader();
        }
        StringBuffer keyBuff = new StringBuffer(cls.getName() + ";");
        if(interfaces != null){
            for(int i = 0; i< interfaces.length; i++ ){
                keyBuff
                .append(interfaces[i].getName() + ";");
            }
        }
        String key = keyBuff.toString();
        
        java.util.Map map = (java.util.Map) cache.get(loader);
        if ( map == null ) {
            map = new java.util.Hashtable();
            cache.put(loader, map);
        }
        Class result = (Class) map.get(key);
        
        
        
        if (result == null) {
            String class_name = cls.getName() + CLASS_SUFIX;
            if (class_name.startsWith("java")) {
                class_name = CLASS_PREFIX + class_name;
            }
            class_name += index++;
            JavaClass clazz = enhance(cls, class_name, interfaces );
            byte b[] = clazz.getBytes();
            java.lang.reflect.Method m =
            ClassLoader.class.getDeclaredMethod(
            "defineClass",
            new Class[] { String.class, byte[].class, int.class, int.class });
            // protected method invocaton
            boolean flag = m.isAccessible();
            m.setAccessible(true);
            result =
            (Class) m.invoke(
            loader,
            new Object[] { clazz.getClassName(), b, new Integer(0), new Integer(b.length)});
            m.setAccessible(flag);
            map.put(key, result);
        }
        
        Factory factory =  (Factory)factories.get(result);
        if( factory == null ){
            factory = (Factory)result.getConstructor(
            new Class[] {
                Class.forName(MethodInterceptor.class.getName(), true, loader)
            }).newInstance(new Object[] { null });
            factories.put(result,factory);
            
        }
        return factory.newInstance(ih);
        
    }
    
    private static void addConstructor(ClassGen cg) throws Throwable {
        
        
        //single arg constructor
        String parentClass = cg.getSuperclassName();
        InstructionFactory factory = new InstructionFactory(cg);
        ConstantPoolGen cp = cg.getConstantPool(); // cg creates constant pool
        InstructionList il = new InstructionList();
        MethodGen costructor = new MethodGen(ACC_PUBLIC, // access flags
        Type.VOID, // return type
        new Type[] { // argument types
            new ObjectType(INTERCEPTOR_CLASS_NAME)}, null, // arg names
            CONSTRUCTOR_NAME, cg.getClassName(), il, cp);
            
            
            il.append(new ALOAD(0));
            il.append(
            factory.createInvoke(
            parentClass,
            CONSTRUCTOR_NAME,
            Type.VOID,
            new Type[] {},
            INVOKESPECIAL));
            
            
            il.append(new ALOAD(0));
            il.append(new ALOAD(1));
            il.append(
            factory.createFieldAccess(
            cg.getClassName(),
            FIELD_NAME,
            new ObjectType(INTERCEPTOR_CLASS_NAME),
            PUTFIELD));
            il.append(new RETURN());
            cg.addMethod(getMethod(costructor));
            
            //factory sometimes usefull and has meaning for performance
            
            il = new InstructionList();
            MethodGen newInstance = toMethodGen(
            Factory.class.getMethod("newInstance",
            new Class[]{ MethodInterceptor.class } ),
            cg.getClassName(),
            il,
            cp
            );
            il.append( new NEW(cp.addClass( new ObjectType(cg.getClassName()) )) );
            il.append( new DUP());
            il.append( new ALOAD(1) );
            il.append( new INVOKESPECIAL( addNewInstanceRef(cp, cg.getClassName()) ) ) ;
            il.append( new ARETURN());
            cg.addMethod(getMethod(newInstance));
            
            //serialization support
            il = new InstructionList();
            MethodGen writeReplace = new MethodGen(ACC_PRIVATE, // access flags
            Type.OBJECT, // return type
            new Type[] {}, null, // arg names
            "writeReplace", cg.getClassName(), il, cp);
            
            
            il.append(new ALOAD(0));
            il.append(new INVOKESTATIC( addWriteReplace(cp) ) );
            il.append(new  ARETURN());
            
            cg.addMethod(getMethod(writeReplace));
            
            
            
    }
    
    
    private static void addHandlerField(ClassGen cg) {
        // TODO: ACC_PRIVATE
        ConstantPoolGen cp = cg.getConstantPool();
        FieldGen fg =
        new FieldGen(ACC_PUBLIC, new ObjectType(INTERCEPTOR_CLASS_NAME), FIELD_NAME, cp);
        cg.addField(fg.getField());
    }
    
    private static ClassGen getClassGen(
    String class_name,
    Class parentClass,
    Class[] interfaces) {
        ClassGen gen =
        new ClassGen(class_name, parentClass.getName(), SOURCE_FILE,
        ACC_PUBLIC | ACC_FINAL , null );
        if (interfaces != null) {
            for (int i = 0; i < interfaces.length; i++) {
                gen.addInterface(interfaces[i].getName());
            }
        }
        gen.addInterface( Factory.class.getName() );
        return gen;
    }
    
    private static JavaClass enhance(
    Class parentClass,
    String class_name,
    Class interfaces[]
    ) throws Throwable {
        java.util.HashMap methodTable = new java.util.HashMap();
        ClassGen cg = getClassGen(class_name, parentClass, interfaces);
        ConstantPoolGen cp = cg.getConstantPool(); // cg creates constant pool
        addHandlerField(cg);
        addConstructor(cg);
        int after = addAfterRef(cp);
        int invokeSuper = addInvokeSupperRef(cp);
        java.util.Set methodSet = new java.util.HashSet();
        
        java.util.List allMethods = new java.util.ArrayList();
        
        String packageName = getPackageName( class_name );
        
        // Order is very important: must add parentClass, then
        // its superclass chain, then each interface and
        // its superinterfaces.
        addDeclaredMethods(allMethods, parentClass);
        if (interfaces != null) {
            for (int j = 0; j < interfaces.length; j++) {
                addDeclaredMethods(allMethods, interfaces[j]);
            }
        }
        
        for (java.util.Iterator i = allMethods.iterator(); i.hasNext(); ) {
            java.lang.reflect.Method m = (java.lang.reflect.Method) i.next();
            int mod = m.getModifiers();
            if (!java.lang.reflect.Modifier.isStatic(mod)
            && !java.lang.reflect.Modifier.isFinal(mod)
            &&  isVisible( m , packageName )   ) {
                
                methodSet.add(new MethodWrapper(m));
            }
        }
        
        
        int cntr = 0;
        for (java.util.Iterator i = methodSet.iterator(); i.hasNext();) {
            java.lang.reflect.Method method = ((MethodWrapper) i.next()).method;
            String fieldName = "METHOD_" + (cntr++);
            cg.addMethod(generateMethod(method, fieldName, cg,  after, invokeSuper));
            methodTable.put(fieldName, method);
        }
        
        generateClInit(cg, cp, methodTable);
        
        JavaClass jcl = cg.getJavaClass();
        return jcl;
    }
    
    private static String getPackageName( String className  )
    throws Throwable{
        
        int index = className.lastIndexOf('.');
        if( index == -1 ){
          return "";
        }
        return className.substring( 0, index );
        
    }
    
    private static boolean isVisible(java.lang.reflect.Method m ,
    String packageName )throws Throwable{
        
        
        int mod = m.getModifiers();
        
        if( java.lang.reflect.Modifier.isPrivate( mod )){
            
            return false;
        }
        
        if( java.lang.reflect.Modifier.isProtected( mod ) ||
        java.lang.reflect.Modifier.isPublic( mod ) ){
            
            return true;
        }
        
        //package scope:
        
        return getPackageName( m.getDeclaringClass().getName()
        ).equals( packageName );
    }
    
    
    private static void addDeclaredMethods(java.util.List methodList, Class clazz) {
        methodList.addAll(java.util.Arrays.asList(clazz.getDeclaredMethods()));
        if (clazz.isInterface()) {
            Class[] interfaces = clazz.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                addDeclaredMethods(methodList, interfaces[i]);
            }
        } else {
            Class superclazz = clazz.getSuperclass();
            if (superclazz != null) {
                addDeclaredMethods(methodList, superclazz);
            }
        }
    }
    
    private static void addMethodField(String fieldName, ClassGen cg) {
        ConstantPoolGen cp = cg.getConstantPool();
        FieldGen fg =
        new FieldGen( ACC_FINAL | ACC_STATIC, METHOD_OBJECT, fieldName, cp );
        cg.addField(fg.getField());
    }
    
    
    private static int createArgArray(
    InstructionList il,
    InstructionFactory factory,
    ConstantPoolGen cp,
    Type[] args) {
        
        int argCount = args.length;
        if (argCount > 5)
            il.append(new BIPUSH((byte) argCount));
        else
            il.append(new ICONST((byte) argCount));
        il.append(new ANEWARRAY(cp.addClass(Type.OBJECT)));
        int load = 1;
        for (int i = 0; i < argCount; i++) {
            il.append(new DUP());
            if (i > 5)
                il.append(new BIPUSH((byte) i));
            else
                il.append(new ICONST((byte) i));
            if (args[i] instanceof BasicType) {
                if (args[i].equals(Type.BOOLEAN)) {
                    il.append(new NEW(cp.addClass(BOOLEAN_OBJECT)));
                    il.append(new DUP());
                    il.append(new ILOAD(load++));
                    il.append(
                    new INVOKESPECIAL(
                    cp.addMethodref(Boolean.class.getName(), CONSTRUCTOR_NAME, "(Z)V")));
                } else if (args[i].equals(Type.INT)) {
                    il.append(new NEW(cp.addClass(INTEGER_OBJECT)));
                    il.append(new DUP());
                    il.append(new ILOAD(load++));
                    il.append(
                    new INVOKESPECIAL(
                    cp.addMethodref(Integer.class.getName(), CONSTRUCTOR_NAME, "(I)V")));
                } else if (args[i].equals(Type.CHAR)) {
                    il.append(new NEW(cp.addClass(CHARACTER_OBJECT)));
                    il.append(new DUP());
                    il.append(new ILOAD(load++));
                    il.append(
                    new INVOKESPECIAL(
                    cp.addMethodref(Character.class.getName(), CONSTRUCTOR_NAME, "(C)V")));
                } else if (args[i].equals(Type.BYTE)) {
                    il.append(new NEW(cp.addClass(BYTE_OBJECT)));
                    il.append(new DUP());
                    il.append(new ILOAD(load++));
                    il.append(
                    new INVOKESPECIAL(
                    cp.addMethodref(Byte.class.getName(), CONSTRUCTOR_NAME, "(B)V")));
                } else if (args[i].equals(Type.SHORT)) {
                    il.append(new NEW(cp.addClass(SHORT_OBJECT)));
                    il.append(new DUP());
                    il.append(new ILOAD(load++));
                    il.append(
                    new INVOKESPECIAL(
                    cp.addMethodref(Short.class.getName(), CONSTRUCTOR_NAME, "(S)V")));
                } else if (args[i].equals(Type.LONG)) {
                    il.append(new NEW(cp.addClass(LONG_OBJECT)));
                    il.append(new DUP());
                    il.append(new LLOAD(load));
                    load += 2;
                    il.append(
                    new INVOKESPECIAL(
                    cp.addMethodref(Long.class.getName(), CONSTRUCTOR_NAME, "(J)V")));
                } else if (args[i].equals(Type.DOUBLE)) {
                    il.append(new NEW(cp.addClass(DOUBLE_OBJECT)));
                    il.append(new DUP());
                    il.append(new DLOAD(load));
                    load += 2;
                    il.append(
                    new INVOKESPECIAL(
                    cp.addMethodref(Double.class.getName(), CONSTRUCTOR_NAME, "(D)V")));
                } else if (args[i].equals(Type.FLOAT)) {
                    il.append(new NEW(cp.addClass(FLOAT_OBJECT)));
                    il.append(new DUP());
                    il.append(new FLOAD(load++));
                    il.append(
                    new INVOKESPECIAL(
                    cp.addMethodref(Float.class.getName(), CONSTRUCTOR_NAME, "(F)V")));
                }
                il.append(new AASTORE());
            } else {
                il.append(new ALOAD(load++));
                il.append(new AASTORE());
            }
        }
        return load;
    }
    
    private static Method getMethod(MethodGen mg) {
        mg.stripAttributes(true);
        mg.setMaxLocals();
        mg.setMaxStack();
        return mg.getMethod();
    }
    
    private static InstructionHandle generateReturnValue(
    InstructionList il,
    InstructionFactory factory,
    ConstantPoolGen cp,
    Type returnType,
    int stack) {
        
        
        if (returnType.equals(Type.VOID)) {
            return il.append(new RETURN());
        }
        il.append(new ASTORE(stack));
        il.append(new ALOAD(stack));
        if ((returnType instanceof ObjectType) || ( returnType instanceof ArrayType) ) {
            if (returnType instanceof ArrayType){
                il.append(new CHECKCAST(cp.addArrayClass((ArrayType)returnType)));
                return il.append(new ARETURN());
            }
            if (!returnType.equals(Type.OBJECT)){
                il.append(new CHECKCAST(cp.addClass((ObjectType) returnType)));
                return il.append(new ARETURN());
            }else {
                return il.append(new ARETURN());
            }
            
        }
        if (returnType instanceof BasicType) {
            if (returnType.equals(Type.BOOLEAN)) {
                IFNONNULL ifNNull = new IFNONNULL(null);
                il.append(ifNNull);
                il.append(new ICONST(0) );
                il.append(new IRETURN());
                ifNNull.setTarget(il.append(new ALOAD(stack)));
                il.append(new CHECKCAST(cp.addClass(BOOLEAN_OBJECT)));
                il.append(
                factory.createInvoke(
                Boolean.class.getName(),
                "booleanValue",
                Type.BOOLEAN,
                new Type[] {},
                INVOKEVIRTUAL));
                return il.append(new IRETURN());
            } else if (returnType.equals(Type.CHAR)) {
                IFNONNULL ifNNull = new IFNONNULL(null);
                il.append(ifNNull);
                il.append(new ICONST(0) );
                il.append(new IRETURN());
                ifNNull.setTarget(il.append(new ALOAD(stack)));
                il.append(new CHECKCAST(cp.addClass(CHARACTER_OBJECT)));
                il.append(
                factory.createInvoke(
                Character.class.getName(),
                "charValue",
                Type.CHAR,
                new Type[] {},
                INVOKEVIRTUAL));
                return il.append(new IRETURN());
            } else if (returnType.equals(Type.LONG)) {
                IFNONNULL ifNNull = new IFNONNULL(null);
                il.append(ifNNull);
                il.append(new LCONST(0) );
                il.append(new LRETURN());
                ifNNull.setTarget(il.append(new ALOAD(stack)));
                il.append(new CHECKCAST(cp.addClass(NUMBER_OBJECT)));
                il.append(
                factory.createInvoke(
                Number.class.getName(),
                "longValue",
                Type.LONG,
                new Type[] {},
                INVOKEVIRTUAL));
                return il.append(new LRETURN());
            } else if (returnType.equals(Type.DOUBLE)) {
                IFNONNULL ifNNull = new IFNONNULL(null);
                il.append(ifNNull);
                il.append(new DCONST(0) );
                il.append(new DRETURN());
                ifNNull.setTarget(il.append(new ALOAD(stack)));
                
                il.append(new CHECKCAST(cp.addClass(NUMBER_OBJECT)));
                il.append(
                factory.createInvoke(
                Number.class.getName(),
                "doubleValue",
                Type.DOUBLE,
                new Type[] {},
                INVOKEVIRTUAL));
                return il.append(new DRETURN());
            } else if (returnType.equals(Type.FLOAT)) {
                IFNONNULL ifNNull = new IFNONNULL(null);
                il.append(ifNNull);
                il.append(new FCONST(0) );
                il.append(new FRETURN());
                ifNNull.setTarget(il.append(new ALOAD(stack)));
                il.append(new CHECKCAST(cp.addClass(NUMBER_OBJECT)));
                il.append(
                factory.createInvoke(
                java.lang.Number.class.getName(),
                "floatValue",
                Type.FLOAT,
                new Type[] {},
                INVOKEVIRTUAL));
                return il.append(new FRETURN());
            } else {
                IFNONNULL ifNNull = new IFNONNULL(null);
                il.append(ifNNull);
                il.append(new ICONST(0) );
                il.append(new IRETURN());
                ifNNull.setTarget(il.append(new ALOAD(stack)));
                il.append(new CHECKCAST(cp.addClass(NUMBER_OBJECT)));
                il.append(
                factory.createInvoke(
                Number.class.getName(),
                "intValue",
                Type.INT,
                new Type[] {},
                INVOKEVIRTUAL));
                return il.append(new IRETURN());
            }
        }
        throw new java.lang.InternalError(returnType.toString());
    }
    
    
    private static Instruction newWrapper(Type type, ConstantPoolGen cp) {
        
        if (type instanceof BasicType) {
            if (type.equals(Type.BOOLEAN)) {
                return new NEW(cp.addClass(BOOLEAN_OBJECT));
            } else if (type.equals(Type.INT)) {
                return new NEW(cp.addClass(INTEGER_OBJECT));
            } else if (type.equals(Type.CHAR)) {
                return new NEW(cp.addClass(CHARACTER_OBJECT));
            } else if (type.equals(Type.BYTE)) {
                return new NEW(cp.addClass(BYTE_OBJECT));
            } else if (type.equals(Type.SHORT)) {
                return new NEW(cp.addClass(SHORT_OBJECT));
            } else if (type.equals(Type.LONG)) {
                return new NEW(cp.addClass(LONG_OBJECT));
            } else if (type.equals(Type.DOUBLE)) {
                return new NEW(cp.addClass(DOUBLE_OBJECT));
            } else if (type.equals(Type.FLOAT)) {
                return new NEW(cp.addClass(FLOAT_OBJECT));
            }
        }
        return null;
    }
    
    private static Instruction initWrapper(Type type, ConstantPoolGen cp) {
        
        if (type instanceof BasicType) {
            if (type.equals(Type.BOOLEAN)) {
                return new INVOKESPECIAL(
                cp.addMethodref(Boolean.class.getName(), CONSTRUCTOR_NAME, "(Z)V"));
            } else if (type.equals(Type.INT)) {
                return new INVOKESPECIAL(
                cp.addMethodref(Integer.class.getName(), CONSTRUCTOR_NAME, "(I)V"));
            } else if (type.equals(Type.CHAR)) {
                return new INVOKESPECIAL(
                cp.addMethodref(Character.class.getName(), CONSTRUCTOR_NAME, "(C)V"));
            } else if (type.equals(Type.BYTE)) {
                return new INVOKESPECIAL(
                cp.addMethodref(Byte.class.getName(), CONSTRUCTOR_NAME, "(B)V"));
            } else if (type.equals(Type.SHORT)) {
                return new INVOKESPECIAL(
                cp.addMethodref(Short.class.getName(), CONSTRUCTOR_NAME, "(S)V"));
            } else if (type.equals(Type.LONG)) {
                return new INVOKESPECIAL(
                cp.addMethodref(Long.class.getName(), CONSTRUCTOR_NAME, "(J)V"));
            } else if (type.equals(Type.DOUBLE)) {
                return new INVOKESPECIAL(
                cp.addMethodref(Double.class.getName(), CONSTRUCTOR_NAME, "(D)V"));
            } else if (type.equals(Type.FLOAT)) {
                return new INVOKESPECIAL(
                cp.addMethodref(Float.class.getName(), CONSTRUCTOR_NAME, "(F)V"));
            }
        }
        throw new InternalError(type.toString());
    }
    
    private static int loadArg(InstructionList il, Type t, int index, int pos) {
        
        if (t instanceof BasicType) {
            if (t.equals(Type.LONG)) {
                il.append(new LLOAD(pos));
                pos += 2;
                return pos;
            } else if (t.equals(Type.DOUBLE)) {
                il.append(new DLOAD(pos));
                pos += 2;
                return pos;
            } else if (t.equals(Type.FLOAT)) {
                il.append(new FLOAD(pos));
                return ++pos;
            } else {
                il.append(new ILOAD(pos));
                return ++pos;
            }
        } else {
            il.append(new ALOAD(pos));
            return ++pos;
        }
    }
    
    private static Type[] toType(Class cls[]) {
        
        Type tp[] = new Type[cls.length];
        for (int i = 0; i < cls.length; i++) {
            tp[i] = toType(cls[i]);
        }
        return tp;
    }
    
    private static Type toType(Class cls) {
        
        if (cls.equals(void.class)) {
            return Type.VOID;
        }
        if (cls.isPrimitive()) {
            if (int.class.equals(cls)) {
                return Type.INT;
            } else if (char.class.equals(cls)) {
                return Type.CHAR;
            } else if (short.class.equals(cls)) {
                return Type.SHORT;
            } else if (byte.class.equals(cls)) {
                return Type.BYTE;
            } else if (long.class.equals(cls)) {
                return Type.LONG;
            } else if (float.class.equals(cls)) {
                return Type.FLOAT;
            } else if (double.class.equals(cls)) {
                return Type.DOUBLE;
            } else if (boolean.class.equals(cls)) {
                return Type.BOOLEAN;
            }
        } else if (cls.isArray()) {
            return new ArrayType( toType(cls.getComponentType()),cls.getName().lastIndexOf('[') + 1);
        } else
            return new ObjectType(cls.getName());
        throw new java.lang.InternalError(cls.getName());
    }
    
    private static void invokeSuper(ClassGen cg, MethodGen mg, Type args[]) {
        
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionList il = mg.getInstructionList();
        int pos = 1;
        il.append(new ALOAD(0)); //this
        for (int i = 0; i < args.length; i++) { //load args to stack
            pos = loadArg(il, args[i], i, pos);
        }
        il.append(
        new INVOKESPECIAL(
        cp.addMethodref(cg.getSuperclassName(), mg.getName(), mg.getSignature())));
    }
    
    private static void invokeStatic( String className, ClassGen cg, MethodGen mg, Type args[]) {
        
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionList il = mg.getInstructionList();
        int pos = 1;
        
        for (int i = 0; i < args.length; i++) { //load args to stack
            pos = loadArg(il, args[i], i, pos);
        }
        il.append(
        new INVOKESTATIC(
        cp.addMethodref(className, mg.getName(), mg.getSignature())));
    }
    
    
    private static MethodGen toMethodGen(
    java.lang.reflect.Method mtd,
    String className,
    InstructionList il,
    ConstantPoolGen cp) {
        
        MethodGen mg = new MethodGen(
        ACC_FINAL  | (    mtd.getModifiers()
        & ~ACC_ABSTRACT & ~ACC_NATIVE & ~ACC_SYNCHRONIZED ),
        toType(mtd.getReturnType()),
        toType(mtd.getParameterTypes()),
        null,
        mtd.getName(),
        className,
        il,
        cp);
        
        Class [] exeptions = mtd.getExceptionTypes();
        
        for( int i = 0 ; i< exeptions.length; i++ ){
            mg.addException( exeptions[i].getName() );
        }
        
        return mg;
        
    }
    
    private static Method generateMethod(
    java.lang.reflect.Method method,
    String fieldName,
    ClassGen cg,
    int after,
    int invokeSuper) {
        
        InstructionList il = new InstructionList();
        InstructionFactory factory = new InstructionFactory(cg);
        ConstantPoolGen cp = cg.getConstantPool();
        MethodGen mg = toMethodGen(method, cg.getClassName(), il, cp);
        
        Type types[] = mg.getArgumentTypes();
        int argCount = types.length;
        addMethodField(fieldName, cg);
        boolean returnsValue = !mg.getReturnType().equals(Type.VOID);
        boolean abstractM =  java.lang.reflect.Modifier.isAbstract(method.getModifiers());
        
        InstructionHandle ehEnd = null;
        GOTO gotoHandled = null;
        IFEQ ifInvoke = null;
        InstructionHandle condition = null;
        InstructionHandle ehHandled = null;
        InstructionHandle ehStart = null;
        InstructionHandle start = il.getStart();
        
        //GENERATE ARG ARRAY
        
        //generates:
        /*
          Object args[]= new Object[]{ arg1, new Integer(arg2)  };
         
         */
        int loaded = createArgArray(il, factory, cp, mg.getArgumentTypes());
        int argArray = loaded;
        il.append(new ASTORE(argArray));
        
        
        //DEFINE LOCAL VARIABLES
        il.append(new ACONST_NULL());
        int resultFromSuper = ++loaded;
        il.append(new ASTORE(resultFromSuper));
        il.append(new ICONST(0));
        int superInvoked = ++loaded;
        il.append(new ISTORE(superInvoked));
        il.append(new ACONST_NULL());
        int error = ++loaded;
        il.append(new ASTORE(error));
        
        
        if (!abstractM) {
            il.append(new ALOAD(0)); //this.handler
            il.append(
            factory.createFieldAccess(
            cg.getClassName(),
            FIELD_NAME,
            new ObjectType(INTERCEPTOR_CLASS_NAME),
            GETFIELD));
            
            //GENERATE INVOKE SUPER
            il.append(new ALOAD(0)); //this
            il.append(factory.createGetStatic(cg.getClassName(), fieldName, METHOD_OBJECT));
            il.append(new ALOAD(argArray));
            il.append(new INVOKEINTERFACE(invokeSuper, 4));
            
            //test returned true
            ifInvoke = new IFEQ(null);
            condition = il.append(ifInvoke);
            il.append(new ICONST(1));
            ehStart = il.append(new ISTORE(superInvoked)); // Ivoked = true
            
            Instruction wrapper = newWrapper(mg.getReturnType(), cp);
            if (wrapper != null) {
                ehStart = il.append(wrapper);
                il.append(new DUP());
            }
            
            invokeSuper(cg, mg, types);
            
            
            if (wrapper != null) {
                il.append(initWrapper(mg.getReturnType(), cp));
            }
            if (returnsValue) {
                ehEnd = il.append(new ASTORE(resultFromSuper));
            }
            gotoHandled = new GOTO(null);
            if (!returnsValue) {
                ehEnd = il.append(gotoHandled);
            } else {
                il.append(gotoHandled);
            }
            ehHandled = il.append(new ASTORE(error));
        }
        
        InstructionHandle endif = il.append(new ALOAD(0)); //this.handler
        
        if (!abstractM) {
            
            ifInvoke.setTarget(endif);
            gotoHandled.setTarget(endif);
        }
        
        //-------------------------------
        il.append(
        factory.createFieldAccess(
        cg.getClassName(),
        FIELD_NAME,
        new ObjectType(INTERCEPTOR_CLASS_NAME),
        GETFIELD));
        
        // INVOKE AFTER RETURN
        il.append(new ALOAD(0)); //this
        il.append(factory.createGetStatic(cg.getClassName(), fieldName, METHOD_OBJECT));
        il.append(new ALOAD(argArray));
        il.append(new ILOAD(superInvoked));
        il.append(new ALOAD(resultFromSuper));
        il.append(new ALOAD(error));
        il.append(new INVOKEINTERFACE(after, 7));
        
        //GENERATE RETURN VALUE
        
        //generates :
        /*
          if( result == null ){
         
            return 0;
         
          } else {
         
            return ((Number)result).intValue();
         
         }
         
         */
        
        InstructionHandle exitMethod =
        generateReturnValue(il, factory, cp, mg.getReturnType(), ++loaded);
        if (!abstractM) {
            mg.addExceptionHandler(ehStart, ehEnd, ehHandled, Type.THROWABLE);
        }
        
       //Exception handlers:
        /*
         
           }catch( RuntimeException re  ){
     
            throw re;
         }
         
         */
        
        ehHandled = il.append( new  ASTORE( ++loaded ) );
        ehEnd     = ehHandled;
        ehStart   = il.getStart();
        il.append( new  ALOAD(loaded) );
        il.append( new  ATHROW() );
        
        mg.addExceptionHandler(ehStart, ehEnd, ehHandled, new ObjectType(RuntimeException.class.getName()) );
        
        //Error :
        ehHandled =  il.append( new  ASTORE( ++loaded ) );
        il.append( new  ALOAD(loaded) );
        il.append( new  ATHROW() );
        
        mg.addExceptionHandler(ehStart, ehEnd, ehHandled, new ObjectType(Error.class.getName()) );
        
        Class exeptions[] = method.getExceptionTypes();
        
        for( int i = 0; i < exeptions.length; i++  ){
        
           // generates : 
           /*
         
           }catch( DeclaredException re  ){
     
            throw re;
         }
         
         */
         
            
            ehHandled =  il.append( new  ASTORE( ++loaded ) );
            il.append( new  ALOAD(loaded) );
            il.append( new  ATHROW() );
            
            mg.addExceptionHandler(ehStart, ehEnd, ehHandled, new ObjectType(exeptions[i].getName()) );
            
            
        }
        
      //generates :
        
      /* }catch( Exception e){
     
         
         throw new java.lang.reflect.UndeclaredThrowableException(e);
     
     }*/  
        
        ehHandled = il.append(  new  ASTORE( ++loaded ) );
        il.append(  new  NEW( cp.addClass("java.lang.reflect.UndeclaredThrowableException") ) );
        il.append(  new  DUP() );
        il.append(  new  ALOAD(loaded ) );
        il.append(  new INVOKESPECIAL(
        cp.addMethodref("java.lang.reflect.UndeclaredThrowableException",
        "<init>","(Ljava/lang/Throwable;)V") ) );
        il.append( new ATHROW() );
        
        mg.addExceptionHandler(ehStart, ehEnd, ehHandled, new ObjectType(Throwable.class.getName()) );
        
        
        
        
        mg.setMaxStack();
        mg.setMaxLocals();
        Method result = getMethod(mg);
        
        
        return result;
    }
    
    private static void loadClass(InstructionList  il, ClassGen cg, ConstantPoolGen cp,Class cls ){
        
        Instruction instruction = null;
        String cln = "Ljava/lang/Class;";
        String t   = "TYPE";
        
        if(cls == int.class){
            
            instruction = new GETSTATIC( cp.addFieldref(Integer.class.getName(), t, cln ));
            
        }
        if(cls == byte.class){
            
            instruction = new GETSTATIC( cp.addFieldref(Byte.class.getName(), t, cln ));
            
        }
        
        if(cls == char.class){
            
            instruction = new GETSTATIC( cp.addFieldref(Character.class.getName(), t, cln ));
            
        }
        if( cls == short.class){
            
            instruction = new GETSTATIC( cp.addFieldref(Short.class.getName(), t, cln ));
            
        }
        
        if( cls == boolean.class){
            
            instruction = new GETSTATIC( cp.addFieldref( Boolean.class.getName(), t, cln) );
            
        }
        if( cls == long.class){
            
            instruction = new GETSTATIC( cp.addFieldref( Long.class.getName(), t, cln) );
            
        }
        
        if( cls == float.class ){
            
            instruction = new GETSTATIC( cp.addFieldref( Float.class.getName(), t, cln) );
            
        }
        
        if( cls == double.class ){
            
            instruction = new GETSTATIC( cp.addFieldref( Double.class.getName(), t, cln ));
            
        }
        
        
        if( instruction != null ){
            
            il.append(instruction);
            
        }else{
            
            il.append( new LDC( cp.addString( cls.getName()) ) );
            il.append( new INVOKESTATIC( cp.addMethodref( cg.getClassName(),
            "findClass","(Ljava/lang/String;)Ljava/lang/Class;")
            )
            ) ;
        }
        
    }
    
    private static void generateClInit(ClassGen cg, ConstantPoolGen cp,  java.util.HashMap  methods){
     
      //generates :
      /*
      static{   
         
      Class [] args;
      Class cls = findClass("java.lang.Object");
      args = new Class[0];
      METHOD_1 = cls.getDeclaredMethod("toString", args );  
         ................
       }
    */
        
        InstructionList  il = new InstructionList();
        MethodGen cinit = new MethodGen(
        ACC_STATIC , // access flags
        Type.VOID, // return type
        new Type[] { }, null, // arg names
        "<clinit>", cg.getClassName(), il, cp );
        
        MethodGen findClass = generateFindClass( cg, cp );
        
        for( java.util.Iterator i = methods.keySet().iterator(); i.hasNext(); ){
            
            String fieldName      = (String)i.next();
            java.lang.reflect.Method method =
            (java.lang.reflect.Method)methods.get( fieldName );
            Class[] args = method.getParameterTypes();
            String declaring    = method.getDeclaringClass().getName();
            
            il.append( new  LDC( cp.addString(declaring) ));
            il.append( new  INVOKESTATIC(
            cp.addMethodref(  findClass   ) ) );
            il.append( new  ASTORE(1) );
            
            
            il.append( new ICONST( args.length )  );
            il.append( new ANEWARRAY( cp.addClass( CLASS_OBJECT ) ) );
            
            for( int j = 0; j < args.length; j++   ){
                
                il.append( new DUP() );
                il.append( new ICONST(j) );
                loadClass( il, cg,  cp, args[j] );
                il.append(  new AASTORE() );
                
            }
            
            il.append( new ASTORE(0) );
            il.append( new ALOAD(1) );
            il.append( new LDC( cp.addString(method.getName() ) ) );
            il.append( new ALOAD(0) );
            il.append( new INVOKEVIRTUAL(
            cp.addMethodref("java.lang.Class","getDeclaredMethod",
            "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;")
            )
            );
            
            il.append( new PUTSTATIC(
            cp.addFieldref( cg.getClassName(), fieldName,
            "Ljava/lang/reflect/Method;" ) )
            );
        }
        
        il.append( new  RETURN() );
        cg.addMethod( getMethod( cinit ) );
        
    }
    
    
    private static MethodGen generateFindClass( ClassGen cg, ConstantPoolGen cp ){
   
   // generates:    
     /*
   static private Class findClass(String name ) throws Exception{
      try{
      
          return Class.forName(name);
      
     }catch( java.lang.ClassNotFoundException cne ){
      
          throw new java.lang.NoClassDefFoundError( cne.getMessage() );
          
     }
       
      
   }
    */
        
        
        InstructionList  il = new InstructionList();
        MethodGen findClass = new MethodGen( ACC_PRIVATE | ACC_STATIC , // access flags
        CLASS_OBJECT, // return type
        new Type[] { Type.STRING }, null, // arg names
        "findClass", cg.getClassName(), il, cp);
        
        InstructionHandle start = il.append( new ALOAD(0));
        
        il.append( new INVOKESTATIC( cp.addMethodref("java.lang.Class",
        "forName",
        "(Ljava/lang/String;)Ljava/lang/Class;" )
        )
        );
        
        InstructionHandle h1 = il.append( new ARETURN() );
        
        InstructionHandle h2 = il.append( new ASTORE(1) );
        
        il.append( new NEW(cp.addClass("java.lang.NoClassDefFoundError") ) );
        il.append( new DUP() );
        il.append( new ALOAD(1) );
        il.append( new INVOKEVIRTUAL( cp.addMethodref("java.lang.ClassNotFoundException",
        "getMessage","()Ljava/lang/String;") )
        );
        il.append( new INVOKESPECIAL( cp.addMethodref("java.lang.NoClassDefFoundError",
        CONSTRUCTOR_NAME,
        "(Ljava/lang/String;)V" ))
        );
        il.append( new ATHROW() );
        
        findClass.addExceptionHandler(  start, h1, h2,
        new ObjectType("java.lang.ClassNotFoundException") );
        
        
        cg.addMethod( getMethod( findClass ) );
        return findClass;
    }
    
    public static class InternalReplace implements java.io.Serializable{
        
        private String parentClassName;
        
        private String [] interfaceNameList;
        
        private MethodInterceptor mi;
        
        public InternalReplace(){
            
        }
        
        
        private InternalReplace( String parentClassName,
        String [] interfaceList, MethodInterceptor mi ){
            
            this.parentClassName = parentClassName;
            this.interfaceNameList   = interfaceNameList;
            this.mi = mi;
        }
        
        public static Object writeReplace( Object enhanced ) throws ObjectStreamException{
            
            
            MethodInterceptor mi = Enhancer.getMethodInterceptor( enhanced );
            
            String parentClassName = enhanced.getClass().getSuperclass().getName();
            
            Class interfaces[] = enhanced.getClass().getInterfaces();
            
            String [] interfaceNameList = new String[ interfaces.length  ];
            
            for( int i = 0 ; i < interfaces.length; i++  ) {
                interfaceNameList[i] = interfaces[i].getName();
            }
            
            return new InternalReplace( parentClassName, interfaceNameList, mi );
            
        }
        
        
        Object readResolve() throws ObjectStreamException{
            try{
                
                ClassLoader loader = this.getClass().getClassLoader();
                Class parent = loader.loadClass(parentClassName);
                Class interfaceList[] = null;
                if( interfaceNameList != null ){
                    interfaceList = new Class[interfaceNameList.length];
                    for( int i = 0; i< interfaceNameList.length; i++ ){
                        interfaceList[i] = loader.loadClass( interfaceNameList[i] );
                    }
                }
                
                return Enhancer.enhance( parent, interfaceList, mi, loader );
                
            }catch( Throwable t ){
                throw new ObjectStreamException(){};
            }
            
            
        }
        
    }
    
    
    static class MethodWrapper {
        java.lang.reflect.Method method;
        MethodWrapper(java.lang.reflect.Method method) {
            if (method == null) {
                throw new NullPointerException();
            }
            this.method = method;
        }
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof MethodWrapper)) {
                return false;
            }
            return Enhancer.equals(method, ((MethodWrapper) obj).method );
        }
        public int hashCode() {
            return method.getName().hashCode();
        }
    }
    
    private static boolean equals(
    java.lang.reflect.Method m1,
    java.lang.reflect.Method m2) {
        
        if (m1 == m2) {
            
            return true;
        }
        if (m1.getName().equals(m2.getName())) {
            Class[] params1 = m1.getParameterTypes();
            Class[] params2 = m2.getParameterTypes();
            if (params1.length == params2.length) {
                for (int i = 0; i < params1.length; i++) {
                    if (!params1[i].getName().equals( params2[i].getName() ) ) {
                        return false;
                    }
                }
                
                if(!m1.getReturnType().getName().
                equals(m2.getReturnType().getName()) ){
                    throw new java.lang.IllegalStateException(
                    "Can't implement:\n" + m1.getDeclaringClass().getName() +
                    "\n      and\n" + m2.getDeclaringClass().getName() + "\n"+
                    m1.toString() + "\n" + m2.toString());
                }
                return true;
            }
        }
        
        return false;
    }
    
    
}