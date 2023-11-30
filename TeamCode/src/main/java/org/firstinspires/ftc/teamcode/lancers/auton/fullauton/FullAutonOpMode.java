package org.firstinspires.ftc.teamcode.lancers.auton.fullauton;

import android.graphics.Canvas;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.internal.camera.calibration.CameraCalibration;
import org.firstinspires.ftc.teamcode.lancers.LancersBotConfig;
import org.firstinspires.ftc.teamcode.lancers.auton.StartPosition;
import org.firstinspires.ftc.teamcode.lancers.auton.TeamScoringElementLocation;
import org.firstinspires.ftc.teamcode.lancers.util.LancersMecanumDrive;
import org.firstinspires.ftc.teamcode.lancers.util.OpModeUtil;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.VisionProcessor;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.tfod.TfodProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.opencv.core.Mat;

import java.util.List;
import java.util.Objects;

/**
 * Holds common code shared between different auton modes.
 * Implements {@link org.firstinspires.ftc.robotcontroller.external.samples.ConceptDoubleVision} & uses {@link LancersMecanumDrive}
 */
public class FullAutonOpMode extends LinearOpMode {
    private final @NotNull StartPosition startPosition;

    public FullAutonOpMode(final @NotNull StartPosition startPosition) {
        super();
        this.startPosition = startPosition;
    }

    private @Nullable LancersMecanumDrive drive = null;

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

    private class BasicTeamScoringElementRecognizingProcessor implements VisionProcessor { // aka the pipeline
        // adaptation of old https://github.com/LancerRobotics/CenterStage/blob/16e9bbfd05611b5ae0403141e7f121bac8492ac2/TeamCode/src/main/java/org/firstinspires/ftc/teamcode/opmode/OpenCV.java
        // Previously contained OpenCV Code from tutorial https://www.youtube.com/watch?v=547ZUZiYfQE&t=37s

        // probably will be the right size, but will be changed when initialized
        int width = 640;
        int height = 480;

        final @NotNull Object lock = new Object();

        private @Nullable TeamScoringElementLocation teamScoringElementLocation = null;

        @Override
        public void init(int width, int height, CameraCalibration calibration) {
            // Will only be called once
            this.width = width;
            this.height = height;
        }

        @Override
        public Object processFrame(Mat frame, long captureTimeNanos) {
            if (teamScoringElementLocation != null) {
                return teamScoringElementLocation; // we already know where the TSE is, no need to process
            }
            synchronized (lock) {
                startPosition.getAllianceColor().getScalar(); // TODO: find third of screen with this color
                // copy code from https://github.com/LancerRobotics/CenterStage/blob/16e9bbfd05611b5ae0403141e7f121bac8492ac2/TeamCode/src/main/java/org/firstinspires/ftc/teamcode/opmode/OpenCV.java#L65
                return null; // object returned will be passed to onDrawFrame
            }
        }

        @Override
        public void onDrawFrame(Canvas canvas, int onscreenWidth, int onscreenHeight, float scaleBmpPxToCanvasPx, float scaleCanvasDensity, Object userContext) {
            final @Nullable TeamScoringElementLocation location = (TeamScoringElementLocation) userContext; // may be null
            synchronized (lock) {
                // TODO: Annotate canvas with info from processFrame, just boxes

                // TODO: if the location isn't null, draw some text that says "TSE HERE!" or something
            }
        }

        public @Nullable TeamScoringElementLocation getTeamScoringElementLocation() {
            return TeamScoringElementLocation.CENTER;
        }
    }

    private final @NotNull BasicTeamScoringElementRecognizingProcessor tseProcessor = new BasicTeamScoringElementRecognizingProcessor();

    private @Nullable VisionPortal visionPortal = null;

    private void initVision() {
        final @NotNull WebcamName webcam = hardwareMap.get(WebcamName.class, LancersBotConfig.WEBCAM);

        visionPortal = new VisionPortal.Builder()
                .addProcessors(aprilTag, tfod, tseProcessor)
                .setCamera(webcam)

                // Choose a camera resolution. Not all cameras support all resolutions.
                //.setCameraResolution(new Size(640, 480))

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
        visionPortal.setProcessorEnabled(tseProcessor, true); // suitable for comp #1
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
        OpModeUtil.initMultipleTelemetry(this);

        waitForStart();

        while (!isStopRequested()) {
            idle(); // thread will yield whenever continue is called (0 second sleep)
            Objects.requireNonNull(drive).update();
            telemetry.update();

            // See auton psuedocode https://docs.google.com/document/d/1lLHZNmnYf7C67mSHjxOZpvPCSCouX_pffa1dQ7LE-PQ/edit
            // We can disable/enable proccessors as needed to save CPU cycles

            // first actions in auton (first 10 seconds)
            if (tseProcessor.getTeamScoringElementLocation() == null) {
                continue; // don't move from starting position until we know where the TSE is
            } else {
                visionPortal.setProcessorEnabled(tseProcessor, false); // done using, save cycles
            }
            // TODO: Place purple pixel on TSE spike strip
            // TODO: Place yellow pixel on backboard according to TSE location

            // for remainder of time until time to park
            // TODO: 3 points for backstage pixels 5 for backboard pixels, just white ones from stacks

            // TODO: Park in backstage then break loop
            // auton period is 30 seconds, start parking at 20 seconds?
        }

        cleanup(); // no need to autoclose the drive nor vision, easier w/ 2 objects
    }
}
