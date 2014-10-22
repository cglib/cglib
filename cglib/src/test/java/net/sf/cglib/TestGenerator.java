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
package net.sf.cglib;

import net.sf.cglib.core.AbstractClassGenerator;
import net.sf.cglib.core.ReflectUtils;

abstract public class TestGenerator extends AbstractClassGenerator {
    private static int counter;
    
    public TestGenerator(Source source) {
        super(source);
    }

    protected ClassLoader getDefaultClassLoader() {
        return null;
    }

    protected Object firstInstance(Class type) throws Exception {
        return ReflectUtils.newInstance(type);
    }

    protected Object nextInstance(Object instance) throws Exception {
        return instance;
    }

    public Object create() {
        return create(new Integer(counter++));
    }
}
