package net.sf.cglib.transform.impl;

import org.objectweb.asm.Type;

public interface InterceptFieldFilter {
    boolean acceptRead(Type owner, String name);
    boolean acceptWrite(Type owner, String name);
}
