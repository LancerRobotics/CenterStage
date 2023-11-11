package org.firstinspires.ftc.teamcode.opmode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.LancersBotConfig;

@TeleOp
public class MecanumTeleOp extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        //https://gm0.org/ja/latest/docs/software/tutorials/gamepad.html
        // Declare our motors
        // Make sure your ID's match your configuration
        DcMotor leftFront = hardwareMap.dcMotor.get(LancersBotConfig.LEFT_FRONT_MOTOR);
        DcMotor leftRear = hardwareMap.dcMotor.get(LancersBotConfig.LEFT_REAR_MOTOR);
        DcMotor rightFront = hardwareMap.dcMotor.get(LancersBotConfig.RIGHT_FRONT_MOTOR);
        DcMotor rightRear = hardwareMap.dcMotor.get(LancersBotConfig.RIGHT_REAR_MOTOR);

        Servo servoLeft = hardwareMap.servo.get(LancersBotConfig.LEFT_SLIDE_MOTOR); //Two slide servos
        Servo servoRight = hardwareMap.servo.get(LancersBotConfig.RIGHT_SLIDE_MOTOR); //when the gamepad 2 left joystick is moved up, the slides should go up, when moved down, the slides should go down

         //continously rotating servos for outtake drop angle named servoFront and servoBack

        DcMotor intakeMotor = hardwareMap.dcMotor.get(LancersBotConfig.INTAKE_MOTOR);



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
            final double ly = -gamepad1.left_stick_y; // Remember, Y stick value is reversed
            final double lx = gamepad1.left_stick_x * 1.1; // Counteract imperfect strafing

            final double rx = gamepad1.right_stick_x;

            // Denominator is the largest motor power (absolute value) or 1
            // This ensures all the powers maintain the same ratio,
            // but only if at least one is out of the range [-1, 1]
            double denominator = Math.max(Math.abs(ly) + Math.abs(lx) + Math.abs(rx), 1);
            double frontLeftPower = (ly + lx + rx) / denominator;
            double backLeftPower = (ly - lx + rx) / denominator;
            double frontRightPower = (ly - lx - rx) / denominator;
            double backRightPower = (ly + lx - rx) / denominator;

            leftFront.setPower(frontLeftPower);
            leftRear.setPower(backLeftPower);
            rightFront.setPower(frontRightPower);
            rightRear.setPower(backRightPower);

            // intake movement
            final double lx2 = gamepad2.left_stick_x; // left joystick gamepad 2
            intakeMotor.setPower(0.8);


            //Outtake movement
            //when gamepad2 left joystick goes up, the left motor should go positive and the right motor should go negative
            //when gamepad2 right joystick goes up, the right motor should go positive and the left motor should go negative
            //Make sure that both motors are spinning at the same magnitude AT ALL TIMES
            double slidemotorpowerleft = gamepad2.left_stick_y;
            double slidemotorpowerright = -slidemotorpowerleft;
            //hopefully made it so that the two powers are constantly spinning at the same magnitude


            // left joystick gamepad 2 up is move motor intake left down is move motor intake right
            // outtake four servos in total two servos on the slides two servos on the wheels. Two slide servos
            // called servoLeft and servoRight.
            // those set perpendicular to the backboard
            // servoFront servoBack continuous rotation servos set it to an angle as intaking set it to
            // bind to one of the buttons manualy rotate it left and right f the joystick and up and down
            //

        }
    }
}