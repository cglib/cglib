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

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

import java.util.*;
/**
 *
 * @author  baliuka
 */
public class BeanGenerator extends ClassLoader implements ClassFileConstants {
    
    public static final String CLASS_NAME = "net.sf.cglib.proxy.Bean$Generated$By$CGLIB";
    
    private static final String BEAN_INFO_CLASS_NAME = CLASS_NAME + "BeanInfo";
    
    private List properties    = new ArrayList();
    private Set   names        = new HashSet();
    private List  methods      = new ArrayList();
    private Class beanInfo;
    private Class result;
    
    
    /** Creates a new instance of BeanUtils */
    public BeanGenerator( ) {
        
    }
    
    /** Creates a new instance of BeanUtils */
    public BeanGenerator( ClassLoader parent ) {
        super(parent);
    }
    // can be implemented without code generation
    public static class Info extends java.beans.SimpleBeanInfo {
        
        private BeanGenerator generator ;
        
        public Info(){
          generator = (BeanGenerator)getClass().getClassLoader();   
        }
       
        
    } 
        
        
        protected Class findClass(String name)
        throws ClassNotFoundException{
           
            if( name.equals(BEAN_INFO_CLASS_NAME) ){
                
                if( result != null ){
                    
                    if( beanInfo != null ){
                        return beanInfo;
                    }
                    
                 java.io.InputStream is = getParent().
                                          getResourceAsStream(
                                          Info.class.getName().replace('.','/') + ".class"   
                    );
                 if( is == null ){
                     throw new ClassNotFoundException("Can't find " + name);
                 }
                 
                java.io.ByteArrayOutputStream out = new  java.io.ByteArrayOutputStream();
                
                try{ 
                 while(is.available() > 0){
                   out.write( is.read() );
                 }
                }catch(java.io.IOException ioe){
                    ioe.printStackTrace();
                     throw new ClassNotFoundException( ioe.getMessage() );               
                }
                 byte [] b  = out.toByteArray();
                 beanInfo =  defineClass(b,0,b.length);
                
                 
                  return  beanInfo;
                    
                }else {
                  throw new ClassNotFoundException();
                }
            }else if( name.equals( CLASS_NAME )){
                 return generate();
            }
            return super.findClass(name);
        }
        
        
        
        private void generateMethod( ClassGen cg, String name,
        java.lang.reflect.Method mtd ){
            
            ConstantPoolGen cp = cg.getConstantPool();
            InstructionList il = new InstructionList();
            Type params[] = ClassFileUtils.toType(mtd.getParameterTypes());
            Type ret      = ClassFileUtils.toType(mtd.getReturnType());
            MethodGen mg  = new MethodGen( ACC_PUBLIC,
            ret, params , null, name, cg.getClassName(), il, cp);
            
            Class [] exeptions = mtd.getExceptionTypes();
            
            for( int i = 0 ; i< exeptions.length; i++ ){
                mg.addException( exeptions[i].getName() );
            }
            int index = 1;
            for( int i = 0; i< params.length; i++){
                index = ClassFileUtils.loadArg(il,params[i], index );
            }
            il.append( new INVOKESTATIC(
            cp.addMethodref( mtd.getDeclaringClass().getName(),
            name, mg.getSignature() ) ) );
            il.append( ClassFileUtils.newReturn(ret) );
            cg.addMethod(  ClassFileUtils.getMethod( mg ) );
            
        }
        private void generateProperty(ClassGen cg, String name, Class  type ){
            
            ConstantPoolGen cp = cg.getConstantPool();
            InstructionList il = new InstructionList();
            
            Type propType = ClassFileUtils.toType(type);
            FieldGen fg = new FieldGen( ACC_PRIVATE , propType , name, cp );
            cg.addField(fg.getField());
            
            char propName[] = name.toCharArray();
            propName[0] = Character.toUpperCase( propName[0] );
            
            MethodGen getter = new MethodGen( ACC_PUBLIC ,  propType , new Type[0],
            null, type == Boolean.TYPE ? "is" : "get" + new String(propName),
            cg.getClassName(), il,  cp);
            il.append( new ALOAD(0)  );
            il.append( new GETFIELD(
            cp.addFieldref(cg.getClassName(), name,
            propType.getSignature()) )  );
            il.append( ClassFileUtils.newReturn(propType) );
            
            cg.addMethod(  ClassFileUtils.getMethod(getter) );
            
            il.dispose();
            
            MethodGen setter = new MethodGen( ACC_PUBLIC , Type.VOID , new Type[]{propType},
            null, "set" + new String(propName), cg.getClassName(), il,  cp);
            
            il.append( new ALOAD(0) );
            ClassFileUtils.loadArg( il, propType, 1);
            il.append( new PUTFIELD(
            cp.addFieldref(cg.getClassName(), name,
            propType.getSignature()) )  );
            
            il.append( new RETURN() );
            
            cg.addMethod( ClassFileUtils.getMethod(setter) );
            
        }
        
