package net.sf.cglib.transform;

import org.objectweb.asm.Type;

public class ExampleTask extends AbstractTransformTask {
    protected ClassTransformer getClassTransformer() {
        return new AddPropertyTransformer(new String[]{ "chris" }, new Type[]{ Type.LONG_TYPE });
    }

    public boolean accept(String name) {
        return name.indexOf("Example") >= 0;
    }
}
