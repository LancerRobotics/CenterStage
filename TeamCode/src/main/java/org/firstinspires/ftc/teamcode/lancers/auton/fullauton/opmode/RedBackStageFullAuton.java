package org.firstinspires.ftc.teamcode.lancers.auton.fullauton.opmode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import org.firstinspires.ftc.teamcode.lancers.auton.StartPosition;
import org.firstinspires.ftc.teamcode.lancers.auton.fullauton.FullAutonOpMode;
import org.firstinspires.ftc.teamcode.lancers.util.Constants;


/**
 * Bootstrap wrapper class for {@link FullAutonOpMode}.
 * Any code should be stored in {@link FullAutonOpMode}, not this class.
 */
@Autonomous(preselectTeleOp = Constants.TELEOP_NAME, group = "FullAuton")
public final class RedBackStageFullAuton extends FullAutonOpMode {
    public RedBackStageFullAuton() {
        super(StartPosition.RED_BACK_STAGE);
    }
}