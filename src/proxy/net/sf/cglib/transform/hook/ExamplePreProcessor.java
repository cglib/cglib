package net.sf.cglib.transform.hook;

import net.sf.cglib.core.*;
import net.sf.cglib.transform.*;
import org.objectweb.asm.Type;

public class ExamplePreProcessor extends AbstractPreProcessor {
    private static final Type PRINT_STREAM =
      TypeUtils.parseType("java.io.PrintStream");
    private static final Signature PRINTLN =
      TypeUtils.parseSignature("void println(String)");
    
    protected ClassTransformer getClassTransformer(String name) {
        return new EmittingTransformer() {
            public CodeEmitter begin_method(int access, Signature sig, Type[] exceptions) {
                CodeEmitter e = super.begin_method(access, sig, exceptions);
                if (!TypeUtils.isAbstract(access)) {
                    e.getstatic(Constants.TYPE_SYSTEM, "err", PRINT_STREAM);
                    e.push("Running " + sig.getName() + sig.getDescriptor());
                    e.invoke_virtual(PRINT_STREAM, PRINTLN);
                }
                return e;
            }
        };
    }
}
