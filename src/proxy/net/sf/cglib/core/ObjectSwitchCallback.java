package net.sf.cglib.core;

import org.objectweb.asm.Label;

public interface ObjectSwitchCallback {
    void processCase(Object key, Label end) throws Exception;
    void processDefault() throws Exception;
}

