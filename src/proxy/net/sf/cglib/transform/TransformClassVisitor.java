package net.sf.cglib.transform;

import java.io.*;


import net.sf.cglib.util.Opcodes;
import org.objectweb.asm.*;

import java.lang.reflect.Modifier;


/**
 *
 * @author  baliuka
 */
public class TransformClassVisitor implements ClassVisitor{
    
    ReadWriteFieldFilter filter;
    ClassWriter cw = new ClassWriter(true);
    ClassReader cr;
    String className;
    boolean addedCallback = false;
    static String callbackDesc = Type.getType( ReadWriteFieldCallback.class ).getDescriptor();
    static String callbackName = ReadWriteFieldCallback.class.getName().replace('.','/');
    
    
    /** Creates a new instance of TransformClassVisitor */
    public TransformClassVisitor(InputStream is, ReadWriteFieldFilter filter)throws java.io.IOException {
      
       this.filter = filter;
       cr = new ClassReader(is);
       
    }
    
    public byte[] transform(){
         cr.accept(this, false);
         return cw.toByteArray();
    }
    
    public void visit(int access, String name, String superName, String[] interfaces, String sourceFile) {
        className = name;
        //TODO: generate get/set callback
        cw.visit(access,name,superName, interfaces, sourceFile  );
    }
    
    public void visitEnd() {
        cw.visitEnd();
    }
    
    private void addWriteMethod(String name, String desc){
  
        
        Type type = Type.getType(desc);
        
        CodeVisitor cv = cw.visitMethod( 
                             Modifier.PUBLIC , 
                             Signature.writeMethod( name ), 
                             Signature.writeMethodSignature(desc),
                             new  String[]{} );
        
        cv.visitVarInsn ( Opcodes.ALOAD, 0 );//this
        cv.visitFieldInsn( 
                        Opcodes.GETFIELD,
                        className,
                        Signature.READ_WRITE_CALLBACK,
                        callbackDesc               
                  );                     
        
        Label ifNull = new Label();
        cv.visitJumpInsn(Opcodes.IFNULL,ifNull);
        cv.visitVarInsn ( Opcodes.ALOAD, 0 );//this
        cv.visitVarInsn ( Opcodes.ALOAD, 0 );//this
        cv.visitFieldInsn( 
                        Opcodes.GETFIELD,
                        className,
                        Signature.READ_WRITE_CALLBACK,
                        callbackDesc               
                  );                     
        
        cv.visitVarInsn ( Opcodes.ALOAD, 0 );//this
        cv.visitLdcInsn(name);
        cv.visitVarInsn ( Opcodes.ALOAD, 0 );//this
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
       cv.visitVarInsn ( Opcodes.ALOAD, 0 );//this 
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
        cv.visitVarInsn ( Opcodes.ALOAD, 0 );//this                        
        cv.visitFieldInsn( 
                        Opcodes.GETFIELD,
                        className,
                        Signature.READ_WRITE_CALLBACK,
                        callbackDesc               
                  );                     
        Label ifNull = new Label();
        cv.visitJumpInsn(Opcodes.IFNULL,ifNull);
        cv.visitVarInsn ( Opcodes.ALOAD, 0 );//this
        cv.visitFieldInsn( 
                        Opcodes.GETFIELD,
                        className,
                        Signature.READ_WRITE_CALLBACK,
                        callbackDesc               
                  );                     
        cv.visitVarInsn ( Opcodes.ALOAD, 0 );//this
        cv.visitLdcInsn(name);
        cv.visitVarInsn ( Opcodes.ALOAD, 0 );//this
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
       cv.visitVarInsn ( Opcodes.ALOAD, 0 );//this
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
   
    
    private void addCallbackField(){
        
        if(!addedCallback){
            cw.visitField(
               Modifier.PRIVATE|Modifier.TRANSIENT,
               Signature.READ_WRITE_CALLBACK,
               callbackDesc , 
               null 
               );
             addedCallback = true;
          }
    }
    
    public void visitField(int access, String name, String desc, Object value) {
        
       
        
        if( filter.acceptRead( Type.getType("L" + className + ";").getClassName(), name)){
          
          addCallbackField();   
          addReadMethod(name, desc);
        
        }
        
        if( filter.acceptWrite(  Type.getType("L" + className + ";").getClassName(), name)){
            
           addCallbackField(); 
           addWriteMethod(name, desc);
        }
        
        
        cw.visitField(access, name, desc, value );
    }
    
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        cw.visitInnerClass( name, outerName, innerName,  access  );
    }
    
    public CodeVisitor visitMethod(int access, String name, String desc, String[] exceptions) {
        return new TransformCodeVisitor( cw.visitMethod(access, name, desc, exceptions  ),filter); 
    }
    
}
