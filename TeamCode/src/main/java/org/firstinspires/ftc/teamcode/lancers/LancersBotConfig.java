package org.firstinspires.ftc.teamcode.lancers;

import org.jetbrains.annotations.NotNull;

/**
 * Constants reflecting the LancersBotConfig on the Driver Hub
 */
public final class LancersBotConfig {
    private LancersBotConfig() {
    }

    private static final @NotNull String WIFI_SSID = "3415-RC";

    // == CONTROL HUB / LEFT ==

    // Control Hub -- Motors & Encoders
    // https://discord.com/channels/225450307654647808/225451520911605765/999780526594457670
    public static final @NotNull String LEFT_FRONT_MOTOR = "frontLeft"; // 0
    public static final @NotNull String LEFT_REAR_MOTOR = "rearLeft"; // 1
    public static final @NotNull String LEFT_SLIDE_MOTOR = "SlideMotorL"; // 2
    public static final @NotNull String INTAKE_MOTOR = "intake"; // 3

    // Control Hub -- Servos
    // 0
    // 1
    // 2
    // 3
    // 4
    // 5

    // == EXPANSION HUB / RIGHT ==

    // Expansion Hub -- Motors & Encoders
    public static final @NotNull String RIGHT_FRONT_MOTOR = "frontRight"; // 0
    public static final @NotNull String RIGHT_REAR_MOTOR = "rearRight"; // 1
    public static final @NotNull String RIGHT_SLIDE_MOTOR = "SlideMotorR"; // 2
    // 3

    // Expansion Hub -- Servos
    // 0
    // 1
    // 2
    // 3
    // 4
    // 5

    // SERVOS -- we move these around a lot
    public static final @NotNull String RIGHT_OUTTAKE_SERVO = "outtakeRight"; // normal
    public static final @NotNull String LEFT_OUTTAKE_SERVO = "outtakeLeft"; // normal

    public static final @NotNull String BACK_OUTTAKE_SERVO = "outtakeBack"; // continuous
    public static final @NotNull String FRONT_OUTTAKE_SERVO = "outtakeFront"; // continuous

    public static final @NotNull String DRONE_LAUNCHER_SERVO = "drone"; // normal

    // I/O
    public static final @NotNull String WEBCAM = "webcam";
    public static final @NotNull String IMU_NAME = "imu";
}
