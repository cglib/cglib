

package samples;

import java.beans.*;
import net.sf.cglib.*;
import java.util.*;

/**
 *
 * @author  baliuka
 */
public class Beans implements BeforeAfterInterceptor{
    
    private PropertyChangeSupport propertySupport;
   
    
    /** Creates new Bean */
    public Beans() {
        
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        
        propertySupport.addPropertyChangeListener(listener);
        
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }
    
    public static  Object newInstance( Class clazz ){
        try{
            Beans interceptor = new Beans();
            Object bean = Enhancer.enhance( clazz, null, interceptor );
            interceptor.propertySupport = new PropertyChangeSupport( bean );
            return bean;
        }catch( Throwable e ){
            e.printStackTrace();
            throw new Error(e.getMessage());
        }
        
    }
    
    static final Class C[] = new Class[0];
    static final Object emptyArgs [] = new Object[0];
    
    public Object afterReturn( Object obj, java.lang.reflect.Method method,
    Object[] args, boolean invokedSuper,
    Object retValFromSuper, java.lang.Throwable e) throws java.lang.Throwable {
        
        String name = method.getName();
        if( name.equals("addPropertyChangeListener")) {
            addPropertyChangeListener((PropertyChangeListener)args[0]);
        }else if ( name.equals( "removePropertyChangeListener" ) ){
            removePropertyChangeListener((PropertyChangeListener)args[0]);
        }
        
        
        if( name.startsWith("set") &&
        args.length == 1 &&
        method.getReturnType() == Void.TYPE ){
            
            char propName[] = name.substring("set".length()).toCharArray();
            
            propName[0] = Character.toLowerCase( propName[0] );
            propertySupport.firePropertyChange( new String( propName ) , null , args[0]);
            
        }
        
        
        
        return retValFromSuper;
    }
    
    
    public boolean invokeSuper(Object obj, java.lang.reflect.Method method, Object[] args) throws java.lang.Throwable {
        // validation and security can be implemented in this method
        return true;
    }
    
    public static void main( String args[] ){
        
        Bean  bean =  (Bean)newInstance( Bean.class );
        
        bean.addPropertyChangeListener(
        new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent evt){
                System.out.println(evt);
            }
        }
        );
        
        bean.setSampleProperty("TEST");
        
        
    }
    
    
}
