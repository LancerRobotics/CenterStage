package org.firstinspires.ftc.teamcode.lancers.util;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.jetbrains.annotations.NotNull;

public final class OpModeUtil {
    private OpModeUtil() {
    }

    public static void initMultipleTelemetry(final @NotNull LinearOpMode opMode) {
        final @NotNull Telemetry dashboardTelemetry = FtcDashboard.getInstance().getTelemetry();
        final @NotNull MultipleTelemetry multipleTelemetry = new MultipleTelemetry(opMode.telemetry, dashboardTelemetry);

        opMode.telemetry = multipleTelemetry; // sort of a monkeypatch but it works
    }
}
