package net.sf.cglib.transform;

import net.sf.cglib.core.CodeGenerationException;
import net.sf.cglib.core.ClassGenerator;
import net.sf.cglib.core.DebuggingLoader;
import org.objectweb.asm.ClassReader;
import java.io.IOException;

abstract public class AbstractLoader extends ClassLoader {
    private ClassFilter filter;
    
    protected AbstractLoader(ClassLoader parent, ClassFilter filter) {
        super(parent);
        this.filter = filter;
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        if (!filter.accept(name)) {
            return super.loadClass(name);
        }

        ClassReader r;
        try {
            r = new ClassReader(name);
        } catch (IOException e) {
            throw new ClassNotFoundException(name + ":" + e.getMessage());
        }

        byte[] data = DebuggingLoader.getBytes(getGenerator(r));
        return super.defineClass(name, data, 0, data.length);
    }

    protected ClassGenerator getGenerator(ClassReader r) {
        return new ClassReaderGenerator(r, true); // skipDebug?
    }
}
