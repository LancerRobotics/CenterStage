package org.firstinspires.ftc.teamcode.lancers;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import org.jetbrains.annotations.NotNull;

@Config
public class LancersRobot {
    final @NotNull HardwareMap hardwareMap;

    public LancersRobot(final @NotNull HardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;
    }

    // outtake basket

    private static final double SERVO_HORIZONTAL_POSITION = 0.54d;

    private static final double SERVO_VERTICAL_POSITION = 0.83d;

    public void initOuttakeBasket() {
        bringOuttakeHorizontal();
    }

    public void bringOuttakeHorizontal() {
        setOuttakePosition(SERVO_HORIZONTAL_POSITION);
    }

    public void bringOuttakeVertical() {
        setOuttakePosition(SERVO_VERTICAL_POSITION);
    }

    public double getOutakePosition() {
        final @NotNull Servo leftOuttake = hardwareMap.servo.get(LancersBotConfig.LEFT_OUTTAKE_SERVO);
        return leftOuttake.getPosition();
    }

    public void setOuttakePosition(final double outtakePosition) {
        final @NotNull Servo leftOuttake = hardwareMap.servo.get(LancersBotConfig.LEFT_OUTTAKE_SERVO);
        final @NotNull Servo rightOuttake = hardwareMap.servo.get(LancersBotConfig.RIGHT_OUTTAKE_SERVO);

        double targetLeftPos = outtakePosition;

        if (targetLeftPos > 0.9d) {
            targetLeftPos = 0.9d;
        } else if (targetLeftPos < 0.1d) {
            targetLeftPos = 0.1d;
        }

        final double targetRightPos = 1 - targetLeftPos;

        leftOuttake.setPosition(targetLeftPos);
        rightOuttake.setPosition(targetRightPos);
    }

    // outtake wheels

    /**
     * @param power negative is in positive is out
     */
    public void setOuttakeWheelSpeed(final float power) {
        final @NotNull CRServo backOuttake = hardwareMap.crservo.get(LancersBotConfig.BACK_OUTTAKE_SERVO);
        final @NotNull CRServo frontOuttake = hardwareMap.crservo.get(LancersBotConfig.FRONT_OUTTAKE_SERVO);
        final float backOuttakePower = power;
        final float frontOuttakePower = -backOuttakePower;
        backOuttake.setPower(backOuttakePower);
        frontOuttake.setPower(frontOuttakePower);
    }

    // slider movement

    private static final double MAX_SINGLE_SLIDER_SPEED = 0.3d;
    private static final double MAX_SLIDER_SPEED = 0.6d;

    public void doSliderMovement(final double sliderPower, final boolean useLeftSlider, final boolean useRightSlider) {
        final @NotNull DcMotor leftSlider = hardwareMap.dcMotor.get(LancersBotConfig.LEFT_SLIDE_MOTOR);
        final @NotNull DcMotor rightSlider = hardwareMap.dcMotor.get(LancersBotConfig.RIGHT_SLIDE_MOTOR);

        final double multiplier = (useLeftSlider && useRightSlider) ? MAX_SLIDER_SPEED : MAX_SINGLE_SLIDER_SPEED;

        final double leftSliderPower = (useLeftSlider ? sliderPower : 0.0d) * multiplier;
        final double rightSliderPower = (useRightSlider ? -sliderPower : 0.0d) * multiplier;

        leftSlider.setPower(leftSliderPower);
        rightSlider.setPower(rightSliderPower);
    }

    public void doSliderMovement(final double sliderPower) {
        doSliderMovement(sliderPower, true, true);
    }

    // intake

    public void doIntakeMovement(final double power) {
        final @NotNull DcMotor intake = hardwareMap.get(DcMotor.class, LancersBotConfig.INTAKE_MOTOR);
        intake.setPower(power);
    }
}