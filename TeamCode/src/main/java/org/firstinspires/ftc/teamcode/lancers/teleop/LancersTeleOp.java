package org.firstinspires.ftc.teamcode.lancers.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.teamcode.lancers.LancersBotConfig;
import org.firstinspires.ftc.teamcode.lancers.util.Constants;
import org.firstinspires.ftc.teamcode.lancers.util.ControlUtil;
import org.firstinspires.ftc.teamcode.lancers.util.LancersMecanumDrive;
import org.firstinspires.ftc.teamcode.lancers.util.OpModeUtil;
import org.jetbrains.annotations.NotNull;

// https://learnroadrunner.com/advanced.html#using-road-runner-in-teleop if roadrunner needed
@TeleOp(name = Constants.TELEOP_NAME)
public final class LancersTeleOp extends LinearOpMode {
    // Loop Tasks

    /**
     * Implementation of <a href="https://gm0.org/fr/latest/docs/software/tutorials/mecanum-drive.html">...</a>
     *
     * @param gamepad Gamepad that moves the robot
     * @author Alex Zhang
     */
    private void mecanumMovement(final @NotNull Gamepad gamepad) {
        // Holding "A" will make the bot move faster

        final double speedMultiplier = gamepad.a ? 1.0d : 0.8d;

        final @NotNull DcMotor leftFront = hardwareMap.dcMotor.get(LancersBotConfig.LEFT_FRONT_MOTOR);
        final @NotNull DcMotor leftRear = hardwareMap.dcMotor.get(LancersBotConfig.LEFT_REAR_MOTOR);
        final @NotNull DcMotor rightFront = hardwareMap.dcMotor.get(LancersBotConfig.RIGHT_FRONT_MOTOR);
        final @NotNull DcMotor rightRear = hardwareMap.dcMotor.get(LancersBotConfig.RIGHT_REAR_MOTOR);

        // Gamepad positions
        final double ly = -ControlUtil.adjustStickMovement(gamepad.left_stick_y) * speedMultiplier; // Remember, Y stick value is reversed
        final double lx = ControlUtil.adjustStickMovement(gamepad.left_stick_x) * speedMultiplier; // Counteract imperfect strafing
        final double rx = ControlUtil.adjustStickMovement(gamepad.right_stick_x) * speedMultiplier;

        // Denominator is the largest motor power (absolute value) or 1
        // This ensures all the powers maintain the same ratio,
        // but only if at least one is out of the range [-1, 1]
        final double denominator = Math.max(Math.abs(ly) + Math.abs(lx) + Math.abs(rx), 1);

        final double frontLeftPower = (ly + lx + rx) / denominator;
        final double backLeftPower = (ly - lx + rx) / denominator;
        final double frontRightPower = (ly - lx - rx) / denominator;
        final double backRightPower = (ly + lx - rx) / denominator;

        leftFront.setPower(frontLeftPower);
        leftRear.setPower(backLeftPower);
        rightFront.setPower(frontRightPower);
        rightRear.setPower(backRightPower);
    }

    public void intakeMovement(final @NotNull Gamepad gamepad) {
        final @NotNull DcMotor intakeMotor = hardwareMap.dcMotor.get(LancersBotConfig.INTAKE_MOTOR);

        if (gamepad.right_trigger > ControlUtil.TRIGGER_THRESHOLD) {
            intakeMotor.setPower(ControlUtil.adjustTriggerMovement(gamepad.right_trigger));
        } else if (gamepad.left_trigger > ControlUtil.TRIGGER_THRESHOLD) {
            // for placing pixels forward (mainly redundant code from auton)
            // divided by 2 to give finer control than SUCKING IN pixels
            intakeMotor.setPower(-ControlUtil.adjustTriggerMovement(gamepad.left_trigger) / 2.0d);
        } else {
            intakeMotor.setPower(0.0d);
        }
    }

    public static final double MAX_SINGLE_SLIDER_SPEED = 0.3d;
    public static final double MAX_SLIDER_SPEED = 0.6d;

    // TODO: Eventually break this out to it's own class along with intake & outtake for sharing code between auton and teleop
    public void sliderMovement(final @NotNull Gamepad gamepad) {
        // right trigger: expand
        // left trigger: contract

        // if left bumper is pressed, move only left slider and do so at 0.3 speed
        // if right bumper is pressed, move only right slider and do so at 0.3 speed

        final @NotNull DcMotor leftSlider = hardwareMap.dcMotor.get(LancersBotConfig.LEFT_SLIDE_MOTOR);
        final @NotNull DcMotor rightSlider = hardwareMap.dcMotor.get(LancersBotConfig.RIGHT_SLIDE_MOTOR);

        double sliderPower = 0.0d;
        if (gamepad.right_trigger > ControlUtil.TRIGGER_THRESHOLD) {
            sliderPower = ControlUtil.adjustTriggerMovement(gamepad.right_trigger);
        } else if (gamepad.left_trigger > ControlUtil.TRIGGER_THRESHOLD) {
            sliderPower = -ControlUtil.adjustTriggerMovement(gamepad.left_trigger);
        } else {
            sliderPower = 0.0d; // brake sliders
        }

        double leftSliderPower = sliderPower * MAX_SLIDER_SPEED;
        double rightSliderPower = -leftSliderPower;

        if (gamepad.right_bumper) {
            leftSliderPower = 0.0d;
            rightSliderPower = sliderPower * MAX_SINGLE_SLIDER_SPEED;
        } else if (gamepad.left_bumper) {
            leftSliderPower = sliderPower * MAX_SINGLE_SLIDER_SPEED;
            rightSliderPower = 0.0d;
        }

        leftSlider.setPower(leftSliderPower);
        rightSlider.setPower(rightSliderPower);
    }

