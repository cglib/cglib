package net.sf.cglib.transform;

import java.io.*;


import net.sf.cglib.core.*;

import org.objectweb.asm.*;
import org.objectweb.asm.Label;

import java.util.*;
import java.lang.reflect.*;
import net.sf.cglib.core.Constants;


/**
 *
 * @author  baliuka
 */
public class FieldTransformer extends ClassTransformer{
    
    ReadWriteFieldFilter filter;
    String className;   
    Set interfaces     = new HashSet();
    Map fields         = new HashMap();
    Map types          = new HashMap();
 
 
    
    static String callbackDesc = Type.getType( ReadWriteFieldCallback.class ).getDescriptor();
    static String callbackName = ReadWriteFieldCallback.class.getName().replace('.','/');
    
    
    /** Creates a new instance of TransformClassVisitor */
    public FieldTransformer(ReadWriteFieldFilter filter) throws java.io.IOException {
        
        this.filter = filter;
        interfaces.add(Signature.getInternalName(Transformed.class));
        
    }
    
    
    
    
    
    
    public void visit(int access, String name, String superName, String[] ifaces, String sourceFile) {
        if( Modifier.isInterface(access) ){
         throw new IllegalArgumentException("can not transform interface " + name);
        }
        try{
            className = name;
            interfaces.addAll(Arrays.asList(ifaces));
            
       getTarget().visit(
            access,
            name,
            superName,
            (String[])interfaces.toArray( new String[]{}),
            sourceFile
            );
            addCallbackField();
            implemetTransform();
            

        }catch(Exception e){
            throw new CodeGenerationException(e);
        }
    }
    
    public void visitEnd() {
        
        String types[] = {"Z","C","B","S","I","F","J","D","Ljava/lang/Object;"};
        for( int i = 0; i < types.length; i++  ){
            
            List fields = getFields(null,types[i]);
                
           
            CodeVisitor cv = getTarget().visitMethod(
                              Modifier.PUBLIC,
                              Signature.fieldGetName( types[i] ),
                              Signature.fieldGetSignature(types[i]),
                              new  String[]{} 
                );
            
            
            for(Iterator iterator = fields.iterator(); iterator.hasNext();  ){
                
             String name = iterator.next().toString();   
             Type type = Type.getType(this.types.get(name).toString());
             cv.visitVarInsn(Constants.ALOAD,1);     
             cv.visitLdcInsn(name);
             cv.visitMethodInsn(Constants.INVOKEVIRTUAL,"java/lang/Object","equals","(Ljava/lang/Object;)Z");
             Label label = new Label();
             cv.visitJumpInsn( Constants.IFEQ, label );
             cv.visitVarInsn(  Constants.ALOAD, 0 );
             cv.visitFieldInsn( Constants.GETFIELD, className, name, type.getDescriptor() );
             cv.visitInsn( type.getOpcode(Constants.IRETURN) );   
             cv.visitLabel(label);
            }
           cv.visitTypeInsn(Constants.NEW,"java/lang/NoSuchFieldError"); 
           cv.visitInsn(Constants.DUP);
           cv.visitMethodInsn(Constants.INVOKESPECIAL,"java/lang/NoSuchFieldError","<init>","()V");
           cv.visitInsn(Constants.ATHROW);  
           
           cv.visitMaxs(0, 0);     
           
           
           cv = getTarget().visitMethod(
                              Modifier.PUBLIC,
                              Signature.fieldSetName( types[i] ),
                              Signature.fieldSetSignature(types[i]),
                              new  String[]{} 
                );
           for(Iterator iterator = fields.iterator(); iterator.hasNext();  ){
                
             String name = iterator.next().toString();   
             Type type = Type.getType(this.types.get(name).toString());
             
             cv.visitVarInsn(Constants.ALOAD,1);
             cv.visitLdcInsn(name);
             cv.visitMethodInsn(
                  Constants.INVOKEVIRTUAL,
                  Type.getType(Object.class).getInternalName(),
                  "equals",
                  "(Ljava/lang/Object;)Z"
                  );
             Label label = new Label();
             cv.visitJumpInsn(Constants.IFEQ, label );
             cv.visitVarInsn(Constants.ALOAD, 0 );
             cv.visitFieldInsn(Constants.GETFIELD, className, name, type.getDescriptor() );
             cv.visitVarInsn(type.getOpcode(Constants.ISTORE),3 + type.getSize() - 1);
             cv.visitVarInsn(Constants.ALOAD, 0 );
             cv.visitVarInsn(type.getOpcode(Constants.ILOAD),2);
             if(type.getSort() == Type.OBJECT){
              cv.visitTypeInsn(Constants.CHECKCAST, type.getInternalName() );
             }
             cv.visitFieldInsn(Constants.PUTFIELD, className, name, type.getDescriptor() );
             cv.visitVarInsn(type.getOpcode(Constants.ILOAD),3 + type.getSize() - 1);
             cv.visitInsn(type.getOpcode(Constants.IRETURN));
             cv.visitLabel(label);
           }     
                
           cv.visitTypeInsn(Constants.NEW,"java/lang/NoSuchFieldError"); 
           cv.visitInsn(Constants.DUP);
           cv.visitMethodInsn(Constants.INVOKESPECIAL,"java/lang/NoSuchFieldError","<init>","()V");
           cv.visitInsn(Constants.ATHROW);
           
           cv.visitMaxs(0, 0);          
        }
        
        
        getTarget().visitEnd();
    }
    
