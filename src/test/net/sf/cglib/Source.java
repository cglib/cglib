/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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
 * 4. The names "Apache" and "Apache Software Foundation" must
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
package net.sf.cglib;

import junit.framework.TestCase;

public abstract class Source implements java.io.Serializable{
    
    public static class CheckedException extends Exception{}
    public static class UndeclaredException extends Exception{}


    public Source() {
    }
    
    public void callAll(){
     protectedMethod();
     packageMethod();
     abstractMethod();
     synchronizedMethod();
     finalMethod();
     intType(1);
     longType(1L);
     floatType(1f);
     doubleType(1.0);
     objectType("1") ;
     voidType();
     multiArg(1,1,1,1,"","","");
    }
    
    protected void protectedMethod(){}
    
    void packageMethod(){} 
    
    abstract void abstractMethod();
    
    public void throwChecked()throws CheckedException{
      throw new CheckedException();
    }
    
     
    
     public void throwIndexOutOfBoundsException(){
       throw new IndexOutOfBoundsException();
    }

     public void throwAbstractMethodError(){
       throw new AbstractMethodError();
    }
 
    
    public synchronized void synchronizedMethod(){}
    
    public final void finalMethod(){ }
    
    public int intType(int val){
      return val;
    }
    public long longType(long val){
      return val;
    }
    public double doubleType(double val){
      return val;
    }
    public float floatType(float val){
      return val;
    }
    
    public boolean booleanType(boolean val){
      return val;
    }
    
    public short shortType(short val){
      return val;
    }
    
    public char charType(char val){
      return val;
    }
    
    public byte byteType(byte val){
      return val;
    }
    
    public int [] arrayType(int val[]){
      return val;
    }
    
    
    public Object objectType(Object val){
      return val;
    }
    public void voidType(){
    
    } 
    public void multiArg( int arg1, long arg2, 
                           double arg3, float arg4, Object arg5, Object arg6, Object arg7  ){
    
    }
    
}
