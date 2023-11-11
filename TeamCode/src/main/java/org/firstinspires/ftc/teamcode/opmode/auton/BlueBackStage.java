package org.firstinspires.ftc.teamcode.opmode.auton;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.opmode.OpenCV;

/**
 * Bootstrap wrapper class for {@link org.firstinspires.ftc.teamcode.opmode.OpenCV}
 */
@Autonomous
public class BlueBackStage extends OpMode {
    public static final AutonStartMode AUTON_START_MODE = AutonStartMode.BLUE_BACK_STAGE;
    final OpenCV openCV = new OpenCV(this, AUTON_START_MODE);

    @Override
    public void init() {
        openCV.init();
    }

    @Override
    public void loop() {
        openCV.loop();
    }
}
