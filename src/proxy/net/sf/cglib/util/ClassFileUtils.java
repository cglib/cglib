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

package net.sf.cglib.util;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

/**
 *  private utility
 * @author  baliuka
 */
public class ClassFileUtils implements ClassFileConstants {
    
    
    private ClassFileUtils() {
    }
    
public static Instruction getIntConst( int i, ConstantPoolGen cp){
        
            if( i < 0  ){
                 return new LDC( cp.addInteger(i) ); 
            }else if( i<= 5 ){ 
                  return ( new  ICONST( i ) );
               }else  if ( i < Byte.MAX_VALUE){
                  return ( new  BIPUSH((byte)i ) ); 
               }else if ( i < Short.MAX_VALUE ) {
                  return ( new  SIPUSH((short)i ) ); 
               }else{
                  return new LDC( cp.addInteger(i) );
               } 
            
    
    }
    
  
    
  public  static Class defineClass( final ClassLoader  loader,
    final String name,final byte[] b){
        
        return (Class) java.security.AccessController.doPrivileged(
        new java.security.PrivilegedAction() {
            public Object run() {
                try{
                    java.lang.reflect.Method m =
                    ClassLoader.class.getDeclaredMethod(
                    "defineClass",
                    new Class[] { String.class, byte[].class, int.class, int.class });
                    // protected method invocaton
                    boolean flag = m.isAccessible();
                    m.setAccessible(true);
                    Class  result =
                    (Class) m.invoke(
                    loader,
                    new Object[] { name, b, new Integer(0), new Integer(b.length)});
                    m.setAccessible(flag);
                    
                    return result;
                    
                 }catch( java.lang.reflect.InvocationTargetException ite  ){
                     
                    Throwable e = ite.getTargetException();
                    throw new Error( e.getClass().getName() + ":" + e.getMessage());
                   
                  }catch( Throwable e ){
                      
                    throw new Error( e.getClass().getName() + ":" + e.getMessage());
                }
            }
        });
        
        
        
    }
    
