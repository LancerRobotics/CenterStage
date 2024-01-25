package org.firstinspires.ftc.teamcode.lancers.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import org.firstinspires.ftc.teamcode.lancers.config.Constants;
import org.firstinspires.ftc.teamcode.lancers.config.LancersBotConfig;
import org.firstinspires.ftc.teamcode.lancers.util.LancersMecanumDrive;
import org.firstinspires.ftc.teamcode.lancers.util.LancersOpMode;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

// https://learnroadrunner.com/advanced.html#using-road-runner-in-teleop if roadrunner needed
@TeleOp(name = Constants.TELEOP_NAME)
public final class LancersTeleOp extends LancersOpMode {
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

        final double speedMultiplier = gamepad.b ? 1.0 : 0.8;

        if (gamepad.right_trigger > ControlUtil.TRIGGER_THRESHOLD) {
            intakeMotor.setPower(ControlUtil.adjustTriggerMovement(gamepad.right_trigger) * speedMultiplier);
        } else if (gamepad.left_trigger > ControlUtil.TRIGGER_THRESHOLD) {
            // for placing pixels forward/unsticking
            // divided by 2 to give finer control than SUCKING IN pixels
            intakeMotor.setPower(-ControlUtil.adjustTriggerMovement(gamepad.left_trigger) / 2.0d);
        } else {
            intakeMotor.setPower(0.0d);
        }
    }

    public void sliderMovement(final @NotNull Gamepad gamepad) {
        // right trigger: expand
        // left trigger: contract

        // if left bumper is pressed, move only left slider and do so at 0.3 speed
        // if right bumper is pressed, move only right slider and do so at 0.3 speed

        double sliderPower = 0.0d;
        if (gamepad.right_trigger > ControlUtil.TRIGGER_THRESHOLD) {
            sliderPower = ControlUtil.adjustTriggerMovement(gamepad.right_trigger);
        } else if (gamepad.left_trigger > ControlUtil.TRIGGER_THRESHOLD) {
            sliderPower = -ControlUtil.adjustTriggerMovement(gamepad.left_trigger);
        }

        final boolean useLeft;
        final boolean useRight;
        if (gamepad.right_bumper) {
            useLeft = false;
            useRight = true;
        } else if (gamepad.left_bumper) {
            useLeft = true;
            useRight = false;
        } else {
            useLeft = true;
            useRight = true;
        }

        Objects.requireNonNull(robot).doSliderMovement(sliderPower, useLeft, useRight);
    }

    public void outtakeLinearMovement(final @NotNull Gamepad gamepad) {
        // Right stick y
        Objects.requireNonNull(robot).setOuttakeWheelSpeed(gamepad.right_stick_y);
    }

    private static final double MAX_BASKET_ANGULAR_SPEED_MULTIPLIER = 0.01d;

    public void outtakeAngularMovement(final @NotNull Gamepad gamepad) {
        Objects.requireNonNull(robot);

        // Left stick y
        if (gamepad.b) {
            // Set to horizontal position
            robot.bringOuttakeHorizontal();
        } else if (gamepad.a) {
            // Set to vertical position
            robot.bringOuttakeVertical();
        } else {
            final double displacement = gamepad.left_stick_y * MAX_BASKET_ANGULAR_SPEED_MULTIPLIER;
            final double targetPosition = robot.getOutakePosition() + displacement;
            robot.setOuttakePosition(targetPosition);

            // Make sure targetLeftPos is in range for servo
            telemetry.addData("Outtake position", targetPosition);
        }
    }

    @Override
    public void runOpMode() throws InterruptedException {
        // https://gm0.org/ja/latest/docs/software/tutorials/gamepad
        // https://github.com/NoahBres/road-runner-quickstart/blob/advanced-examples/TeamCode/src/main/java/org/firstinspires/ftc/teamcode/drive/advanced/TeleOpJustLocalizer.java#L69

        // NOTE: If an auton didn't run or if the global state in the drive persisted, the pose data may be incorrect.
        //       This can be fixed by running an auton that requires the bot be in a specific starting positon
        //       (e.g. one of the parking autons or the full auton) (this only matters for launching the drone)
        try (final @NotNull LancersMecanumDrive drive = new LancersMecanumDrive(hardwareMap)) {
            initCommon();

            waitForStart();

            if (isStopRequested()) return;

            while (opModeIsActive()) {
                drive.update(); // sends trajectory data to dashboard

                // Gamepad 1 / Movement
                mecanumMovement(gamepad1);
                intakeMovement(gamepad1);

                // Gamepad 2 / Scoring
                sliderMovement(gamepad2);
                outtakeLinearMovement(gamepad2);
                outtakeAngularMovement(gamepad2);

                telemetry.update();
            }
        }
    }
}
