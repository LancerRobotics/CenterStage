package org.firstinspires.ftc.teamcode.lancers.auton;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.jetbrains.annotations.NotNull;

public abstract class LancersAutonOpMode extends LinearOpMode {
    // I'm not sure if this should be changed, but it doesn't change anything.
    public @NotNull Telemetry telemetry = new MultipleTelemetry(super.telemetry, FtcDashboard.getInstance().getTelemetry());

    public final @NotNull StartPosition startPosition;

    public LancersAutonOpMode(final @NotNull StartPosition startPosition) {
        super();
        this.startPosition = startPosition;
    }
}
