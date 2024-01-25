package org.firstinspires.ftc.teamcode.lancers.auton.full;

import android.util.Size;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.lancers.LancersBotConfig;
import org.firstinspires.ftc.teamcode.lancers.auton.LancersAutonOpMode;
import org.firstinspires.ftc.teamcode.lancers.auton.StartPosition;
import org.firstinspires.ftc.teamcode.lancers.util.LancersMecanumDrive;
import org.firstinspires.ftc.teamcode.lancers.vision.pipeline.TeamScoringElementPipeline;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequenceBuilder;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.tfod.TfodProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static org.firstinspires.ftc.teamcode.lancers.auton.AllianceColor.BLUE;

/**
 * Holds common code shared between different auton modes.
 * Implements {@link org.firstinspires.ftc.robotcontroller.external.samples.ConceptDoubleVision} & uses {@link LancersMecanumDrive}
 */
public class FullAutonOpMode extends LancersAutonOpMode {
    private @Nullable LancersMecanumDrive drive = null;

    public FullAutonOpMode(@NotNull StartPosition startPosition) {
        super(startPosition);
    }

    private void initDrive() {
        drive = new LancersMecanumDrive(hardwareMap);
        drive.setPoseEstimate(startPosition.getStartPose());
    }

    // Computer vision processors & initialization
    // https://ftc-docs.firstinspires.org/en/latest/programming_resources/index.html#vision-programming

    // https://ftc-docs.firstinspires.org/en/latest/apriltag/vision_portal/apriltag_intro/apriltag-intro.html
    // https://ftc-docs.firstinspires.org/en/latest/apriltag/vision_portal/apriltag_intro/apriltag-intro.html#advanced-use
    private final @NotNull AprilTagProcessor aprilTag = new AprilTagProcessor.Builder()

            // The following default settings are available to un-comment and edit as needed.
            //.setDrawAxes(false)
            //.setDrawCubeProjection(false)
            //.setDrawTagOutline(true)
            //.setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
            //.setTagLibrary(AprilTagGameDatabase.getCenterStageTagLibrary())
            //.setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES)

            // == CAMERA CALIBRATION ==
            // If you do not manually specify calibration parameters, the SDK will attempt
            // to load a predefined calibration for your camera.
            //.setLensIntrinsics(578.272, 578.272, 402.145, 221.506)
            // ... these parameters are fx, fy, cx, cy.

            // Lancers: Our camera is a Logitech C920 and has a default configuration. It will work fine without calibration.

            .build();

    // https://ftc-docs.firstinspires.org/en/latest/programming_resources/vision/tensorflow_cs_2023/tensorflow-cs-2023.html
    // https://ftc-docs.firstinspires.org/en/latest/ftc_ml/index.html
    private final @NotNull TfodProcessor tfod = new TfodProcessor.Builder()

            // With the following lines commented out, the default TfodProcessor Builder
            // will load the default model for the season. To define a custom model to load,
            // choose one of the following:
            //   Use setModelAssetName() if the custom TF Model is built in as an asset (AS only).
            //   Use setModelFileName() if you have downloaded a custom team model to the Robot Controller.
            //.setModelAssetName(TFOD_MODEL_ASSET)
            //.setModelFileName(TFOD_MODEL_FILE)

            // The following default settings are available to un-comment and edit as needed to
            // set parameters for custom models.
            //.setModelLabels(LABELS)
            //.setIsModelTensorFlow2(true)
            //.setIsModelQuantized(true)
            //.setModelInputSize(300)
            //.setModelAspectRatio(16.0 / 9.0)

            .build();

    private final @NotNull TeamScoringElementPipeline tseProcessor = new TeamScoringElementPipeline(this);

    private @Nullable VisionPortal visionPortal = null;

