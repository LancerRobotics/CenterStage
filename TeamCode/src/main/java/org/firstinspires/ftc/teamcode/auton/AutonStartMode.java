package org.firstinspires.ftc.teamcode.auton;

import com.acmerobotics.roadrunner.geometry.Pose2d;

import org.jetbrains.annotations.NotNull;

public enum AutonStartMode {
    // Coordinates from RRPathGen's coordinate system
    BLUE_BACK_STAGE(new Pose2d(11.11d, 36.50d, Math.toRadians(-90.0d))),
    BLUE_FRONT_STAGE(new Pose2d(-36.50d, 35.90d, Math.toRadians(-90.0d))),
    RED_BACK_STAGE(new Pose2d(-36.90d, -35.70d, Math.toRadians(90.0d))),
    RED_FRONT_STAGE(new Pose2d(11.11d, -36.50d, Math.toRadians(90.0d)));

    // @Getter // I would much prefer to use Lombok, but as of 2023-11-11, Android Studio Giraffe does not support it
    private final @NotNull Pose2d startPose;

    // TODO: Replace with lombok
    public @NotNull Pose2d getStartPose() {
        return startPose;
    }

    AutonStartMode(final @NotNull Pose2d startPose) {
        this.startPose = startPose;
    }

    public enum AllianceColor {
        // TODO: Dynamic parking location to prevent colliding with other teams
        BLUE(new Pose2d(60.30d, 60.30d, Math.toRadians(0))),
        RED(new Pose2d(60.30d, -60.30d, Math.toRadians(0)));

        private final @NotNull Pose2d parkingPose;

        // TODO: Replace with lombok
        public @NotNull Pose2d getParkingPose() {
            return parkingPose;
        }

        AllianceColor(final @NotNull Pose2d parkingPose) {
            this.parkingPose = parkingPose;
        }
    }

    public @NotNull AllianceColor getAllianceColor() {
        switch (this) {
            case RED_BACK_STAGE:
            case RED_FRONT_STAGE:
                return AllianceColor.RED;
            case BLUE_BACK_STAGE:
            case BLUE_FRONT_STAGE:
            default: // already exhaustive
                return AllianceColor.BLUE;
        }
    }
}
