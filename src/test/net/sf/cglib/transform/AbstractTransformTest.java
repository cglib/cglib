/*
 *
 *
 * $Id: AbstractTransformTest.java,v 1.1 2003/10/05 16:05:30 baliuka Exp $
 */

package net.sf.cglib.transform;

import junit.framework.*;
import net.sf.cglib.core.CodeGenerationException;
/**
 *
 * @author  baliuka
 */
abstract public class AbstractTransformTest  extends TestCase{
   
    
     /** Creates a new instance of AbstractTransformTest */
    public AbstractTransformTest() {  }
   
    /** Creates a new instance of AbstractTransformTest */
    public AbstractTransformTest(String s) {
       super(s);
    }
    
    protected abstract TransformingLoader.ClassTransformerFactory getTransformer()throws Exception;
    
     public Class transform()throws Exception{
       
       
       ClassLoader loader = new TransformingLoader(
          AbstractTransformTest.class.getClassLoader(),
          new ClassFilter(){
            public boolean accept(String name){
                
                return ! name.startsWith("java") &&
                       ! name.startsWith("junit") ;
            } 
         },
         getTransformer()
        );
        
        
        return loader.loadClass(getClass().getName());
        
       
    
     }
    
    
}
