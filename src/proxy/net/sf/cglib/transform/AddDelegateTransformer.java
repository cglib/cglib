
package net.sf.cglib.transform;

import net.sf.cglib.core.*;

import java.lang.reflect.*;
import java.util.*;

import org.objectweb.asm.*;
import org.objectweb.asm.Constants;

/**
 *
 * @author  baliuka
 */
public class AddDelegateTransformer extends ClassTransformer {
    
    Class delegateIf[] = null ;
    Class delegateImpl = null ;
    String className;
    
    /** Creates a new instance of AddDelegateTransformer */
    public AddDelegateTransformer(Class delegateIf[], Class delegateImpl) {
        try{
            delegateImpl.getConstructor( new Class[]{Object.class});
            this.delegateIf = delegateIf;
            this.delegateImpl = delegateImpl;
            
        }catch(Exception e){
            throw new CodeGenerationException(e);
        }
    }
    
    
    public void visit(int access, String name, String superName, String[] ifaces, String sourceFile) {
        
        try{
            className = name;
            List interfaces = new ArrayList();
            interfaces.addAll(Arrays.asList(ifaces));
            
            
            for(int i = 0; i< delegateIf.length; i++){
                interfaces.add(Signature.getInternalName(delegateIf[i]));
            }
            
            
            super.visit(
            access,
            name,
            superName,
            (String[])interfaces.toArray( new String[]{}),
            sourceFile
            );
            
       super.visitField(
        Modifier.PRIVATE|Modifier.TRANSIENT,
        Signature.DELEGATE,
        Type.getDescriptor(Object.class) ,
        null
        );
      
            
            implementDelegate();
            
            
        }catch(Exception e){
            throw new CodeGenerationException(e);
        }
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
        
        CodeVisitor cv =  super.visitMethod( Modifier.PUBLIC,
        m.getName(),
        Type.getMethodDescriptor(m),
        (String[])exeptions.toArray(new String[]{}) );
        
        cv.visitVarInsn(Constants.ALOAD, 0 );
        cv.visitFieldInsn( Constants.GETFIELD,
        className,
        Signature.DELEGATE,
        Type.getDescriptor(Object.class));
        cv.visitTypeInsn(Constants.CHECKCAST,Signature.getInternalName(delegateImpl));
        
        for(int i = 1; i <= m.getParameterTypes().length; i++){
            Type type = Type.getType(m.getParameterTypes()[ i - 1]);
            cv.visitVarInsn(type.getOpcode(Constants.ILOAD), i );
        }
        
        cv.visitMethodInsn(
        Constants.INVOKEVIRTUAL,
        Signature.getInternalName(delegateImpl),
        m.getName(),
        Type.getMethodDescriptor(delegate)
        );
        Type type = Type.getType(m.getReturnType());
        cv.visitInsn(type.getOpcode(Constants.IRETURN));
        cv.visitMaxs( 0, 0 );
        
    }
    
    private void implementDelegate()throws Exception{
        for( int i = 0; i <  delegateIf.length; i++ ){
            Method methods[] = delegateIf[i].getMethods();
            for( int j = 0; j < methods.length; j++  ){
                if( Modifier.isAbstract(methods[j].getModifiers()) ){
                    addDelegate(methods[j]);
                }
            }
        }
        
    }
    
     public CodeVisitor visitMethod(int access, String name, String desc, String[] exceptions) {
        CodeVisitor cv = super.visitMethod(access, name, desc, exceptions  );
        if(name.equals("<init>")){
           return new TransformInit(cv);
        }else{
            return cv;
        }
    }
   
    
   class TransformInit extends CodeAdapter {
    
    boolean transFormInit;
    CodeVisitor cv;
    /** Creates a new instance of TransformCodeVisitor */
    public TransformInit( CodeVisitor cv) {
        super(cv);
        this.cv = cv;
        transFormInit = true;
    }
    
    
    
    
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        
        cv.visitMethodInsn(opcode, owner, name, desc );
        //transform constructors
        if(transFormInit && opcode == Constants.INVOKESPECIAL){
          cv.visitVarInsn( Constants.ALOAD, 0 );  
          cv.visitTypeInsn(Constants.NEW, Type.getInternalName(delegateImpl));
          cv.visitInsn( Constants.DUP );
          cv.visitVarInsn( Constants.ALOAD, 0 );
          cv.visitMethodInsn(Constants.INVOKESPECIAL,Type.getInternalName(delegateImpl), "<init>", "(Ljava/lang/Object;)V"); 
          cv.visitFieldInsn(Constants.PUTFIELD,className, Signature.DELEGATE, Type.getDescriptor(Object.class) );
          transFormInit = false;  
        }
        
        
    }
    
    
    
}

     
     
}



