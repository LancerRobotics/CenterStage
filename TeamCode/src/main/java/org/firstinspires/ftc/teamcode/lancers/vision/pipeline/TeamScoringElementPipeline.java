package org.firstinspires.ftc.teamcode.lancers.vision.pipeline;

import android.graphics.Canvas;
import lombok.Data;
import lombok.Getter;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.internal.camera.calibration.CameraCalibration;
import org.firstinspires.ftc.teamcode.lancers.auton.TeamScoringElementLocation;
import org.firstinspires.ftc.teamcode.lancers.fullauton.FullAutonOpMode;
import org.firstinspires.ftc.teamcode.lancers.vision.VisionUtil;
import org.firstinspires.ftc.vision.VisionProcessor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.firstinspires.ftc.teamcode.lancers.config.Constants.DEBUG;
import static org.firstinspires.ftc.teamcode.lancers.vision.CanvasUtil.drawMatOntoCanvas;
import static org.firstinspires.ftc.teamcode.lancers.vision.CanvasUtil.makeGraphicsRect;

public class TeamScoringElementPipeline implements VisionProcessor { // aka pipeline
    private static final @NotNull DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
    private static final ColorSpace COLOR_SPACE_TO_USE = ColorSpace.HSV; // can be switched between RGB and HSV for testing

    // adaptation of old https://github.com/LancerRobotics/CenterStage/blob/16e9bbfd05611b5ae0403141e7f121bac8492ac2/TeamCode/src/main/java/org/firstinspires/ftc/teamcode/opmode/OpenCV.java
    // Previously contained OpenCV Code from tutorial https://www.youtube.com/watch?v=547ZUZiYfQE&t=37s
    private final FullAutonOpMode opMode;
    private final @NotNull Telemetry telemetry;


    // probably will be the right size by default, but will be changed when initialized
    int width = 640;
    int height = 480;

    final @NotNull Object processLock = new Object();
    final @NotNull Object drawLock = new Object();

    @Getter
    private @Nullable TeamScoringElementLocation teamScoringElementLocation = null;

    public TeamScoringElementPipeline(FullAutonOpMode opMode) {
        this.opMode = opMode;
        this.telemetry = opMode.multipleTelemetry;
    }

    @Override
    public void init(int width, int height, CameraCalibration calibration) {
        // Will only be called once
        this.width = width;
        this.height = height;
    }

    private static final int SUBREGION_COUNT = 5; // 3 has the right region clip into the center line, 5 is better

    static {
        assert SUBREGION_COUNT % 2 == 1; // must be odd
    }

    private @NotNull Rect getZoneForLocation(final @NotNull TeamScoringElementLocation location) {
        switch (location) {
            case LEFT:
                return new Rect(1, 1, (width / SUBREGION_COUNT) - 1, height - 1);
            case CENTER:
                return new Rect(
                        width / SUBREGION_COUNT,
                        height / 3,
                        ((width / SUBREGION_COUNT) * (SUBREGION_COUNT - 2)) - 1,
                        (height / 3) - 1
                );
            case RIGHT:
                return new Rect((width / SUBREGION_COUNT) * (SUBREGION_COUNT - 1), 1, (width / SUBREGION_COUNT) - 1, height - 1);
            default:
                throw new IllegalArgumentException("Unknown location: " + location);
        }
    }

    private @NotNull Scalar getTargetColor() {
        switch (COLOR_SPACE_TO_USE) {
            case RGB:
                return opMode.startPosition.getAllianceColor().getTeamScoringElementColorRGB();
            case HSV:
                return opMode.startPosition.getAllianceColor().getTeamScoringElementColorHSV();
            default:
                throw new IllegalArgumentException("Unknown color space: " + COLOR_SPACE_TO_USE);
        }
    }

    private @NotNull Mat getHeatmap(final @NotNull Mat frame) {
        switch (COLOR_SPACE_TO_USE) {
            case RGB:
                final @NotNull Mat heatmap = new Mat(height, width, CvType.CV_8UC1);
                VisionUtil.convertToSimilarityHeatMapEuclideanRGB(frame, getTargetColor(), heatmap);
                return heatmap;
            case HSV:
                final @NotNull Mat hsvMat = new Mat(height, width, CvType.CV_8UC3);
                Imgproc.cvtColor(frame, hsvMat, Imgproc.COLOR_RGB2HSV);
                final @NotNull Mat heatmapMat = new Mat(height, width, CvType.CV_8UC1);
                VisionUtil.convertToSimilarityHeatMapHSV(hsvMat, getTargetColor(), heatmapMat);
                return heatmapMat;
            default:
                throw new IllegalArgumentException("Unknown color space: " + COLOR_SPACE_TO_USE);
        }
    }

