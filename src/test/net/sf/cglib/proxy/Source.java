
package net.sf.cglib.proxy;

import junit.framework.TestCase;

public abstract class Source {
    

    public Source() {
    }
    
    public void callAll(){
     protectedMethod();
     packageMethod();
     abstractMethod();
     finalMethod();
     intType(1);
     longType(1L);
     floatType(1f);
     doubleType(1.0);
     objectType("1") ;
     voidType();
     multiArg(1,1,1,1,"");
    }
    
    protected void protectedMethod(){}
    
    void packageMethod(){} 
    
    abstract void abstractMethod();
    
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
    public Object objectType(Object val){
      return val;
    }
    public void voidType(){
    
    } 
    public void multiArg( int arg1, long arg2, 
                           double arg3, float arg4, Object arg5  ){
    
    }
    
}
