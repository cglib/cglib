package net.sf.cglib.transform.impl;

import net.sf.cglib.transform.*;
import java.util.*;
import net.sf.cglib.core.*;
import org.objectweb.asm.Type;

public class AddPropertyTransformer extends ClassEmitterTransformer {
    private final String[] names;
    private final Type[] types;

    public AddPropertyTransformer(Map props) {
        int size = props.size();
        names = (String[])props.keySet().toArray(new String[size]);
        types = new Type[size];
        for (int i = 0; i < size; i++) {
            types[i] = (Type)props.get(names[i]);
        }
    }

    public AddPropertyTransformer(String[] names, Type[] types) {
        this.names = names;
        this.types = types;
    }

    public void end_class() {
        if (!TypeUtils.isAbstract(getAccess())) {
            EmitUtils.add_properties(this, names, types);
        }
        super.end_class();
    }
}
