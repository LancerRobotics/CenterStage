package org.firstinspires.ftc.teamcode.lancers;

import org.jetbrains.annotations.NotNull;

/**
 * Constants reflecting the LancersBotConfig on the Driver Hub
 */
public final class LancersBotConfig {
    private LancersBotConfig() {
    }

    // == CONTROL HUB / LEFT ==

    // Control Hub -- Motors & Encoders
    public static final @NotNull String LEFT_FRONT_MOTOR = "frontLeft"; // 0
    public static final @NotNull String LEFT_REAR_MOTOR = "rearLeft"; // 1
    public static final @NotNull String LEFT_SLIDE_MOTOR = "SlideMotorL"; // 2
    public static final @NotNull String INTAKE_MOTOR = "intake"; // 3

    // Control Hub -- Servos
    // 0
    // 1
    // 2
    // 3
    public static final @NotNull String LEFT_OUTTAKE_SERVO = "outtakeLeft"; // normal // 4
    public static final @NotNull String FRONT_OUTTAKE_SERVO = "outtakeFront"; // continuous // 5

    // == EXPANSION HUB / RIGHT ==

    // Expansion Hub -- Motors & Encoders
    public static final @NotNull String RIGHT_FRONT_MOTOR = "frontRight"; // 0
    public static final @NotNull String RIGHT_REAR_MOTOR = "rearRight"; // 1
    public static final @NotNull String RIGHT_SLIDE_MOTOR = "SlideMotorR"; // 2
    // 3

    // Expansion Hub -- Servos
    public static final @NotNull String BACK_OUTTAKE_SERVO = "outtakeBack"; // continuous // 0
    public static final @NotNull String RIGHT_OUTTAKE_SERVO = "outtakeRight"; // normal // 1
    // 2
    // 3
    // 4
    // 5

    // I/O
    public static final @NotNull String WEBCAM = "webcam";
    public static final @NotNull String IMU_NAME = "imu";
}
