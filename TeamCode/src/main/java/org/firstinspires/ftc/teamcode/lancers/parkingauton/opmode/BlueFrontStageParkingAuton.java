package org.firstinspires.ftc.teamcode.lancers.parkingauton.opmode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import org.firstinspires.ftc.teamcode.lancers.auton.StartPosition;
import org.firstinspires.ftc.teamcode.lancers.config.Constants;
import org.firstinspires.ftc.teamcode.lancers.parkingauton.ParkingAutonOpMode;


/**
 * Bootstrap for {@link ParkingAutonOpMode}
 */
@Autonomous(preselectTeleOp = Constants.TELEOP_NAME, group = "ParkingAuton")
public final class BlueFrontStageParkingAuton extends ParkingAutonOpMode {
    public BlueFrontStageParkingAuton() {
        super(StartPosition.BLUE_FRONT_STAGE);
    }
}
