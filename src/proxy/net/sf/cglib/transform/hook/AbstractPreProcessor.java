package net.sf.cglib.transform.hook;

import java.util.Hashtable;
import net.sf.cglib.core.*;
import net.sf.cglib.transform.*;
import org.codehaus.aspectwerkz.hook.ClassPreProcessor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

abstract public class AbstractPreProcessor implements ClassPreProcessor {
    public void initialize(Hashtable hashtable) {
    }

    public byte[] preProcess(String name, byte[] abyte, ClassLoader caller) {
        try {
            ClassTransformer t = getClassTransformer(name);
            if (t == null)
                return abyte;
            ClassWriter w = new DebuggingClassWriter(true);
            ClassGenerator gen = new ClassReaderGenerator(new ClassReader(abyte), false);
            gen = new TransformingGenerator(gen, t);
            gen.generateClass(w);
            return w.toByteArray();
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }
    }

    abstract protected ClassTransformer getClassTransformer(String name);
}
