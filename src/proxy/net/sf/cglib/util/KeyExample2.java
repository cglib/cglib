package net.sf.cglib.util;

public class KeyExample2 extends KeyFactory {
    public KeyExample2(Object o1, Object o2) {
        int multiplier = 21;
        int result = 17;
        result *= multiplier;
        result += o1.hashCode();
        result *= multiplier;
        result += o2.hashCode();
        result *= multiplier;
        hash = result;
    }
}
