
package net.sf.cglib.transform;

import junit.framework.*;
import java.util.*;
import net.sf.cglib.core.*;

/**
 *
 * @author  baliuka
 */
public class TestAddClassInit extends AbstractTransformTest{
    
   static Class registred;
   
   static int i = 0;
   
   static {  
    
       i = 11;  
   
   }
    
   public static void register(Class cls){
    
        registred = cls;
    
        
    }
   
   
    public TestAddClassInit(){  }
    
    public void testInitTransform(){
        assertEquals(i,11);   
    }
   
    public void testRegistred(){
        
       
        assertNotNull(registred);
        
    
    }
    
    public TestAddClassInit(String s){
       super(s);
    }
    
    protected ClassTransformerFactory getTransformer() throws Exception{
        
       return new ClassTransformerFactory (){
        
           public ClassTransformer newInstance(){
             try{  
               return new   AddStaticInitTransformer(
                 TestAddClassInit.class.
                        getMethod("register",new Class[]{Class.class}) 
               );          
             }catch(Exception e){
               throw new CodeGenerationException(e);
             }          
        }   
       };
        
    }
    
    public static void main(String[] args) throws Exception{
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() throws Exception{
        return new TestSuite( new TestAddClassInit( ).transform() );
    }
    
    
}
