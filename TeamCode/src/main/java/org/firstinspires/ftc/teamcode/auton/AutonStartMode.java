package org.firstinspires.ftc.teamcode.auton;

import org.jetbrains.annotations.NotNull;

public enum AutonStartMode {
    BLUE_BACK_STAGE,
    BLUE_FRONT_STAGE,
    RED_BACK_STAGE,
    RED_FRONT_STAGE;

    public enum AllianceColor {
        BLUE,
        RED;
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
