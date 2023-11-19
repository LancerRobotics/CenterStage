package org.firstinspires.ftc.teamcode.drive;

import androidx.annotation.NonNull;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.localization.TwoTrackingWheelLocalizer;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.lancers.LancersBotConfig;
import org.firstinspires.ftc.teamcode.lancers.util.UnitUtil;
import org.firstinspires.ftc.teamcode.util.Encoder;

import java.util.Arrays;
import java.util.List;

/*
 * Sample tracking wheel localizer implementation assuming the standard configuration:
 *
 *    ^
 *    |
 *    | ( x direction)
 *    |
 *    v
 *    <----( y direction )---->

 *        (forward)
 *    /--------------\
 *    |     ____     |
 *    |     ----     |    <- Perpendicular Wheel
 *    |           || |
 *    |           || |    <- Parallel Wheel
 *    |              |
 *    |              |
 *    \--------------/
 *
 */
// https://learnroadrunner.com/dead-wheels.html#two-wheel-odometry
public class TwoWheelTrackingLocalizer extends TwoTrackingWheelLocalizer {
    public static double TICKS_PER_REV = 8192;
    public static double WHEEL_RADIUS = 1.37795276; // in
    public static double GEAR_RATIO = StandardTrackingWheelLocalizer.GEAR_RATIO; // output (wheel) speed / input (encoder) speed

    // See https://media.discordapp.net/attachments/758394775728160797/1175844683235209347/image.png?ex=656cb5b2&is=655a40b2&hm=005702568aa1f721b8270147c50300fc592aa2cd42fe9284f820e122ac609c0f&=
    public static double PARALLEL_X = UnitUtil.mmToInches(-182.418d); // X is the up and down direction
    public static double PARALLEL_Y = UnitUtil.mmToInches(-112.327d); // Y is the strafe direction

    public static double PERPENDICULAR_X = UnitUtil.mmToInches(-184.853d);
    public static double PERPENDICULAR_Y = UnitUtil.mmToInches(91.071d);

    // Multipliers to adjust for dimensional inaccuracy in both direction
    // Added https://learnroadrunner.com/dead-wheels.html#adjusting-the-wheel-radius
    public static double X_MULTIPLIER = 1;
    public static double Y_MULTIPLIER = 1;

    // Parallel/Perpendicular to the forward axis
    // Parallel wheel is parallel to the forward axis
    // Perpendicular is perpendicular to the forward axis
    private Encoder parallelEncoder, perpendicularEncoder;

    private SampleMecanumDrive drive;

    public TwoWheelTrackingLocalizer(HardwareMap hardwareMap, SampleMecanumDrive drive) {
        super(Arrays.asList(
                new Pose2d(PARALLEL_X, PARALLEL_Y, 0),
                new Pose2d(PERPENDICULAR_X, PERPENDICULAR_Y, Math.toRadians(90))
        ));

        this.drive = drive;

        parallelEncoder = new Encoder(hardwareMap.get(DcMotorEx.class, LancersBotConfig.RIGHT_FRONT_MOTOR)); // Expansion Hub port 0
        perpendicularEncoder = new Encoder(hardwareMap.get(DcMotorEx.class, LancersBotConfig.LEFT_FRONT_MOTOR)); // Control hub port 0

        // TODO: reverse any encoders using Encoder.setDirection(Encoder.Direction.REVERSE)
    }

    public static double encoderTicksToInches(double ticks) {
        return WHEEL_RADIUS * 2 * Math.PI * GEAR_RATIO * ticks / TICKS_PER_REV;
    }

    @Override
    public double getHeading() {
        return drive.getRawExternalHeading();
    }

    @Override
    public Double getHeadingVelocity() {
        return drive.getExternalHeadingVelocity();
    }


    @NonNull
    @Override
    public List<Double> getWheelPositions() {
        return Arrays.asList(
                encoderTicksToInches(parallelEncoder.getCurrentPosition()) * X_MULTIPLIER,
                encoderTicksToInches(perpendicularEncoder.getCurrentPosition()) * Y_MULTIPLIER
        );
    }

    @NonNull
    @Override
    public List<Double> getWheelVelocities() {
        // TODO: If your encoder velocity can exceed 32767 counts / second (such as the REV Through Bore and other
        //  competing magnetic encoders), change Encoder.getRawVelocity() to Encoder.getCorrectedVelocity() to enable a
        //  compensation method

        return Arrays.asList(
                encoderTicksToInches(parallelEncoder.getRawVelocity()) * X_MULTIPLIER,
                encoderTicksToInches(perpendicularEncoder.getRawVelocity()) * Y_MULTIPLIER
        );
    }
}