    private void initVision() {
        final @NotNull WebcamName webcam = hardwareMap.get(WebcamName.class, LancersBotConfig.WEBCAM);

        visionPortal = new VisionPortal.Builder()
                .addProcessors(aprilTag, tfod, tseProcessor)
                .setCamera(webcam)

                // Choose a camera resolution. Not all cameras support all resolutions.
                .setCameraResolution(new Size(640, 480))

                // Enable the RC preview (LiveView).  Set "false" to omit camera monitoring.
                .enableLiveView(true)

                // Set the stream format; MJPEG uses less bandwidth than default YUY2.
                //builder.setStreamFormat(VisionPortal.StreamFormat.YUY2)

                // Choose whether or not LiveView stops if no processors are enabled.
                // If set "true", monitor shows solid orange screen if no processors enabled.
                // If set "false", monitor shows camera view without annotations.
                .setAutoStopLiveView(true)

                .build();

        visionPortal.setProcessorEnabled(aprilTag, false); // shining is right; we don't need this (yet)
        visionPortal.setProcessorEnabled(tfod, false); // tfod & aprilTag can be enabled as we need them, don't strip out unused code
        visionPortal.setProcessorEnabled(tseProcessor, false); // suitable for comp #1
    }

    // Runtime code

    private void cleanup() {
        if (drive != null) {
            drive.close();
        }

        if (visionPortal != null) {
            visionPortal.close();
        }
    }

