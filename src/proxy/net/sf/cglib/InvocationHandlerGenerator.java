package net.sf.cglib;

import net.sf.cglib.util.CodeGenerator;

class InvocationHandlerGenerator
extends BeforeAfterGenerator
{
    public static final InvocationHandlerGenerator INSTANCE = new InvocationHandlerGenerator();

    protected void unbox(CodeGenerator cg, Class type) {
        cg.unbox(type);
    }
}
