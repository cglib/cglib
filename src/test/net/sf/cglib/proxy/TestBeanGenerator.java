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

import junit.framework.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import java.beans.*;
import java.util.*;

/**
 *
 * @author baliuka
 */
public class TestBeanGenerator extends TestCase {
    
    public TestBeanGenerator(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(TestBeanGenerator.class);
        suite.addTest(InfoTest.suite());
        
        return suite;
    }
    
    /** Test of getProperties method, of class net.sf.cglib.proxy.BeanGenerator. */
    public void testGetProperties() {
        fail("The test case is empty.");
    }
    
    /** Test of getMethods method, of class net.sf.cglib.proxy.BeanGenerator. */
    public void testGetMethods() {
        fail("The test case is empty.");
    }
    
    /** Test of generate method, of class net.sf.cglib.proxy.BeanGenerator. */
    public void testGenerate() {
        fail("The test case is empty.");
    }
    
    /** Test of copyMethods method, of class net.sf.cglib.proxy.BeanGenerator. */
    public void testCopyMethods() {
        fail("The test case is empty.");
    }
    
    /** Test of addMethod method, of class net.sf.cglib.proxy.BeanGenerator. */
    public void testAddMethod() {
        fail("The test case is empty.");
    }
    
    /** Test of addProperties method, of class net.sf.cglib.proxy.BeanGenerator. */
    public void testAddProperties() {
        fail("The test case is empty.");
    }
    
    /** Test of addProperty method, of class net.sf.cglib.proxy.BeanGenerator. */
    public void testAddProperty() {
        fail("The test case is empty.");
    }
    
    public static class InfoTest extends TestCase {
        
        public InfoTest(java.lang.String testName) {
            super(testName);
        }
        
        public static void main(java.lang.String[] args) {
            junit.textui.TestRunner.run(suite());
        }
        
        public static Test suite() {
            TestSuite suite = new TestSuite(InfoTest.class);
            
            return suite;
        }
        
        /** Test of getBeanDescriptor method, of class net.sf.cglib.proxy.BeanGenerator.Info. */
        public void testGetBeanDescriptor() {
            fail("The test case is empty.");
        }
        
        /** Test of getDefaultEventIndex method, of class net.sf.cglib.proxy.BeanGenerator.Info. */
        public void testGetDefaultEventIndex() {
            fail("The test case is empty.");
        }
        
        /** Test of getEventSetDescriptors method, of class net.sf.cglib.proxy.BeanGenerator.Info. */
        public void testGetEventSetDescriptors() {
            fail("The test case is empty.");
        }
        
        /** Test of getMethodDescriptors method, of class net.sf.cglib.proxy.BeanGenerator.Info. */
        public void testGetMethodDescriptors() {
            fail("The test case is empty.");
        }
        
        /** Test of getPropertyDescriptors method, of class net.sf.cglib.proxy.BeanGenerator.Info. */
        public void testGetPropertyDescriptors() {
            fail("The test case is empty.");
        }
        
    }
    
    
    
}
