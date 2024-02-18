package org.firstinspires.ftc.teamcode.lancers;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.drive.MecanumDrive;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.*;
import lombok.Getter;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Config
public class LancersRobot {
    private final @NotNull HardwareMap hardwareMap;
    @Getter
    private final @NotNull LancersMecanumDrive drive;

    public LancersRobot(final @NotNull HardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;
        this.drive = new LancersMecanumDrive(hardwareMap);
    }

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

    public void configureMotors() {
        configureMotors(hardwareMap);
    }

    // outtake basket

    public static double OUTTAKE_SERVO_HORIZONTAL_POSITION = 0.54d;

    public static double OUTTAKE_SERVO_VERTICAL_POSITION = 0.83d;

    public void initOuttakeBasket() {
        bringOuttakeHorizontal();
    }

    public void bringOuttakeHorizontal() {
        setOuttakePosition(OUTTAKE_SERVO_HORIZONTAL_POSITION);
    }

    public void bringOuttakeVertical() {
        setOuttakePosition(OUTTAKE_SERVO_VERTICAL_POSITION);
    }

    public double getOutakePosition() {
        final @NotNull Servo leftOuttake = hardwareMap.servo.get(LancersBotConfig.LEFT_OUTTAKE_SERVO);
        return leftOuttake.getPosition();
    }

    public void setOuttakePosition(final double outtakePosition) {
        final @NotNull Servo leftOuttake = hardwareMap.servo.get(LancersBotConfig.LEFT_OUTTAKE_SERVO);
        final @NotNull Servo rightOuttake = hardwareMap.servo.get(LancersBotConfig.RIGHT_OUTTAKE_SERVO);

        double targetLeftPos = outtakePosition;

        if (targetLeftPos > 0.9d) {
            targetLeftPos = 0.9d;
        } else if (targetLeftPos < 0.1d) {
            targetLeftPos = 0.1d;
        }

        final double targetRightPos = 1 - targetLeftPos;

        leftOuttake.setPosition(targetLeftPos);
        rightOuttake.setPosition(targetRightPos);
    }

    // drone launcher

    public static double DRONE_LAUNCHER_SERVO_START_POSITION = 0.0d;
    public static double DRONE_LAUNCHER_SERVO_END_POSITION = 0.5d;

    public void launchDrone() {
        setDroneLauncherPosition(DRONE_LAUNCHER_SERVO_END_POSITION);
    }

    public void resetDroneLauncher() {
        setDroneLauncherPosition(DRONE_LAUNCHER_SERVO_START_POSITION);
    }

    public void setDroneLauncherPosition(final double position) {
        final @NotNull Servo droneLauncher = hardwareMap.servo.get(LancersBotConfig.DRONE_LAUNCHER_SERVO);
        droneLauncher.setPosition(position);
    }

    // outtake wheels

    /**
     * @param power negative is in positive is out
     */
    public void setOuttakeWheelSpeed(final float power) {
        final @NotNull CRServo backOuttake = hardwareMap.crservo.get(LancersBotConfig.BACK_OUTTAKE_SERVO);
        final @NotNull CRServo frontOuttake = hardwareMap.crservo.get(LancersBotConfig.FRONT_OUTTAKE_SERVO);
        final float backOuttakePower = power;
        final float frontOuttakePower = -backOuttakePower;
        backOuttake.setPower(backOuttakePower);
        frontOuttake.setPower(frontOuttakePower);
    }

    // slider movement

    private static final double MAX_SINGLE_SLIDER_SPEED = 0.3d;
    private static final double MAX_SLIDER_SPEED = 0.6d;

    public void doSliderMovement(final double sliderPower, final boolean useLeftSlider, final boolean useRightSlider) {
        final @NotNull DcMotor leftSlider = hardwareMap.dcMotor.get(LancersBotConfig.LEFT_SLIDE_MOTOR);
        final @NotNull DcMotor rightSlider = hardwareMap.dcMotor.get(LancersBotConfig.RIGHT_SLIDE_MOTOR);

        final double multiplier = (useLeftSlider && useRightSlider) ? MAX_SLIDER_SPEED : MAX_SINGLE_SLIDER_SPEED;

        final double leftSliderPower = (useLeftSlider ? sliderPower : 0.0d) * multiplier;
        final double rightSliderPower = (useRightSlider ? -sliderPower : 0.0d) * multiplier;

        leftSlider.setPower(leftSliderPower);
        rightSlider.setPower(rightSliderPower);
    }

    public void doSliderMovement(final double sliderPower) {
        doSliderMovement(sliderPower, true, true);
    }

    // intake

    public void doIntakeMovement(final double power) {
        final @NotNull DcMotor intake = hardwareMap.get(DcMotor.class, LancersBotConfig.INTAKE_MOTOR);
        intake.setPower(power);
    }

    public void cleanup() {
        drive.close();
    }

    /**
     * A wrapper around {@link SampleMecanumDrive} that allows for state data to be persisted between opmodes.
     */
    public static class LancersMecanumDrive extends SampleMecanumDrive implements AutoCloseable {
        private LancersMecanumDrive(final @NotNull HardwareMap hardwareMap) {
            super(hardwareMap);
            PoseStorage.restoreStoredPose(this);
        }

        @Override
        public void close() {
            PoseStorage.updateStoredPose(this);
        }

        private static class PoseStorage {
            // If we ever make serious use of this, we should probably migrate this to using the database
            // https://learnroadrunner.com/advanced.html#transferring-pose-between-opmodes:~:text=Another%20downside%20is,%23
            private PoseStorage() {
            }

            private static @Nullable Pose2d lastPose = null;

            private static void updateStoredPose(@NotNull MecanumDrive drive) {
                lastPose = drive.getPoseEstimate();
            }

            private static void restoreStoredPose(@NotNull MecanumDrive drive) {
                if (lastPose != null) {
                    drive.setPoseEstimate(lastPose);
                }
            }
        }
    }
}
