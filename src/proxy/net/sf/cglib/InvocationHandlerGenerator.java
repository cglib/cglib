package net.sf.cglib;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import net.sf.cglib.util.CodeGenerator;

class InvocationHandlerGenerator
extends MethodInterceptorGenerator
{
    public static final InvocationHandlerGenerator INSTANCE = new InvocationHandlerGenerator();

    protected void unbox(CodeGenerator cg, Class type) {
        cg.unbox(type);
    }
}
