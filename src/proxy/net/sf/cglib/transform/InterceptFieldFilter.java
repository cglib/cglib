package net.sf.cglib.transform;

import org.objectweb.asm.Type;

public interface InterceptFieldFilter {
    boolean acceptRead(Type owner, String name);
    boolean acceptWrite(Type owner, String name);
}
