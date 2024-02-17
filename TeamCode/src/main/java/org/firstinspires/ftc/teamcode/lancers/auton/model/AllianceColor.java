package org.firstinspires.ftc.teamcode.lancers.auton.model;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import org.firstinspires.ftc.teamcode.lancers.vision.util.VisionUtil;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.Scalar;

public enum AllianceColor {
    BLUE,
    RED;

    public @NotNull AllianceColor getOpposite() {
        switch (this) {
            case BLUE:
                return RED;
            case RED:
            default: // already exhaustive
                return BLUE;
        }
    }

    /**
     * Parking spot closest to the backstage parking spot
     * @return {@link Pose2d}
     */
    public @NotNull Pose2d getBackstageParkingSpot() {
        switch (this) {
            case BLUE:
                return new Pose2d(60.95, 62, Math.toRadians(0.0d));
            case RED:
            default: // already exhaustive
                return new Pose2d(60.95, -62, Math.toRadians(0.0d));
        }
    }

    /**
     * Parking spot closest to the middle
     * @return {@link Pose2d}
     */
    public @NotNull Pose2d getFrontstageParkingSpot() {
        switch (this) {
            case BLUE:
                return new Pose2d(58.12, 62, Math.toRadians(0.0d));
            case RED:
            default: // already exhaustive
                return new Pose2d(58.12, -62, Math.toRadians(0.0d));
        }
    }

    public @NotNull Scalar getColor() {
        switch (this) {
            case BLUE:
                return new Scalar(0, 0, 255);
            case RED:
            default: // already exhaustive
                return new Scalar(255, 0, 0);
        }
    }

    public @NotNull Scalar getTeamScoringElementColorRGB() {
        switch (this) {
            case BLUE:
                return new Scalar(40, 40, 150);
            case RED:
            default: // already exhaustive
                return new Scalar(233, 100, 100);
        }
    }

    public @NotNull Scalar getTeamScoringElementColorHSV() {
        return VisionUtil.rgbScalarToHSVScalar(getTeamScoringElementColorRGB());
    }
}
