package org.firstinspires.ftc.teamcode.lancers.fullauton.opmode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import org.firstinspires.ftc.teamcode.lancers.auton.StartPosition;
import org.firstinspires.ftc.teamcode.lancers.config.Constants;
import org.firstinspires.ftc.teamcode.lancers.fullauton.FullAutonOpMode;


/**
 * Bootstrap wrapper class for {@link FullAutonOpMode}
 * Any code should be stored in {@link FullAutonOpMode}, not this class.
 */
@Autonomous(preselectTeleOp = Constants.TELEOP_NAME, group = "FullAuton")
//@Disabled
public final class RedFrontStageFullAuton extends FullAutonOpMode {
    public RedFrontStageFullAuton() {
        super(StartPosition.RED_FRONT_STAGE);
    }
}
