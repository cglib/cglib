package net.sf.cglib.transform;

public class ExampleTask extends AbstractTransformTask {
    protected ClassTransformer getClassTransformer() {
        return new ExampleTransformer(new String[]{ "chris" }, new Class[]{ Long.class });
    }

    public boolean accept(String name) {
        return name.indexOf("Example") >= 0;
    }
}
