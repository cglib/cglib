
package net.sf.cglib.transform;

import java.lang.reflect.*;

import org.objectweb.asm.*;

/**
 *
 * @author  baliuka
 */
public class AddClintTransformer extends ClassTransformer {
    
    Method classInit;
    boolean generateClinit;
    String className;
    /** Creates a new instance of AddClintTransformer */
    public AddClintTransformer(Method classInit) {
          if(!Modifier.isStatic( classInit.getModifiers())) {
          throw new IllegalArgumentException( classInit + " is not static" );
        }
        if( classInit.getParameterTypes().length != 1 || 
            classInit.getParameterTypes()[0] != Class.class ||
            classInit.getReturnType() != Void.TYPE   ){
            throw new IllegalArgumentException( classInit + " illegal signature" );
        
        }
        generateClinit = true;
        this.classInit = classInit;
   
    }
    
    public void visitEnd() {
    
       if(generateClinit){
          CodeVisitor cv = getTarget().visitMethod( Modifier.STATIC,
          "<clinit>", "()V" , new String[]{} );
            generateClassInit(cv);
          cv.visitInsn(Constants.RETURN);  
          cv.visitMaxs(0,0);
        }
   
        getTarget().visitEnd();
    }
    
   private void generateClassInit(CodeVisitor cv){
     if(generateClinit){
         cv.visitLdcInsn(className.replace('/','.'));
         cv.visitMethodInsn(
             Constants.INVOKESTATIC,
             Type.getInternalName(Class.class), 
             "forName", 
              "(Ljava/lang/String;)Ljava/lang/Class;"
             );
         cv.visitMethodInsn(
             Constants.INVOKESTATIC,
             Type.getInternalName(classInit.getDeclaringClass()), 
             classInit.getName(), 
              "(Ljava/lang/Class;)V"
             );
         
         
         
         generateClinit = false;
     }    
       
    }

   public void visit(int access, String name, String superName, String[] ifaces, String sourceFile) {
     className = name;
     getTarget().visit(access, name, superName,  ifaces, sourceFile);
   }
   
   public CodeVisitor visitMethod(int access, String name, String desc, String[] exceptions) {
        CodeVisitor cv = getTarget().visitMethod(access, name, desc, exceptions  );
        if(name.equals("<clinit>")){
            generateClassInit(cv);
        }
        return cv;
    }
    
   
}
