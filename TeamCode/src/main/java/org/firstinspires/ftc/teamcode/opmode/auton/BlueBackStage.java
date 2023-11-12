package org.firstinspires.ftc.teamcode.opmode.auton;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.auton.AutonCommon;
import org.firstinspires.ftc.teamcode.auton.AutonStartMode;

/**
 * Bootstrap wrapper class for {@link AutonCommon}
 * Any code should be stored in {@link AutonCommon}, not this class.
 */
@Autonomous(preselectTeleOp = "MecanumTeleOp")
public class BlueBackStage extends OpMode {
    public static final AutonStartMode AUTON_START_MODE = AutonStartMode.BLUE_BACK_STAGE;
    final AutonCommon auton = new AutonCommon(this, AUTON_START_MODE);

    @Override
    public void init() {
        auton.init();
    }

    @Override
    public void loop() {
        auton.loop();
    }
}
