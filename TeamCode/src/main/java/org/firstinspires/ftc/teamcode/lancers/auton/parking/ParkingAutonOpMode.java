package org.firstinspires.ftc.teamcode.lancers.auton.parking;

import org.firstinspires.ftc.teamcode.lancers.auton.AllianceColor;
import org.firstinspires.ftc.teamcode.lancers.auton.LancersAutonOpMode;
import org.firstinspires.ftc.teamcode.lancers.auton.StartPosition;
import org.firstinspires.ftc.teamcode.lancers.util.LancersMecanumDrive;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;
import org.jetbrains.annotations.NotNull;

public class ParkingAutonOpMode extends LancersAutonOpMode {
    public ParkingAutonOpMode(@NotNull StartPosition startPosition) {
        super(startPosition);
    }

    @Override
    public void runOpMode() throws InterruptedException {
        try (final @NotNull LancersMecanumDrive drive = new LancersMecanumDrive(hardwareMap)) {
            drive.setPoseEstimate(startPosition.getStartPose());

            TrajectorySequence trajectorySequence = drive.trajectorySequenceBuilder(startPosition.getStartPose())
                    .lineTo(startPosition.getSafeMovementPose().vec()) // TODO: do better
                    .turn(Math.toRadians(startPosition.getAllianceColor() == AllianceColor.RED ? -90 : 90)) // turn towards backboard
                    .lineTo(startPosition.getAllianceColor().getBackstageParkingSpot())
                    .build();

            waitForStart();

            if (isStopRequested()) return;

            drive.followTrajectorySequence(trajectorySequence);
        }
    }
}
