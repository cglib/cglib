package net.sf.cglib.transform;

import java.io.*;


import net.sf.cglib.util.Opcodes;
import net.sf.cglib.util.ReflectUtils;
import net.sf.cglib.core.*;

import org.objectweb.asm.*;
import org.objectweb.asm.Label;

import java.util.*;
import java.lang.reflect.*;


/**
 *
 * @author  baliuka
 */
public class TransformClassVisitor implements ClassVisitor{
    
    ReadWriteFieldFilter filter;
    ClassWriter cw     = new ClassWriter(true);
    Set interfaces     = new HashSet();
    Class delegateIf   = null ;
    Class delegateImpl = null ;
    Map fields         = new HashMap();
    Map types         = new HashMap();
    
    
    ClassReader cr;
    String className;
    
    static String callbackDesc = Type.getType( ReadWriteFieldCallback.class ).getDescriptor();
    static String callbackName = ReadWriteFieldCallback.class.getName().replace('.','/');
    
    
    /** Creates a new instance of TransformClassVisitor */
    public TransformClassVisitor(InputStream is, ReadWriteFieldFilter filter)throws java.io.IOException {
        
        this.filter = filter;
        cr = new ClassReader(is);
        interfaces.add(Signature.getInternalName(Transformed.class));
        
    }
    
    public void setDelegate(Class delegateIf, Class delegateImpl ){
        try{
         delegateImpl.getConstructor( new Class[]{Object.class});
         this.delegateIf = delegateIf;
         this.delegateImpl = delegateImpl;
        }catch(Exception e){
          throw new CodeGenerationException(e);
        }
    }
    
    public byte[] transform(){
        cr.accept(this, false);
        return cw.toByteArray();
    }
    
    
    
    
    public void visit(int access, String name, String superName, String[] ifaces, String sourceFile) {
        try{
            className = name;
            interfaces.addAll(Arrays.asList(ifaces));
            
            if(delegateIf != null){
                
                interfaces.add(Signature.getInternalName(delegateIf));
                
            }
            cw.visit(
            access,
            name,
            superName,
            (String[])interfaces.toArray( new String[]{}),
            sourceFile
            );
            addCallbackField();
            implemetTransform();
            
            if(delegateIf != null){
                
                implementDelegate();
                
            }
        }catch(Exception e){
            throw new CodeGenerationException(e);
        }
    }
    