    private @NotNull FrameOutputContext computeContextFromFrame(final @NotNull Mat frame) {
        final @NotNull Mat heatmapMat = getHeatmap(frame);

        // NOTE: currently, we convert the mat to a heatmap BEFORE we chop it up into rectangles
        // this is fine because our rectangles cover the entire screen. if we ever edit the rectangles to only cover
        // part of the screen, then we should change the order of this code.

        // third step:
        // crop heatmap to the 3 zones
        final @NotNull List<FrameOutputContext.CalculatedDetectionZone> zoneList =
                Arrays.stream(TeamScoringElementLocation.values())
                        .parallel()
                        .map(
                                (location) -> {
                                    final @NotNull Rect rect = getZoneForLocation(location);
                                    final @NotNull Mat submat = heatmapMat.submat(rect);
                                    final double sum = Core.sumElems(submat).val[0];
                                    return new FrameOutputContext.CalculatedDetectionZone(rect, location, sum, submat);
                                }
                        )
                        .collect(Collectors.toList());

        // log
        if (DEBUG) {
            for (@NotNull FrameOutputContext.CalculatedDetectionZone zone : zoneList) {
                telemetry.addData("Zone " + zone.getLocation().name(), zone.getSum());
            }
        }

        // return
        return new FrameOutputContext(Collections.unmodifiableSet(new HashSet<>(zoneList)));
    }

    private static final double CONFIDENCE_THRESHOLD = 0.0; // unknown if required

    private static final long CONFIDENCE_THRESHOLD_NANOS = 1_000_000_000L; // 1 second
    private long lastKnownLocationTimeNanos = 0;
    private @Nullable TeamScoringElementLocation lastKnownLocation = null;
    private static final long FRAMES_SINCE_LAST_KNOWN_LOCATION_THRESHOLD = 3;
    private long framesSinceLastKnownLocation = 0;

    @Contract(pure = false) // there is a side effect
    public boolean findIfEstimateIsGood(final @Nullable TeamScoringElementLocation tse, final long captureTimeNanos) {
        // with both the estimated location and the timestamp of when the frame was captured,
        // we can check to see if we got the same answer for at least 1 second
        // if we did, we can be confident that the answer is correct (return true)
        // if we didn't, we can't be confident that the answer is correct (return false)

        // if the estimated location is null, we can't be confident that the answer is correct,
        // and we should clear our answer
        if (lastKnownLocation != tse) {
            lastKnownLocation = tse;
            lastKnownLocationTimeNanos = captureTimeNanos;
            return false; // we need to wait longer
        } else {
            framesSinceLastKnownLocation++;
            if (framesSinceLastKnownLocation < FRAMES_SINCE_LAST_KNOWN_LOCATION_THRESHOLD) {
                return false; // we need to wait longer
            }
            // if the estimated location is the same as the last known location,
            // we can be confident that the answer is correct if it has remanined the same for a second
            return (captureTimeNanos - lastKnownLocationTimeNanos) >= CONFIDENCE_THRESHOLD_NANOS;
        }
    }

    // https://media.discordapp.net/attachments/758394776403574834/1196581089863548938/Screenshot_2024-01-15_at_5.25.56_PM.png?ex=65b825fb&is=65a5b0fb&hm=c2c4928fb3b626f0ec75705caaf153a499ed7dda07672dcfa5d16f5e8be73eaf&=&format=webp&quality=lossless&width=1722&height=1294
    @Override
    public Object processFrame(Mat frame, long captureTimeNanos) {
        synchronized (processLock) {
            try {
                // these are some known attributes of the mat
                assert frame.channels() == 4; // RGBA
                assert frame.type() == CvType.CV_8UC4; // 8 bit unsigned int, 4 channels
                assert frame.height() == height;
                assert frame.width() == width;

                final @NotNull FrameOutputContext ctx = computeContextFromFrame(frame);
                final @Nullable TeamScoringElementLocation tse = ctx.getLocation();
                final boolean isEstimateGood = findIfEstimateIsGood(tse, captureTimeNanos);

                if (isEstimateGood) {
                    teamScoringElementLocation = tse;
                }

                return ctx;
            } catch (CvException e) {
                // get the line number of the error
                if (DEBUG) {
                    final @NotNull StackTraceElement[] stackTrace = e.getStackTrace();
                    telemetry.addData("OpenCV Error", e.getMessage());
                    telemetry.addData("OpenCV Stacktrace", String.join("\n", Arrays.stream(stackTrace).map(StackTraceElement::toString).toArray(String[]::new)));
                }
                return null;
            }
        }
    }

