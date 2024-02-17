package org.firstinspires.ftc.teamcode.lancers.auton.opmode;

import org.firstinspires.ftc.teamcode.lancers.auton.model.AllianceColor;
import org.firstinspires.ftc.teamcode.lancers.auton.model.StagePosition;
import org.firstinspires.ftc.teamcode.lancers.auton.model.StartPosition;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ParkingAutonOpMode extends LancersAutonBaseOpMode {
    public ParkingAutonOpMode(@NotNull StartPosition startPosition) {
        super(startPosition);
    }

    @Override
    public void runOpMode() throws InterruptedException {
        try {
            initCommon();
            Objects.requireNonNull(robot);
            Objects.requireNonNull(drive);
            drive.setPoseEstimate(startPosition.getStartPose());

            TrajectorySequence trajectorySequence = drive.trajectorySequenceBuilder(startPosition.getStartPose())
                    .lineTo(startPosition.getSafeMovementPose().vec()) // TODO: do better
                    .turn(Math.toRadians(startPosition.getAllianceColor() == AllianceColor.RED ? -90 : 90)) // turn towards backboard
                    .lineTo(startPosition.getAllianceColor().getBackstageParkingSpot().vec())
                    .build();

            waitForStart();

            if (startPosition.getStagePosition() == StagePosition.FRONT) {
                sleep(20_000);
            }

            if (isStopRequested()) return;

            drive.followTrajectorySequence(trajectorySequence);
        } finally {
            robot.cleanup();
        }
    }
}
