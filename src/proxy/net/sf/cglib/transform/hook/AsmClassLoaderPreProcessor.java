package net.sf.cglib.transform.hook;

import java.io.*;
import net.sf.cglib.core.RemappingCodeVisitor;
import org.codehaus.aspectwerkz.hook.ClassLoaderPatcher;
import org.codehaus.aspectwerkz.hook.ClassLoaderPreProcessor;
import org.objectweb.asm.*;

/**
 * Instruments the java.lang.ClassLoader to plug in the ClassPreProcessor
 * mechanism using ASM.
 *
 * @author Chris Nokleberg
 */
public class AsmClassLoaderPreProcessor implements ClassLoaderPreProcessor {
    private static final String DESC_CORE = "Ljava/lang/String;[BIILjava/security/ProtectionDomain;";
    private static final String DESC_PREFIX = "(" + DESC_CORE;
    private static final String DESC_HELPER = "(Ljava/lang/ClassLoader;" + DESC_CORE + ")[B";

    public AsmClassLoaderPreProcessor() {
    }

    public byte[] preProcess(byte[] b) {
        try {
            ClassWriter w = new ClassWriter(true) {
                private boolean flag;
                public void visit(int access, String name, String superName, String[] interfaces, String sourceFile) {
                    super.visit(access, name, superName, interfaces, sourceFile);
                    flag = name.equals("java/lang/ClassLoader"); // is this ok?
                }
                public CodeVisitor visitMethod(int access, String name, String desc, String[] exceptions, Attribute attrs) {
                    CodeVisitor v = super.visitMethod(access, name, desc, exceptions, attrs);
                    if (flag) {
                        v = new PreProcessingVisitor(v, access, desc);
                    }
                    return v;
                }
            };
            new ClassReader(b).accept(w, false);
            return w.toByteArray();
        } catch (Exception e) {
            System.err.println("failed to patch ClassLoader:");
            e.printStackTrace();
            return b;
        }
    }

    private static class PreProcessingVisitor extends RemappingCodeVisitor {
        public PreProcessingVisitor(CodeVisitor v, int access, String desc) {
            super(v, access, Type.getArgumentTypes(desc));
        }

        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            if ("defineClass0".equals(name) && "java/lang/ClassLoader".equals(owner)) {
                Type[] args = Type.getArgumentTypes(desc);
                if (args.length < 5 || !desc.startsWith(DESC_PREFIX)) {
                     throw new Error("non standard JDK, native call not supported: " + desc);
                }
                int[] locals = new int[args.length];
                for (int i = args.length - 1; i >= 0; i--) {
                    cv.visitVarInsn(args[i].getOpcode(Constants.ISTORE),
                                    locals[i] = nextLocal(args[i].getSize()));
                }
                for (int i = 0; i < 5; i++) {
                    cv.visitVarInsn(args[i].getOpcode(Constants.ILOAD), locals[i]);
                }
                super.visitMethodInsn(Constants.INVOKESTATIC,
                                      "org/codehaus/aspectwerkz/hook/impl/ClassPreProcessorHelper",
                                      "defineClass0Pre",
                                      DESC_HELPER);
                cv.visitVarInsn(Constants.ASTORE, locals[1]);
                cv.visitVarInsn(Constants.ALOAD, 0);
                cv.visitVarInsn(Constants.ALOAD, locals[0]); // name
                cv.visitVarInsn(Constants.ALOAD, locals[1]); // bytes
                cv.visitInsn(Constants.ICONST_0); // offset
                cv.visitVarInsn(Constants.ALOAD, locals[1]);
                cv.visitInsn(Constants.ARRAYLENGTH); // length
                cv.visitVarInsn(Constants.ALOAD, locals[4]); // protection domain
                for (int i = 5; i < args.length; i++) {
                    cv.visitVarInsn(args[i].getOpcode(Constants.ILOAD), locals[i]);
                }
            }
            super.visitMethodInsn(opcode, owner, name, desc);
        }
    }

    public static void main(String args[]) throws Exception {
        ClassLoaderPreProcessor me = new AsmClassLoaderPreProcessor();
        InputStream is = ClassLoader.getSystemClassLoader().getParent().getResourceAsStream("java/lang/ClassLoader.class");
        byte[] out = me.preProcess(ClassLoaderPatcher.inputStreamToByteArray(is));
        is.close();
        OutputStream os = new FileOutputStream("_boot/java/lang/ClassLoader.class");
        os.write(out);
        os.close();
    }
}