    public void visitEnd() {
        
        String types[] = {"Z","C","B","S","I","F","J","D","Ljava/lang/Object;"};
        for( int i = 0; i < types.length; i++  ){
            
            List fields = getFields(null,types[i]);
                
           
            CodeVisitor cv = cw.visitMethod(
                              Modifier.PUBLIC,
                              Signature.fieldGetName( types[i] ),
                              Signature.fieldGetSignature(types[i]),
                              new  String[]{} 
                );
            
            
            for(Iterator iterator = fields.iterator(); iterator.hasNext();  ){
                
             String name = iterator.next().toString();   
             Type type = Type.getType(this.types.get(name).toString());
             cv.visitVarInsn(Opcodes.ALOAD,1);     
             cv.visitLdcInsn(name);
             cv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,"java/lang/Object","equals","(Ljava/lang/Object;)Z");
             Label label = new Label();
             cv.visitJumpInsn( Opcodes.IFEQ, label );
             cv.visitVarInsn(  Opcodes.ALOAD, 0 );
             cv.visitFieldInsn( Opcodes.GETFIELD, className, name, type.getDescriptor() );
             cv.visitInsn( type.getOpcode(Opcodes.IRETURN) );   
             cv.visitLabel(label);
            }
           cv.visitTypeInsn(Opcodes.NEW,"java/lang/NoSuchFieldError"); 
           cv.visitInsn(Opcodes.DUP);
           cv.visitMethodInsn(Opcodes.INVOKESPECIAL,"java/lang/NoSuchFieldError","<init>","()V");
           cv.visitInsn(Opcodes.ATHROW);  
           
           cv.visitMaxs(0, 0);     
           
           
           cv = cw.visitMethod(
                              Modifier.PUBLIC,
                              Signature.fieldSetName( types[i] ),
                              Signature.fieldSetSignature(types[i]),
                              new  String[]{} 
                );
           for(Iterator iterator = fields.iterator(); iterator.hasNext();  ){
                
             String name = iterator.next().toString();   
             Type type = Type.getType(this.types.get(name).toString());
             
             cv.visitVarInsn(Opcodes.ALOAD,1);
             cv.visitLdcInsn(name);
             cv.visitMethodInsn(
                  Opcodes.INVOKEVIRTUAL,
                  Type.getType(Object.class).getInternalName(),
                  "equals",
                  "(Ljava/lang/Object;)Z"
                  );
             Label label = new Label();
             cv.visitJumpInsn(Opcodes.IFEQ, label );
             cv.visitVarInsn(Opcodes.ALOAD, 0 );
             cv.visitFieldInsn(Opcodes.GETFIELD, className, name, type.getDescriptor() );
             cv.visitVarInsn(type.getOpcode(Opcodes.ISTORE),3 + type.getSize() - 1);
             cv.visitVarInsn(Opcodes.ALOAD, 0 );
             cv.visitVarInsn(type.getOpcode(Opcodes.ILOAD),2);
             if(type.getSort() == Type.OBJECT){
              cv.visitTypeInsn(Opcodes.CHECKCAST, type.getInternalName() );
             }
             cv.visitFieldInsn(Opcodes.PUTFIELD, className, name, type.getDescriptor() );
             cv.visitVarInsn(type.getOpcode(Opcodes.ILOAD),3 + type.getSize() - 1);
             cv.visitInsn(type.getOpcode(Opcodes.IRETURN));
             cv.visitLabel(label);
           }     
                
           cv.visitTypeInsn(Opcodes.NEW,"java/lang/NoSuchFieldError"); 
           cv.visitInsn(Opcodes.DUP);
           cv.visitMethodInsn(Opcodes.INVOKESPECIAL,"java/lang/NoSuchFieldError","<init>","()V");
           cv.visitInsn(Opcodes.ATHROW);
           
           cv.visitMaxs(0, 0);          
        }
        
        
        cw.visitEnd();
    }
    
    private void addWriteMethod(String name, String desc){
        
        
        Type type = Type.getType(desc);
        
        CodeVisitor cv = cw.visitMethod(
        Modifier.PUBLIC,
        Signature.writeMethod( name ),
        Signature.writeMethodSignature(desc),
        new  String[]{} );
        
        cv.visitVarInsn( Opcodes.ALOAD, 0 );//this
        
        cv.visitFieldInsn(
        Opcodes.GETFIELD,
        className,
        Signature.READ_WRITE_CALLBACK,
        callbackDesc
        );
        
        Label ifNull = new Label();
        cv.visitJumpInsn(Opcodes.IFNULL,ifNull);
        cv.visitVarInsn( Opcodes.ALOAD, 0 );//this
        cv.visitVarInsn( Opcodes.ALOAD, 0 );//this
        
        cv.visitFieldInsn(
        Opcodes.GETFIELD,
        className,
        Signature.READ_WRITE_CALLBACK,
        callbackDesc
        );
        
        cv.visitVarInsn( Opcodes.ALOAD, 0 );//this
        cv.visitLdcInsn(name);
        cv.visitVarInsn( Opcodes.ALOAD, 0 );//this
        
        cv.visitFieldInsn(
        Opcodes.GETFIELD,
        className,
        name,
        desc
        );
        cv.visitVarInsn(type.getOpcode(Opcodes.ILOAD), 1 );
        
        cv.visitMethodInsn(
        Opcodes.INVOKEINTERFACE,
        callbackName,
        Signature.writeCallbackName(desc),
        Signature.writeCallbackSignature(desc)
        );
        if(Signature.isObject(desc)){
            cv.visitTypeInsn(Opcodes.CHECKCAST, Type.getType(desc).getClassName().replace('.','/') );
        }
        cv.visitFieldInsn(
        Opcodes.PUTFIELD,
        className,
        name,
        desc
        );
        
        Label go = new Label();
        cv.visitJumpInsn(Opcodes.GOTO,go);
        cv.visitLabel(ifNull);
        cv.visitVarInsn( Opcodes.ALOAD, 0 );//this
        cv.visitVarInsn(type.getOpcode(Opcodes.ILOAD), 1 );
        
        cv.visitFieldInsn(
        Opcodes.PUTFIELD,
        className,
        name,
        desc
        );
        cv.visitLabel(go);
        cv.visitInsn( Opcodes.RETURN );
        cv.visitMaxs(0, 0);
        
    }
    
    private void addReadMethod(String name, String desc){
        
        Type type = Type.getType(desc);
        
        CodeVisitor cv = cw.visitMethod(
        Modifier.PUBLIC ,
        Signature.readMethod( name ),
        Signature.readMethodSignature(desc),
        new  String[]{} );
        cv.visitVarInsn( Opcodes.ALOAD, 0 );//this
        
        cv.visitFieldInsn(
        Opcodes.GETFIELD,
        className,
        Signature.READ_WRITE_CALLBACK,
        callbackDesc
        );
        Label ifNull = new Label();
        cv.visitJumpInsn(Opcodes.IFNULL,ifNull);
        cv.visitVarInsn( Opcodes.ALOAD, 0 );//this
        cv.visitFieldInsn(
        Opcodes.GETFIELD,
        className,
        Signature.READ_WRITE_CALLBACK,
        callbackDesc
        );
        cv.visitVarInsn( Opcodes.ALOAD, 0 );//this
        cv.visitLdcInsn(name);
        cv.visitVarInsn( Opcodes.ALOAD, 0 );//this
        
        cv.visitFieldInsn(
        Opcodes.GETFIELD,
        className,
        name,
        desc
        );
        
        cv.visitMethodInsn(
        Opcodes.INVOKEINTERFACE,
        callbackName,
        Signature.readCallbackName(desc),
        Signature.readCallbackSignature(desc)
        );
        if(Signature.isObject(desc)){
            cv.visitTypeInsn(Opcodes.CHECKCAST, Type.getType(desc).getClassName().replace('.','/') );
        }
        cv.visitInsn( type.getOpcode(Opcodes.IRETURN) );
        cv.visitLabel(ifNull);
        cv.visitVarInsn( Opcodes.ALOAD, 0 );//this
        
        cv.visitFieldInsn(
        Opcodes.GETFIELD,
        className,
        name,
        desc
        );
        if(Signature.isObject(desc)){
            cv.visitTypeInsn(Opcodes.CHECKCAST, Type.getType(desc).getClassName().replace('.','/') );
        }
        cv.visitInsn( type.getOpcode(Opcodes.IRETURN) );
        cv.visitMaxs(0, 0);
        
    }
    
    private void implemetTransform(){
        
        CodeVisitor cv =  cw.visitMethod(Modifier.PUBLIC, "setReadWriteFieldCallback",
        Type.getMethodDescriptor(
        ReflectUtils.findMethod("net.sf.cglib.transform.Transformed." +
        "setReadWriteFieldCallback(net.sf.cglib.transform." +
        "ReadWriteFieldCallback)") ) , new String[]{} );
        
        cv.visitVarInsn(Opcodes.ALOAD, 0 );
        cv.visitVarInsn(Opcodes.ALOAD, 1 );
        
        cv.visitFieldInsn(
        Opcodes.PUTFIELD,
        className,
        Signature.READ_WRITE_CALLBACK,
        callbackDesc
        );
        
        cv.visitInsn(Opcodes.RETURN);
        cv.visitMaxs(0,0);
        
        
        cv =  cw.visitMethod(Modifier.PUBLIC, "getReadWriteFieldCallback",
        
        Type.getMethodDescriptor(
        ReflectUtils.findMethod("net.sf.cglib.transform.Transformed." +
        "getReadWriteFieldCallback()") ) , new String[]{} );
        
        cv.visitVarInsn(Opcodes.ALOAD, 0 );
        
        cv.visitFieldInsn(
        Opcodes.GETFIELD,
        className,
        Signature.READ_WRITE_CALLBACK,
        callbackDesc
        );
        
        cv.visitInsn(Opcodes.ARETURN);
        cv.visitMaxs(0,0);
        
        cv =  cw.visitMethod(Modifier.PUBLIC, "getDelegate",
        
        Type.getMethodDescriptor(
        ReflectUtils.findMethod("net.sf.cglib.transform.Transformed." +
        "getDelegate()") ) , new String[]{} );
        
        cv.visitVarInsn(Opcodes.ALOAD, 0 );
        
        cv.visitFieldInsn(
        Opcodes.GETFIELD,
        className,
        Signature.DELEGATE,
        Type.getDescriptor(Object.class)
        );
        
        cv.visitInsn(Opcodes.ARETURN);
        cv.visitMaxs(0,0);
        
        
        cv =  cw.visitMethod(Modifier.PUBLIC, "setDelegate",
        Type.getMethodDescriptor(
        ReflectUtils.findMethod("net.sf.cglib.transform.Transformed." +
        "setDelegate(Object)") ) , new String[]{} );
        
        cv.visitVarInsn(Opcodes.ALOAD, 0 );
        cv.visitVarInsn(Opcodes.ALOAD, 1 );
        
        cv.visitFieldInsn(
        Opcodes.PUTFIELD,
        className,
        Signature.DELEGATE,
        Type.getDescriptor(Object.class)
        );
        
        cv.visitInsn(Opcodes.RETURN);
        cv.visitMaxs(0,0);
        
    }
    
    private void addDelegate(Method m)throws Exception{
        
        
        List exeptions = new ArrayList(m.getExceptionTypes().length);
        for(int i = 0; i< m.getExceptionTypes().length; i++ ){
            exeptions.add(Signature.getInternalName( m.getExceptionTypes()[i]) );
        }
        
        Method delegate = delegateImpl.getMethod(m.getName(), m.getParameterTypes() );
        if(!delegate.getReturnType().getName().equals(m.getReturnType().getName())){
          throw  new IllegalArgumentException( "invalid  delegate signature  " + delegate);
        }
        
        CodeVisitor cv =  cw.visitMethod( Modifier.PUBLIC,
        m.getName(),
        Type.getMethodDescriptor(m),
        (String[])exeptions.toArray(new String[]{}) );
        
        cv.visitVarInsn(Opcodes.ALOAD, 0 );
        cv.visitFieldInsn( Opcodes.GETFIELD,
        className,
        Signature.DELEGATE,
        Type.getDescriptor(Object.class));
        cv.visitTypeInsn(Opcodes.CHECKCAST,Signature.getInternalName(delegateImpl));
        
        for(int i = 1; i <= m.getParameterTypes().length; i++){
            Type type = Type.getType(m.getParameterTypes()[ i - 1]);
            cv.visitVarInsn(type.getOpcode(Opcodes.ILOAD), i );
        }
        
        cv.visitMethodInsn(
        Opcodes.INVOKEVIRTUAL,
        Signature.getInternalName(delegateImpl),
        m.getName(),
        Type.getMethodDescriptor(delegate)
        );
        Type type = Type.getType(m.getReturnType());
        cv.visitInsn(type.getOpcode(Opcodes.IRETURN));
        cv.visitMaxs( 0, 0 );
        
    }
    
    private void implementDelegate()throws Exception{
        
        Method methods[] = delegateIf.getMethods();
        for( int i = 0; i < methods.length; i++  ){
            if( Modifier.isAbstract(methods[i].getModifiers()) ){
                addDelegate(methods[i]);
            }
        }
        
    }
    
    private void addCallbackField(){
        
        
        cw.visitField(
        Modifier.PRIVATE|Modifier.TRANSIENT,
        Signature.READ_WRITE_CALLBACK,
        callbackDesc ,
        null
        );
        
        cw.visitField(
        Modifier.PRIVATE|Modifier.TRANSIENT,
        Signature.DELEGATE,
        Type.getDescriptor(Object.class) ,
        null
        );
        
        
        
    }
    
    private String descriptor(String desc){
        
        if(Signature.isObject(desc)){
            return Type.getDescriptor(Object.class);
        }else{
            return desc;
        }
    }
    
    private List getFields(String name,String desc){
        
        List list = (List)fields.get(descriptor(desc));
        if( list == null ){
            list = new ArrayList();
            fields.put(descriptor(desc), list);
        }
        
        if(name != null){
            list.add(name);
            types.put(name, desc );
        }
        
        return list;
    }
    
    public void visitField(int access, String name, String desc, Object value) {
        
        getFields(name,desc);
        
        if( filter.acceptRead( Type.getType("L" + className + ";").getClassName(), name)){
            
            addReadMethod(name, desc);
            
        }
        
        if( filter.acceptWrite(  Type.getType("L" + className + ";").getClassName(), name)){
            
            addWriteMethod(name, desc);
        }
        
        
        cw.visitField(access, name, desc, value );
    }
    
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        cw.visitInnerClass( name, outerName, innerName,  access  );
    }
    
    public CodeVisitor visitMethod(int access, String name, String desc, String[] exceptions) {
        
        return new TransformCodeVisitor( this, 
                                        cw.visitMethod(access, name, desc, exceptions  ),
                                        filter,
                                         name.equals( "<init>" ) ? delegateImpl : null
                                        );
    }
    
    /** Getter for property className.
     * @return Value of property className.
     *
     */
    public java.lang.String getClassName() {
        return className;
    }
    
    /** Setter for property className.
     * @param className New value of property className.
     *
     */
    public void setClassName(java.lang.String className) {
        this.className = className;
    }
    
}
