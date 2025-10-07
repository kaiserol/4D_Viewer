package de.uzk.utils;

public class NumberUtils {
    private NumberUtils() {
    }

    public static boolean valueFitsInRange(double value, double minValue, double maxValue, double stepSize) {
        // Check if the value is within the specified range
        if (value < minValue || value > maxValue) {
            return false;
        }

        // Calculate the remainder when dividing (value - minValue) by stepSize
        double epsilonRemainder = 1e-10;
        double remainder = (value - minValue + epsilonRemainder) % stepSize;

        double epsilon = 1e-9; // Adjust the epsilon as needed for your precision
        // Check if the remainder is close to zero within a small epsilon
        return Math.abs(remainder) < epsilon;
    }

    public static int turn90Left(int oldAngle) {
        int angle = oldAngle % 360;
        if (angle == 0) return 270;

        int remainder = angle % 90;
        if (remainder == 0) {
            return (360 + angle - 90) % 360;
        }
        return (360 + angle - remainder) % 360;
    }

    public static int turn90Right(int oldAngle) {
        int angle = oldAngle % 360;
        if (angle == 270) return 0;

        int remainder = angle % 90;
        if (remainder == 0) {
            return (360 + angle + 90) % 360;
        }
        return (360 + angle - remainder + 90) % 360;
    }

    public static boolean isInteger(Number number) {
        return number != null && number.getClass() == Integer.class;
    }

    public static boolean isDouble(Number number) {
        return number != null && number.getClass() == Double.class;
    }


}
