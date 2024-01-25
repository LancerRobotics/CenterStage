package org.firstinspires.ftc.teamcode.lancers;

import com.acmerobotics.dashboard.config.Config;
import org.jetbrains.annotations.NotNull;

@Config
public final class LancersConstants {
    private LancersConstants() {
    }

    // This needs to be constant since it is used in annotations. If the name of the TeleOp mode needs to be changed
    // for any reason, this is the only place that needs to be changed. ALWAYS REFER TO THE TELEOP MODE BY THIS CONSTANT!
    public final static @NotNull String TELEOP_NAME = "LancersTeleOp";
    // When compiling for a competition, set this to false
    public static boolean DEBUG = true; // extra telemetry; don't change
}
