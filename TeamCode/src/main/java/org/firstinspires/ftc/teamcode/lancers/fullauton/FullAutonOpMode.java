package org.firstinspires.ftc.teamcode.lancers.fullauton;

import android.util.Size;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.lancers.auton.LancersAutonOpMode;
import org.firstinspires.ftc.teamcode.lancers.auton.StartPosition;
import org.firstinspires.ftc.teamcode.lancers.config.LancersBotConfig;
import org.firstinspires.ftc.teamcode.lancers.util.LancersMecanumDrive;
import org.firstinspires.ftc.teamcode.lancers.vision.pipeline.TeamScoringElementPipeline;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.tfod.TfodProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Holds common code shared between different auton modes.
 * Implements {@link org.firstinspires.ftc.robotcontroller.external.samples.ConceptDoubleVision} & uses {@link LancersMecanumDrive}
 */
public class FullAutonOpMode extends LancersAutonOpMode {
    private static final int AUTONOMOUS_PERIOD_LENGTH_SECONDS = 30;
    private static final int TIME_TO_PARK_SECONDS = 10;
    private static final int SAFE_CYCLING_TIME = AUTONOMOUS_PERIOD_LENGTH_SECONDS - TIME_TO_PARK_SECONDS;

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

    private enum CyclingSubState {
        FIND_STACK,
        MOVE_TO_STACK,
        PICK_UP_STACK,
        MOVE_TO_BACKBOARD,
        PLACE_STACK,
        DONE
    }

    private @NotNull CyclingSubState cyclingSubState = CyclingSubState.FIND_STACK;

    public void runOneStepCyclingStateMachine() {
        assert state == State.CYCLE;

        Objects.requireNonNull(drive);
        Objects.requireNonNull(visionPortal);

        // See auton psuedocode https://docs.google.com/document/d/1lLHZNmnYf7C67mSHjxOZpvPCSCouX_pffa1dQ7LE-PQ/edit
        // We can disable/enable proccessors as needed to save CPU cycles

        switch (cyclingSubState) {
            case FIND_STACK:
                if (!visionPortal.getProcessorEnabled(tfod)) {
                    visionPortal.setProcessorEnabled(tfod, true); // wait for stack to be found
                }
                // TODO: get to an area where the stack can be seen
                if (tfod.getRecognitions() != null) {
                    visionPortal.setProcessorEnabled(tfod, false); // done using, save cycles
                    cyclingSubState = CyclingSubState.MOVE_TO_STACK;
                }
                break;
        }
    }

    private enum State {
        INIT,

        FIND_TSE,

        PLACE_PURPLE_PIXEL__MOVE_INTO_POSITION,
        PLACE_PURPLE_PIXEL__DEPOSIT_PIXEL,

        PLACE_YELLOW_PIXEL__MOVE_INTO_POSITION_FOR_FINDING_TAG,
        PLACE_YELLOW_PIXEL__SCAN_APRILTAG_AFFIRM_POSE,
        PLACE_YELLOW_PIXEL__MOVE_ONTO_BACKBOARD,
        PLACE_YELLOW_PIXEL__SLIDE_UP,
        PLACE_YELLOW_PIXEL__DEPOSIT,

        CYCLE__MOVE_TO_SAFE_STARTING_SPOT,
        CYCLE,

        PARK__NEUTRAL_POSITION,
        PARK__NEUTRAL_POSITION_BACKSTAGE,
        PARK,

        DONE
    }

    private @NotNull State state = State.INIT;

