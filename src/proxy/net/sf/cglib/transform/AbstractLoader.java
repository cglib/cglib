package net.sf.cglib.transform;

import net.sf.cglib.core.CodeGenerationException;
import net.sf.cglib.core.ClassGenerator;
import net.sf.cglib.core.DebuggingClassWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
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

        try {
            ClassWriter w = new DebuggingClassWriter(true);
            getGenerator(r).generateClass(w);
            byte[] b = w.toByteArray();
            return super.defineClass(name, b, 0, b.length);
        } catch (RuntimeException e) {
            throw e;
        } catch (Error e) {
            throw e;
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }
    }

    protected ClassGenerator getGenerator(ClassReader r) {
        return new ClassReaderGenerator(r, true); // skipDebug?
    }
}