    public @NotNull TrajectorySequence getTrajectorySequence(final @NotNull StartPosition.TeamScoringElementLocation teamScoringElementLocation) {
        // we can't run this trajectory right away because we need to wait for the tse to be discovered
        Objects.requireNonNull(drive);
        Objects.requireNonNull(robot);

        final @NotNull TrajectorySequenceBuilder builder = drive.trajectorySequenceBuilder(drive.getPoseEstimate());
        // move into position to put down the pixel

        // helper constants for reusing trajectory code
        final double faceInDeg = startPosition.getAllianceColor() == BLUE ? -90.0d : 90.0d;
        final double faceOutDeg = startPosition.getAllianceColor() == BLUE ? 90.0d : -90.0d;
        final double faceBoardDeg = 0.0d;
        final double faceDroneFieldDeg = 180.0d;
        final double flipY = startPosition.getAllianceColor() == BLUE ? -1 : 1; // turns red to blue

        builder.lineToConstantHeading(startPosition.getSafeMovementPose().vec());
        switch (teamScoringElementLocation) {
            case CENTER:
                switch (startPosition) {
                    case RED_FRONT_STAGE:
                    case BLUE_FRONT_STAGE:
                        builder.splineTo(new Vector2d(-43.63, -35.35 * flipY), Math.toRadians(faceInDeg));
                        break;
                    case RED_BACK_STAGE:
                    case BLUE_BACK_STAGE:
                        builder.splineTo(new Vector2d(17.24, -35.89 * flipY), Math.toRadians(faceInDeg));
                        break;
                }
                break;
            case LEFT:
                builder.turn(Math.toRadians(90.0d)) // counter-clockwise
                        .strafeRight(22.0d);
                break;
            case RIGHT:
                builder.turn(Math.toRadians(-90.0d)) // clockwise
                        .strafeLeft(22.0d);
                break;
        }

        // drop the pixel
        final double INTAKE_RUNTIME = 0.6d;
        final double INTAKE_POWER = 0.55d;
        builder.UNSTABLE_addTemporalMarkerOffset(0.0, () -> {
            robot.doIntakeMovement(-INTAKE_POWER);
        });
        builder.UNSTABLE_addTemporalMarkerOffset(INTAKE_RUNTIME, () -> {
            robot.doIntakeMovement(0.0d);
        });
        builder.waitSeconds(INTAKE_RUNTIME);

        // back facing backwards
        if (teamScoringElementLocation != StartPosition.TeamScoringElementLocation.CENTER) {
            // we need to avoid clipping truss
            final boolean isFacingTruss;
            switch (startPosition) {
                case BLUE_FRONT_STAGE:
                case RED_BACK_STAGE:
                    isFacingTruss = teamScoringElementLocation == StartPosition.TeamScoringElementLocation.LEFT;
                    break;
                case RED_FRONT_STAGE:
                case BLUE_BACK_STAGE:
                default:
                    isFacingTruss = teamScoringElementLocation == StartPosition.TeamScoringElementLocation.RIGHT;
            }
            if (isFacingTruss) {
                builder.back(2.0d);
            } else {
                builder.forward(1.0d);
            }
        }

        // get backstage
        switch (startPosition.getStagePosition()) {
            case BACK:
                builder.lineToConstantHeading(startPosition.getSafeMovementPose().vec());
                // we will need to turn to face infield
                // opposite of previous switch
                switch (teamScoringElementLocation) {
                    case RIGHT:
                        builder.turn(Math.toRadians(90.0d)); // counter-clockwise
                        break;
                    case LEFT:
                        builder.turn(Math.toRadians(-90.0d)); // clockwise
                        break;
                }
                switch (startPosition.getAllianceColor()) {
                    case BLUE:
                        builder.strafeLeft(7.0d);
                        break;
                    case RED:
                        builder.strafeRight(7.0d);
                        break;
                }
                builder.splineTo(new Vector2d(37.07, -36.76 * flipY), Math.toRadians(faceInDeg));
                break;
            case FRONT:
                builder
                        .lineToSplineHeading(startPosition.getSafeMovementPose())
                        .lineToSplineHeading(new Pose2d(-58.52, -60.71 * flipY, Math.toRadians(faceInDeg)))
                        .lineToConstantHeading(new Vector2d(-58.52, -12.97 * flipY)) // go forward
                        .turn(Math.toRadians(startPosition.getAllianceColor() == BLUE ? 90 : -90)) // can't spline here
                        .lineToConstantHeading(new Vector2d(34.47, -12.81 * flipY));
                break;
        }

        // align onto backboard while bringing slides up
        final double SLIDER_RUNTIME = 0.6d;
        // note: do not move basket while sliders are up until they are screwed in
        builder.UNSTABLE_addTemporalMarkerOffset(0.0, () -> {
            robot.doSliderMovement(1.0d);
        });
        builder.UNSTABLE_addTemporalMarkerOffset(0.3, () -> {
            robot.bringOuttakeVertical();
        });
        builder.UNSTABLE_addTemporalMarkerOffset(SLIDER_RUNTIME, () -> {
            robot.doSliderMovement(0.0d);
        });
        builder.waitSeconds(1.3d); // wait for dog ears to settle

        // line up onto board
        final double BOARD_X = 50.04d;
        final double OUTERMOST_SLOT_Y = -42.47;
        final double CENTERMOST_SLOT_Y = -36.57;
        final double INNERMOST_SLOT_Y = -29.21;
        // if we are blue, outermost is left
        // if we are red, outermost is right

        final double boardY;
        switch (teamScoringElementLocation) {
            case LEFT:
                switch (startPosition.getAllianceColor()) {
                    case BLUE:
                        boardY = OUTERMOST_SLOT_Y;
                        break;
                    default: // exhaustive
                    case RED:
                        boardY = INNERMOST_SLOT_Y;
                        break;
                }
                break;
            case RIGHT:
                switch (startPosition.getAllianceColor()) {
                    case BLUE:
                        boardY = INNERMOST_SLOT_Y;
                        break;
                    default: // exhaustive
                    case RED:
                        boardY = OUTERMOST_SLOT_Y;
                        break;
                }
                break;
            default: // exhaustive
            case CENTER:
                boardY = CENTERMOST_SLOT_Y;
                break;
        }

        builder.lineToSplineHeading(new Pose2d(BOARD_X, boardY * flipY, Math.toRadians(faceDroneFieldDeg)));

        // drop out pixel (by now we should be on back)
        builder.UNSTABLE_addTemporalMarkerOffset(0.0, () -> {
            robot.setOuttakeWheelSpeed(1.0f);
        });
        builder.UNSTABLE_addTemporalMarkerOffset(1.5, () -> {
            robot.setOuttakeWheelSpeed(0.0f);
        });
        builder.waitSeconds(1.0); // don't wait forever

        // move forward about 3 inches to bring slides down
        builder.forward(4.0d);

        // slides down
        // avoid automatically bring basket horizontal
        builder.UNSTABLE_addTemporalMarkerOffset(0.0, () -> {
            robot.bringOuttakeHorizontal();
        });
        builder.UNSTABLE_addTemporalMarkerOffset(0.5, () -> {
            robot.doSliderMovement(-1.0d);
        });
        builder.UNSTABLE_addTemporalMarkerOffset(0.5 + SLIDER_RUNTIME, () -> {
            robot.doSliderMovement(0.0d);
        });

        // park
        builder.forward(3.0d); // clear off board to avoid descoring
        switch (startPosition) {
            case RED_FRONT_STAGE:
            case BLUE_FRONT_STAGE:
                builder.splineTo(new Vector2d(57.51, -12.51 * flipY), Math.toRadians(faceBoardDeg));
                break;
            case RED_BACK_STAGE:
            case BLUE_BACK_STAGE:
                builder.splineTo(new Vector2d(58.12, -61.02 * flipY), Math.toRadians(faceBoardDeg));
                break;
        }

        // we are done
        builder.UNSTABLE_addTemporalMarkerOffset(0.0, () -> {
            synchronized (stateMachineLock) {
                switchStateTo(State.DONE);
            }
        });

        return builder.build();
    }


