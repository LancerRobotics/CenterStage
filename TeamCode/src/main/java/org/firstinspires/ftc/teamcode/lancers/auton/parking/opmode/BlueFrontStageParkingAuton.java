package org.firstinspires.ftc.teamcode.lancers.auton.parking.opmode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import org.firstinspires.ftc.teamcode.lancers.LancersConstants;
import org.firstinspires.ftc.teamcode.lancers.auton.StartPosition;
import org.firstinspires.ftc.teamcode.lancers.auton.parking.ParkingAutonOpMode;


/**
 * Bootstrap for {@link ParkingAutonOpMode}
 */
@Autonomous(preselectTeleOp = LancersConstants.TELEOP_NAME, group = "ParkingAuton")
public final class BlueFrontStageParkingAuton extends ParkingAutonOpMode {
    public BlueFrontStageParkingAuton() {
        super(StartPosition.BLUE_FRONT_STAGE);
    }
}
