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
package net.sf.cglib.transform;

import net.sf.cglib.*;
import net.sf.cglib.beans.*;

import org.objectweb.asm.util.*;
import org.objectweb.asm.*;

import junit.framework.*;
import java.util.*;

/**
 * @author baliuka
 * @version $Id: TestTransformVisistor.java,v 1.4 2003/09/12 14:08:30 baliuka Exp $
 */
public class TestTransformVisistor extends TestCase {
    
    ReadWriteFieldFilter acceptAll = new ReadWriteFieldFilter(){
        public boolean acceptRead(String clas, String name){
            
            return true;
        }
        
        public boolean acceptWrite(String clas, String name){
            
            return true;
        }
    };
    
    static class  TransforClassLoader extends ClassLoader{
        
        byte data[];
        String name;
        
        TransforClassLoader(byte data[],String name){
            this.data = data;
            this.name = name;
        }
        public Class loadClass(String name)throws ClassNotFoundException{
            if(this.name.equals(name)){
                return super.defineClass(name,data,0,data.length);
            }else{
                return super.loadClass(name);
            }
        }
        
    }
    
    public TestTransformVisistor(String testName) {
        super(testName);
    }
    
    void print(byte data[]){
        
        ClassReader cr = new ClassReader(data);
        cr.accept(new TraceClassVisitor(null,new java.io.PrintWriter(System.out)), false);
        
    }
    void print(java.io.InputStream is)throws Exception{
        
        ClassReader cr = new ClassReader(is);
        cr.accept(new TraceClassVisitor(null,new java.io.PrintWriter(System.out)), false);
        
    }
    
    void print(Class cls)throws Exception{
        print(openStream(cls));
    }
    
    java.io.InputStream openStream(Class cls)throws Exception{
        return cls.getResourceAsStream( "/" + cls.getName().replace('.','/') + ".class");
    }
    
    public void testFieldTransform( )throws Exception{
        
       Transformed t = ( Transformed )transform( MA.class,
                      PersistenceCapable.class,
                      PersistenceCapableImpl.class
                    );
       
       Callback clb = new Callback();
       t.setReadWriteFieldCallback( clb );
       Object value = "TEST";
       BeanMap map = BeanMap.create( t, t.getClass().getClassLoader() );
       map.put("name", value );
       
       assertEquals(clb.getValue(),value);
       t.setDelegate( new PersistenceCapableImpl() );
       
       PersistenceCapable capable = (PersistenceCapable)t;
       capable.setPersistenceManager(value);
       
       assertEquals(capable.getPersistenceManager(),value);
        
    }
    public Object transform( Class cls,Class iface,Class impl )throws Exception{
        
        byte data[] ;
        java.io.InputStream is = openStream( cls );
        
        try{
            
            TransformClassVisitor tv = new TransformClassVisitor(is,acceptAll);
            tv.setDelegate(iface, impl);
            data = tv.transform();
            //print(data);
            
        }finally{
            
            is.close();
            
        }
        //print(data);
        
        TransforClassLoader loader = new TransforClassLoader(data,cls.getName());
        Class transformed = loader.loadClass(cls.getName());
        
        return transformed.newInstance();
        
        
        
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestTransformVisistor.class);
    }

    static class  Callback implements ReadWriteFieldCallback{
        
        Object value;
        
        public Object getValue(){
            
           return value;
           
        }
       
        public boolean readBoolean(Object _this, String name, boolean oldValue) {
            value = new Boolean(oldValue);
            return oldValue;
        }        
       
        public byte readByte(Object _this, String name, byte oldValue) {
            value = new Byte(oldValue);
            return oldValue;
        }
        
        public char readChar(Object _this, String name, char oldValue) {
            value = new Character(oldValue);
            return oldValue;
        }
        
        public double readDouble(Object _this, String name, double oldValue) {
            value = new Double(oldValue);
            return oldValue;
        }
        
        public float readFloat(Object _this, String name, float oldValue) {
            value = new Float(oldValue);
            return oldValue;
        }
        
        public int readInt(Object _this, String name, int oldValue) {
            value = new Integer(oldValue);
            return oldValue;
        }
        
        public long readLong(Object _this, String name, long oldValue) {
            value = new Long(oldValue);
            return oldValue;
        }
        
        public Object readObject(Object _this, String name, Object oldValue) {
            value = oldValue;
            return oldValue;
        }
        
        public short readShort(Object _this, String name, short oldValue) {
            value = new Short(oldValue);
            return oldValue;
        }
        
        public boolean writeBoolean(Object _this, String name, boolean oldValue, boolean newValue) {
            value = new Boolean(newValue);
            return newValue;
        }
        
        public byte writeByte(Object _this, String name, byte oldValue, byte newValue) {
            value = new Byte(newValue);
            return newValue;
        }
        
        public char writeChar(Object _this, String name, char oldValue, char newValue) {
            value = new Character(newValue);
            return newValue;
        }
        
        public double writeDouble(Object _this, String name, double oldValue, double newValue) {
            value = new Double(newValue);
            return newValue;
        }
        
        public float writeFloat(Object _this, String name, float oldValue, float newValue) {
            value = new Float(newValue);
            return newValue;
        }
        
        public int writeInt(Object _this, String name, int oldValue, int newValue) {
            value = new Integer(newValue);
            return newValue;
        }
        
        public long writeLong(Object _this, String name, long oldValue, long newValue) {
            value = new Long(newValue);
            return newValue;
        }
        
        public Object writeObject(Object _this, String name, Object oldValue, Object newValue) {
            value = newValue;
            return newValue;
        }
        
        public short writeShort(Object _this, String name, short oldValue, short newValue) {
            value = new Short(newValue);
            return newValue;
        }
        
        }
       
    
}
