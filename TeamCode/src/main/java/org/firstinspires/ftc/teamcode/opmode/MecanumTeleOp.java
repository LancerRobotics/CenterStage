package org.firstinspires.ftc.teamcode.opmode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp (name = "MecanumTeleOp")
public class MecanumTeleOp extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        // Declare our motors
        // Make sure your ID's match your configuration
        DcMotor leftFront = hardwareMap.dcMotor.get("frontLeft");
        DcMotor leftRear = hardwareMap.dcMotor.get("rearLeft");
        DcMotor rightFront = hardwareMap.dcMotor.get("frontRight");
        DcMotor rightRear = hardwareMap.dcMotor.get("rearRight");
        DcMotor SlideMotorLeft = hardwareMap.dcMotor.get("SlideMotorL");
        DcMotor SlideMotorRight = hardwareMap.dcMotor.get("SlideMotorR");
        DcMotor intakeMotor = hardwareMap.dcMotor.get("intake");

        // Reverse the right side motors. This may be wrong for your setup.
        // If your robot moves backwards when commanded to go forwards,
        // reverse the left side instead.
        // See the note about this earlier on this page.
        leftFront.setDirection(DcMotorSimple.Direction.REVERSE);
        leftRear.setDirection(DcMotorSimple.Direction.REVERSE);

        waitForStart();

        if (isStopRequested()) return;

        while (opModeIsActive()) {
            //Bot movement
            double y = -gamepad1.left_stick_y; // Remember, Y stick value is reversed
            double x = gamepad1.left_stick_x * 1.1; // Counteract imperfect strafing
            double rx = gamepad1.right_stick_x;



            // Denominator is the largest motor power (absolute value) or 1
            // This ensures all the powers maintain the same ratio,
            // but only if at least one is out of the range [-1, 1]
            double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
            double frontLeftPower = (y + x + rx) / denominator;
            double backLeftPower = (y - x + rx) / denominator;
            double frontRightPower = (y - x - rx) / denominator;
            double backRightPower = (y + x - rx) / denominator;

            leftFront.setPower(frontLeftPower);
            leftRear.setPower(backLeftPower);
            rightFront.setPower(frontRightPower);
            rightRear.setPower(backRightPower);

            //Outtake movement
            //when gamepad2 left joystick goes up, the left motor should go positive and the right motor should go negative
            //when gamepad2 right joystick goes up, the right motor should go positive and the left motor should go negative
            //Make sure that both motors are spinning at the same magnitude AT ALL TIMES
            double slidemotorpowerleft = gamepad2.left_stick_y;
            double slidemotorpowerright = -slidemotorpowerleft;
            //hopefully made it so that the two powers are constantly spinning at the same magnitude
            SlideMotorLeft.setPower(slidemotorpowerleft);
            SlideMotorRight.setPower(slidemotorpowerright);
            //I have no clue what I'm doing uhhhh
        }
    }
}