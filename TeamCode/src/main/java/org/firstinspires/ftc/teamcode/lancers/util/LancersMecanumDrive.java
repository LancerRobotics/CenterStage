package org.firstinspires.ftc.teamcode.lancers.util;

import com.acmerobotics.roadrunner.drive.MecanumDrive;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.teamcode.rr.drive.SampleMecanumDrive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A wrapper around {@link SampleMecanumDrive} that allows for telemetry to be added to the driver station and
 * for state data to be persisted between opmodes.
 */
public class LancersMecanumDrive extends SampleMecanumDrive implements AutoCloseable {
    public LancersMecanumDrive(final @NotNull HardwareMap hardwareMap) {
        super(hardwareMap);
        PoseStorage.restoreStoredPose(this);
    }

    @Override
    public void update() {
        super.update();
        PoseStorage.updateStoredPose(this);
    }

    @Override
    public void close() {
        PoseStorage.updateStoredPose(this);
    }

    private static class PoseStorage {
        // If we ever make serious use of this, we should probably migrate this to using the database
        // https://learnroadrunner.com/advanced.html#transferring-pose-between-opmodes:~:text=Another%20downside%20is,%23
        private PoseStorage() {
        }

        private static @Nullable Pose2d lastPose = null;

        private static void updateStoredPose(@NotNull MecanumDrive drive) {
            lastPose = drive.getPoseEstimate();
        }

        private static void restoreStoredPose(@NotNull MecanumDrive drive) {
            if (lastPose != null) {
                drive.setPoseEstimate(lastPose);
            }
        }
    }
}
