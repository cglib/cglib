package net.sf.cglib.transform;

public interface MethodFilter {
    boolean accept(int access, String name, String desc, String[] exceptions);
}
