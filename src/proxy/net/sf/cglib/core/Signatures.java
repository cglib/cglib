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

public interface Signatures {
    public static final Signature EQUALS =
      Signature.parse("boolean equals(Object)");
    public static final Signature GET_DECLARED_METHOD =
      Signature.parse("java.lang.reflect.Method getDeclaredMethod(String, Class[])");
    public static final Signature HASH_CODE =
      Signature.parse("int hashCode()");
    public static final Signature STRING_LENGTH =
      Signature.parse("int length()");
    public static final Signature STRING_CHAR_AT =
      Signature.parse("char charAt(int)");
    public static final Signature FOR_NAME =
      Signature.parse("Class forName(String)");

    public static final Signature GET_NAME =
      Signature.parse("String getName()");
    public static final Signature GET_MESSAGE =
      Signature.parse("String getMessage()");

    public static final Signature BOOLEAN_VALUE =
      Signature.parse("boolean booleanValue()");
    public static final Signature CHAR_VALUE =
      Signature.parse("char charValue()");
    public static final Signature LONG_VALUE =
      Signature.parse("long longValue()");
    public static final Signature DOUBLE_VALUE =
      Signature.parse("double doubleValue()");
    public static final Signature FLOAT_VALUE =
      Signature.parse("float floatValue()");
    public static final Signature INT_VALUE =
      Signature.parse("int intValue()");

    public static final Signature GET =
      Signature.parse("Object get(Object)");
    public static final Signature PUT =
      Signature.parse("Object put(Object, Object)");
    public static final Signature KEY_SET =
      Signature.parse("java.util.Set keySet()");

    public static final Signature STATIC =
      Signature.parse("void <clinit>()");

    public static final Signature CSTRUCT_OBJECT =
      Signature.parse("void <init>(Object)");
    public static final Signature CSTRUCT_CLASS =
      Signature.parse("void <init>(Class)");
    public static final Signature CSTRUCT_OBJECT_ARRAY =
      Signature.parse("void <init>(Object[])");
    public static final Signature CSTRUCT_STRING =
      Signature.parse("void <init>(String)");
    public static final Signature CSTRUCT_STRING_ARRAY =
      Signature.parse("void <init>(String[])");
    public static final Signature CSTRUCT_THROWABLE =
      Signature.parse("void <init>(Throwable)");

    public static final Signature DOUBLE_TO_LONG_BITS =
      Signature.parse("long doubleToLongBits(double)");
    public static final Signature FLOAT_TO_INT_BITS =
      Signature.parse("int floatToIntBits(float)");
}
