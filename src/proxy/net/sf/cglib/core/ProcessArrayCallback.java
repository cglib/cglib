package net.sf.cglib.core;

import org.objectweb.asm.Type;

public interface ProcessArrayCallback {
    void processElement(Type type);
}