    private void addWriteMethod(String name, String desc){
        
        
        Type type = Type.getType(desc);
        
        CodeVisitor cv = getTarget().visitMethod(
        Modifier.PUBLIC,
        Signature.writeMethod( name ),
        Signature.writeMethodSignature(desc),
        new  String[]{} );
        
        cv.visitVarInsn( Constants.ALOAD, 0 );//this
        
        cv.visitFieldInsn(
        Constants.GETFIELD,
        className,
        Signature.READ_WRITE_CALLBACK,
        callbackDesc
        );
        
        Label ifNull = new Label();
        cv.visitJumpInsn(Constants.IFNULL,ifNull);
        cv.visitVarInsn( Constants.ALOAD, 0 );//this
        cv.visitVarInsn( Constants.ALOAD, 0 );//this
        
        cv.visitFieldInsn(
        Constants.GETFIELD,
        className,
        Signature.READ_WRITE_CALLBACK,
        callbackDesc
        );
        
        cv.visitVarInsn( Constants.ALOAD, 0 );//this
        cv.visitLdcInsn(name);
        cv.visitVarInsn( Constants.ALOAD, 0 );//this
        
        cv.visitFieldInsn(
        Constants.GETFIELD,
        className,
        name,
        desc
        );
        cv.visitVarInsn(type.getOpcode(Constants.ILOAD), 1 );
        
        cv.visitMethodInsn(
        Constants.INVOKEINTERFACE,
        callbackName,
        Signature.writeCallbackName(desc),
        Signature.writeCallbackSignature(desc)
        );
        if(Signature.isObject(desc)){
            cv.visitTypeInsn(Constants.CHECKCAST, Type.getType(desc).getClassName().replace('.','/') );
        }
        cv.visitFieldInsn(
        Constants.PUTFIELD,
        className,
        name,
        desc
        );
        
        Label go = new Label();
        cv.visitJumpInsn(Constants.GOTO,go);
        cv.visitLabel(ifNull);
        cv.visitVarInsn( Constants.ALOAD, 0 );//this
        cv.visitVarInsn(type.getOpcode(Constants.ILOAD), 1 );
        
        cv.visitFieldInsn(
        Constants.PUTFIELD,
        className,
        name,
        desc
        );
        cv.visitLabel(go);
        cv.visitInsn( Constants.RETURN );
        cv.visitMaxs(0, 0);
        
    }
    