    // https://deltacv.gitbook.io/eocv-sim/vision-portal/drawing-annotations-using-android-canvas
    @Override
    public void onDrawFrame(Canvas canvas, int onscreenWidth, int onscreenHeight, float scaleBmpPxToCanvasPx, float scaleCanvasDensity, Object userContext) {
        synchronized (drawLock) {
            if (userContext == null) {
                return;
            }

            final @NotNull FrameOutputContext ctx = (FrameOutputContext) userContext;

            drawZones(canvas, scaleBmpPxToCanvasPx, ctx);
        }
    }

    private static void drawZones(Canvas canvas, float scaleBmpPxToCanvasPx, @NotNull FrameOutputContext ctx) {
        for (@NotNull FrameOutputContext.CalculatedDetectionZone zone : ctx.getZones()) {
            final double confidence = zone.getConfidenceRelative(ctx);

            final @NotNull android.graphics.Rect graphicsRect = makeGraphicsRect(zone.getRect(), scaleBmpPxToCanvasPx);

            drawMatOntoCanvas(canvas, graphicsRect, zone.getGrayscaleMat());

            // translate the confidence value (0-1) to red to green
            final int red = Math.min((int) (255 * (1 - confidence)), 255);
            final int green = Math.min((int) (255 * confidence), 255);
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

            // also draw number in center of rect w/ confidence
            canvas.drawText(
                    DECIMAL_FORMAT.format(confidence), // limit to 2 decimal places
                    graphicsRect.centerX(),
                    graphicsRect.centerY(),
                    boxPaint
            );
        }
    }

    private enum ColorSpace {
        RGB,
        HSV;
    }

    @Data
    private final static class FrameOutputContext {
        public final @NotNull Collection<CalculatedDetectionZone> zones;

        public @NotNull Map<TeamScoringElementLocation, CalculatedDetectionZone> getZoneMap() {
            return zones.stream()
                    .collect(Collectors.toMap(CalculatedDetectionZone::getLocation, (a) -> a));
        }

        private @Nullable TeamScoringElementLocation getLocation() {
            // return the location with the highest confidence if the confidence is above a threshold
            // otherwise, return null
            final @NotNull Optional<FrameOutputContext.CalculatedDetectionZone> maxConfidenceZone = zones.stream().max(Comparator.comparingDouble((z) -> z.getConfidenceRelative(this)));
            if (maxConfidenceZone.isPresent()) {
                final @NotNull FrameOutputContext.CalculatedDetectionZone detectionZone = maxConfidenceZone.get();
                if (detectionZone.getConfidenceRelative(this) >= CONFIDENCE_THRESHOLD) {
                    return detectionZone.getLocation();
                }
            }
            return null;
        }

        @Data
        private final static class CalculatedDetectionZone {
            public final @NotNull Rect rect;
            public final @NotNull TeamScoringElementLocation location;
            public final double sum;

            public final @NotNull Mat grayscaleMat;

            public double getNumberOfPixels() {
                return rect.area();
            }

            public double getRelativeSum() {
                // while this is appropriate for adjusting the sum relative to the size of the rectangle,
                // it is important to note that the team scoring element always remains the same size, and may actually
                // be smaller for the center rectangle where the block is bigger.
                return sum / getNumberOfPixels();
            }

            public double getConfidenceRelative(final @NotNull FrameOutputContext ctx) {
                // TODO: test this
                // very simple, just shows what proportion of the color space
                // guaranteed to be between 0 and 1
                return getRelativeSum() / ctx.getZones().stream().mapToDouble(CalculatedDetectionZone::getRelativeSum).sum();
            }
        }
    }
}
