package net.sf.cglib.transform;

import net.sf.cglib.core.CodeGenerationException;
import net.sf.cglib.core.ClassGenerator;
import net.sf.cglib.core.DebuggingClassWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.*;

import java.io.IOException;

abstract public class AbstractClassLoader extends ClassLoader {
    private ClassFilter filter;
    private ClassLoader classPath;
    private static java.security.ProtectionDomain DOMAIN ;
    
    static{
        
        DOMAIN = (java.security.ProtectionDomain)
        java.security.AccessController.doPrivileged(
          new java.security.PrivilegedAction() {
            public Object run() {
               return AbstractClassLoader.class.getProtectionDomain();
            }
        }); 
     }
    
    protected AbstractClassLoader(ClassLoader parent, ClassLoader classPath, ClassFilter filter) {
        super(parent);
        this.filter = filter;
        this.classPath = classPath;
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        
        Class loaded = findLoadedClass(name);
        
        if( loaded != null ){
            if( loaded.getClassLoader() == this ){
               return loaded;
            }//else reload with this class loader
        }
        
        if (!filter.accept(name)) {
            return super.loadClass(name);
        }
        ClassReader r;
        try {
            
           java.io.InputStream is = classPath.getResourceAsStream( 
                       name.replace('.','/') + ".class"
                  ); 
           
           if (is == null) {
               
              throw new ClassNotFoundException(name);
              
           }
           try { 
               
              r = new ClassReader(is);
            
           } finally {
               
              is.close();
             
           }
        } catch (IOException e) {
            throw new ClassNotFoundException(name + ":" + e.getMessage());
        }

        try {
            ClassWriter w =  new DebuggingClassWriter(true);
            getGenerator(r).generateClass( w );
            byte[] b = w.toByteArray();
            return super.defineClass(name, b, 0, b.length, DOMAIN);
        } catch (RuntimeException e) {
            throw e;
        } catch (Error e) {
            throw e;
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }
    }

    protected ClassGenerator getGenerator(ClassReader r) {
        return new ClassReaderGenerator(r, true); // skipDebug?
    }
}
