/*
 * Copyright 2003 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.cglib.core;

import org.objectweb.asm.*;
import java.util.*;

public class RemappingCodeVisitor extends CodeAdapter {
    private State state;
    private IntRef check = new IntRef();

    private static class State {
        Map locals = new HashMap();
        int firstLocal;
        int nextLocal;

        State(int access, Type[] args) {
            nextLocal = ((Constants.ACC_STATIC & access) != 0) ? 0 : 1;
            for (int i = 0; i < args.length; i++) {
                nextLocal += args[i].getSize();
            }
            firstLocal = nextLocal;
        }
    }

    private static class IntRef {
        int key;
        public boolean equals(Object o) {
            return key == ((IntRef)o).key;
        }
        public int hashCode() {
            return key;
        }
    }
    
    public RemappingCodeVisitor(CodeVisitor v, int access, Type[] args) {
        super(v);
        state = new State(access, args);
    }

    public RemappingCodeVisitor(RemappingCodeVisitor wrap) {
        super(wrap.cv);
        this.state = wrap.state;
    }

    protected int nextLocal(int size) {
        int var = state.nextLocal;
        state.nextLocal += size;
        return var;
    }
    
    private int remap(int var, int size) {
        if (var < state.firstLocal) {
            return var;
        }
        check.key = (size == 2) ? ~var : var;
        Integer value = (Integer)state.locals.get(check);
        if (value == null) {
            IntRef ref = new IntRef();
            ref.key = check.key;
            state.locals.put(ref, value = new Integer(nextLocal(size)));
        }
        return value.intValue();
    }
        
    public void visitIincInsn(int var, int increment) {
        cv.visitIincInsn(remap(var, 1), increment);
    }

    public void visitLocalVariable(String name, String desc, Label start, Label end, int index) {
        cv.visitLocalVariable(name, desc, start, end, remap(index, 0));
    }

    public void visitVarInsn(int opcode, int var) {
        int size;
        switch (opcode) {
        case Constants.LLOAD:
        case Constants.LSTORE:
        case Constants.DLOAD:
        case Constants.DSTORE:
            size = 2;
            break;
        default:
            size = 1;
        }
        cv.visitVarInsn(opcode, remap(var, size));
    }

    public void visitMaxs(int maxStack, int maxLocals) {
        cv.visitMaxs(0, 0);
    }
}    

