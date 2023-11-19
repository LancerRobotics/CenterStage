package org.firstinspires.ftc.teamcode.lancers.opmode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.lancers.LancersBotConfig;
import org.jetbrains.annotations.NotNull;

@TeleOp
public class LancersTeleOp extends LinearOpMode {
    final static float TRIGGER_THRESHOLD = 0.15f;
    final static float STICK_THRESHOLD = 0.1f;
    // Loop Tasks

    /**
     * Implementation of <a href="https://gm0.org/fr/latest/docs/software/tutorials/mecanum-drive.html">...</a>
     *
     * @param gamepad Gamepad that moves the robot
     * @author Alex Zhang
     */
    private void mecanumMovement(final @NotNull Gamepad gamepad) {
        final @NotNull DcMotor leftFront = hardwareMap.dcMotor.get(LancersBotConfig.LEFT_FRONT_MOTOR);
        final @NotNull DcMotor leftRear = hardwareMap.dcMotor.get(LancersBotConfig.LEFT_REAR_MOTOR);
        final @NotNull DcMotor rightFront = hardwareMap.dcMotor.get(LancersBotConfig.RIGHT_FRONT_MOTOR);
        final @NotNull DcMotor rightRear = hardwareMap.dcMotor.get(LancersBotConfig.RIGHT_REAR_MOTOR);

        // Gamepad positions
        final double ly = -gamepad.left_stick_y; // Remember, Y stick value is reversed
        final double lx = gamepad.left_stick_x * 1.1; // Counteract imperfect strafing

        final double rx = gamepad.right_stick_x;

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

        if (gamepad.right_trigger > TRIGGER_THRESHOLD) {
            intakeMotor.setPower(gamepad.right_trigger);
        } else if (gamepad.left_trigger > TRIGGER_THRESHOLD) {
            // for placing pixels forward (mainly redundant code from auton)
            // divided by 2 to give finer control than SUCKING IN pixels
            intakeMotor.setPower(-gamepad.left_trigger / 2.0d);
        } else {
            intakeMotor.setPower(0.0d);
        }
    }

    public void sliderMovement(final @NotNull Gamepad gamepad) {
        // Left stick y
        final @NotNull DcMotor leftSlider = hardwareMap.dcMotor.get(LancersBotConfig.LEFT_SLIDE_MOTOR);
        final @NotNull DcMotor rightSlider = hardwareMap.dcMotor.get(LancersBotConfig.RIGHT_SLIDE_MOTOR);

        // Left slider moves clockwise to expand
        // Right slider moves counterclockwise to expand

        // Left slider moves counterclockwise to contract
        // Right slider moves clockwise to contract

        float sliderPower = -gamepad.left_stick_y * 0.5f; // Indirectly limit maximum speed

        // Setup deadzone so sliders brake
        if (Math.abs(sliderPower) < STICK_THRESHOLD) {
            sliderPower = 0.0f;
        }

        final float leftSliderPower = sliderPower;
        final float rightSliderPower = -sliderPower;

        leftSlider.setPower(leftSliderPower);
        rightSlider.setPower(rightSliderPower);
    }

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

    public void initOuttakeBasket() {
        final @NotNull Servo leftOuttake = hardwareMap.servo.get(LancersBotConfig.LEFT_OUTTAKE_SERVO);
        final @NotNull Servo rightOuttake = hardwareMap.servo.get(LancersBotConfig.RIGHT_OUTTAKE_SERVO);
        leftOuttake.setPosition(LEFT_SERVO_HORIZONTAL_POSITION);
        rightOuttake.setPosition(RIGHT_SERVO_HORIZONTAL_POSITION);
    }

    public void outtakeAngularMovement(final @NotNull Gamepad gamepad) {
        // Right stick x
        // Setup button to set servos to special angle
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
            double targetLeftPos = currentLeftPos + (gamepad.right_stick_x * 0.005d);

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

    // Master code

    public void configure() {
        final @NotNull DcMotor leftFront = hardwareMap.dcMotor.get(LancersBotConfig.LEFT_FRONT_MOTOR);
        final @NotNull DcMotor leftRear = hardwareMap.dcMotor.get(LancersBotConfig.LEFT_REAR_MOTOR);

        // Reverse the right side motors. This may be wrong for your setup.
        // If your robot moves backwards when commanded to go forwards,
        // reverse the left side instead.
        // See the note about this earlier on this page.
        leftFront.setDirection(DcMotorSimple.Direction.REVERSE);
        leftRear.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    @Override
    public void runOpMode() throws InterruptedException {
        // https://gm0.org/ja/latest/docs/software/tutorials/gamepad.

        configure();
        initOuttakeBasket();

        waitForStart();

        if (isStopRequested()) return;

        while (opModeIsActive()) {
            // Gamepad 1 / Movement
            mecanumMovement(gamepad1);
            // rigging (when ready)

            // Gamepad 2
            intakeMovement(gamepad2);
            sliderMovement(gamepad2);
            outtakeLinearMovement(gamepad2);
            outtakeAngularMovement(gamepad2);

            telemetry.update();
        }
    }
}
