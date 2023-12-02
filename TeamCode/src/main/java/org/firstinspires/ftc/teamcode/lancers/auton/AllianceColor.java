package org.firstinspires.ftc.teamcode.lancers.auton;

import com.acmerobotics.roadrunner.geometry.Vector2d;
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

    public @NotNull Vector2d getBackstageParkingSpot() {
        switch (this) {
            case BLUE:
                return new Vector2d(60.95, 62);
            case RED:
            default: // already exhaustive
                return new Vector2d(60.95, -62);
        }
    }

    public @NotNull Scalar getScalar() {
        switch (this) {
            case BLUE:
                return new Scalar(0, 0, 255);
            case RED:
            default: // already exhaustive
                return new Scalar(255, 0, 0);
        }
    }
}
