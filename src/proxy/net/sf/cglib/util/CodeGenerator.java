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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.cglib.util.ClassFileUtils;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;


/**
 * Abstract base class for code generators
 * @author  baliuka
 */
public abstract class CodeGenerator implements ClassFileConstants {
	
	protected ClassGen cg;
	protected InstructionList il = new InstructionList();
	protected ConstantPoolGen cp;
	protected MethodGen mg;
    protected ClassLoader loader;
	
	private Class returnType;
	private Map  branches;  
	private Map  labels;
	private Map  locals;//TODO: map index to name and type 
	
	
	protected CodeGenerator( String className, String parentClassName, ClassLoader loader  ){
	  
	  this.loader = loader;
	  cg = new ClassGen(className,parentClassName,SOURCE_FILE,ACC_PUBLIC,null); 
	  cp = cg.getConstantPool();
	  
	
	}
	
	protected CodeGenerator(){
	
	}
	/**
	 * 
	 * method used to generate code  
	 *
	 * */
	abstract protected void generate();
	
	 protected Class define(){
       generate(); 
      return ClassFileUtils.defineClass(loader,cg.getClassName(),
                                         cg.getJavaClass().getBytes() );     
	
	}

    protected Class getReturnType(){
       return returnType; 
    }
    protected void begin_method(java.lang.reflect.Method method){
      returnType = method.getReturnType();	
      mg = ClassFileUtils.toMethodGen(method,cg.getClassName(),il, cp );    	
    
    }
    
    protected void begin_constructor(java.lang.reflect.Constructor constructor){
      returnType = Void.TYPE;	
      mg = new MethodGen(ACC_PUBLIC,Type.VOID,
                  ClassFileUtils.toType(constructor.getParameterTypes()),null,
                  CONSTRUCTOR_NAME,cg.getClassName(),il,cp);
      
    
    }
    

    protected	void end_method(){

    setTargets();
    //TDOO: PRINT DEBUG
    //System.out.print(mg.getMethod());
    //System.out.print(mg.getMethod().getCode());
    cg.addMethod(ClassFileUtils.getMethod(mg));	
    il.dispose();      
    
  }


    private void append(  Instruction intruction ){
       il.append(intruction);
    }

    private void append( String label, Instruction intruction ){
  
    if(label != null){	
   
     if( labels == null ){
         labels = new HashMap();       	      
     }   	
     
    if( null != labels.put(label, il.append( intruction ) ) ){
    	
       throw new IllegalStateException("dublicated label " + label);
    
    }
     
   }	
  
  }

   private void append( BranchInstruction intruction, String label ){
   	
   	 if( branches == null ){
       branches = new HashMap();       	 
   	 }
   	 

     List list = (List)branches.get(label);
   
     if( list == null ){
     	
      list = new LinkedList(); 
      branches.put(label,list);
   
     } 
    	
    list.add(intruction);
    il.append(intruction);
  
  }
  
  private void setTargets(){
   
    if(labels != null && branches != null  ){
       
      for( Iterator labelIterator = labels.entrySet().iterator(); labelIterator.hasNext(); ){
         
         Map.Entry label = (Map.Entry)labelIterator.next();          
         List branchInstructions = (List)branches.get(label.getKey());
         
         if( branchInstructions != null ){
         
           for( Iterator instructions = branchInstructions.iterator(); instructions.hasNext();   ){
                        
             BranchInstruction intruction = (BranchInstruction)instructions.next();
             intruction.setTarget( (InstructionHandle)label.getValue() );
           
           }
         
         }
      
      }     
    
     
    }
      
    if( labels != null){
    	
       labels.clear();
       
     } 
     
    if( branches != null ){
    	
       branches.clear();
    }
  

  
  }
  
  //-------------- branch istructions:
  
   protected    void ifeq(String label ){ append( new IFEQ(null), label ); }
   protected    void ifne(String label){ append( new IFNE(null), label ); }
   protected    void iflt(String label){ append( new IFLT(null), label ); }
   protected    void ifge(String label){ append( new IFGE(null), label ); }
   protected    void ifgt(String label){ append( new IFGT(null), label ); }
   protected    void ifle(String label){ append( new IFLE(null), label ); }
   protected    void goTo(String label){ append( new GOTO(null), label ); }
   protected    void jsr(String label ){ append( new JSR(null), label ); }
   protected    void ifnull(String label){ append( new IFNULL(null), label ); }
   protected    void ifnonnoll(String label){ append( new IFNONNULL(null), label ); }
  

 //----------------- TODO: frontend for exeption handlers ------------------------

 //------------------ Instructions, not implemented: -------------  

	
   protected  void nop(){ append( new NOP()); }             
   protected  void nop(String label){ append( label, new NOP() ); }             
  
   protected  void aconst_null(){ append( new ACONST_NULL() ); }
  
   protected  void push( int value){ 
  	append( ClassFileUtils.getIntConst(value,cp) ); 
  }
  protected  void push( long value){ 
  	append( new LDC( cp.addLong(value) ) ); 
  }
   protected  void push( float value ){ 
  	append( new LDC( cp.addFloat(value) ) ); 
  }
   protected  void push( double value){
  	 append( new LDC( cp.addDouble(value) ) ) ;  
  }
   protected  void push( byte value ){
  	 append( new BIPUSH(value) ); 
  }
   protected  void push( short value){
  	 append( new SIPUSH(value) ); 
  }
  
  
   
