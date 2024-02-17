package org.firstinspires.ftc.teamcode.lancers.teleop.util;

public final class ControlUtil {
    public final static float TRIGGER_THRESHOLD = 0.15f;
    public final static double STICK_THRESHOLD = 0.1d;

    private ControlUtil() {
    }


    /**
     * Translate the stick magnitude between the trigger threshold and the max velocity.
     *
     * @param stickMagnitude the magnitude
     * @return adjusted magnitude
     */
    public static double adjustStickMovement(final double stickMagnitude) {
        if (Math.abs(stickMagnitude) < STICK_THRESHOLD) {
            return 0d;
        }

        // right now, the magnitude is between STICK_THRESHOLD and 1

        // if stickMagnitude is on the positive side, subtract the threshold
        // if stickMagnitude is on the negative side, add the threshold

        return (stickMagnitude + (stickMagnitude > 0 ? -STICK_THRESHOLD : STICK_THRESHOLD)) * (1d / (1d - STICK_THRESHOLD));
    }

    /**
     * Translate the trigger magnitude between the trigger threshold and the max velocity.
     *
     * @param triggerMagnitude the magnitude
     * @return adjusted magnitude
     */
    public static float adjustTriggerMovement(final float triggerMagnitude) {
        if (triggerMagnitude < TRIGGER_THRESHOLD) {
            return 0f;
        }

        // right now, the magnitude is between TRIGGER_THRESHOLD and 1

        return (triggerMagnitude - TRIGGER_THRESHOLD) * (1f / (1f - TRIGGER_THRESHOLD));
    }
}