    private void addReadMethod(String name, String desc){
        
        Type type = Type.getType(desc);
        
        CodeVisitor cv = getTarget().visitMethod(
        Modifier.PUBLIC ,
        Signature.readMethod( name ),
        Signature.readMethodSignature(desc),
        new  String[]{} );
        cv.visitVarInsn( Constants.ALOAD, 0 );//this
        
        cv.visitFieldInsn(
        Constants.GETFIELD,
        className,
        Signature.READ_WRITE_CALLBACK,
        callbackDesc
        );
        Label ifNull = new Label();
        cv.visitJumpInsn(Constants.IFNULL,ifNull);
        cv.visitVarInsn( Constants.ALOAD, 0 );//this
        cv.visitFieldInsn(
        Constants.GETFIELD,
        className,
        Signature.READ_WRITE_CALLBACK,
        callbackDesc
        );
        cv.visitVarInsn( Constants.ALOAD, 0 );//this
        cv.visitLdcInsn(name);
        cv.visitVarInsn( Constants.ALOAD, 0 );//this
        
        cv.visitFieldInsn(
        Constants.GETFIELD,
        className,
        name,
        desc
        );
        
        cv.visitMethodInsn(
        Constants.INVOKEINTERFACE,
        callbackName,
        Signature.readCallbackName(desc),
        Signature.readCallbackSignature(desc)
        );
        if(Signature.isObject(desc)){
            cv.visitTypeInsn(Constants.CHECKCAST, Type.getType(desc).getClassName().replace('.','/') );
        }
        cv.visitInsn( type.getOpcode(Constants.IRETURN) );
        cv.visitLabel(ifNull);
        cv.visitVarInsn( Constants.ALOAD, 0 );//this
        
        cv.visitFieldInsn(
        Constants.GETFIELD,
        className,
        name,
        desc
        );
        if(Signature.isObject(desc)){
            cv.visitTypeInsn(Constants.CHECKCAST, Type.getType(desc).getClassName().replace('.','/') );
        }
        cv.visitInsn( type.getOpcode(Constants.IRETURN) );
        cv.visitMaxs(0, 0);
        
    }
    
