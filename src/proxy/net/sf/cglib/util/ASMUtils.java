package net.sf.cglib.util;

// Adapted from ASM Type class

public class ASMUtils {
    private ASMUtils() { }

    public static String getInternalName(Class type) {
        return getInternalName(type.getName());
    }

    public static String getInternalName(String className) {
        return (className == null) ? null : className.replace('.', '/');
    }

    public static String[] getInternalNames(Class[] classes) {
        if (classes == null)
            return null;
        String[] copy = new String[classes.length];
        for (int i = 0; i < copy.length; i++) {
            copy[i] = getInternalName(classes[i]);
        }
        return copy;
    }

    public static String getMethodDescriptor(Class returnType, Class[] parameterTypes) {
        StringBuffer buf = new StringBuffer();
        buf.append('(');
        for (int i = 0; i < parameterTypes.length; ++i) {
            getDescriptor(buf, parameterTypes[i]);
        }
        buf.append(')');
        getDescriptor(buf, returnType);
        return buf.toString();
    }

    public static String getDescriptor(Class type) {
        StringBuffer buf = new StringBuffer();
        getDescriptor(buf, type);
        return buf.toString();
    }

    private static void getDescriptor (final StringBuffer buf, final Class c) {
        Class d = c;
        while (true) {
            if (d.isPrimitive()) {
                char car;
                if (d == Integer.TYPE) {
                    car = 'I';
                } else if (d == Void.TYPE) {
                    car = 'V';
                } else if (d == Boolean.TYPE) {
                    car = 'Z';
                } else if (d == Byte.TYPE) {
                    car = 'B';
                } else if (d == Character.TYPE) {
                    car = 'C';
                } else if (d == Short.TYPE) {
                    car = 'S';
                } else if (d == Double.TYPE) {
                    car = 'D';
                } else if (d == Float.TYPE) {
                    car = 'F';
                } else /*if (d == Long.TYPE)*/ {
                    car = 'J';
                }
                buf.append(car);
                return;
            } else if (d.isArray()) {
                buf.append('[');
                d = d.getComponentType();
            } else {
                buf.append('L');
                String name = d.getName();
                int len = name.length();
                for (int i = 0; i < len; ++i) {
                    char car = name.charAt(i);
                    buf.append(car == '.' ? '/' : car);
                }
                buf.append(';');
                return;
            }
        }
    }
}
