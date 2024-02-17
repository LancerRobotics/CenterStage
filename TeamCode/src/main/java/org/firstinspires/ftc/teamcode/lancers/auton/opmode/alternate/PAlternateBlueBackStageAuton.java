package org.firstinspires.ftc.teamcode.lancers.auton.opmode.alternate;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import org.firstinspires.ftc.teamcode.lancers.LancersConstants;
import org.firstinspires.ftc.teamcode.lancers.auton.model.StartPosition;
import org.firstinspires.ftc.teamcode.lancers.auton.opmode.FullAutonOpMode;

/**
 * Bootstrap wrapper class for {@link FullAutonOpMode}
 * Any code should be stored in {@link FullAutonOpMode}, not this class.
 */
@Autonomous(preselectTeleOp = LancersConstants.TELEOP_NAME, group = "PAlternateCubeFullAuton")
//@Disabled
public final class PAlternateBlueBackStageAuton extends FullAutonOpMode {
    public PAlternateBlueBackStageAuton() {
        super(StartPosition.BLUE_BACK_STAGE, true, true);
    }
}
