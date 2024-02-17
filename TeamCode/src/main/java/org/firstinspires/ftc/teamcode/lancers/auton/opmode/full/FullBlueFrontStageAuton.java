package org.firstinspires.ftc.teamcode.lancers.auton.opmode.full;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import org.firstinspires.ftc.teamcode.lancers.LancersConstants;
import org.firstinspires.ftc.teamcode.lancers.auton.model.StartPosition;
import org.firstinspires.ftc.teamcode.lancers.auton.opmode.FullAutonOpMode;

/**
 * Bootstrap wrapper class for {@link FullAutonOpMode}
 * Any code should be stored in {@link FullAutonOpMode}, not this class.
 */
@Autonomous(preselectTeleOp = LancersConstants.TELEOP_NAME, group = "CubeFullAuton")
//@Disabled
public final class FullBlueFrontStageAuton extends FullAutonOpMode {
    public FullBlueFrontStageAuton() {
        super(StartPosition.BLUE_FRONT_STAGE, true, false);
    }
}
