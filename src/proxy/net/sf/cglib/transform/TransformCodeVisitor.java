package net.sf.cglib.transform;

import org.objectweb.asm.*;
import net.sf.cglib.core.Constants;
/**
 *
 * @author  baliuka
 */
 class TransformCodeVisitor extends CodeAdapter {
    
    CodeVisitor cv;
    ReadWriteFieldFilter filter;
    TransformClassVisitor tcv;
    /** Creates a new instance of TransformCodeVisitor */
    public TransformCodeVisitor(TransformClassVisitor tcv ,CodeVisitor cv, ReadWriteFieldFilter filter) {
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