    private void implemetTransform(){
        
        CodeVisitor cv =  getTarget().visitMethod(Modifier.PUBLIC, "setReadWriteFieldCallback",
        Type.getMethodDescriptor(
        ReflectUtils.findMethod("net.sf.cglib.transform.Transformed." +
        "setReadWriteFieldCallback(net.sf.cglib.transform." +
        "ReadWriteFieldCallback)") ) , new String[]{} );
        
        cv.visitVarInsn(Constants.ALOAD, 0 );
        cv.visitVarInsn(Constants.ALOAD, 1 );
        
        cv.visitFieldInsn(
        Constants.PUTFIELD,
        className,
        Signature.READ_WRITE_CALLBACK,
        callbackDesc
        );
        
        cv.visitInsn(Constants.RETURN);
        cv.visitMaxs(0,0);
        
        
        cv =  getTarget().visitMethod(Modifier.PUBLIC, "getReadWriteFieldCallback",
        
        Type.getMethodDescriptor(
        ReflectUtils.findMethod("net.sf.cglib.transform.Transformed." +
        "getReadWriteFieldCallback()") ) , new String[]{} );
        
        cv.visitVarInsn(Constants.ALOAD, 0 );
        
        cv.visitFieldInsn(
        Constants.GETFIELD,
        className,
        Signature.READ_WRITE_CALLBACK,
        callbackDesc
        );
        
        cv.visitInsn(Constants.ARETURN);
        cv.visitMaxs(0,0);
        /*
        cv =  getTarget().visitMethod(Modifier.PUBLIC, "getDelegate",
        
        Type.getMethodDescriptor(
        ReflectUtils.findMethod("net.sf.cglib.transform.Transformed." +
        "getDelegate()") ) , new String[]{} );
        
        cv.visitVarInsn(Constants.ALOAD, 0 );
        
        cv.visitFieldInsn(
        Constants.GETFIELD,
        className,
        Signature.DELEGATE,
        Type.getDescriptor(Object.class)
        );
        
        cv.visitInsn(Constants.ARETURN);
        cv.visitMaxs(0,0);
        
        
        cv =  getTarget().visitMethod(Modifier.PUBLIC, "setDelegate",
        Type.getMethodDescriptor(
        ReflectUtils.findMethod("net.sf.cglib.transform.Transformed." +
        "setDelegate(Object)") ) , new String[]{} );
        
        cv.visitVarInsn(Constants.ALOAD, 0 );
        cv.visitVarInsn(Constants.ALOAD, 1 );
        
        cv.visitFieldInsn(
        Constants.PUTFIELD,
        className,
        Signature.DELEGATE,
        Type.getDescriptor(Object.class)
        );
        
        cv.visitInsn(Constants.RETURN);
        cv.visitMaxs(0,0);
         */
        
    }
    
    
    private void addCallbackField(){
        
        
        getTarget().visitField(
        Modifier.PRIVATE|Modifier.TRANSIENT,
        Signature.READ_WRITE_CALLBACK,
        callbackDesc ,
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
    
      if(!Modifier.isStatic(access)){
            
           getFields(name,desc);
        
        if( filter.acceptRead( Type.getType("L" + className + ";").getClassName(), name)){
            
            addReadMethod(name, desc);
            
        }
        
        if( filter.acceptWrite(  Type.getType("L" + className + ";").getClassName(), name)){
            
            addWriteMethod(name, desc);
        }
      }    
        
        getTarget().visitField(access, name, desc, value );
    }
    
    
    
    public CodeVisitor visitMethod(int access, String name, String desc, String[] exceptions) {
        CodeVisitor cv = getTarget().visitMethod(access, name, desc, exceptions  );
        
        return new TransformCodeVisitor( this, cv, filter );
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


class TransformCodeVisitor extends CodeAdapter {
    
    CodeVisitor cv;
    ReadWriteFieldFilter filter;
    FieldTransformer tcv;
    /** Creates a new instance of TransformCodeVisitor */
    public TransformCodeVisitor(FieldTransformer tcv ,CodeVisitor cv, ReadWriteFieldFilter filter) {
        super(cv);
        this.cv = cv;
        this.filter = filter;
        this.tcv = tcv;
    }
    
    
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        
        switch( opcode  ){
        
            case Constants.GETFIELD :
                
                if(filter.acceptRead(Type.getType("L" + owner + ";").getClassName(), name )){
                    
                    cv.visitMethodInsn( 
                    
                            Constants.INVOKEVIRTUAL ,
                            owner, 
                            Signature.readMethod(name), 
                            Type.getMethodDescriptor(Type.getType(desc), new Type[]{}) 
                            
                          );//visitMethodInsn
                    
                    return;
                    
                }else{
                    
                  break;
                }
                
                
            case Constants.PUTFIELD :    
                
                if(filter.acceptWrite(Type.getType("L" + owner + ";").getClassName(), name )){
                     
                    cv.visitMethodInsn( 
                            Constants.INVOKEVIRTUAL ,
                            owner, 
                            Signature.writeMethod(name), 
                            Type.getMethodDescriptor(
                            
                                                    Type.VOID_TYPE , 
                                                    new Type[]{Type.getType(desc)}
                    
                                         )//getMethodDescriptor 
                          );//visitMethodInsn
                    
                    return;
                    
                }else{
                    
                  break;
                }
                
            default:    
                break;
        }
        cv.visitFieldInsn(opcode,owner,name, desc );    
    }
    
    
    
    
    
}

