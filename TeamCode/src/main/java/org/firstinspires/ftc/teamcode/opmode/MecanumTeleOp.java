package org.firstinspires.ftc.teamcode.opmode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.LancersBotConfig;
import org.jetbrains.annotations.NotNull;

@TeleOp
public class MecanumTeleOp extends LinearOpMode {
    // Loop Tasks

    /**
     * Implementation of <a href="https://gm0.org/fr/latest/docs/software/tutorials/mecanum-drive.html">...</a>
     * @author Alex Zhang
     * @param gamepad Gamepad that moves the robot
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

        // PW - I can't confirm that this is necessary.
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

        intakeMotor.setPower(gamepad.right_trigger);
    }

    public void sliderMovement(final @NotNull Gamepad gamepad) {
        // Left stick y
        final @NotNull DcMotor leftSlider = hardwareMap.dcMotor.get(LancersBotConfig.LEFT_SLIDE_MOTOR);
        final @NotNull DcMotor rightSlider = hardwareMap.dcMotor.get(LancersBotConfig.RIGHT_SLIDE_MOTOR);

        // Left slider moves clockwise to expand
        // Right slider moves counterclockwise to expand

        // Left slider moves counterclockwise to contract
        // Right slider moves clockwise to contract

        final float leftSliderPower = gamepad.left_stick_y;
        final float rightSliderPower = -leftSliderPower;

        leftSlider.setPower(leftSliderPower);
        rightSlider.setPower(rightSliderPower);
    }

    public void outtakeMovement(final @NotNull Gamepad gamepad) {
        // Right stick x
        final @NotNull Servo leftOuttake = hardwareMap.servo.get(LancersBotConfig.LEFT_OUTTAKE_SERVO);
        final @NotNull Servo rightOuttake = hardwareMap.servo.get(LancersBotConfig.RIGHT_OUTTAKE_SERVO);

        final @NotNull CRServo backOuttake = hardwareMap.crservo.get(LancersBotConfig.BACK_OUTTAKE_SERVO);
        final @NotNull CRServo frontOuttake = hardwareMap.crservo.get(LancersBotConfig.FRONT_OUTTAKE_SERVO);

        // TODO: OutTake TeleOp
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

        waitForStart();

        if (isStopRequested()) return;

        while (opModeIsActive()) {
            // Gamepad 1 / Movement
            mecanumMovement(gamepad1);
            // rigging (when ready)

            // Gamepad 2
            intakeMovement(gamepad2);
            sliderMovement(gamepad2);
            outtakeMovement(gamepad2);
        }
    }
}
