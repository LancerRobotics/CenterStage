package org.firstinspires.ftc.teamcode.lancers.auton.full.yellowless_tensorflow_opmode.yellowless_opencv_opmode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import org.firstinspires.ftc.teamcode.lancers.LancersConstants;
import org.firstinspires.ftc.teamcode.lancers.auton.StartPosition;
import org.firstinspires.ftc.teamcode.lancers.auton.full.FullAutonOpMode;


/**
 * Bootstrap wrapper class for {@link FullAutonOpMode}.
 * Any code should be stored in {@link FullAutonOpMode}, not this class.
 */
@Autonomous(preselectTeleOp = LancersConstants.TELEOP_NAME, group = "YellowlessWhitePixelFullAuton")
//@Disabled
public final class YellowlessRedBackStageWhitePixelFullAuton extends FullAutonOpMode {
    public YellowlessRedBackStageWhitePixelFullAuton() {
        super(StartPosition.RED_BACK_STAGE, DetectionType.TENSORFLOW, false);
    }
}
