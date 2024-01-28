package org.firstinspires.ftc.teamcode.lancers.auton.full.tensorflow_opmode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import org.firstinspires.ftc.teamcode.lancers.LancersConstants;
import org.firstinspires.ftc.teamcode.lancers.auton.StartPosition;
import org.firstinspires.ftc.teamcode.lancers.auton.full.FullAutonOpMode;


/**
 * Bootstrap wrapper class for {@link FullAutonOpMode}
 * Any code should be stored in {@link FullAutonOpMode}, not this class.
 */
@Autonomous(preselectTeleOp = LancersConstants.TELEOP_NAME, group = "WhitePixelFullAuton")
//@Disabled
public final class RedFrontStageWhitePixelFullAuton extends FullAutonOpMode {
    public RedFrontStageWhitePixelFullAuton() {
        super(StartPosition.RED_FRONT_STAGE, DetectionType.TENSORFLOW);
    }
}