    public static int loadArg(InstructionList il, Type t,  int pos) {
        
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
            } else { // int, byte, ... are integers
                il.append(new ILOAD(pos));
                return ++pos;
            }
        } else {
            il.append(new ALOAD(pos));
            return ++pos;
        }
    }
    
    
   public static MethodGen toMethodGen(
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
    
    
   public static Type[] toType(Class cls[]) {
        
        Type tp[] = new Type[cls.length];
        for (int i = 0; i < cls.length; i++) {
            tp[i] = toType(cls[i]);
        }
        return tp;
    }
    
   public static Type toType(Class cls) {
        
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
    
   public static int createArgArray(
    InstructionList il,
    InstructionFactory factory,
    ConstantPoolGen cp,
    Type[] args) {
        
        int argCount = args.length;
       
        il.append(getIntConst(argCount,cp));
       
       
        il.append(new ANEWARRAY(cp.addClass(Type.OBJECT)));
        int load = 1;
        for (int i = 0; i < argCount; i++) {
            il.append(new DUP());
            il.append(getIntConst( i, cp ) );
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
    
    
    
   public static Method getMethod(MethodGen mg) {
        mg.stripAttributes(true);
        mg.setMaxLocals();
        mg.setMaxStack();
        return mg.getMethod();
    }
    
    
   public static Instruction initWrapper(Type type, ConstantPoolGen cp) {
        
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
    
    
    
   public static void invokeSuper(ClassGen cg, MethodGen mg, Type args[]) {
        
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionList il = mg.getInstructionList();
        int pos = 1;
        il.append(new ALOAD(0)); //this
        for (int i = 0; i < args.length; i++) { //load args to stack
            pos = loadArg(il, args[i], pos);
        }
        il.append(
        new INVOKESPECIAL(
        cp.addMethodref(cg.getSuperclassName(), mg.getName(), mg.getSignature())));
    }
    
    
    
   public static Instruction newReturn( Type t ){
        
        if (t instanceof BasicType) {
            if (t.equals(Type.LONG)) {
                return new LRETURN();
            } else if (t.equals(Type.DOUBLE)) {
                return new DRETURN();
            } else if (t.equals(Type.FLOAT)) {
                return new FRETURN();
            }else if(t.equals(Type.VOID)){
                return new RETURN();
            }else {
                return new IRETURN();
            }
        } else {
            return new ARETURN();
        }
        
    }
    
    
   public static Instruction newWrapper(Type type, ConstantPoolGen cp) {
        
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
    
    
   public static InstructionHandle generateReturnValue(
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
    
    
public  static void loadClass(InstructionList  il, ClassGen cg, ConstantPoolGen cp,Class cls ){
        
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
            FIND_CLASS,"(Ljava/lang/String;)Ljava/lang/Class;")
            )
            ) ;
        }
        
    }
    
    
  public  static MethodGen generateFindClass( ClassGen cg, ConstantPoolGen cp ){
        
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
        FIND_CLASS, cg.getClassName(), il, cp);
        
        
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
        
        
        cg.addMethod( ClassFileUtils.getMethod( findClass ) );
        return findClass;
    }
    
 public   static String getPackageName( String className  )
    throws Throwable{
        
        int index = className.lastIndexOf('.');
        if( index == -1 ){
            return "";
        }
        return className.substring( 0, index );
        
    }
    
    
   public static boolean equals(
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
                    throw new java.lang.IllegalArgumentException(
                    "Can't implement:\n" + m1.getDeclaringClass().getName() +
                    "\n      and\n" + m2.getDeclaringClass().getName() + "\n"+
                    m1.toString() + "\n" + m2.toString());
                }
                return true;
            }
        }
        
        return false;
    }
    
    
    
    
   public static boolean isVisible(java.lang.reflect.Member m ,
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
        
        return ClassFileUtils.getPackageName( m.getDeclaringClass().getName()
        ).equals( packageName );
      
    }
    

 public static void castObject(
                        ConstantPoolGen cp,InstructionList il,
                        Type returnType ) {
        
        
        if ( ( returnType instanceof ObjectType ) || 
             ( returnType instanceof ArrayType ) ) {
            
            if (returnType instanceof ArrayType){
                il.append( new CHECKCAST(cp.addArrayClass((ArrayType)returnType)));
                
            }
            if (!returnType.equals(Type.OBJECT)){
                
              il.append (new CHECKCAST(cp.addClass((ObjectType) returnType)));
                
            }
         
        }else   if (returnType instanceof BasicType) {
            if (returnType.equals(Type.BOOLEAN)) {
                il.append (new CHECKCAST(cp.addClass(BOOLEAN_OBJECT)));
                il.append( new INVOKEVIRTUAL( 
                           cp.addMethodref( Boolean.class.getName(),"booleanValue","()Z" )
                          ) 
                        );
                
            } else if (returnType.equals(Type.CHAR)) {
                 il.append (new CHECKCAST(cp.addClass(CHARACTER_OBJECT)));
                 il.append( new INVOKEVIRTUAL( 
                           cp.addMethodref( Character.class.getName(),"charValue","()C" )
                          ) 

                        );
              
            } else if (returnType.equals(Type.LONG)) {
               il.append(new CHECKCAST(cp.addClass(NUMBER_OBJECT)));
               il.append( new INVOKEVIRTUAL( 
                           cp.addMethodref( Number.class.getName(),"longValue","()J" )
                          ) 
                        );
              
            } else if (returnType.equals(Type.DOUBLE)) {
                il.append( new CHECKCAST(cp.addClass(NUMBER_OBJECT)));
                il.append( new INVOKEVIRTUAL( 
                           cp.addMethodref( Number.class.getName(),"doubleValue","()D" )
                          ) 
                        );
                
                
            } else if (returnType.equals(Type.FLOAT)) {
               il.append (new CHECKCAST(cp.addClass(NUMBER_OBJECT)));
               il.append( new INVOKEVIRTUAL( 
                           cp.addMethodref( Number.class.getName(),"floatValue","()F" )
                          ) 
                        );
                
            } else if ( returnType.equals( Type.BYTE ) ) {
               il.append ( new CHECKCAST(cp.addClass(NUMBER_OBJECT)) );
               il.append ( new INVOKEVIRTUAL( 
                           cp.addMethodref( Number.class.getName(),"byteValue","()B" )
                          ) 
                        );
               
             } else if ( returnType.equals( Type.SHORT ) ) {
               il.append ( new CHECKCAST(cp.addClass(NUMBER_OBJECT)) );
               il.append ( new INVOKEVIRTUAL( 
                           cp.addMethodref( Number.class.getName(),"shortValue","()S" )
                          ) 
                        );
             } else if ( returnType.equals( Type.INT ) ) {
               il.append ( new CHECKCAST(cp.addClass(NUMBER_OBJECT)) );
               il.append ( new INVOKEVIRTUAL( 
                           cp.addMethodref( Number.class.getName(),"intValue","()I" )
                          ) 
                        );
             }else throw new java.lang.InternalError(returnType.toString());
        }else throw new java.lang.InternalError(returnType.toString());
        
    }

    
}

