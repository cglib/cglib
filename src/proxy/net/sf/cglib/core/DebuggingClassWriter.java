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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.*;

public class DebuggingClassWriter extends ClassWriter {
    private static String debugLocation;
    private String className;
    private String superName;

    static {
        debugLocation = System.getProperty("cglib.debugLocation");
        if (debugLocation != null) {
            System.err.println("CGLIB debugging enabled, writing to '" + debugLocation + "'");
        }
    }

    public DebuggingClassWriter(boolean computeMaxs) {
        super(computeMaxs);
    }

    public void visit(int access, String name, String superName, String[] interfaces, String sourceFile) {
        className = name.replace('/', '.');
        this.superName = superName.replace('/', '.');
        super.visit(access, name, superName, interfaces, sourceFile);
    }

    public String getClassName() {
        return className;
    }

    public String getSuperName() {
        return superName;
    }

    public byte[] toByteArray() {
        byte[] b = super.toByteArray();
        if (debugLocation != null) {
            
            try {
                new File(debugLocation).mkdirs();
                File file = new File(new File(debugLocation), className + ".class");
                OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                try{
                  out.write(b);
                }finally{
                  out.close();
                }
               
                 file = new File(new File(debugLocation), className + ".asm");
                 out = new BufferedOutputStream(new FileOutputStream(file));
                try{
                    
                  ClassReader cr = new ClassReader(b);
                  PrintWriter pw = new PrintWriter(new OutputStreamWriter(out));
                  TraceClassVisitor tcv = new TraceClassVisitor(null, pw );
                  cr.accept(tcv, false);
                  pw.flush();
                  
                 }finally{
                   out.close();
                 }
                
             
                
            } catch (IOException e) {
                throw new CodeGenerationException(e);
            }
        }
        return b;
    }
}
