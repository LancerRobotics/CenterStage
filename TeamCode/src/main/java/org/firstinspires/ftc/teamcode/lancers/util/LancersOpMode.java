package org.firstinspires.ftc.teamcode.lancers.util;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.jetbrains.annotations.NotNull;

public abstract class LancersOpMode extends LinearOpMode {
    public final @NotNull MultipleTelemetry multipleTelemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
}
