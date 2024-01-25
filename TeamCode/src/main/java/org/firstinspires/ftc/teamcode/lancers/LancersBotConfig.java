package org.firstinspires.ftc.teamcode.lancers;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.jetbrains.annotations.NotNull;

/**
 * Constants reflecting the LancersBotConfig on the Driver Hub
 */
public final class LancersBotConfig {
    private LancersBotConfig() {
    }

    private static final @NotNull String WIFI_SSID = "3415-RC";
    private static final @NotNull String WIFI_PASSWORD = "MaChiChi";

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

    /**
     * Configure the motors to go in the correct direction
     */
    public static void configureMotors(final @NotNull HardwareMap hardwareMap) {
        final @NotNull DcMotor leftFront = hardwareMap.dcMotor.get(LancersBotConfig.LEFT_FRONT_MOTOR);
        final @NotNull DcMotor leftRear = hardwareMap.dcMotor.get(LancersBotConfig.LEFT_REAR_MOTOR);
        final @NotNull DcMotor rightFront = hardwareMap.dcMotor.get(LancersBotConfig.RIGHT_FRONT_MOTOR);
        final @NotNull DcMotor rightRear = hardwareMap.dcMotor.get(LancersBotConfig.RIGHT_REAR_MOTOR);

        // Reverse the right side motors. This may be wrong for your setup.
        // If your robot moves backwards when commanded to go forwards,
        // reverse the left side instead.
        // See the note about this earlier on this page.
        leftFront.setDirection(DcMotorSimple.Direction.REVERSE);
        leftRear.setDirection(DcMotorSimple.Direction.REVERSE);
        rightFront.setDirection(DcMotorSimple.Direction.FORWARD);
        rightRear.setDirection(DcMotorSimple.Direction.FORWARD);

        // Turn on bulk reads to help optimize loop times
        // https://github.com/NoahBres/road-runner-quickstart/blob/b4b850fa1b7492ccc668c4955c682fa19cf101c9/TeamCode/src/main/java/org/firstinspires/ftc/teamcode/drive/advanced/TeleOpJustLocalizer.java#L81C13-L84C14
        for (LynxModule module : hardwareMap.getAll(LynxModule.class)) {
            module.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }
    }
}
