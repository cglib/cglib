package net.sf.cglib.util;

public class KeyExample extends KeyFactory {
    public KeyExample(int i, String s, boolean b, float f, long el, byte q, int[] ints) {
        int multiplier = 21;
        int result = 17;
        result *= multiplier;
        result += i;
        result *= multiplier;
        result += s.hashCode();
        result *= multiplier;
        result += (b ? 0 : 1);
        result *= multiplier;
        result += Float.floatToIntBits(f);
        result *= multiplier;
        result += (int)(el ^ (el >>> 32));
        result *= multiplier;
        result += (int)q;
        result *= multiplier;
        for (int j = 0; j < ints.length; j++) {
            result += ints[j];
            result *= multiplier;
        }
        hash = result;
    }
}
