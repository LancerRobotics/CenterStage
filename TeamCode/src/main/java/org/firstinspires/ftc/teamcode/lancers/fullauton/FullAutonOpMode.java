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

    @Override
    public void runOpMode() throws InterruptedException {
        initDrive();
        initVision();

        waitForStart();
        final double opModeStartTime = getRuntime(); // the attribute startTime is actually the init time
        while (!isStopRequested()) {
            idle(); // thread will yield whenever continue is called (0 second sleep)
            Objects.requireNonNull(drive).update();
            Objects.requireNonNull(visionPortal); // for type hinting
            telemetry.update();
            multipleTelemetry.update();

            if (opModeStartTime + AUTONOMOUS_PERIOD_LENGTH_SECONDS < getRuntime()) {
                break; // break out of loop if we are out of time; get ready to assume teleop
            }

            // See auton psuedocode https://docs.google.com/document/d/1lLHZNmnYf7C67mSHjxOZpvPCSCouX_pffa1dQ7LE-PQ/edit
            // We can disable/enable proccessors as needed to save CPU cycles

            // after we reach each "checkpoint," the loop should be continued
            // loop can be broken after we are done with our auton and ready to assume teleop

            // first actions in auton (first 10 seconds)
            if (tseProcessor.getTeamScoringElementLocation() == null) {
                visionPortal.setProcessorEnabled(tseProcessor, true); // wait for TSE to be found
                continue; // don't move from starting position until we know where the TSE is
            } else {
                multipleTelemetry.addData("TSE Location", tseProcessor.getTeamScoringElementLocation().name());
                visionPortal.setProcessorEnabled(tseProcessor, false); // done using, save cycles
            }

            // TODO: Place purple pixel on TSE spike strip

            // TODO: Place yellow pixel on backboard according to TSE location
            // -- TODO: move robot into position where it can see apriltags on backboard
            // -- TODO: enable apriltag processor
            // -- TODO: wait to find apriltag
            // -- TODO: place apriltag

            // cycling: for remainder of time until time to park
            if (opModeStartTime + SAFE_CYCLING_TIME > getRuntime()) {
                // TODO: 3 points for backstage pixels 5 for backboard pixels, just white ones from stacks
                // will need to use a state machine to track this,
                // also input shaping to get bot back on track if it is crashed into
                continue;
            }

            // TODO: Park in backstage then break loop
            // depending on current position of bot, follow different paths to park
            break;
        }

        cleanup(); // no need to autoclose the drive nor vision, easier w/ 2 objects
    }
}
