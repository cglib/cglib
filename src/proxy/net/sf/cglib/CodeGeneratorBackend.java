/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package net.sf.cglib;

abstract public class CodeGeneratorBackend {
    protected String className;
    protected Class superclass;
    protected boolean debug;

    protected CodeGeneratorBackend(String className, Class superclass) {
        this.className = className;
        this.superclass = superclass;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    abstract public byte[] getBytes();
    abstract public void ifeq(String label);
    abstract public void ifne(String label);
    abstract public void iflt(String label);
    abstract public void ifge(String label);
    abstract public void ifgt(String label);
    abstract public void ifle(String label);
    abstract public void goTo(String label);
    abstract public void ifnull(String label);
    abstract public void ifnonnull(String label);
    abstract public void if_icmplt(String label);
    abstract public void if_icmpne(String label);
    abstract public void if_icmpeq(String label);
    abstract public void nop(String label);
    abstract public void imul();
    abstract public void iadd();
    abstract public void lushr();
    abstract public void lxor();
    abstract public void ixor();
    abstract public void l2i();
    abstract public void dcmpg();
    abstract public void fcmpg();
    abstract public void lcmp();
    abstract public void aconst_null();
    abstract public void arraylength();
    abstract public void newarray(Class clazz);
    abstract public void anewarray(Class clazz);
    abstract public void new_instance(String className);
    abstract public void checkcast(String className);
    abstract public void instance_of(String className);
    abstract public void athrow();
    abstract public void pop();
    abstract public void pop2();
    abstract public void dup();
    abstract public void dup2();
    abstract public void dup_x1();
    abstract public void dup_x2();
    abstract public void swap();
    abstract public void invoke_interface(String className, String methodName, Class returnType, Class[] parameterTypes);
    abstract public void invoke_virtual(String className, String methodName, Class returnType, Class[] parameterTypes);
    abstract public void invoke_static(String className, String methodName, Class returnType, Class[] parameterTypes);
    abstract public void invoke_special(String className, String methodName, Class returnType, Class[] parameterTypes);
    abstract public void declare_field(int modifiers, Class type, String name);
    abstract public void getfield(String className, String fieldName, Class type);
    abstract public void putfield(String className, String fieldName, Class type);
    abstract public void getstatic(String className, String fieldName, Class type);
    abstract public void putstatic(String className, String fieldName, Class type);
    abstract public void begin_static();
    abstract public void begin_constructor(Class[] parameterTypes);
    abstract public void declare_interface(Class iface);
    abstract public void begin_method(int modifiers, Class returnType, String name,
                                      Class[] parameterTypes, Class[] exceptionTypes);
    abstract public Object start_range();
    abstract public Object end_range();
    abstract public void handle_exception(Object start, Object end, Class exceptionType);
    abstract public void end_method();
    abstract public void ldc(String value);
    abstract public void ldc(double value);
    abstract public void ldc(long value);
    abstract public void ldc(int value);
    abstract public void ldc(float value);
    abstract public void laload();
    abstract public void daload();
    abstract public void faload();
    abstract public void saload();
    abstract public void caload();
    abstract public void iaload();
    abstract public void baload();
    abstract public void aaload();
    abstract public void lastore();
    abstract public void dastore();
    abstract public void fastore();
    abstract public void sastore();
    abstract public void castore();
    abstract public void iastore();
    abstract public void bastore();
    abstract public void aastore();
    abstract public void iconst(int value);
    abstract public void bipush(byte value);
    abstract public void sipush(short value);
    abstract public void lconst(long value);
    abstract public void fconst(float value);
    abstract public void dconst(double value);
    abstract public void lload(int index);
    abstract public void dload(int index);
    abstract public void fload(int index);
    abstract public void iload(int index);
    abstract public void aload(int index);
    abstract public void lstore(int index);
    abstract public void dstore(int index);
    abstract public void fstore(int index);
    abstract public void istore(int index);
    abstract public void astore(int index);
    abstract public void returnVoid();
    abstract public void lreturn();
    abstract public void dreturn();
    abstract public void freturn();
    abstract public void ireturn();
    abstract public void areturn();
    abstract public void iinc(int index, int amount);
}