    private enum State {
        INIT,

        FIND_TSE,

        TRAJECTORY,

        DONE
    }

    private @NotNull State state = State.INIT;

    private void doStateSwitchActivity(final @NotNull State newState) {
        Objects.requireNonNull(drive);
        Objects.requireNonNull(visionPortal);

        switch (newState) {
            case FIND_TSE:
                visionPortal.setProcessorEnabled(tseProcessor, true);
                break;
            case TRAJECTORY:
                assert tseProcessor.getTeamScoringElementLocation() != null;
                final @NotNull TrajectorySequence trajectorySequence = getTrajectorySequence(tseProcessor.getTeamScoringElementLocation());
                drive.followTrajectorySequenceAsync(trajectorySequence);
                break;
            case DONE:
                this.requestOpModeStop();
                break;
        }
    }

    private void switchStateTo(final @NotNull State newState) {
        state = newState;
        doStateSwitchActivity(newState);
    }

    final @NotNull Object stateMachineLock = new Object();

    // https://learnroadrunner.com/advanced.html#finite-state-machine-following
    private void runStateMachine() {
        Objects.requireNonNull(drive);
        Objects.requireNonNull(visionPortal);

        // See auton psuedocode https://docs.google.com/document/d/1lLHZNmnYf7C67mSHjxOZpvPCSCouX_pffa1dQ7LE-PQ/edit
        // We can disable/enable proccessors as needed to save CPU cycles

        synchronized (stateMachineLock) {
            switch (state) {
                case FIND_TSE:
                    if (tseProcessor.getTeamScoringElementLocation() != null) {
                        visionPortal.setProcessorEnabled(tseProcessor, false); // done using, save cycles
                        switchStateTo(State.TRAJECTORY);
                    }
                    break;
                case TRAJECTORY:
                    if (drive.isBusy()) {
                        // trajectory is running, let's just wait for it to conclude
                        break;
                    }
            }
        }
    }

    public void runOneStepBackgroundTasks() {
        Objects.requireNonNull(drive).update();
        // if we ever add PID for the slides, it would go here too, but we don't have encoders on the sides right now

        multipleTelemetry.addData("State", state);
        multipleTelemetry.addData("Running trajectory", drive.isBusy());

        telemetry.update();
        multipleTelemetry.update();
    }

    double opModeStartTime = 0.0;

    // https://learnroadrunner.com/advanced.html#finite-state-machine-following
    @Override
    public void runOpMode() throws InterruptedException {
        initDrive();
        initVision();
        initCommon();
        opModeStartTime = getRuntime(); // the attribute startTime is actually the init time

        waitForStart();
        switchStateTo(State.FIND_TSE);

        try {
            while (!isStopRequested()) {
                // we need to yield at the start of the thread in case our code is terminated halfway through
                Thread.yield(); // don't hog resources; allow other threads to run like the vision thread(s)
                runOneStepBackgroundTasks();
                runStateMachine();
            }
        } finally {
            cleanup(); // no need to autoclose the drive nor vision, easier w/ 2 objects
        }
    }
}