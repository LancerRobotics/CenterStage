package org.firstinspires.ftc.teamcode.lancers.util;

/**
 * Helper class for translating between mm and in for code between CAD and RoadRunner.
 */
public final class UnitUtil {
    private UnitUtil() {
    }

    public static double mmToInches(final double mm) {
        return mm / 25.4d;
    }
}
