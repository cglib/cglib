package net.sf.cglib;

import java.lang.reflect.*;
import java.util.List;
import net.sf.cglib.util.CodeGenerator;
import net.sf.cglib.util.CodeGenerationException;

interface CallbackGenerator
{
    void generate(CodeGenerator cg, List methods, Context context) throws Exception;
    void generateStatic(CodeGenerator cg, List methods) throws Exception;

    interface Context
    {
        void emitCallback();
        int getModifiers(Method method);
    }
}
