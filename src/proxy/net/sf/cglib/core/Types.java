/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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
package net.sf.cglib.core;

import org.objectweb.asm.Type;

public interface Types {

    public static final Type[] EMPTY = {};

    public static final Type OBJECT_ARRAY = Signature.parseType("Object[]");
    public static final Type OBJECT = Signature.parseType("Object");
    public static final Type CLASS = Signature.parseType("Class");
    public static final Type BIG_INTEGER = Signature.parseType("java.math.BigInteger");
    public static final Type BIG_DECIMAL = Signature.parseType("java.math.BigDecimal");
    public static final Type CHARACTER = Signature.parseType("Character");
    public static final Type BOOLEAN = Signature.parseType("Boolean");
    public static final Type DOUBLE = Signature.parseType("Double");
    public static final Type FLOAT = Signature.parseType("Float");
    public static final Type LONG = Signature.parseType("Long");
    public static final Type INTEGER = Signature.parseType("Integer");
    public static final Type SHORT = Signature.parseType("Short");
    public static final Type BYTE = Signature.parseType("Byte");
    public static final Type NUMBER = Signature.parseType("Number");
    public static final Type STRING = Signature.parseType("String");
    public static final Type THREAD_LOCAL = Signature.parseType("ThreadLocal");
    public static final Type THROWABLE = Signature.parseType("Throwable");
    public static final Type ERROR = Signature.parseType("Error");
    public static final Type EXCEPTION = Signature.parseType("Exception");
    public static final Type RUNTIME_EXCEPTION = Signature.parseType("RuntimeException");
    public static final Type METHOD = Signature.parseType("java.lang.reflect.Method");
    public static final Type ILLEGAL_STATE_EXCEPTION = Signature.parseType("IllegalStateException");
    public static final Type ILLEGAL_ARGUMENT_EXCEPTION = Signature.parseType("IllegalArgumentException");
    public static final Type NO_CLASS_DEF_FOUND_ERROR = Signature.parseType("NoClassDefFoundError");
    public static final Type CLASS_NOT_FOUND_EXCEPTION = Signature.parseType("ClassNotFoundException");
    public static final Type ABSTRACT_METHOD_ERROR = Signature.parseType("AbstractMethodError");
    public static final Type CLASS_CAST_EXCEPTION = Signature.parseType("ClassCastException");
    public static final Type NO_SUCH_METHOD_ERROR = Signature.parseType("NoSuchMethodError");
}
    
