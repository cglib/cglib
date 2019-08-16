package net.sf.cglib.proxy;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

import java.lang.reflect.Method;

public class TestEnhancerDeadlock extends TestCase {
    public TestEnhancerDeadlock(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(TestEnhancerDeadlock.class);
    }

    public void testDeadlock() {
        EnhancedClass one =
                (EnhancedClass) Enhancer.create(EnhancedClass.class, new MethodInterceptor() {
                    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) {
                        return "from testDeadlock";
                    }
                });

        Assert.assertEquals("value should be intercepted as per testDeadlock", "from testDeadlock", one.test());
        Assert.assertEquals("value should be intercepted as per static init", "from <clinit>", EnhancedClass.INSTANCE.test());
    }


    public static class EnhancedClass {
        // Call cglib API from within static block to trigger deadlock in Cglib caches
        final static EnhancedClass INSTANCE =
                (EnhancedClass) Enhancer.create(EnhancedClass.class, new MethodInterceptor() {
                    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) {
                        return "from <clinit>";
                    }
                });

        public String test() {
            return "original";
        }
    }
}
