package net.sf.cglib.core;

import org.objectweb.asm.Type;

public interface Customizer {
    void customize(Emitter e, Type type);
}
