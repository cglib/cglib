package net.sf.cglib.proxy;

class D4 implements DI1, DI3 {

    public String herby() {
        return getName();
    }

    public String derby() {
        return getName();
    }

    public String extra() {
        return getName();
    }

    private String getName() {
        return "D4";
    }
}
