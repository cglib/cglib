
package net.sf.cglib.transform;

import java.util.*;
import java.io.*;

/**
 *
 * @author  baliuka
 */
public class TransformLoader extends ClassLoader{
    
    private List transformations = new Vector();
    private ClassFilter filter;
    
    /** Creates a new instance of TransformLoader */
    public TransformLoader(ClassLoader parent, ClassFilter filter) {
        super(parent);
        this.filter = filter;
    }
    
    public void addTransformation(Transformation transformation){
        transformations.add(transformation);
    }
    
    public void removeTransformation(Transformation transformation){
        transformations.remove(transformation);
    }
    
    public boolean containsTransformation(Transformation transformation){
        return transformations.contains(transformation);
    }
    
    public Class loadClass( String name ) throws ClassNotFoundException {
        
       
        
        if( ! filter.accept(name) ){
          return super.loadClass(name);
        }
        
        try{
            byte data[];
            InputStream is = new BufferedInputStream( getParent().getResourceAsStream( name.replace('.','/') + ".class" ) );
            try{
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int b ;
                while( ( b = is.read() ) != -1 ){
                    out.write(b);
                }
                data = out.toByteArray();
                for( Iterator i = transformations.iterator(); i.hasNext();  ){
                    Transformation t = (Transformation)i.next();
                    data = t.transformClassFile(data);
                }
                
                
                return super.defineClass( name, data, 0, data.length  );
                
            }finally{
                is.close();
            }
        }catch(Exception e){
            
            throw new ClassNotFoundException( name + ":" + e.getMessage() );
            
        }
    }
    
    
    
}
