package net.sf.cglib;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import net.sf.cglib.util.CodeGenerator;

class NoOpCallbackGenerator
implements CallbackGenerator
{
    public static final NoOpCallbackGenerator INSTANCE = new NoOpCallbackGenerator();

    public void generate(CodeGenerator cg, List methods, Context context) { }
    public void generateStatic(CodeGenerator cg, List methods) { }
}
