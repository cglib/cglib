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

import junit.framework.*;
import net.sf.cglib.beans.*;
import net.sf.cglib.core.*;
import net.sf.cglib.proxy.*;
import net.sf.cglib.reflect.*;
import net.sf.cglib.transform.*;
import net.sf.cglib.transform.impl.*;
import net.sf.cglib.util.*;

/**
 *@author     Gerhard Froehlich <a href="mailto:g-froehlich@gmx.de">
 *      g-froehlich@gmx.de</a>
 *@version    $Id: TestAll.java,v 1.55 2003/11/18 10:23:22 herbyderby Exp $
 */
public class TestAll extends TestCase {
    
    public static String DEFAULT_DEBUG_LOACATION = System.getProperty("user.home") + 
          System.getProperty("file.separator") + "cglib-debug";
    
    public TestAll(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception{
       
        // System.setSecurityManager( new java.rmi.RMISecurityManager());
        
        System.getProperties().list(System.out);
        TestSuite suite = new TestSuite();

        // root
        suite.addTest(TestEnhancer.suite());
        suite.addTest(TestProxy.suite());
        suite.addTest(TestDispatcher.suite());
        suite.addTest(TestLazyLoader.suite());
        suite.addTest(TestNoOp.suite());
        suite.addTest(TestMixin.suite());

        // beans
        suite.addTest(TestBulkBean.suite());
        suite.addTest(TestBeanMap.suite());
        suite.addTest(TestImmutableBean.suite());
        suite.addTest(TestBeanCopier.suite());

        // reflect
        suite.addTest(TestDelegates.suite());
        suite.addTest(TestFastClass.suite());

        // core
        suite.addTest(TestKeyFactory.suite());
        suite.addTest(TestTinyBitSet.suite());
        
        // util
        suite.addTest(TestParallelSorter.suite());

        // transform
        suite.addTest(TestTransformingLoader.suite());
        suite.addTest(TestAddClassInit.suite());
        suite.addTest(TestProvideFields.suite());
        suite.addTest(TestAddDelegate.suite());
        suite.addTest(TestInterceptFields.suite());
        suite.addTest(TestDemo.suite());

        // performance
        // suite.addTest(TestReflectPerf.suite());
        // suite.addTest(TestXmlParsing.suite());
        return suite;
    }

    public static void main(String args[])throws Exception {
        if(System.getProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY) ==  null){
         System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY,DEFAULT_DEBUG_LOACATION);
        }
        String[] testCaseName = {TestAll.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
       
    }
}

