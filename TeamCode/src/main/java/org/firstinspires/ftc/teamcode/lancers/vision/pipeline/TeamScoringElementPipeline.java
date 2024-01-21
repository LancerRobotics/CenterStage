package org.firstinspires.ftc.teamcode.lancers.vision.pipeline;

import android.graphics.Canvas;
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import lombok.Data;
import lombok.Getter;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.internal.camera.calibration.CameraCalibration;
import org.firstinspires.ftc.teamcode.lancers.auton.TeamScoringElementLocation;
import org.firstinspires.ftc.teamcode.lancers.fullauton.FullAutonOpMode;
import org.firstinspires.ftc.teamcode.lancers.vision.VisionUtil;
import org.firstinspires.ftc.vision.VisionProcessor;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;

import static org.firstinspires.ftc.teamcode.lancers.config.Constants.DEBUG;
import static org.firstinspires.ftc.teamcode.lancers.vision.VisionUtil.makeGraphicsRect;

public class TeamScoringElementPipeline implements VisionProcessor { // aka pipeline
    private static final @NotNull DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    // adaptation of old https://github.com/LancerRobotics/CenterStage/blob/16e9bbfd05611b5ae0403141e7f121bac8492ac2/TeamCode/src/main/java/org/firstinspires/ftc/teamcode/opmode/OpenCV.java
    // Previously contained OpenCV Code from tutorial https://www.youtube.com/watch?v=547ZUZiYfQE&t=37s
    private final FullAutonOpMode opMode;
    private final @NotNull Telemetry telemetry;
    private @Nullable Scalar targetColor;


    // probably will be the right size by default, but will be changed when initialized
    int width = 640;
    int height = 480;

    final @NotNull Object processLock = new Object();
    final @NotNull Object drawLock = new Object();

    @Getter
    private @Nullable TeamScoringElementLocation teamScoringElementLocation = null;

    public TeamScoringElementPipeline(FullAutonOpMode opMode) {
        this.opMode = opMode;
        this.telemetry = new MultipleTelemetry(opMode.telemetry, FtcDashboard.getInstance().getTelemetry());
    }

    @Override
    public void init(int width, int height, CameraCalibration calibration) {
        // Will only be called once
        this.width = width;
        this.height = height;
    }

    // https://media.discordapp.net/attachments/758394776403574834/1196581089863548938/Screenshot_2024-01-15_at_5.25.56_PM.png?ex=65b825fb&is=65a5b0fb&hm=c2c4928fb3b626f0ec75705caaf153a499ed7dda07672dcfa5d16f5e8be73eaf&=&format=webp&quality=lossless&width=1722&height=1294
    @Override
    public Object processFrame(Mat frame, long captureTimeNanos) {
        try {
            synchronized (processLock) {
                assert frame.channels() == 4; // RGBA
                assert frame.type() == CvType.CV_8UC4; // 8 bit unsigned int, 4 channels
                assert frame.height() == height;
                assert frame.width() == width;

                // don't run any opencv stuff before initalization finishes, i don't think the libraries are loaded
                // until the vision portal is created
                if (targetColor == null) {
                    targetColor = opMode.startPosition.getAllianceColor().getTeamScoringElementColorHSV();
                }

                final @NotNull Rect leftRect = new Rect(1, 1, (width / 5) - 1, height - 1);
                final @NotNull Rect centerRect = new Rect(width / 5, 1, ((width / 5) * 3) - 1, height - 1);
                final @NotNull Rect rightRect = new Rect((width / 5) * 4, 1, (width / 5) - 1, height - 1);

                // first step:
                // convert entire RGBA frame to HSV
                final @NotNull Mat hsvMat = new Mat(height, width, CvType.CV_8UC3);
                Imgproc.cvtColor(frame, hsvMat, Imgproc.COLOR_RGB2HSV);

                // second step:
                // convert mat to heatmap
                final @NotNull Mat heatmapMat = new Mat(height, width, CvType.CV_8UC1);
                VisionUtil.convertToSimilarityHeatMapEuclidean(hsvMat, targetColor, heatmapMat);
                // NOTE: euclidean might be too slow, but it's the most accurate

                // third step:
                // crop heatmap to the 3 zones
                final @NotNull Mat leftMatGray = new Mat(heatmapMat, leftRect);
                final @NotNull Mat centerMatGray = new Mat(heatmapMat, centerRect);
                final @NotNull Mat rightMatGray = new Mat(heatmapMat, rightRect);

                // there are couple other things we can do now: we can either do blob detection or find the sum of each range
                // we will do the latter for speed's sake
                final double leftSum = Core.sumElems(leftMatGray).val[0];
                final double centerSum = Core.sumElems(centerMatGray).val[0];
                final double rightSum = Core.sumElems(rightMatGray).val[0];

                // copy code from https://github.com/LancerRobotics/CenterStage/blob/16e9bbfd05611b5ae0403141e7f121bac8492ac2/TeamCode/src/main/java/org/firstinspires/ftc/teamcode/opmode/OpenCV.java#L65

                if (DEBUG) {
                    telemetry.addData("leftSum", leftSum);
                    telemetry.addData("centerSum", centerSum);
                    telemetry.addData("rightSum", rightSum);
                }

                if (DEBUG) {
                    telemetry.update();
                }
                return new TSEContext(
                        null,
                        Arrays.asList(
                                new Pair<>(leftRect, leftSum),
                                new Pair<>(centerRect, centerSum),
                                new Pair<>(rightRect, rightSum)
                        )
                );
            }
        } catch (CvException e) {
            // get the line number of the error
            if (DEBUG) {
                final @NotNull StackTraceElement[] stackTrace = e.getStackTrace();
                telemetry.addData("OpenCV Error", e.getMessage());
                telemetry.addData("OpenCV Stacktrace", String.join("\n", Arrays.stream(stackTrace).map(StackTraceElement::toString).toArray(String[]::new)));
                telemetry.update();
            }
            return null;
        }
    }

