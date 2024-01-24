package org.firstinspires.ftc.teamcode.lancers.auton;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import org.jetbrains.annotations.NotNull;

public enum StartPosition {
    // Coordinates from RRPathGen/MeepMeep/First's coordinate system
    // https://learnroadrunner.com/tools.html#tools
    BLUE_BACK_STAGE,
    BLUE_FRONT_STAGE,
    RED_BACK_STAGE,
    RED_FRONT_STAGE;

    public @NotNull Pose2d getStartPose() {
        switch (this) {
            case RED_BACK_STAGE:
                return new Pose2d(11.37, -62, Math.toRadians(90));
            case RED_FRONT_STAGE:
                return new Pose2d(-37.46, -62, Math.toRadians(90));
            case BLUE_BACK_STAGE:
                return new Pose2d(11.37, 62, Math.toRadians(-90));
            case BLUE_FRONT_STAGE:
            default: // already exhaustive
                return new Pose2d(-37.46, 62, Math.toRadians(-90));
        }
    }

    /**
     * Slightly in front of startPose
     *
     * @return
     */
    public @NotNull Pose2d getSafeMovementPose() {
        final @NotNull Pose2d startPose = getStartPose();
        return new Pose2d(startPose.getX(), startPose.getY() * (60d / 62), startPose.getHeading());
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

    public @NotNull StagePosition getStagePosition() {
        switch (this) {
            case RED_BACK_STAGE:
            case BLUE_BACK_STAGE:
                return StagePosition.BACK;
            case RED_FRONT_STAGE:
            case BLUE_FRONT_STAGE:
            default: // already exhaustive
                return StagePosition.FRONT;
        }
    }

    public enum StagePosition {
        FRONT,
        BACK
    }

    public enum TeamScoringElementLocation {
        LEFT,
        CENTER,
        RIGHT
    }
}
