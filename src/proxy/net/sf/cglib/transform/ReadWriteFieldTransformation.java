/*
 * ReadWriteFieldTransformation.java
 *
 * Created on Sekmadienis, 2003, Rugsëjo 14, 10.55
 */

package net.sf.cglib.transform;

import java.lang.reflect.*;
import java.io.*;
import net.sf.cglib.core.*;
/**
 *
 * @author  baliuka
 */
public class ReadWriteFieldTransformation implements Transformation{
    ReadWriteFieldFilter filter;
    Class[] delegateIf;
    Class delegateImpl;
    
    
    /** Holds value of property classInit. */
    private Method classInit;
    
    /** Creates a new instance of ReadWriteFieldTransformation */
    public ReadWriteFieldTransformation(ReadWriteFieldFilter filter) {
        this.filter = filter;
    }
    
    public byte[] transformClassFile(byte[] data) {
        try{
            TransformClassVisitor tcv = new TransformClassVisitor( new ByteArrayInputStream(data),filter);
            if(classInit != null){
                tcv.setClassInit(classInit);
            }
            if( delegateImpl != null ){
                tcv.setDelegate(delegateIf, delegateImpl);
            }
            return tcv.transform();
        }catch(Exception e){
            throw new CodeGenerationException(e);
        }
    }
    
    /** Getter for property classInit.
     * @return Value of property classInit.
     *
     */
    public Method getClassInit() {
        return this.classInit;
    }
    
    /** Setter for property classInit.
     * @param classInit New value of property classInit.
     *
     */
    public void setClassInit(Method classInit) {
        this.classInit = classInit;
    }
    
    public void setDelegate(Class delegateIf[], Class delegateImpl ){
        
        this.delegateIf = delegateIf;
        this.delegateImpl = delegateImpl;
        
        
    }
    
}
