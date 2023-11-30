package org.firstinspires.ftc.teamcode.lancers.auton.fullauton;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.teamcode.lancers.LancersBotConfig;
import org.firstinspires.ftc.teamcode.lancers.auton.StartPosition;
import org.firstinspires.ftc.teamcode.lancers.util.LancersMecanumDrive;
import org.firstinspires.ftc.teamcode.lancers.util.OpModeUtil;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.tfod.TfodProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Holds common code shared between different auton modes.
 * Implements {@link org.firstinspires.ftc.robotcontroller.external.samples.ConceptDoubleVision} & uses {@link LancersMecanumDrive}
 */
// Previously contained OpenCV Code from tutorial https://www.youtube.com/watch?v=547ZUZiYfQE&t=37s
public class FullAutonOpMode extends LinearOpMode {
    private final @NotNull StartPosition startMode;

    public FullAutonOpMode(final @NotNull StartPosition startMode) {
        super();
        this.startMode = startMode;
    }

    private @Nullable LancersMecanumDrive drive = null;

    private void initDrive() {
        drive = new LancersMecanumDrive(hardwareMap);
        drive.setPoseEstimate(startMode.getStartPose());
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

    // Portal
    private @Nullable VisionPortal visionPortal = null;

    private void initVision() {
        final @NotNull WebcamName webcam = hardwareMap.get(WebcamName.class, LancersBotConfig.WEBCAM);

        visionPortal = new VisionPortal.Builder()
                .addProcessors(aprilTag, tfod)
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
                //.setAutoStopLiveView(false)

                .build();
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
        // Don't use the autoclosable part of the lancermechanumdrive
        initDrive();
        initVision();
        OpModeUtil.initMultipleTelemetry(this);

        waitForStart();

        while (!isStopRequested()) {
            Objects.requireNonNull(drive).update();
            telemetryAprilTag();
            telemetryTfod();
            telemetry.update();

            // See auton psuedocode https://docs.google.com/document/d/1lLHZNmnYf7C67mSHjxOZpvPCSCouX_pffa1dQ7LE-PQ/edit
            // We can disable/enable proccessors as needed to save CPU cycles

            // Startup code
            // TODO: detect position of TSE
            // TODO: Place purple pixel on TSE spike strip
            // TODO: Place pixel on TSE spike strip backboard

            // Main loop code
            // TODO: Build mosiacs?

            // Finalizing code (parking)
            // TODO: Park in backstage NOT colliding with another bot

            idle();
        }

        cleanup();
    }

    /**
     * Add telemetry about AprilTag detections.
     * Source: {@link org.firstinspires.ftc.robotcontroller.external.samples.ConceptDoubleVision}
     */
    private void telemetryAprilTag() {
        List<AprilTagDetection> currentDetections = aprilTag.getDetections();
        telemetry.addData("# AprilTags Detected", currentDetections.size());

        // Step through the list of detections and display info for each one.
        for (AprilTagDetection detection : currentDetections) {
            if (detection.metadata != null) {
                telemetry.addLine(String.format("\n==== (ID %d) %s", detection.id, detection.metadata.name));
                telemetry.addLine(String.format("XYZ %6.1f %6.1f %6.1f  (inch)", detection.ftcPose.x, detection.ftcPose.y, detection.ftcPose.z));
                telemetry.addLine(String.format("PRY %6.1f %6.1f %6.1f  (deg)", detection.ftcPose.pitch, detection.ftcPose.roll, detection.ftcPose.yaw));
                telemetry.addLine(String.format("RBE %6.1f %6.1f %6.1f  (inch, deg, deg)", detection.ftcPose.range, detection.ftcPose.bearing, detection.ftcPose.elevation));
            } else {
                telemetry.addLine(String.format("\n==== (ID %d) Unknown", detection.id));
                telemetry.addLine(String.format("Center %6.0f %6.0f   (pixels)", detection.center.x, detection.center.y));
            }
        }   // end for() loop

    }   // end method telemetryAprilTag()

    /**
     * Add telemetry about TensorFlow Object Detection (TFOD) recognitions.
     * Source: {@link org.firstinspires.ftc.robotcontroller.external.samples.ConceptDoubleVision}
     */
    private void telemetryTfod() {
        List<Recognition> currentRecognitions = tfod.getRecognitions();
        telemetry.addData("# Objects Detected", currentRecognitions.size());

        // Step through the list of recognitions and display info for each one.
        for (Recognition recognition : currentRecognitions) {
            double x = (recognition.getLeft() + recognition.getRight()) / 2;
            double y = (recognition.getTop() + recognition.getBottom()) / 2;

            telemetry.addData("", " ");
            telemetry.addData("Image", "%s (%.0f %% Conf.)", recognition.getLabel(), recognition.getConfidence() * 100);
            telemetry.addData("- Position", "%.0f / %.0f", x, y);
            telemetry.addData("- Size", "%.0f x %.0f", recognition.getWidth(), recognition.getHeight());
        }   // end for() loop

    }   // end method telemetryTfod()
}
