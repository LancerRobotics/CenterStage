package org.firstinspires.ftc.teamcode.lancers.auton.fullauton;

import com.acmerobotics.roadrunner.geometry.Vector2d;
import org.jetbrains.annotations.NotNull;

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
                return new Vector2d(60.95, 60.04);
            case RED:
            default: // already exhaustive
                return new Vector2d(60.95, -60.04);
        }
    }
}
