package org.firstinspires.ftc.teamcode.opmode.auton;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.opmode.OpenCV;


/**
 * Bootstrap wrapper class for {@link org.firstinspires.ftc.teamcode.opmode.OpenCV}
 */
@Autonomous
public class RedFrontStage extends OpMode {
    public static final AutonStartMode AUTON_START_MODE = AutonStartMode.RED_FRONT_STAGE;
    final OpenCV openCV = new OpenCV(this, AUTON_START_MODE);

    @Override
    public void init() {
        openCV.init();
    }

    @Override
    public void loop() {

        openCV.loop();
        // switch statement for if on left or on right side of camera view
        switch(OpenCV.direction) {
            case "left": // if robot determines color on the left
                // robot moves left
                // OpenCV loop still runs
                // stop with equal amount of color between left and right?
                break;
            case "right": // if robot determines color on the right
                // robot moves right
                // OpenCV loop still runs
                // stop with equal amount of color between left and right
                break;
            default: // when none of the cases is true
                // move the bot forward??
                break;
        }
    }
}
