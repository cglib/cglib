/*
 * Copyright 2003,2004 The Apache Software Foundation
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
package net.sf.cglib.transform;

import junit.framework.*;
import net.sf.cglib.core.CodeGenerationException;

/**
 * @author baliuka
 * $Id: AbstractTransformTest.java,v 1.8 2004/06/24 21:15:16 herbyderby Exp $
 */
abstract public class AbstractTransformTest extends TestCase {
   
    
     /** Creates a new instance of AbstractTransformTest */
    public AbstractTransformTest() {
        super(null);
    }
   
    /** Creates a new instance of AbstractTransformTest */
    public AbstractTransformTest(String s) {
       super(s);
    }
    
    protected abstract ClassTransformerFactory getTransformer() throws Exception;
    
    public Class transform() throws Exception {
        ClassLoader loader =
            new TransformingClassLoader(AbstractTransformTest.class.getClassLoader(),
                                        new ClassFilter(){
                                            public boolean accept(String name){
                                                return !(name.startsWith("java")
                                                         || name.startsWith("junit")
                                                         || name.endsWith("Exclude"));
                                            } 
                                        },
                                        getTransformer());
        try {
            return loader.loadClass(getClass().getName());
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw e;
        }
    }

    protected void postProcess(Class c) {
    }
}
