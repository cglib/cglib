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
public class TransformClassVisitor extends ClassTransformer{
    
    ReadWriteFieldFilter filter;
    String className;   
    Set interfaces     = new HashSet();
    Class delegateIf[] = null ;
    Class delegateImpl = null ;
    Map fields         = new HashMap();
    Map types          = new HashMap();
    boolean  generateClinit = false;       
 
    
    static String callbackDesc = Type.getType( ReadWriteFieldCallback.class ).getDescriptor();
    static String callbackName = ReadWriteFieldCallback.class.getName().replace('.','/');
    
    /** Holds value of property classInit. */
    private Method classInit;    
    
    /** Creates a new instance of TransformClassVisitor */
    public TransformClassVisitor( ReadWriteFieldFilter filter)throws java.io.IOException {
        
        this.filter = filter;
        interfaces.add(Signature.getInternalName(Transformed.class));
        
    }
    
    public void setDelegate(Class delegateIf[], Class delegateImpl ){
        try{
         delegateImpl.getConstructor( new Class[]{Object.class});
         this.delegateIf = delegateIf;
         this.delegateImpl = delegateImpl;
        }catch(Exception e){
          throw new CodeGenerationException(e);
        }
    }
    
    
    
    
    
    public void visit(int access, String name, String superName, String[] ifaces, String sourceFile) {
        if( Modifier.isInterface(access) ){
         throw new IllegalArgumentException("can not transform interface " + name);
        }
        try{
            className = name;
            interfaces.addAll(Arrays.asList(ifaces));
            
            if(delegateIf != null){
                for(int i = 0; i< delegateIf.length; i++){
                   interfaces.add(Signature.getInternalName(delegateIf[i]));
                }
                
            }
            getTarget().visit(
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
        
    
        if(generateClinit){
          CodeVisitor cv = getTarget().visitMethod( Modifier.STATIC,
          "<clinit>", "()V" , new String[]{} );
            generateClassInit(cv);
          cv.visitInsn(Constants.RETURN);  
          cv.visitMaxs(0,0);
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
    private void addDelegate(Method m)throws Exception{
        
        
        List exeptions = new ArrayList(m.getExceptionTypes().length);
        for(int i = 0; i< m.getExceptionTypes().length; i++ ){
            exeptions.add(Signature.getInternalName( m.getExceptionTypes()[i]) );
        }
        
        Method delegate = delegateImpl.getMethod(m.getName(), m.getParameterTypes() );
        if(!delegate.getReturnType().getName().equals(m.getReturnType().getName())){
          throw  new IllegalArgumentException( "invalid  delegate signature  " + delegate);
        }
        
        CodeVisitor cv =  getTarget().visitMethod( Modifier.PUBLIC,
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
    
    private void addCallbackField(){
        
        
        getTarget().visitField(
        Modifier.PRIVATE|Modifier.TRANSIENT,
        Signature.READ_WRITE_CALLBACK,
        callbackDesc ,
        null
        );
        
        getTarget().visitField(
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
        if(name.equals("<clinit>")){
            generateClassInit(cv);
        }
        return new TransformCodeVisitor( this, 
                                         cv,
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
    
    /** Getter for property classInit.
     * @return Value of property classInit.
     *
     */
    public Method getClassInit() {
        return this.classInit;
    }
    
    /** Setter for property classInit.
     * @param classInit New value of property classInit.
     *
     */
    public void setClassInit(Method classInit) {
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
    
}
