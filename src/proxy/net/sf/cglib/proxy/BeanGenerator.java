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
import java.beans.*;
import java.util.*;



/**
 *
 * @author  baliuka
 */
public final class BeanGenerator extends ClassLoader implements ClassFileConstants {
    
    public static final String CLASS_NAME = "net.sf.cglib.proxy.Bean$Generated$By$CGLIB";
    
    private static final String BEAN_INFO_CLASS_NAME = CLASS_NAME + "BeanInfo";
    
    private List     properties    = new ArrayList();
    private Set      names         = new HashSet();
    private List     methods       = new ArrayList();
    private HashMap  attributes    = new HashMap();
    private Class beanInfo;
    private Class result;
    
    private final static byte [] B_INFO;
    
    static{

        
           java.io.InputStream is = BeanGenerator.class.getClassLoader().
                getResourceAsStream(
                Info.class.getName().replace('.','/') + ".class"
                );
                if( is == null ){
                    throw new NoClassDefFoundError(Info.class.getName());
                }
                
                java.io.ByteArrayOutputStream out = new  java.io.ByteArrayOutputStream();
                
                try{
                    byte buffer[] = new byte[ 0xFF ]; 
                    
                    while( true ){
                        
                       int r = is.read( buffer );
                       if( r <= 0 ) break;
                       out.write( buffer, 0, r ); 
                       
                    };
                    
                }catch(java.io.IOException ioe){
                    
                    throw new NoClassDefFoundError( ioe.getMessage() );
                }
                B_INFO  = out.toByteArray();
            
    
    }
    
    public  List getProperties(){
      return properties;
    } 
    
    public List  getMethods(){
      return methods;
    }
    
    public Map getAttributes(){
      return attributes;
    }
    
     public BeanGenerator( ) {
       this((Map)null);  
    }
    
    public BeanGenerator( Map attributes ) {
       if( attributes != null ){ 
        this.attributes.putAll( attributes );
       } 
    }
    
    
    public BeanGenerator( ClassLoader parent, Map attributes ) {
       super(parent);
       if( attributes != null ){ 
         this.attributes.putAll( attributes );
       } 
    
    }
     public BeanGenerator( ClassLoader parent ) {
       this( parent, null );
     }
   
    
    // can be implemented without code generation
    public static class Info extends java.beans.SimpleBeanInfo {
        
        private BeanGenerator generator ;
        private BeanDescriptor beanDescriptor;
        private MethodDescriptor[]  methodDescriptors;
        private PropertyDescriptor[] propertyDescriptors;
        
        public Info(){
          
            generator = (BeanGenerator)getClass().getClassLoader();
            BeanProperty p[] = ( BeanProperty[]) generator.getProperties().
                                     toArray( new BeanProperty[]{} );
            BeanMethod m[]   = ( BeanMethod[]  ) generator.getMethods().
                                     toArray( new BeanMethod[]{} );
            propertyDescriptors = new PropertyDescriptor[ p.length ];
            methodDescriptors   = new MethodDescriptor[m.length];
          
            for( int i = 0; i< p.length; i++ ){
              propertyDescriptors[i] = p[i].toPropertyDescriptor();
            }
            
            for( int i = 0; i< m.length; i++ ){
              methodDescriptors[i] = m[i].toMethodDescriptor();
            }
            /*
            beanDescriptor = new BeanDescriptor( generator.getBeanClass() ){
              
                public Enumeration attributeNames(){
                    
                  return new Enumeration(){
                    Iterator i;  
                   { i = generator.getAttributes().keySet().iterator();}
                    
                   public boolean hasMoreElements(){
                     return i.hasNext();
                   }
                   
                   public Object nextElement(){
                     return i.next();
                   }
                   
                  };
                }
                
               public void setValue(String attributeName,
                     Object value){
                generator.getAttributes().put(attributeName, value );
               } 
                
              public Object getValue(String attributeName){
                return generator.getAttributes().get(attributeName);
              }  
            };
            */
          }
        
        
        
        /** Gets the beans <code>BeanDescriptor</code>.
         *
         * @return  A BeanDescriptor providing overall information about
         * the bean, such as its displayName, its customizer, etc.  May
         * return null if the information should be obtained by automatic
         * analysis.
         */
        public BeanDescriptor getBeanDescriptor() {
            
            return beanDescriptor;
        }
        
