package net.sf.cglib.transform;

import org.objectweb.asm.Attribute;

public interface MethodFilter {
    // TODO: pass class name too?
    boolean accept(int access, String name, String desc, String[] exceptions, Attribute attrs);
}
