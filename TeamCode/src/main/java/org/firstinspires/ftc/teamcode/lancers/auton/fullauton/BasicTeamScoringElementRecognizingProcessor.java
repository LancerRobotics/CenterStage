package org.firstinspires.ftc.teamcode.lancers.auton.fullauton;

import android.graphics.Canvas;
import org.firstinspires.ftc.robotcore.internal.camera.calibration.CameraCalibration;
import org.firstinspires.ftc.teamcode.lancers.auton.TeamScoringElementLocation;
import org.firstinspires.ftc.vision.VisionProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

class BasicTeamScoringElementRecognizingProcessor implements VisionProcessor { // aka pipeline
    // adaptation of old https://github.com/LancerRobotics/CenterStage/blob/16e9bbfd05611b5ae0403141e7f121bac8492ac2/TeamCode/src/main/java/org/firstinspires/ftc/teamcode/opmode/OpenCV.java
    // Previously contained OpenCV Code from tutorial https://www.youtube.com/watch?v=547ZUZiYfQE&t=37s

    private final FullAutonOpMode opMode;
    // probably will be the right size, but will be changed when initialized
    int width = 640;
    int height = 480;

    final @NotNull Object lock = new Object();

    private @Nullable TeamScoringElementLocation teamScoringElementLocation = null;

    public BasicTeamScoringElementRecognizingProcessor(FullAutonOpMode opMode) {
        this.opMode = opMode;
    }

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
            // EK does this by
            final @NotNull Rect leftRect = new Rect(1, 1, width / 3, height);
            final @NotNull Rect centerRect = new Rect(width, 1, width / 3, height);
            final @NotNull Rect rightRect = new Rect(width * 2, 1, width / 3, height);

            final @NotNull Mat leftMat = frame.submat(leftRect);
            final @NotNull Mat centerMat = frame.submat(centerRect);
            final @NotNull Mat rightMat = frame.submat(rightRect);

            final @NotNull Mat centerColorMap = new Mat();
            final @NotNull Mat rightColorMap = new Mat();
            final @NotNull Mat leftColorMap = new Mat();

            opMode.startPosition.getAllianceColor().getScalar(); // TODO: find third of screen with this color
            // copy code from https://github.com/LancerRobotics/CenterStage/blob/16e9bbfd05611b5ae0403141e7f121bac8492ac2/TeamCode/src/main/java/org/firstinspires/ftc/teamcode/opmode/OpenCV.java#L65
            return null; // object returned will be passed to onDrawFrame
        }
    }

    @Override
    public void onDrawFrame(Canvas canvas, int onscreenWidth, int onscreenHeight, float scaleBmpPxToCanvasPx, float scaleCanvasDensity, Object userContext) {
        final @Nullable TeamScoringElementLocation location = (TeamScoringElementLocation) userContext; // may be null
        synchronized (lock) {


            // TODO: if the location isn't null, draw some text that says "TSE HERE!" or something
        }
    }

    public @Nullable TeamScoringElementLocation getTeamScoringElementLocation() {
        return TeamScoringElementLocation.CENTER;
    }
}
