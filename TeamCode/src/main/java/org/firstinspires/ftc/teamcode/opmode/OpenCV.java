package org.firstinspires.ftc.teamcode.opmode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.opmode.auton.AutonStartMode;

/**
 * Holds common OpenCV code shared between different auton modes.
 */
// https://www.youtube.com/watch?v=547ZUZiYfQE&t=37s
public class OpenCV {
    final OpMode opMode;
    final AutonStartMode startMode;

    public OpenCV(final OpMode opMode, final AutonStartMode startMode) {
        this.opMode = opMode;
        this.startMode = startMode;
    }

    public void init() {

    }

    public void loop() {

    }
}
