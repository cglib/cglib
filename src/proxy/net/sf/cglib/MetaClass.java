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
package net.sf.cglib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import net.sf.cglib.util.*;

/**
 *
 * @author  baliuka
 */

public abstract class MetaClass  {
    private static final FactoryCache cache = new FactoryCache(MetaClass.class);
    private static final Constructor GENERATOR =
      ReflectUtils.findConstructor("MetaClass$Generator(Class, String[], String[], Class[])");
    private static final MetaClassKey KEY_FACTORY =
      (MetaClassKey)KeyFactory.create(MetaClassKey.class, null);
    private static final MemberKey MEMBER_KEY_FACTORY =
    (MemberKey)KeyFactory.create(MemberKey.class, null);
    
    protected Class target;
    protected String[] getters, setters;
    protected Class[] types;
    protected Map members = new HashMap();
    
    
     interface MetaClassKey {
        public Object newInstance(Class target, String[] getters,
        String[] setters, Class[] types);
    }
    
     interface MemberKey {
        public Object newInstance( Class target, String name, Class types[]  );
    }

    protected MetaClass() { }
        
    public Class[] getPropertyTypes(){
        
        return (Class[])types.clone();
    }
    
    private void addMembers(){
        
        java.lang.reflect.Method [] methods = target.getMethods();
        for(int i = 0; i < methods.length; i++ ){
            Object key = MEMBER_KEY_FACTORY.newInstance(target, methods[i].getName(),
            methods[i].getParameterTypes() );
            members.put(key, MethodProxy.create(methods[i],methods[i]));
        }
        
        java.lang.reflect.Constructor [] constructors = target.getConstructors();
        for(int i = 0; i < constructors.length; i++ ){
            Object key = MEMBER_KEY_FACTORY.newInstance(target, "<init>" ,
            constructors[i].getParameterTypes() );
            members.put(key, ConstructorProxy.create(constructors[i]));
        }
    }
    
    public MethodProxy getMethod( String name, Class[] types  ){
        Object key = MEMBER_KEY_FACTORY.newInstance(target, name, types );
        return (MethodProxy)members.get(key);
    }
    
    public ConstructorProxy getConstructor( Class[] types  ){
        Object key = MEMBER_KEY_FACTORY.newInstance(target, "<init>" , types );
        return (ConstructorProxy)members.get(key);
    }
    
    
    public String[] getGetters(){
        
        return (String[])getters.clone();
    }
    
    public String[] getSetters(){
        
        return (String[])setters.clone();
    }
    
        
    private static void validate( Class target, String getters[],
    String setters[], Class types[],
    Method getters_out[], Method setters_out[] ){
        
        
        
        int length = types.length;
        
        if( setters.length != length || length != getters.length ){
            
            throw new IllegalArgumentException("accessor array length must be equal type array length");
            
        }
        String last = null;
        try{
            
            for(int i=0; i< types.length; i++ ){
                
                if( getters[i] != null ){
                    last = getters[i];
                    Method method = ReflectUtils.findDeclaredMethod(target,last,new Class[]{});
                    
                    int mod = method.getModifiers();
                    
                    if(method.getReturnType() != types[i] || Modifier.isPrivate(mod) ){
                        throw new IllegalArgumentException(last);
                    }
                    getters_out[i] = method;
                }
                
                if( setters[i] != null ){
                    last = setters[i];
                    Method method = ReflectUtils.findDeclaredMethod( target, last, new Class[]{ types[i] });
                    if( Modifier.isPrivate(method.getModifiers()) ){
                        throw new IllegalArgumentException(last);
                    }
                    setters_out[i] = method;
                }
                
                
            }
            
        }catch( NoSuchMethodException nesme ){
            
            throw new IllegalArgumentException(last);
            
        }
        
    }
    
    public static MetaClass getInstance(ClassLoader loader,
                                        Class target,
                                        String[] getters,
                                        String[] setters,
                                        Class[] types) {
        synchronized (cache) {
            MetaClass singleton =
                (MetaClass)cache.getFactory(loader,
                                            KEY_FACTORY.newInstance(target, getters, setters, types),
                                            GENERATOR,
                                            target,
                                            getters,
                                            setters,
                                            types);
            if (singleton.target == null) {
                singleton.target = target;

                int len = getters.length;
                singleton.getters = new String[len];
                System.arraycopy(getters, 0, singleton.getters, 0, len);

                singleton.setters = new String[len];
                System.arraycopy(setters, 0, singleton.setters, 0, len);
                                               
                singleton.types = new Class[types.length];
                System.arraycopy(types, 0, singleton.types, 0, types.length);

                singleton.addMembers();
            }
            return singleton;
        }
    }

    public abstract Object newInstance();
    
    public abstract Object[] getPropertyValues( Object bean );
    
    public abstract void setPropertyValues( Object bean, Object[] values );
    
    
    
    static private class Generator extends CodeGenerator {
        
        
        private Class target;
        private Method [] getters, setters;
        
        public Generator(Class target, String[] getters, String[] setters, Class[] types) {
            setSuperclass(MetaClass.class);
            setNamePrefix(target.getName());

            this.target = target;
            this.getters = new Method[getters.length];
            this.setters = new Method[setters.length];
            validate(target, getters, setters, types, this.getters, this.setters);
        }
        
        public  void generate() throws NoSuchMethodException {
            
            //------------- Generate constructor -------------

            null_constructor();
            
            //------------- newInstance -------------------------
            
            Method newInstance = MetaClass.class.getMethod("newInstance",new Class[]{});
            
            begin_method( newInstance );
            
            new_instance(target);
            dup();
            invoke_constructor(target);
            return_value();
            
            end_method();
            
            //------------- getPropertyValues -------------------------
            
            Method getPropertyValues =
            MetaClass.class.getMethod("getPropertyValues", new Class[]{ Object.class });
            
            begin_method(getPropertyValues);
            
            Local bean = make_local();
            load_arg(0);
            checkcast(target);
            store_local(bean);
            push(getters.length);
            newarray();
            
            for( int i = 0; i < getters.length; i++  ){
                if( getters[i] != null ){
                    dup();
                    push(i);
                    load_local(bean);
                    invoke(getters[i]);
                    box(getters[i].getReturnType());
                    aastore();
                }//write only
            }
            return_value();
            
            end_method();
            
            //------------- setPropertyValues -------------------------
            
            
            Method setPropertyValues =
            MetaClass.class.getMethod("setPropertyValues",
            new Class[]{ Object.class, Object[].class } );
            
            begin_method(setPropertyValues);
            
            load_arg(0);
            checkcast(target);
            load_arg(1);
            
            for( int i = 0; i < setters.length; i++  ){
                
                if(setters[i] != null){
                    dup2();
                    aaload(i);
                    unbox(setters[i].getParameterTypes()[0]);
                    invoke(setters[i]);
                }//read only
            }
            return_value();
            
            end_method();
        }
        
        
        
    }
    
    
    
}
