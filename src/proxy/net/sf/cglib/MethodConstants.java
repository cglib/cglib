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
package net.sf.cglib;

import java.lang.reflect.Method;

/**
 * @author Chris Nokleberg <a href="mailto:chris@nokleberg.com">chris@nokleberg.com</a>
 * @version $Id: MethodConstants.java,v 1.1 2002/12/21 20:21:54 herbyderby Exp $
 */
abstract public class MethodConstants {
    public static final Method EQUALS;
    public static final Method GET_DECLARED_METHOD;
    public static final Method HASH_CODE;
    public static final Method FLOAT_TO_INT_BITS;
    public static final Method DOUBLE_TO_LONG_BITS;
    public static final Method FOR_NAME;
    public static final Method THROWABLE_GET_MESSAGE;
    public static final Method DEFINE_CLASS;
    public static final Method BOOLEAN_VALUE;
    public static final Method CHAR_VALUE;
    public static final Method LONG_VALUE;
    public static final Method DOUBLE_VALUE;
    public static final Method FLOAT_VALUE;
    public static final Method INT_VALUE;
    public static final Method SHORT_INT_VALUE;
    public static final Method BYTE_INT_VALUE;

    static {
        try {
            EQUALS = Object.class.getDeclaredMethod("equals", Constants.TYPES_OBJECT);
            HASH_CODE = Object.class.getDeclaredMethod("hashCode", null);
            FLOAT_TO_INT_BITS = Float.class.getDeclaredMethod("floatToIntBits", new Class[]{ float.class });
            DOUBLE_TO_LONG_BITS = Double.class.getDeclaredMethod("doubleToLongBits", new Class[]{ double.class });
            FOR_NAME = Class.class.getDeclaredMethod("forName", Constants.TYPES_STRING);
            THROWABLE_GET_MESSAGE = Throwable.class.getDeclaredMethod("getMessage", null);

            Class[] types = new Class[]{ String.class, Class[].class };
            GET_DECLARED_METHOD = Class.class.getDeclaredMethod("getDeclaredMethod", types);

            types = new Class[]{ String.class, byte[].class, int.class, int.class };
            DEFINE_CLASS = ClassLoader.class.getDeclaredMethod("defineClass", types);
                                                               
            BOOLEAN_VALUE = Boolean.class.getDeclaredMethod("booleanValue", null);
            CHAR_VALUE = Character.class.getDeclaredMethod("charValue", null);
            LONG_VALUE = Number.class.getDeclaredMethod("longValue", null);
            DOUBLE_VALUE = Number.class.getDeclaredMethod("doubleValue", null);
            FLOAT_VALUE = Number.class.getDeclaredMethod("floatValue", null);
            INT_VALUE = Number.class.getDeclaredMethod("intValue", null);
            SHORT_INT_VALUE = Number.class.getDeclaredMethod("intValue", null);
            BYTE_INT_VALUE = Number.class.getDeclaredMethod("intValue", null);
        } catch (NoSuchMethodException e) {
            throw new CodeGenerationException(e); // impossible
        }
    }
}
