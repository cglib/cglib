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
package net.sf.cglib.transform.impl;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.cglib.core.Constants;
import net.sf.cglib.core.EmitUtils;
import net.sf.cglib.core.ReflectUtils;
import net.sf.cglib.transform.ClassFilter;
import net.sf.cglib.transform.ClassTransformer;
import net.sf.cglib.transform.ClassTransformerChain;
import net.sf.cglib.transform.ClassTransformerFactory;
import net.sf.cglib.transform.TransformingClassLoader;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @version $Id: TestTransformingLoader.java,v 1.6 2006/03/05 02:43:17 herbyderby Exp $
 */
public class TestTransformingLoader extends net.sf.cglib.CodeGenTestCase {

    private static final ClassFilter TEST_FILTER = new ClassFilter() {
        public boolean accept(String name) {
            System.err.println("Loading " + name);
            return name.startsWith("net.sf.cglib.");
        }
    };

    private ClassTransformer getExampleTransformer(String name, Type type) {
        return new AddPropertyTransformer(new String[]{ name }, new Type[]{ type });
    }

    public void testExample() throws Exception {
        ClassTransformer t1 = getExampleTransformer("herby", Constants.TYPE_STRING);
        ClassTransformer t2 = getExampleTransformer("derby", Type.DOUBLE_TYPE);
        ClassTransformer chain = new ClassTransformerChain(new ClassTransformer[]{ t1, t2 });
        Class loaded = loadHelper(chain, Example.class);
        Object obj = loaded.newInstance();
        String value = "HELLO";
        loaded.getMethod("setHerby", new Class[]{ String.class }).invoke(obj, new Object[]{ value });
        assertTrue(value.equals(loaded.getMethod("getHerby", (Class[])null).invoke(obj, (Object[])null)));

        loaded.getMethod("setDerby", new Class[]{ Double.TYPE }).invoke(obj, new Object[]{ new Double(1.23456789d) });
    }

    private static Class inited;

    public static void initStatic(Class foo) {
        System.err.println("INITING: " + foo);
    }

    public void testAddStatic() throws Exception {
        Method m = ReflectUtils.findMethod("net.sf.cglib.transform.impl.TestTransformingLoader.initStatic(Class)");
        ClassTransformer t = new AddStaticInitTransformer(m);
        // t = new ClassTransformerChain(new ClassTransformer[]{ t, new ClassTransformerTee(new org.objectweb.asm.util.TraceClassVisitor(null, new java.io.PrintWriter(System.out))) });
        Class loaded = loadHelper(t, Example.class);
        Object obj = loaded.newInstance();
        // TODO
    }

    public void testInterceptField() throws Exception {
        ClassTransformer t = new InterceptFieldTransformer(new InterceptFieldFilter() {
            public boolean acceptRead(Type owner, String name) {
                return true;
            }
            public boolean acceptWrite(Type owner, String name) {
                return true;
            }
        });
        Class loaded = loadHelper(t, Example.class);
        // TODO
    }

    public void testFieldProvider() throws Exception {
        ClassTransformer t = new FieldProviderTransformer();
        Class loaded = loadHelper(t, Example.class);
        // TODO
//         FieldProvider fp = (FieldProvider)loaded.newInstance();
//         assertTrue(((Integer)fp.getField("example")).intValue() == 42);
//         fp.setField("example", new Integer(6));
//         assertTrue(((Integer)fp.getField("example")).intValue() == 6);
//         assertTrue(fp.getField("example") == null);
//         try {
//             fp.getField("dsfjkl");
//             fail("expected exception");
//         } catch (IllegalArgumentException ignore) { }
    }

    private static Class loadHelper( final ClassTransformer t, Class target) throws ClassNotFoundException {
        ClassLoader parent = TestTransformingLoader.class.getClassLoader();
        TransformingClassLoader loader = new TransformingClassLoader(parent, TEST_FILTER,
        
           new ClassTransformerFactory(){
                  public ClassTransformer  newInstance(){
                     return t;
                  }
        }
        
        );
        return loader.loadClass(target.getName());
    }

