package net.sf.cglib.transform.impl;

import java.lang.reflect.Method;

import net.sf.cglib.core.CodeEmitter;
import net.sf.cglib.core.Constants;
import net.sf.cglib.core.MethodInfo;
import net.sf.cglib.core.ReflectUtils;
import net.sf.cglib.core.Signature;
import net.sf.cglib.transform.ClassEmitterTransformer;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.Type;

/**
 * @author	Mark Hobson
 */
public class AddInitTransformer extends ClassEmitterTransformer {
    private MethodInfo info;
    
    public AddInitTransformer(Method method) {
        info = ReflectUtils.getMethodInfo(method);
        
        Type[] types = info.getSignature().getArgumentTypes();
        if (types.length != 1 ||
        !types[0].equals(Constants.TYPE_OBJECT) ||
        !info.getSignature().getReturnType().equals(Type.VOID_TYPE)) {
            throw new IllegalArgumentException(method + " illegal signature");
        }
    }
    
    public CodeEmitter begin_method(int access, Signature sig, Type[]
    exceptions, Attribute attrs) {
        final CodeEmitter emitter = super.begin_method(access, sig,
        exceptions, attrs);
        
        if (sig.getName().equals(Constants.CONSTRUCTOR_NAME)) {
            return new CodeEmitter(emitter) {
                public void visitInsn(int opcode) {
                    if (opcode == Constants.RETURN) {
                        load_this();
                        invoke(info);
                    }
                    super.visitInsn(opcode);
                }
            };
        }
        return emitter;
    }
}

