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

/**
 * @author Juozas Baliuka <a href="mailto:baliuka@mwm.lt">baliuka@mwm.lt</a>
 * @version $Id: Constants.java,v 1.14 2003/10/05 03:57:20 herbyderby Exp $
 */
public interface Constants extends org.objectweb.asm.Constants {
    public static final Class[] EMPTY_CLASS_ARRAY = {};
    public static final Type[] TYPES_EMPTY = {};
    
    public static final Type TYPE_OBJECT_ARRAY = TypeUtils.parseType("Object[]");
    public static final Type TYPE_CLASS_ARRAY = TypeUtils.parseType("Class[]");
    public static final Type TYPE_STRING_ARRAY = TypeUtils.parseType("String[]");

    public static final Type TYPE_OBJECT = TypeUtils.parseType("Object");
    public static final Type TYPE_CLASS = TypeUtils.parseType("Class");
    public static final Type TYPE_CHARACTER = TypeUtils.parseType("Character");
    public static final Type TYPE_BOOLEAN = TypeUtils.parseType("Boolean");
    public static final Type TYPE_DOUBLE = TypeUtils.parseType("Double");
    public static final Type TYPE_FLOAT = TypeUtils.parseType("Float");
    public static final Type TYPE_LONG = TypeUtils.parseType("Long");
    public static final Type TYPE_INTEGER = TypeUtils.parseType("Integer");
    public static final Type TYPE_SHORT = TypeUtils.parseType("Short");
    public static final Type TYPE_BYTE = TypeUtils.parseType("Byte");
    public static final Type TYPE_NUMBER = TypeUtils.parseType("Number");
    public static final Type TYPE_STRING = TypeUtils.parseType("String");
    public static final Type TYPE_THROWABLE = TypeUtils.parseType("Throwable");
    public static final Type TYPE_BIG_INTEGER = TypeUtils.parseType("java.math.BigInteger");
    public static final Type TYPE_BIG_DECIMAL = TypeUtils.parseType("java.math.BigDecimal");
    public static final Type TYPE_STRING_BUFFER = TypeUtils.parseType("StringBuffer");
    public static final Type TYPE_RUNTIME_EXCEPTION = TypeUtils.parseType("RuntimeException");
    public static final Type TYPE_ERROR = TypeUtils.parseType("Error");
    
    public static final String CONSTRUCTOR_NAME = "<init>";
    public static final String STATIC_NAME = "<clinit>";
    public static final String SOURCE_FILE = "<generated>";

    public static final int PRIVATE_FINAL_STATIC = ACC_PRIVATE | ACC_FINAL | ACC_STATIC;

    public static final int SWITCH_STYLE_TRIE = 0;
    public static final int SWITCH_STYLE_HASH = 1;
}
