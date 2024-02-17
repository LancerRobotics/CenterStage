package org.firstinspires.ftc.teamcode.lancers.auton.opmode;

import org.firstinspires.ftc.teamcode.lancers.LancersBaseOpMode;
import org.firstinspires.ftc.teamcode.lancers.auton.model.StartPosition;
import org.jetbrains.annotations.NotNull;

public abstract class LancersAutonBaseOpMode extends LancersBaseOpMode {
    public final @NotNull StartPosition startPosition;

    public LancersAutonBaseOpMode(final @NotNull StartPosition startPosition) {
        super();
        this.startPosition = startPosition;
    }
}
