package org.firstinspires.ftc.teamcode.lancers.opmode.auton;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.lancers.auton.AutonCommon;
import org.firstinspires.ftc.teamcode.lancers.auton.AutonStartMode;


/**
 * Bootstrap wrapper class for {@link AutonCommon}.
 * Any code should be stored in {@link AutonCommon}, not this class.
 */
@Autonomous(preselectTeleOp = "LancersTeleOp")
public class RedBackStage extends OpMode {
    public static final AutonStartMode AUTON_START_MODE = AutonStartMode.RED_BACK_STAGE;
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
