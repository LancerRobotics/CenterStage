package org.firstinspires.ftc.teamcode.auton;

/**
 * Determines the side that the camera is mounted on. This is currently always left, but it may change in the future.
 */
public enum CameraMountSide {
    LEFT_FRONT_CAMERA,
    RIGHT_FRONT_CAMERA;

    public static final CameraMountSide MOUNTING_LOCATION = CameraMountSide.LEFT_FRONT_CAMERA;
}
