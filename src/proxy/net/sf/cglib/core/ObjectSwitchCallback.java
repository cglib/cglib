package net.sf.cglib.core;

public interface ObjectSwitchCallback {
    void processCase(Object key, org.objectweb.asm.Label end) throws Exception;
    void processDefault() throws Exception;
}