        public Class generate(){
            
            if( result != null ){
                return result;
            }
            
            ClassGen cg = new ClassGen( CLASS_NAME , Object.class.getName(),SOURCE_FILE,
            ACC_PUBLIC  , null );
            
            cg.addEmptyConstructor( ACC_PUBLIC );
            
            for( Iterator i = properties.iterator(); i.hasNext();  ){
                BeanProperty prop = (BeanProperty)i.next();
                generateProperty( cg, prop.name, prop.type );
                
            }
            
            for( Iterator i = methods.iterator(); i.hasNext();  ){
                BeanMethod m = (BeanMethod)i.next();
                generateMethod( cg, m.name, m.method );
                
            }
            
            byte b[] = cg.getJavaClass().getBytes();
            result = defineClass( cg.getClassName(), b, 0, b.length );

            
            return result;
            
            
        }
        
        public BeanGenerator copyMethods( Class clasz ){
            
            java.lang.reflect.Method methods[] = clasz.getMethods();
            
            for( int i = 0; i< methods.length; i++ ){
                
                int mod = methods[i].getModifiers();
                
                if( java.lang.reflect.Modifier.isStatic(mod) &&
                java.lang.reflect.Modifier.isPublic(mod ) ){
                    
                    this.methods.add( new BeanGenerator.BeanMethod(
                    methods[i].getName(),
                    methods[i])
                    );
                    
                }
                
            }
            return this;
        }
        
        public BeanGenerator addMethod( String name, java.lang.reflect.Method method ){
            
            int mod = method.getModifiers();
            if( !java.lang.reflect.Modifier.isStatic(mod) ||
            ! java.lang.reflect.Modifier.isPublic(mod ) ){
                
                throw new IllegalArgumentException( method.toString() );
            }
            
            methods.add( new BeanGenerator.BeanMethod(name,method) );
            return this;
        }
        
        public BeanGenerator addMethod(  java.lang.reflect.Method method ){
            addMethod( null, method );
            return this;
        }
        
        
        public BeanGenerator addProperties( java.util.Map props ){
            
            for( java.util.Iterator i = props.entrySet().iterator(); i.hasNext();  ){
                Map.Entry entry = (Map.Entry)i.next();
                addProperty((String)entry.getKey(),(Class)entry.getValue());
            }
            return this;
        }
        
        public BeanGenerator addProperties( java.beans.PropertyDescriptor[] descriptors ){
            
            for( int i = 0; i < descriptors.length; i++  ){
                addProperty(descriptors[i].getName(),
                descriptors[i].getPropertyType());
            }
            return this;
        }
        
        
        
        public BeanGenerator addProperty( String name, Class type ){
            
            if( names.contains(name) ){
                throw new IllegalArgumentException("Dublicate property name " + name);
            }
            
            names.add( name );
            properties.add( new BeanProperty( name, type) );
            return this;
        }
        
        static class BeanProperty{
            String name;
            Class type;
            BeanProperty( String name, Class type ){
                this.name = name;
                this.type = type;
            }
            
            
        }
        
        static class BeanMethod{
            String name;
            java.lang.reflect.Method method;
            BeanMethod( String name, java.lang.reflect.Method method ){
                this.name = name == null ?  method.getName() : name;
                this.method = method;
            }
            
            
        }
        
        
        public static void main(String args[])throws Exception{
            
            for(int j = 0; j< 2 ;j++){
                
            Class cls = new BeanGenerator( ).
            addProperty( "property", int.class ).
            addMethod( BeanGenerator.class.getMethod("main",
                                 new Class[]{ String[].class }) ).
            copyMethods( java.lang.Math.class ).
            generate();
            
            
            
            java.beans.BeanInfo info = java.beans.Introspector.getBeanInfo(cls);
            
            
            info.getDefaultEventIndex() ;
            
            Object obj = cls.newInstance();
            Object methods[] = cls.getMethods();
            
            
            
            for( int i = 0; i< methods.length; i++ ){
                //System.out.println( methods[i] );
            }
            
            
        }
        
        }
        
    }
