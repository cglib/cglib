
package net.sf.cglib.transform;
import junit.framework.*;
/**
 *
 * @author  baliuka
 */
public class TestAddDelegate extends AbstractTransformTest {
    
    /** Creates a new instance of TestAddDelegate */
    public TestAddDelegate(String name) {
        super(name);
    }
    
    public interface Interface {
    
        Object getDelegte();
        
        Object getTarget();
    
    }
    
    public void test(){
        
     Interface i = (Interface)this;
     assertEquals(i.getTarget(),this);
     
    }
    
    public static class ImplExclude implements Interface {
    
        private Object target;
        
        public ImplExclude(Object target){
          this.target = target;
        }
        
        public Object getDelegte() {
          return this;
        }        
    
        public Object getTarget(){
           return  target;  
        }
    }
    
    public TestAddDelegate() {
        super(null);
    }
    
    
    protected ClassTransformerFactory getTransformer() throws Exception {

        return new ClassTransformerFactory(){
        
               public ClassTransformer newInstance(){
          
                   return new AddDelegateTransformer(new Class[]{Interface.class} , ImplExclude.class );
                   
               }
        
        };
        
        
    }
    
    
    public static void main(String[] args) throws Exception{
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() throws Exception{
        
        return new TestSuite( new TestAddDelegate().transform() );
        
    }
    
}
