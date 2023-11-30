package org.firstinspires.ftc.teamcode.lancers.auton;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.jetbrains.annotations.NotNull;

public abstract class LancersAutonOpMode extends LinearOpMode {
    public final @NotNull StartPosition startPosition;

    public LancersAutonOpMode(final @NotNull StartPosition startPosition) {
        super();
        this.startPosition = startPosition;
    }
}
