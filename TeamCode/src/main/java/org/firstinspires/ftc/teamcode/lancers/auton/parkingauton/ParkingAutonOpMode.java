package org.firstinspires.ftc.teamcode.lancers.auton.parkingauton;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.teamcode.lancers.auton.StartPosition;
import org.firstinspires.ftc.teamcode.lancers.util.LancersMecanumDrive;
import org.firstinspires.ftc.teamcode.lancers.util.OpModeUtil;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;
import org.jetbrains.annotations.NotNull;

public class ParkingAutonOpMode extends LinearOpMode {
    private final @NotNull StartPosition startPosition;

    public ParkingAutonOpMode(final @NotNull StartPosition startPosition) {
        super();
        this.startPosition = startPosition;
    }

    @Override
    public void runOpMode() throws InterruptedException {
        try (final @NotNull LancersMecanumDrive drive = new LancersMecanumDrive(hardwareMap)) {
            OpModeUtil.initMultipleTelemetry(this);

            drive.setPoseEstimate(startPosition.getStartPose());

            TrajectorySequence trajectorySequence = drive.trajectorySequenceBuilder(startPosition.getStartPose())
                    .lineTo(startPosition.getAllianceColor().getBackstageParkingSpot())
                    .build();

            waitForStart();

            if (isStopRequested()) return;

            drive.followTrajectorySequence(trajectorySequence);
        }
    }
}
