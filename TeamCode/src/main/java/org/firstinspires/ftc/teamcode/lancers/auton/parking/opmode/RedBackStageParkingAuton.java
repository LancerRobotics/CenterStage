package org.firstinspires.ftc.teamcode.lancers.auton.parking.opmode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import org.firstinspires.ftc.teamcode.lancers.LancersConstants;
import org.firstinspires.ftc.teamcode.lancers.auton.StartPosition;
import org.firstinspires.ftc.teamcode.lancers.auton.parking.ParkingAutonOpMode;


/**
 * Bootstrap for {@link ParkingAutonOpMode}
 */
@Autonomous(preselectTeleOp = LancersConstants.TELEOP_NAME, group = "ParkingAuton")
public final class RedBackStageParkingAuton extends ParkingAutonOpMode {
    public RedBackStageParkingAuton() {
        super(StartPosition.RED_BACK_STAGE);
    }
}
