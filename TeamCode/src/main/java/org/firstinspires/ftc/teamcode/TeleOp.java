package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp (name = "LancersTeleOp")

public class TeleOp extends OpMode {
   DcMotor FrontLeft = null;
   DcMotor FrontRight = null;
   DcMotor BackLeft = null;
   DcMotor BackRight = null;
    @Override
    public void init() {

telemetry.addData("Initialization", "is a success");
telemetry.update();
    }

    @Override
    public void loop() {


    }
}
