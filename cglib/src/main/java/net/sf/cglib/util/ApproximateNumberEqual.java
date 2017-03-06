package net.sf.cglib.util;

/**
 * Created by Ayman Elkfrawy on 6/15/2016.
 */
public class ApproximateNumberEqual {

    public static final double EPSILON = 0.00000001;

    private ApproximateNumberEqual() {
    }

    public static boolean floatEquals(float n1, float n2) {
        return Math.abs(n1 - n2) <= EPSILON;
    }

    public static boolean doubleEquals(double n1, double n2) {
        return Math.abs(n1 - n2) <= EPSILON;
    }
}
