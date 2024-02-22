
package net.sf.cglib.transform.impl;

import java.io.IOException;
import java.lang.reflect.Method;

import junit.framework.*;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 *
 * @author hengyunabc
 */
public class UndeclaredThrowableStrategyTest extends TestCase {

    static class DemoMethodInterceptor implements MethodInterceptor {

        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            return null;
        }

    }

    @SuppressWarnings("unchecked")
    private static <T> T newInstance(Class<T> clazz) {
        try {
            DemoMethodInterceptor interceptor = new DemoMethodInterceptor();
            Enhancer e = new Enhancer();

            UndeclaredThrowableStrategy ss = new UndeclaredThrowableStrategy(
                    java.lang.reflect.UndeclaredThrowableException.class);
            e.setStrategy(ss);
            e.setSuperclass(clazz);
            e.setCallback(interceptor);
            return (T) e.create();
        } catch (Throwable e) {
            e.printStackTrace();
            throw new Error(e.getMessage());
        }
    }

    static class Person {

        public Person() {
            try {
                new String("persion");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class Student extends Person {
        public Student() {
            try {
                String s = new String("student");
                if (s.length() < 0) {
                    throw new IllegalArgumentException("e");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class Tom extends Person {
        public Tom() throws IOException {
            try {
                String s = new String("student");
                if (s.length() < 0) {
                    throw new IOException("e");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void test() {
        newInstance(Object.class);
        newInstance(Person.class);
        newInstance(Student.class);
        newInstance(Tom.class);
    }

}