    // TODO: move
    public void outtakeLinearMovement(final @NotNull Gamepad gamepad) {
        // Right stick y
        final @NotNull CRServo backOuttake = hardwareMap.crservo.get(LancersBotConfig.BACK_OUTTAKE_SERVO);
        final @NotNull CRServo frontOuttake = hardwareMap.crservo.get(LancersBotConfig.FRONT_OUTTAKE_SERVO);

        final float backOuttakePower = gamepad.right_stick_y;
        final float frontOuttakePower = -backOuttakePower;

        backOuttake.setPower(backOuttakePower);
        frontOuttake.setPower(frontOuttakePower);
    }

    private static final double LEFT_SERVO_HORIZONTAL_POSITION = 0.54d;
    private static final double LEFT_SERVO_VERTICAL_POSITION = 0.83d;
    private static final double RIGHT_SERVO_HORIZONTAL_POSITION = 1.0d - LEFT_SERVO_HORIZONTAL_POSITION;
    private static final double RIGHT_SERVO_VERTICAL_POSITION = 1.0d - LEFT_SERVO_VERTICAL_POSITION;

    // TODO: move
    public void initOuttakeBasket() {
        final @NotNull Servo leftOuttake = hardwareMap.servo.get(LancersBotConfig.LEFT_OUTTAKE_SERVO);
        final @NotNull Servo rightOuttake = hardwareMap.servo.get(LancersBotConfig.RIGHT_OUTTAKE_SERVO);
        leftOuttake.setPosition(LEFT_SERVO_HORIZONTAL_POSITION);
        rightOuttake.setPosition(RIGHT_SERVO_HORIZONTAL_POSITION);
    }

    // TODO: move
    public void outtakeAngularMovement(final @NotNull Gamepad gamepad) {
        // Left stick y
        // Setup button to set servos to a special angle
        final @NotNull Servo leftOuttake = hardwareMap.servo.get(LancersBotConfig.LEFT_OUTTAKE_SERVO);
        final @NotNull Servo rightOuttake = hardwareMap.servo.get(LancersBotConfig.RIGHT_OUTTAKE_SERVO);

        if (gamepad.b) {
            // Set to horizontal position
            leftOuttake.setPosition(LEFT_SERVO_HORIZONTAL_POSITION);
            rightOuttake.setPosition(RIGHT_SERVO_HORIZONTAL_POSITION);
        } else if (gamepad.a) {
            // Set to vertical position
            leftOuttake.setPosition(LEFT_SERVO_VERTICAL_POSITION);
            rightOuttake.setPosition(RIGHT_SERVO_VERTICAL_POSITION);
        } else {
            final double currentLeftPos = leftOuttake.getPosition();
            double targetLeftPos = currentLeftPos + (gamepad.left_stick_y * 0.005d);

            // Make sure targetLeftPos is in range for servo
            if (targetLeftPos > 0.9d) {
                targetLeftPos = 0.9d;
            } else if (targetLeftPos < 0.1d) {
                targetLeftPos = 0.1d;
            }

            final double targetRightPos = 1 - targetLeftPos;

            leftOuttake.setPosition(targetLeftPos);
            rightOuttake.setPosition(targetRightPos);
            telemetry.addData("Left outtake servo position: ", targetLeftPos);
        }
    }

    @Override
    public void runOpMode() throws InterruptedException {
        // https://gm0.org/ja/latest/docs/software/tutorials/gamepad
        // https://github.com/NoahBres/road-runner-quickstart/blob/advanced-examples/TeamCode/src/main/java/org/firstinspires/ftc/teamcode/drive/advanced/TeleOpJustLocalizer.java#L69

        // NOTE: If an auton didn't run or if the global state in the drive persisted, the pose data may be incorrect.
        //       This can be fixed by running an auton that requires the bot be in a specific starting positon
        //       (e.g. one of the parking autons or the full auton) (this only matters for launching the drone)
        try (final LancersMecanumDrive drive = new LancersMecanumDrive(hardwareMap)) {
            LancersBotConfig.configureMotors(hardwareMap);
            OpModeUtil.initMultipleTelemetry(this);
            initOuttakeBasket();

            waitForStart();

            if (isStopRequested()) return;

            while (opModeIsActive()) {
                drive.update();
                drive.addTelemetry(telemetry);

                // Gamepad 1 / Movement
                mecanumMovement(gamepad1);
                intakeMovement(gamepad1);

                // Gamepad 2
                sliderMovement(gamepad2);
                outtakeLinearMovement(gamepad2);
                outtakeAngularMovement(gamepad2);

                telemetry.update();
            }
        }
    }
}