  protected  void alocal(int index){
   
      append(new ASTORE(index) );
    
    }
 protected boolean wrapp(Class type){   
  Instruction wrapper = 
   ClassFileUtils.newWrapper( ClassFileUtils.toType(type), cp );   
   
   if( wrapper != null ){
     append( wrapper );
     return true;
   }else{
     return false;
   }
  
 } 
    
 protected  void load_alocal(int index){
  
     append(new ALOAD(index));
 }   


 protected  void new_aaray(){ 
 	
    append( new ANEWARRAY(cp.addClass(Type.OBJECT)));
    
  } 
 
   protected  void load_this(){
   
    append( new ALOAD(0) );	
  	
  }
 
  
   protected  void load_args( int fromArg, int count ){
  	
  	int pos = fromArg;
  	 for( int i = 0; i < count; i++   ){
  	  pos = ClassFileUtils.loadArg(il,mg.getArgumentType(fromArg + i - 1),pos);
  	 }
  	
  }
  
   protected  void load_args(){
  	int pos = 1;
  	 for( int i = 0; i < mg.getArgumentTypes().length; i++   ){
  	  pos = ClassFileUtils.loadArg(il,mg.getArgumentType( i ),pos);
  	 
  	 }
 
   }
   protected  void load_arg(int index){
  	 load_args(index,1);
   }
  
  
  
   protected  void aload( int index ){
     push(index);
     append( new AALOAD() );
  	
  }
   protected  void store( String local ){
  	//TODO
  	
  }
   protected  void astore( Class componentType, int index ){ 
   //TODO
  
  }
  

  
   protected    void pop() { append( new POP() ); }
   protected    void dup() {  append( new DUP() ); }
   protected    void swap(){ append( new SWAP() ); }
   
   protected    void pop(String  label) { append( label, new POP() ); }
   protected    void dup(String  label) { append( label, new DUP() ); }
   protected    void swap(String label) { append( label, new SWAP() ); }
  
  
  
    
   protected    void return_value(Class type){ 
  
    append(ClassFileUtils.newReturn(ClassFileUtils.toType(type)));	
   	
  }
  
  // --------------- Field access ----------------
  
   protected    void getstatic( Class clazz, String name ){
    //TODO:
   }
   protected    void getstatic( Field field ){
    //TODO:
   
   }
  
  //current class
   protected   void getstatic( String name ){
    //TODO:
   
   }
   protected   void putstatic( String name ){
    //TODO:
   
   }
  
   protected    void getfield(String name){
     //TODO:
   
   }
   protected    void putfield(String name){
    //TODO:
   
   } 
  
  //super class
  
   protected    void super_getfield(String name){
    //TODO:
   
   }
   protected    void super_putfield(String name){
    //TODO:
   
   } 
  
  
   // --------------- Invoke method ----------------
  
   protected   void invoke(Method method){
   
    //TODO:
   
   }
  
   protected   void super_invoke(Method method){
    //TODO:
   
   }
   
   protected void invoke_costructor(Class type){
   
   append( new INVOKESPECIAL( cp.addMethodref(
           type.getName(),CONSTRUCTOR_NAME,
           Type.getMethodSignature(Type.VOID, new Type[]{} ) 
            )
        )
       );
   
   }


 protected  void invoke_virtual(Method method){
  append( new INVOKEVIRTUAL( cp.addMethodref( 
                              method.getDeclaringClass().getName() ,
                                  method.getName(),Type.getMethodSignature(
                                   ClassFileUtils.toType(method.getReturnType()),
                                   ClassFileUtils.toType(method.getParameterTypes())
                                          )
                                        ) 
                                     ) 
                                  ); 
                     
 }          

   
   protected void super_invoke(Constructor constructor){
   	
    append( new INVOKESPECIAL( cp.addMethodref(
           cg.getSuperclassName(),CONSTRUCTOR_NAME,
           Type.getMethodSignature(Type.VOID,
               ClassFileUtils.toType( 
               constructor.getParameterTypes() 
               ) 
            )
        )
       )
      );
        
   
   }
  
  
   protected void new_instance( Class type ){ 
  	append( new NEW( cp.addClass(type.getName() ) ) ); 
   }
 
   protected void  init( Class type ){
    append(ClassFileUtils.initWrapper( ClassFileUtils.toType( type ), cp )); 
   }
   
   protected void  aastore(){
    append(new AASTORE());
   }
  
   protected   void athrow(){ append( new ATHROW() ); }
   protected   void athrow(String label){ append(label,new ATHROW() ); }
  
  
   protected   void checkcast(Class type){ 
  	append( new CHECKCAST(cp.addClass( type.getName() )) ); 
   }
   protected   void cast(Class type){ 
       Type returnType = ClassFileUtils.toType(type);
       ClassFileUtils.castObject(cp, il, returnType);
  
   }
          
   
   protected   void instanceOf(Class type){ 
  	append( new INSTANCEOF(cp.addClass( type.getName() ) ) ); 
   }
  
	
	
}