        /** A bean may have a "default" event that is the event that will
         * mostly commonly be used by humans when using the bean.
         * @return Index of default event in the EventSetDescriptor array
         * 		returned by getEventSetDescriptors.
         * <P>	Returns -1 if there is no default event.
         */
        public int getDefaultEventIndex() {
            //TODO:
            return -1;
        }
        
        /** Gets the beans <code>EventSetDescriptor</code>s.
         *
         * @return  An array of EventSetDescriptors describing the kinds of
         * events fired by this bean.  May return null if the information
         * should be obtained by automatic analysis.
         */
        public EventSetDescriptor[] getEventSetDescriptors() {
            //TODO:
            return null;
        }
        
        
        /** Gets the beans <code>MethodDescriptor</code>s.
         *
         * @return An array of MethodDescriptors describing the externally
         * visible methods supported by this bean.  May return null if
         * the information should be obtained by automatic analysis.
         */
        public MethodDescriptor[] getMethodDescriptors() {
            return methodDescriptors;
        }
        
        /** Gets the beans <code>PropertyDescriptor</code>s.
         *
         * @return An array of PropertyDescriptors describing the editable
         * properties supported by this bean.  May return null if the
         * information should be obtained by automatic analysis.
         * <p>
         * If a property is indexed, then its entry in the result array will
         * belong to the IndexedPropertyDescriptor subclass of PropertyDescriptor.
         * A client of getPropertyDescriptors can use "instanceof" to check
         * if a given PropertyDescriptor is an IndexedPropertyDescriptor.
         */
        public PropertyDescriptor[] getPropertyDescriptors() {
             return propertyDescriptors;
        }
        
    }
    
    
    protected Class findClass(String name)
    throws ClassNotFoundException{
        
        if( name.equals(BEAN_INFO_CLASS_NAME) ){
            
            if( result != null ){
                
                if( beanInfo != null ){
                    return beanInfo;
                }
                
                beanInfo =  defineClass( Info.class.getName(), B_INFO,0,B_INFO.length);
                
                
                return  beanInfo;
                
            }else {
                throw new ClassNotFoundException();
            }
        }else if( name.equals( CLASS_NAME )){
            return getBeanClass();
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
        
        //TODO: custom names
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
    
    private void validate(){
       if( result != null ){
          throw new IllegalStateException("Bean is generated");
        }
    }
    public Class getBeanClass(){
        
        //TODO: serialization
        
        if( result != null ){
          return result; 
        }
        
        ClassGen cg = new ClassGen( CLASS_NAME , Object.class.getName(),SOURCE_FILE,
        ACC_PUBLIC  , null );
        
        cg.addEmptyConstructor( ACC_PUBLIC );
        
        for( Iterator i = properties.iterator(); i.hasNext();  ){
            BeanProperty prop = (BeanProperty)i.next();
            generateProperty( cg, prop.getName(), prop.getType() );
            
        }
        
        for( Iterator i = methods.iterator(); i.hasNext();  ){
            BeanMethod m = (BeanMethod)i.next();
            generateMethod( cg, m.getName(), m.getMethod() );
            
        }
        
        byte b[] = cg.getJavaClass().getBytes();
        result = defineClass( cg.getClassName(), b, 0, b.length );
        
        
        return result;
        
        
    }
    
    public BeanGenerator copyMethods( Class clasz ){
        
        validate();
        
        java.lang.reflect.Method methods[] = clasz.getMethods();
        
        for( int i = 0; i< methods.length; i++ ){
            
            int mod = methods[i].getModifiers();
            
            if( java.lang.reflect.Modifier.isStatic(mod) &&
            java.lang.reflect.Modifier.isPublic(mod ) ){
                
                this.methods.add( new BeanGenerator.BeanMethod( methods[i]));
                
            }
            
        }
        return this;
    }
    
    public BeanGenerator addMethod( java.lang.reflect.Method method, Map attributes ){
        
        validate();
        
        int mod = method.getModifiers();
        if( !java.lang.reflect.Modifier.isStatic(mod) ||
        ! java.lang.reflect.Modifier.isPublic(mod ) ){
            
            throw new IllegalArgumentException( method.toString() );
        }
        
        BeanGenerator.BeanMethod bm = new BeanGenerator.BeanMethod(method);
        if(attributes != null){
          bm.getAttributes().putAll(attributes);
        } 
        methods.add( bm );
        
        return this;
    }
    
    
    
    public BeanGenerator addProperties( java.util.Map props ){
        validate();
        for( java.util.Iterator i = props.entrySet().iterator(); i.hasNext();  ){
            Map.Entry entry = (Map.Entry)i.next();
            addProperty((String)entry.getKey(),(Class)entry.getValue(),null);
        }
        
        return this;
    }
    
    protected Map getAttributes( FeatureDescriptor d ){
            Map attributes = new HashMap();
            Enumeration names = d.attributeNames();
            while( names.hasMoreElements() ){
              String name = (String)names.nextElement() ;  
              attributes.put( name, d.getValue( name ) );
            }
            return attributes;
        }
       
    
    public BeanGenerator addProperties( java.beans.PropertyDescriptor[] descriptors ){
        
        for( int i = 0; i < descriptors.length; i++  ){
            addProperty(descriptors[i].getName(),
            descriptors[i].getPropertyType(),getAttributes(descriptors[i]));
        }
        
        return this;
    }
    
    
    
    public BeanGenerator addProperty( String name, Class type, Map attributes ){
        
        validate();
        
        if( names.contains(name) ){
            throw new IllegalArgumentException("Dublicate property name " + name);
        }
        
        names.add( name );
        BeanProperty bp = new BeanProperty( name, type);
        if( attributes != null ){
             bp.getAttributes().putAll( attributes );
        }
        properties.add( bp );
        
        return this;
    }
    
    abstract public class Feature {
        
      final  private String name;
      final  private Map attributes = new HashMap();
        Feature(String name){
            this.name = name;
        }
        public String getName(){
            return name;
        }
        public Map getAttributes(){
          return attributes;
        }
        
         protected void copyAttributes(FeatureDescriptor d){
            for( Iterator i = attributes.entrySet().iterator(); i.hasNext() ; ){
               Map.Entry entry = (Map.Entry)i.next(); 
               d.setValue((String)entry.getKey(), entry.getValue() );
            }
        }
    }
  
    //TODO: indexed properties
    
  final public class BeanProperty extends Feature{
        
        final private Class type;
        PropertyDescriptor descriptor;
        
        BeanProperty( String name, Class type ){
            super(name);
            this.type = type;
        }
        public Class getType(){
            return type;
        }
        public PropertyDescriptor toPropertyDescriptor(){
            if(descriptor == null ){
                
             try{
                 //TODO: custom names
              String mName =  Character.toUpperCase( getName().charAt(0) ) +
                                getName().substring(1);
              
              String get = ( Boolean.TYPE == type ? "is" : "get" ) + mName;
              String set = "set" + mName;
              
              
              java.lang.reflect.Method getM = null;
              java.lang.reflect.Method setM = null;
              try{
               getM = BeanGenerator.this.result.
                   getMethod( get , null );
               }catch( NoSuchMethodException nsme ){
               
               }
              try{
               setM  = BeanGenerator.this.result.
                   getMethod( set , new Class[]{type} );
               }catch( NoSuchMethodException nsme ){
               
               }
              
               descriptor = new PropertyDescriptor( getName(), getM, setM   );  
                 
             }catch (IntrospectionException ie ){
                 
                 throw new NoSuchMethodError( ie.getMessage() );
             }
               copyAttributes( descriptor );
            }   
            
            return descriptor;
        }
        
    }
    
   final public class BeanMethod extends Feature{
       
        private java.lang.reflect.Method method;
        MethodDescriptor descriptor;
        
        BeanMethod(java.lang.reflect.Method method ){
            super(method.getName());
            this.method = method;
        }
        
        public java.lang.reflect.Method  getMethod(){
            return method;
        }
        
        public MethodDescriptor toMethodDescriptor(){
            if(descriptor == null ){
             try{
                 
              java.lang.reflect.Method beanMethod = BeanGenerator.this.result.
               getMethod( getName(), method.getParameterTypes() ); 
               descriptor = new MethodDescriptor( beanMethod );  
               
             }catch( NoSuchMethodException nsme ){
                 
                 throw new NoSuchMethodError( nsme.getMessage() );
             }
               copyAttributes( descriptor );
            }   
            return descriptor; 
        }
    }
    
    
}
