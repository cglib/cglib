package net.sf.cglib;

import java.lang.reflect.Method;

public class Callbacks
{
    public static final int NO_OP = 0;
    public static final int INTERCEPT = 1;
    public static final int JDK_PROXY = 2;
    public static final int LAZY_LOAD = 3;
    static final int MAX_VALUE = 3; // should be set to current max index
    
    private Object[] callbacks = new Object[MAX_VALUE + 1];

    public Object get(int type) {
        return callbacks[type];
    }

    public void set(int type, Object callback) {
        if (callback == null ||
            !getType(type).isAssignableFrom(callback.getClass())) {
            throw new IllegalArgumentException("Callback " + callback + " has incorrect type " + type);
        }
        callbacks[type] = callback;
    }

    static Class getType(int type) {
        switch (type) {
        case JDK_PROXY:
        case INTERCEPT:
            return MethodInterceptor.class;
        case LAZY_LOAD:
            return LazyLoader.class;
        default:
            return null;
        }
    }

    static CallbackGenerator getGenerator(int type) {
        switch (type) {
        case INTERCEPT:
            return MethodInterceptorGenerator.INSTANCE;
        case LAZY_LOAD:
            return LazyLoaderGenerator.INSTANCE;
        case NO_OP:
            return NoOpCallbackGenerator.INSTANCE;
        case JDK_PROXY:
            return InvocationHandlerGenerator.INSTANCE;
        default:
            return null;
        }
    }
}