    // https://deltacv.gitbook.io/eocv-sim/vision-portal/drawing-annotations-using-android-canvas
    @Override
    public void onDrawFrame(Canvas canvas, int onscreenWidth, int onscreenHeight, float scaleBmpPxToCanvasPx, float scaleCanvasDensity, Object userContext) {
        if (userContext == null) {
            return;
        }
        final @NotNull TSEContext ctx = (TSEContext) userContext;
        synchronized (drawLock) {
            for (Pair<Rect, Double> zone : ctx.getZones()) {
                final @NotNull android.graphics.Rect graphicsRect = makeGraphicsRect(zone.getValue0(), scaleBmpPxToCanvasPx);

                // translate the confidence value (0-1) to red to green
                final int red = Math.min((int) (255 * (1 - zone.getValue1())), 255);
                final int green = Math.min((int) (255 * zone.getValue1()), 255);
                final int blue = 0;

                // make a android graphics paint
                final @NotNull android.graphics.Paint boxPaint = new android.graphics.Paint();
                boxPaint.setARGB(255, red, green, blue);
                boxPaint.setStrokeWidth(5);
                boxPaint.setStyle(android.graphics.Paint.Style.STROKE);
                boxPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
                boxPaint.setTextSize(30);
                // text border
                boxPaint.setShadowLayer(1, 0, 0, 0xFF000000);

                canvas.drawRect(graphicsRect, boxPaint);

                // also draw number in center of rect
                canvas.drawText(
                        DECIMAL_FORMAT.format(zone.getValue1()), // limit to 2 decimal places
                        graphicsRect.centerX(),
                        graphicsRect.centerY(),
                        boxPaint
                );
            }

            if (ctx.getLocation() != null) {
                final @NotNull android.graphics.Paint textPaint = new android.graphics.Paint();
                textPaint.setTextSize(60);
                textPaint.setARGB(255, 255, 255, 255);
                textPaint.setStyle(android.graphics.Paint.Style.FILL);
                textPaint.setTextAlign(android.graphics.Paint.Align.CENTER);

                // draw text in
                canvas.drawText(
                        ctx.getLocation().toString(),
                        (float) canvas.getWidth() / 2,
                        (float) canvas.getHeight() / 2,
                        textPaint
                );
            }
        }
    }

    @Data
    private final static class TSEContext {
        public final @Nullable TeamScoringElementLocation location;
        public final @NotNull Collection<Pair<Rect, Double>> zones;
    }
}
