package net.sf.cglib.transform;

import net.sf.cglib.core.ClassGenerator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

public class ClassReaderGenerator implements ClassGenerator {
    private ClassReader r;
    private boolean skipDebug;
    
    public ClassReaderGenerator(ClassReader r, boolean skipDebug) {
        this.r = r;
        this.skipDebug = skipDebug;
    }
    
    public void generateClass(ClassVisitor v) {
        r.accept(v, skipDebug);
    }
}
