package org.firstinspires.ftc.teamcode.lancers.util;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.teamcode.lancers.LancersRobot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class LancersOpMode extends LinearOpMode {
    public final @NotNull MultipleTelemetry multipleTelemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

    public @Nullable LancersRobot robot;

    public void initCommon() {
        robot = new LancersRobot(hardwareMap);
        robot.initOuttakeBasket();
    }
}
