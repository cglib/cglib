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
package net.sf.cglib.transform.impl;

import net.sf.cglib.transform.*;
import java.util.*;
import net.sf.cglib.core.TypeUtils;
import org.objectweb.asm.Type;

public class ExampleTask extends AbstractTransformTask {
    private List properties = new ArrayList();
    private String fieldSuffix = "";
    private ClassTransformer transformer;

    protected ClassTransformer getClassTransformer(String name) {
        return transformer;
    }
    
    protected void beforeExecute() {
        ClassTransformer t1 = new AccessFieldTransformer(new AccessFieldTransformer.Callback() {
            public String getPropertyName(Type owner, String fieldName) {
                return fieldName + fieldSuffix;
            }
        });
        int size = properties.size();
        String[] names = new String[size];
        Type[] types = new Type[size];
        for (int i = 0; i < size; i++) {
            NewProperty p = (NewProperty)properties.get(i);
            names[i] = p.name;
            types[i] = TypeUtils.parseType(p.type);
        }
        ClassTransformer t2 = new AddPropertyTransformer(names, types);
        transformer = new ClassTransformerChain(new ClassTransformer[]{ t1, t2 });
    }

    public static class NewProperty {
        private String name;
        private String type;

        public void setName(String name) {
            this.name = name;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public void addNewproperty(NewProperty prop) {
        properties.add(prop);
    }

    public void setFieldsuffix(String fieldSuffix) {
        this.fieldSuffix = fieldSuffix;
    }
}
