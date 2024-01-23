package org.firstinspires.ftc.teamcode.lancers.auton;

import org.firstinspires.ftc.teamcode.lancers.util.LancersOpMode;
import org.jetbrains.annotations.NotNull;

public abstract class LancersAutonOpMode extends LancersOpMode {
    public final @NotNull StartPosition startPosition;

    public LancersAutonOpMode(final @NotNull StartPosition startPosition) {
        super();
        this.startPosition = startPosition;
    }
}
