package org.firstinspires.ftc.teamcode.lancers.auton;

import com.acmerobotics.roadrunner.geometry.Pose2d;

import org.jetbrains.annotations.NotNull;

public enum AutonStartMode {
    // Coordinates from RRPathGen's coordinate system
    BLUE_BACK_STAGE,
    BLUE_FRONT_STAGE,
    RED_BACK_STAGE,
    RED_FRONT_STAGE;

    public @NotNull Pose2d getStartPose() {
        switch (this) {
            case RED_BACK_STAGE:
                return new Pose2d(11.37, -60, Math.toRadians(90));
            case RED_FRONT_STAGE:
                return new Pose2d(-37.46, -60, Math.toRadians(90));
            case BLUE_BACK_STAGE:
                return new Pose2d(11.37, 60, Math.toRadians(-90));
            case BLUE_FRONT_STAGE:
            default: // already exhaustive
                return new Pose2d(-37.46, 60, Math.toRadians(-90));
        }
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