    public void runOneStepTopLevelStateMachine() {
        Objects.requireNonNull(drive);
        Objects.requireNonNull(visionPortal);

        // See auton psuedocode https://docs.google.com/document/d/1lLHZNmnYf7C67mSHjxOZpvPCSCouX_pffa1dQ7LE-PQ/edit
        // We can disable/enable proccessors as needed to save CPU cycles

        switch (state) {
            case FIND_TSE:
                if (!visionPortal.getProcessorEnabled(tseProcessor)) {
                    visionPortal.setProcessorEnabled(tseProcessor, true); // wait for TSE to be found
                }
                if (tseProcessor.getTeamScoringElementLocation() != null) {
                    visionPortal.setProcessorEnabled(tseProcessor, false); // done using, save cycles
                    state = State.PLACE_PURPLE_PIXEL__MOVE_INTO_POSITION;
                }
                break;
            case PLACE_PURPLE_PIXEL__MOVE_INTO_POSITION:
                assert tseProcessor.getTeamScoringElementLocation() != null;
                // TODO: move in front of spike strip in question
                state = State.PLACE_PURPLE_PIXEL__DEPOSIT_PIXEL;
                break;
            case PLACE_PURPLE_PIXEL__DEPOSIT_PIXEL:
                // TODO: run intake in reverse to deposit pixel
                state = State.PLACE_YELLOW_PIXEL__MOVE_INTO_POSITION_FOR_FINDING_TAG;
                break;
            case PLACE_YELLOW_PIXEL__MOVE_INTO_POSITION_FOR_FINDING_TAG:
                // TODO: move to a place in front of the backboard
                state = State.PLACE_YELLOW_PIXEL__SCAN_APRILTAG_AFFIRM_POSE;
                break;
            case PLACE_YELLOW_PIXEL__SCAN_APRILTAG_AFFIRM_POSE:
                if (!visionPortal.getProcessorEnabled(aprilTag)) {
                    visionPortal.setProcessorEnabled(aprilTag, true); // wait for apriltag(s) to be found
                }
                // TODO: wait for the apriltag to be found, and then update the pose estimate in the drive
                // TODO: this could be unnecessary? see if we desync too much w/ fully calibrated RR
                if (true) { // when updated
                    if (visionPortal.getProcessorEnabled(aprilTag)) {
                        visionPortal.setProcessorEnabled(aprilTag, false); // done using, save cycles
                    }
                    state = State.PLACE_YELLOW_PIXEL__MOVE_ONTO_BACKBOARD;
                }
                break;
            case PLACE_YELLOW_PIXEL__MOVE_ONTO_BACKBOARD:
                // TODO: move into a position with the back of the robot against the backboard so we can slide up & deposit
                state = State.PLACE_YELLOW_PIXEL__SLIDE_UP;
                break;
            case PLACE_YELLOW_PIXEL__SLIDE_UP:
                // TODO: move the slides up about halfway
                state = State.PLACE_YELLOW_PIXEL__DEPOSIT;
                break;
            case PLACE_YELLOW_PIXEL__DEPOSIT:
                // TODO: move the basket wheels backwards to deposit the pixel
                state = State.CYCLE__MOVE_TO_SAFE_STARTING_SPOT;
                break;
            case CYCLE__MOVE_TO_SAFE_STARTING_SPOT:
                // TODO: give the backboard and other robots a large berth before beginning to cycle
                state = State.CYCLE;
                break;
            case CYCLE:
                if (opModeStartTime + SAFE_CYCLING_TIME < getRuntime()) {
                    // it isn't safe to cycle anymore, time to park
                    state = State.PARK__NEUTRAL_POSITION;
                }
                runOneStepCyclingStateMachine();
                if (state != State.CYCLE && visionPortal.getProcessorEnabled(tfod)) {
                    visionPortal.setProcessorEnabled(tfod, false); // done using, save cycles
                }
                break;
            case PARK__NEUTRAL_POSITION:
                // TODO: move to a neutral position with room to move
                state = State.PARK__NEUTRAL_POSITION_BACKSTAGE;
                break;
            case PARK__NEUTRAL_POSITION_BACKSTAGE:
                // TODO: we may already be in front of the backboard, but if we are frontstage we should spline over
                state = State.PARK;
                break;
            case PARK:
                // TODO: Park on the line backstage
                state = State.DONE;
                break;
        }
    }

    public void runOneStepBackgroundTasks() {
        telemetry.update();
        multipleTelemetry.update();

        Objects.requireNonNull(drive).update();
        // if we ever add PID for the slides, it would go here too, but we don't have encoders on the sides right now

        multipleTelemetry.addData("State", state);
        multipleTelemetry.addData("Cycling Substate", cyclingSubState);
    }

    double opModeStartTime = 0.0;

    // https://learnroadrunner.com/advanced.html#finite-state-machine-following
    @Override
    public void runOpMode() throws InterruptedException {
        state = State.INIT;
        initDrive();
        initVision();

        waitForStart();
        opModeStartTime = getRuntime(); // the attribute startTime is actually the init time
        state = State.FIND_TSE;

        while (!isStopRequested()) {
            runOneStepBackgroundTasks();
            runOneStepTopLevelStateMachine();
            if (state == State.DONE) {
                break;
            }
            Thread.yield(); // don't hog resources; allow other threads to run like the vision thread(s)
        }

        cleanup(); // no need to autoclose the drive nor vision, easier w/ 2 objects
    }
}
