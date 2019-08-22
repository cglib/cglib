package net.sf.cglib.classloader;

import java.io.*;
import java.net.URL;

public class FilteringClassLoader extends ClassLoader {

    private final ClassLoader delegatingClassLoader; // Class loader to load actual implementation of filtered classes/resources
    private final String[] filterPrefixIncludes;     // Class name prefixes which should be filtered by this classloader (except for excluded)
    private final String[] filterPrefixExcludes;     // Class name prefixes which should be ignored by this classloader 

    public FilteringClassLoader(ClassLoader parent, ClassLoader delegatingClassLoader, String[] filterPrefixIncludes, String[] filterPrefixExcludes) {
        super(parent);
        this.delegatingClassLoader = delegatingClassLoader;
        this.filterPrefixIncludes = filterPrefixIncludes;
        this.filterPrefixExcludes = filterPrefixExcludes;
    }

    public FilteringClassLoader(ClassLoader parent, ClassLoader delegatingClassLoader, String ... filterPrefixIncludes) {
        this(parent, delegatingClassLoader, filterPrefixIncludes, null);
    }

    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (!needFiltering(name)) {
            return super.loadClass(name, resolve);
        }

        synchronized(this) {
            // Check whether this class is already loaded by this classloader
            Class<?> cl = findLoadedClass(name);
            // Do not load from parent classloader - try to find class file in delegating classloader and load it in this classloader
            if (cl == null && delegatingClassLoader != null) {
                InputStream inputStream = delegatingClassLoader.getResourceAsStream(name.replace('.','/')+".class");
                if (inputStream != null) {
                    // Class file was found - load it
                    ByteArrayOutputStream targetBuffer = new ByteArrayOutputStream();
                    try {
                        try {
                            byte[] buffer = new byte[1024];
                            for(int r = inputStream.read(buffer, 0, buffer.length); r > 0; r = inputStream.read(buffer, 0, buffer.length)) {
                                targetBuffer.write(buffer, 0, r);
                            }
                        } finally {
                            inputStream.close();
                        }
                    } catch (IOException e) {
                        throw new ClassNotFoundException("Cannot load class with name '"+name+"' from delegating classloader", e);
                    }
                    byte[] classBytes = targetBuffer.toByteArray();
                    // Define and resolve class (if required)
                    cl = defineClass(name, classBytes, 0, classBytes.length);
                    if (resolve) {
                        resolveClass(cl);
                    }
                }
            }
            // Filtering result - either class loaded in this classloader or exception
            if (cl != null) {
                return cl;
            } else {
                throw new ClassNotFoundException("Class with name '"+name+"' should not be loaded from parent classloader and is not found in delegating classloader");
            }
        }
    }

    public URL getResource(String name) {
        if (!needFiltering(name.replace('/','.'))) {
            return super.getResource(name);
        }

        return delegatingClassLoader != null ? delegatingClassLoader.getResource(name) : null;
    }

    private boolean needFiltering(String name) {
        if (filterPrefixExcludes != null) {
            for(String filterPrefixExclude : filterPrefixExcludes) {
                if (name.startsWith(filterPrefixExclude)) {
                    return false;
                }
            }
        }
        if (filterPrefixIncludes != null) {
            for(String filterPrefixInclude : filterPrefixIncludes) {
                if (name.startsWith(filterPrefixInclude)) {
                    return true;
                }
            }
        }   
        return false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FilteringClassLoader[include=(");
        join(sb, filterPrefixIncludes);
        sb.append("), exclude=(");
        join(sb, filterPrefixExcludes);
        sb.append(")]");
        return sb.toString();
    }

    private void join(StringBuilder sb, String[] ar) {
        if (ar != null) {
            boolean first = true;
            for (String item : ar) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                sb.append(item);
            }
        }
    }
}