/*
 * Copyright 2002,2003 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.cglib.proxy;

public abstract class Source implements java.io.Serializable{
    
    public static class CheckedException extends Exception{}
    public static class UndeclaredException extends Exception{}

 public String toString(){
   return "";
 }
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
    
    public String [] arrayType(String val[]){
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
