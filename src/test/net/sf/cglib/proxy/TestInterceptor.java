/*
 * Copyright 2003 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.cglib.proxy;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 *@author Juozas Baliuka <a href="mailto:baliuka@mwm.lt">baliuka@mwm.lt</a>
 *@version $Id: TestInterceptor.java,v 1.3 2004/06/24 21:15:16 herbyderby Exp $
 */
public class TestInterceptor implements MethodInterceptor, Serializable {
    String value;
    
    public String getValue() {
        return value;
    }
     
    public TestInterceptor(String ser) {
        value = ser;
    }
   
    public TestInterceptor() {
    }

    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        System.out.println( method );
        Throwable e = null;                                                                            
        boolean invokedSuper = false;                                                                  
        Object retValFromSuper = null;
        if (!Modifier.isAbstract(method.getModifiers()) && 
            invokeSuper(obj, method, args)) {
            invokedSuper = true;                                                                        
            try {
                retValFromSuper = proxy.invokeSuper(obj, args);
            } catch (Throwable t) {
                e = t;
            }
        }
        return afterReturn(obj, method, args, invokedSuper, retValFromSuper, e);
    }
    
    public boolean invokeSuper(Object obj, Method method, Object[] args) throws Throwable {
        return true;
    }

    public Object afterReturn(Object obj, Method method, Object[] args,
                              boolean invokedSuper, Object retValFromSuper,
                              Throwable e) throws Throwable {
        if (e != null)
            throw e.fillInStackTrace();
        return retValFromSuper;
    }
}
