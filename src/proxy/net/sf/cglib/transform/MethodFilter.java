package net.sf.cglib.transform;

public interface MethodFilter {
    // TODO: pass class name too?
    boolean accept(int access, String name, String desc, String[] exceptions);
}
