package org.firstinspires.ftc.teamcode.lancers;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class LancersBaseOpMode extends LinearOpMode {
    public final @NotNull MultipleTelemetry multipleTelemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

    public @Nullable LancersRobot robot;
    public @Nullable SampleMecanumDrive drive;

    public void initCommon() {
        robot = new LancersRobot(hardwareMap);
        drive = robot.getDrive();
        robot.initOuttakeBasket();
    }
}
