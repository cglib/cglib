package net.sf.cglib.core;

import org.objectweb.asm.ClassWriter;

public class ClassLoaderAwareClassWriter extends ClassWriter {

    private ClassLoader cl;

    public ClassLoaderAwareClassWriter(int flags) {
        super(flags);
    }

    public void setClassLoader(ClassLoader cl) {
        this.cl = cl;
    }

    public ClassLoader getClassLoader() {
        return cl;
    }

    @Override
    protected String getCommonSuperClass(final String type1, final String type2) {
        Class<?> c, d;
        ClassLoader currentClassLoader = getClassLoader();
        ClassLoader classLoader = currentClassLoader != null ? currentClassLoader : getClass().getClassLoader();
        try {
            c = Class.forName(type1.replace('/', '.'), false, classLoader);
            d = Class.forName(type2.replace('/', '.'), false, classLoader);
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
        if (c.isAssignableFrom(d)) {
            return type1;
        }
        if (d.isAssignableFrom(c)) {
            return type2;
        }
        if (c.isInterface() || d.isInterface()) {
            return "java/lang/Object";
        } else {
            do {
                c = c.getSuperclass();
            } while (!c.isAssignableFrom(d));
            return c.getName().replace('.', '/');
        }
    }
}