    public TestTransformingLoader(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestTransformingLoader.class);
    }

    public void perform(ClassLoader loader) throws Throwable {
    }    
    
    public void testFailOnMemoryLeak() throws Throwable {
    }

    /**
     * Test that an empty setting will result in no prefix.
     *
     * @throws ClassNotFoundException If Example.class is not found
     */
    public void testWithEmptyPrefix() throws ClassNotFoundException {
        System.setProperty(EmitUtils.CGLIB_PROP_PREFIX, "");
        ClassTransformer t1 = getExampleTransformer("herby", Constants.TYPE_STRING);
        ClassTransformer t2 = getExampleTransformer("derby", Type.DOUBLE_TYPE);
        ClassTransformer chain = new ClassTransformerChain(new ClassTransformer[]{t1, t2});
        Class loaded = loadHelper(chain, Example.class);

        Field[] fields = loaded.getDeclaredFields();

        int count=0;
        for (Field field : fields) {
            String fieldName = field.getName();
            if(fieldName.endsWith("herby")){
                count++;
                assertEquals("herby", fieldName);
            }
            if(fieldName.endsWith("derby")){
                count++;
                assertEquals("derby", fieldName);
            }
            System.out.println("Name of the field: "
                    + fieldName);
        }
        assertEquals(2, count);
    }

    /**
     *
     * @throws ClassNotFoundException
     */
    public void testWithTestPrefix() throws ClassNotFoundException {
        System.setProperty(EmitUtils.CGLIB_PROP_PREFIX, "cglib_prop_test_");
        ClassTransformer t1 = getExampleTransformer("herby", Constants.TYPE_STRING);
        ClassTransformer t2 = getExampleTransformer("derby", Type.DOUBLE_TYPE);
        ClassTransformer chain = new ClassTransformerChain(new ClassTransformer[]{t1, t2});
        Class loaded = loadHelper(chain, Example.class);


        Field[] fields = loaded.getDeclaredFields();
        int count = 0;
        for (Field field : fields) {
            String fieldName = field.getName();

            if(fieldName.endsWith("herby")){
                count++;
                assertEquals("cglib_prop_test_herby", fieldName);
            }
            if(fieldName.endsWith("derby")){
                count++;
                assertEquals("cglib_prop_test_derby", fieldName);
            }
            System.out.println("Name of the field: "
                    + fieldName);
        }
        assertEquals(2, count);
    }

    /**
     * Validate that the default cglib behavior is unchanged if the property if the EmitUtils.CGLIB_PROP_PREFIX
     * System property is not set.
     *
     * @throws ClassNotFoundException if Example.class is not found
     */
    public void testWithNoPrefixConfig() throws ClassNotFoundException {
        System.clearProperty(EmitUtils.CGLIB_PROP_PREFIX);

        ClassTransformer t1 = getExampleTransformer("herby", Constants.TYPE_STRING);
        ClassTransformer t2 = getExampleTransformer("derby", Type.DOUBLE_TYPE);
        ClassTransformer chain = new ClassTransformerChain(new ClassTransformer[]{t1, t2});
        Class loaded = loadHelper(chain, Example.class);


        Field[] fields = loaded.getDeclaredFields();
        int count = 0;
        // get the name of every method present in the list
        for (Field field : fields) {
            String fieldName = field.getName();

            if(fieldName.endsWith("herby")){
                count++;
                assertEquals(EmitUtils.CGLIB_PROP_PREFIX_DEFAULT + "herby", fieldName);
            }
            if(fieldName.endsWith("derby")){
                count++;
                assertEquals(EmitUtils.CGLIB_PROP_PREFIX_DEFAULT + "derby", fieldName);
            }
            System.out.println("Name of the field: "
                    + fieldName);
        }
        assertEquals(2, count);
    }

}
