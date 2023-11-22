package org.firstinspires.ftc.teamcode.lancers.auton.fullauton;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.lancers.LancersBotConfig;
import org.firstinspires.ftc.teamcode.lancers.auton.StartPosition;
import org.firstinspires.ftc.teamcode.lancers.util.LancersMecanumDrive;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.tfod.TfodProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private final @NotNull AprilTagProcessor aprilTagProcessor = new AprilTagProcessor.Builder()
            // TODO: Camera Calibration
            .build();

    // https://ftc-docs.firstinspires.org/en/latest/programming_resources/vision/tensorflow_cs_2023/tensorflow-cs-2023.html
    // https://ftc-docs.firstinspires.org/en/latest/ftc_ml/index.html
    private final @NotNull TfodProcessor tfObjectDetector = new TfodProcessor.Builder()
            // TODO: Configure TFOD processor
            // TODO: Build TF model
            .build();

    // Portal
    private @Nullable VisionPortal visionPortal = null;

    private void initVision() {
        final @NotNull WebcamName webcam = hardwareMap.get(WebcamName.class, LancersBotConfig.WEBCAM);

        visionPortal = new VisionPortal.Builder()
                .addProcessors(aprilTagProcessor, tfObjectDetector)
                .setCamera(webcam)
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
        initDrive();
        initVision();

        waitForStart();

        if (isStopRequested()) return;

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

        cleanup();
    }
}
