/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * 4. The names "Apache Cocoon" and "Apache Software Foundation" must
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
package net.sf.cglib.proxy;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 *@author     Juozas Baliuka <a href="mailto:baliuka@mwm.lt">
 *      baliuka@mwm.lt</a>
 *@version    $Id: TestEnhancer.java,v 1.2 2002/09/22 15:17:04 baliuka Exp $
 */
public class TestEnhancer extends TestCase {
    
    
    public TestEnhancer(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        return new TestSuite(TestEnhancer.class);
    }
    
    public static void main(String args[]) {
        String[] testCaseName = {TestEnhancer.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }
    
    
    public void testEnhance()throws Throwable{
        
        MethodInterceptor interceptor =
        new MethodInterceptor(){
            
            
            public boolean invokeSuper( Object obj,java.lang.reflect.Method method,
            Object args[] )
            throws java.lang.Throwable{
                return true;
            }
            
            
            public Object afterReturn(  Object obj,     java.lang.reflect.Method method,
            Object args[],
            boolean invokedSuper, Object retValFromSuper,
            java.lang.Throwable e )throws java.lang.Throwable{
                assertTrue("Pure method " + method,invokedSuper);
                return retValFromSuper;//return the same as supper
            }
            
        };
        
        java.util.Vector vector = (java.util.Vector)Enhancer.enhance(
        java.util.Vector.class,
        new Class[]{java.util.List.class},interceptor );
        
        
        
        java.util.Vector vector2  = (java.util.Vector)Enhancer.enhance(
        java.util.Vector.class,
        new Class[]{java.util.List.class},interceptor );
        
        
        
        
        assertTrue("Cache failed",vector.getClass() == vector2.getClass());
        //TODO : Add meanigful asserts
        
        String value = "VALUE";
        vector.add(null);
        
        vector.elements();
        vector.size();
        vector.add(value);
        vector.remove(value);
        vector.remove(value);
        vector.contains(value);
        vector.get(0);
        vector.contains(value);
        vector.toArray(new Object[]{});
        
    }
    
}



