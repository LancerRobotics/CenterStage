package org.firstinspires.ftc.teamcode.lancers.auton.parkingauton.opmode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import org.firstinspires.ftc.teamcode.lancers.auton.StartPosition;
import org.firstinspires.ftc.teamcode.lancers.auton.parkingauton.ParkingAutonOpMode;
import org.firstinspires.ftc.teamcode.lancers.util.Constants;


/**
 * Bootstrap for {@link ParkingAutonOpMode}
 */
@Autonomous(preselectTeleOp = Constants.TELEOP_NAME, group = "ParkingAuton")
public final class BlueBackStageParkingAuton extends ParkingAutonOpMode {
    public BlueBackStageParkingAuton() {
        super(StartPosition.BLUE_BACK_STAGE);
    }
}
