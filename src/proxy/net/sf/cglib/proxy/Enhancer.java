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
 *@version    $Id: Enhancer.java,v 1.22 2002/10/29 08:10:52 baliuka Exp $
 */
public class Enhancer implements ClassFileConstants {
    
    private static final String FIELD_NAME = "h";
    private static final String CLASS_SUFIX = "$$EnhancedByCGLIB$$";
    private static final String CLASS_PREFIX = "net.sf.cglib.proxy";

    private static int index = 0;
    private static java.util.Map factories = new java.util.HashMap();
    private static java.util.Map cache = new java.util.WeakHashMap();
    
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
    
    
    private Enhancer() {}
    
    public static MethodInterceptor getMethodInterceptor(Object enhanced){
      
            return ((Factory)enhanced).getInterceptor();
        
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
     * source class must have public or protected no args constructor.
     * Code is generated for protected and public not final methods,
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
        
        if( ih == null ){
            
            throw new NullPointerException("MethodInterceptor is null");
        }
        
        
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
        
        
        
        if ( result == null ) {
            
            try{
                
                java.lang.reflect.Constructor construct =  cls.getConstructor( new Class[0] );
                int mod = construct.getModifiers();
                
                if( !( java.lang.reflect.Modifier.isPublic( mod ) ||
                java.lang.reflect.Modifier.isProtected( mod ) )  ){
                    
                    throw new IllegalArgumentException( cls.getName() );
                }
                
            }catch( NoSuchMethodException nsme){
                throw new IllegalArgumentException(nsme.getMessage());
            }
            
            try{
                
                loader.loadClass(cls.getName());
                
                if( interfaces != null ){
                    for( int i = 0; i< interfaces.length; i++ ){
                        loader.loadClass( interfaces[i].getName() );
                    }
                }
                
            }catch( ClassNotFoundException cnfe ){
                
                
                throw new IllegalArgumentException( cnfe.getMessage() );
                
            }
            
            
            String class_name = cls.getName() + CLASS_SUFIX;
            if (class_name.startsWith("java")) {
                class_name = CLASS_PREFIX + class_name;
            }
            class_name += index++;
            JavaClass clazz = enhance(cls, class_name, interfaces );
            result = ClassFileUtils.defineClass(loader, class_name, clazz.getBytes());
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
            factory.createInvoke( parentClass,CONSTRUCTOR_NAME, Type.VOID,
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
            cg.addMethod(ClassFileUtils.getMethod(costructor));
            
            //factory sometimes usefull and has meaning for performance
            
            il = new InstructionList();
            MethodGen newInstance = ClassFileUtils.toMethodGen(
            Factory.class.getMethod("newInstance",
            new Class[]{ MethodInterceptor.class } ),
            cg.getClassName(), il, cp );
            il.append( new NEW(cp.addClass( new ObjectType(cg.getClassName()) )) );
            il.append( new DUP());
            il.append( new ALOAD(1) );
            il.append( new INVOKESPECIAL( addNewInstanceRef(cp, cg.getClassName()) ) ) ;
            il.append( new ARETURN());
            cg.addMethod(ClassFileUtils.getMethod(newInstance));
            
            
            
            il = new InstructionList();
            MethodGen getInterceptor = ClassFileUtils.toMethodGen(
            Factory.class.getMethod( "getInterceptor", new Class[0] ),
            cg.getClassName(), il, cp );
            il.append( new ALOAD(0));
            il.append( new GETFIELD( cp.addFieldref( cg.getClassName(),
            FIELD_NAME, "L" + INTERCEPTOR_CLASS_NAME.replace('.','/') + ";"  ) ) );
            il.append( new ARETURN() );
            cg.addMethod(ClassFileUtils.getMethod(getInterceptor));
            
            //serialization support
            il = new InstructionList();
            MethodGen writeReplace = new MethodGen(ACC_PRIVATE, // access flags
            Type.OBJECT, // return type
            new Type[] {}, null, // arg names
            "writeReplace", cg.getClassName(), il, cp);
            
            
            il.append(new ALOAD(0));
            il.append(new INVOKESTATIC( addWriteReplace(cp) ) );
            il.append(new ARETURN());
            
            cg.addMethod(ClassFileUtils.getMethod(writeReplace));
            
            
            
    }
    
    
    private static void addHandlerField(ClassGen cg) {
        ConstantPoolGen cp = cg.getConstantPool();
        FieldGen fg =
        new FieldGen(ACC_PRIVATE, new ObjectType(INTERCEPTOR_CLASS_NAME), FIELD_NAME, cp);
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
        
        String packageName = ClassFileUtils.getPackageName( class_name );
        
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
            &&  ClassFileUtils.isVisible( m , packageName )   ) {
                
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
        new FieldGen( ACC_PRIVATE | ACC_FINAL | ACC_STATIC, METHOD_OBJECT, fieldName, cp );
        cg.addField(fg.getField());
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
        MethodGen mg = ClassFileUtils.toMethodGen(method, cg.getClassName(), il, cp);
        
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
        int loaded = ClassFileUtils.createArgArray(il, factory, cp, mg.getArgumentTypes());
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
            
            Instruction wrapper = ClassFileUtils.newWrapper(mg.getReturnType(), cp);
            if (wrapper != null) {
                ehStart = il.append(wrapper);
                il.append(new DUP());
            }
            
            ClassFileUtils.invokeSuper(cg, mg, types);
            
            
            if (wrapper != null) {
                il.append(ClassFileUtils.initWrapper(mg.getReturnType(), cp));
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
        ClassFileUtils.generateReturnValue(il, factory, cp, mg.getReturnType(), ++loaded);
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
       
       
         throw new UndeclaredThrowableException(e);
       
     }*/
        
        ehHandled = il.append(  new  ASTORE( ++loaded ) );
        il.append(  new  NEW( cp.addClass(UndeclaredThrowableException.class.getName()) ) );
        il.append(  new  DUP() );
        il.append(  new  ALOAD(loaded ) );
        il.append(  new INVOKESPECIAL(
        cp.addMethodref(UndeclaredThrowableException.class.getName(),
        "<init>","(Ljava/lang/Throwable;)V") ) );
        il.append( new ATHROW() );
        
        mg.addExceptionHandler(ehStart, ehEnd, ehHandled, new ObjectType(Throwable.class.getName()) );
        
        Method result = ClassFileUtils.getMethod(mg);
        
        
        return result;
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
        
        MethodGen findClass = ClassFileUtils.generateFindClass( cg, cp );
        
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
                ClassFileUtils.loadClass( il, cg,  cp, args[j] );
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
        cg.addMethod( ClassFileUtils.getMethod( cinit ) );
        
    }
    
    
    
    static public class InternalReplace implements java.io.Serializable{
        
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
        
        static public Object writeReplace( Object enhanced ) throws ObjectStreamException{
            
            
            MethodInterceptor mi = Enhancer.getMethodInterceptor( enhanced );
            
            String parentClassName = enhanced.getClass().getSuperclass().getName();
            
            Class interfaces[] = enhanced.getClass().getInterfaces();
            
            String [] interfaceNameList = new String[ interfaces.length  ];
            
            for( int i = 0 ; i < interfaces.length; i++  ) {
                interfaceNameList[i] = interfaces[i].getName();
            }
            
            return new InternalReplace( parentClassName, interfaceNameList, mi );
            
        }
        
        
        private Object readResolve() throws ObjectStreamException{
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
                throw new ObjectStreamException(t.getMessage()){};
            }
            
            
        }
        
    }
    
    
    
}