package net.sf.cglib.classloader;

import net.sf.cglib.core.Block;
import net.sf.cglib.core.CodeEmitter;
import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.*;
import net.sf.cglib.transform.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.TestCase;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

/*
  This class contains tests which works with specific class loader structure:
   * (parent) Class-loader which loads cglib classes
   * (child) Class-loader which loads application classes
*/
public class TestMultiLevelClassLoader extends TestCase {

    public static class TestException extends Exception {
        public TestException(Throwable t) {
            super(t);
        }
    }

    public static class TestObject {
        public void foo() throws TestException {
        }
    }

    public static class TestFrameGeneration implements InvocationHandler {

        public Object invoke(Object proxy, Method method, Object[] args) {
            return null;
        } 

        public static void run() throws Exception {
            Enhancer e = new Enhancer();
            e.setSuperclass(TestObject.class);
            e.setCallbacks(new Callback[]{new TestFrameGeneration()});
            Object o = e.create();
            o.getClass().getMethod("foo").invoke(o);
        }
    }

    public static class TestTransformation {
        public static void run() throws Exception {
            ClassLoader transformer = new TransformingClassLoader(TestTransformation.class.getClassLoader(),
                                                                  new ClassFilter() {
                                                                      public boolean accept(String name) {
                                                                          return "net.sf.cglib.classloader.TestMultiLevelClassLoader$TestObject".equals(name);
                                                                      }
                                                                  },
                                                                  new ClassTransformerFactory() {
                                                                      public ClassTransformer newInstance() {
                                                                          return new ClassEmitterTransformer() {
                                                                              @Override
                                                                              public CodeEmitter begin_method(int access, Signature sig, Type[] exceptions) {
                                                                                  CodeEmitter e = super.begin_method(access, sig, exceptions);
                                                                                  if (sig.getName().equals("foo")) {
                                                                                      Label next = e.make_label();
                                                                                      Block block = e.begin_block();
                                                                                      e.push(0);
                                                                                      e.pop();
                                                                                      e.goTo(next);
                                                                                      block.end();
                                                                                      e.catch_exception(block, Type.getType(RuntimeException.class));
                                                                                      e.catch_exception(block, Type.getType(TestException.class));
                                                                                      e.athrow();
                                                                                      e.mark(next);
                                                                                  }
                                                                                  return e;
                                                                              }
                                                                          };
                                                                      }
                                                                  });
            Object o = transformer.loadClass("net.sf.cglib.classloader.TestMultiLevelClassLoader$TestObject").newInstance();
            o.getClass().getMethod("foo").invoke(o);
        }
    }
    
    public void testFrameGeneration() throws Exception {
        runAndUnwrap(buildTestClassLoaderHierarchy(), "net.sf.cglib.classloader.TestMultiLevelClassLoader$TestFrameGeneration");
    }

    public void testTransformation() throws Exception {
        runAndUnwrap(buildTestClassLoaderHierarchy(), "net.sf.cglib.classloader.TestMultiLevelClassLoader$TestTransformation");
    }

    private ClassLoader buildTestClassLoaderHierarchy() {
        ClassLoader currentClassLoader = getClass().getClassLoader();
        // Next two classloader ensures that cglib runtime cannot see test classes from its classloader
        ClassLoader cl1 = new FilteringClassLoader(currentClassLoader, null, "net.sf.cglib.classloader.TestMultiLevelClassLoader$Test");
        ClassLoader cl2 = new FilteringClassLoader(cl1, currentClassLoader, new String[]{"net.sf.cglib.", "org.objectweb.asm"}, 
                                                                            new String[]{"net.sf.cglib.classloader.TestMultiLevelClassLoader$Test"});
        // Ensure that test classes are loaded here
        ClassLoader cl3 = new FilteringClassLoader(cl2, currentClassLoader, "net.sf.cglib.classloader.TestMultiLevelClassLoader$Test");

        return cl3;
    }

    /**
     * Lookup for given test-class in given classloader and unwrap InvocationTargetException
     */
    private void runAndUnwrap(ClassLoader cl, String className) throws Exception {
        try {
            cl.loadClass(className).getMethod("run").invoke(null);
        } catch (InvocationTargetException e) {
            throw e.getCause() instanceof Exception ? (Exception)e.getCause() : e;
        }
    }
}