package org.firstinspires.ftc.teamcode.lancers.vision.pipeline;

import android.graphics.Canvas;
import android.graphics.Paint;
import lombok.Data;
import lombok.Getter;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.internal.camera.calibration.CameraCalibration;
import org.firstinspires.ftc.teamcode.lancers.auton.StartPosition;
import org.firstinspires.ftc.teamcode.lancers.auton.full.FullAutonOpMode;
import org.firstinspires.ftc.teamcode.lancers.vision.CanvasUtil;
import org.firstinspires.ftc.teamcode.lancers.vision.VisionUtil;
import org.firstinspires.ftc.vision.VisionProcessor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.*;
import java.util.stream.Collectors;

import static org.firstinspires.ftc.teamcode.lancers.LancersConstants.DEBUG;
import static org.firstinspires.ftc.teamcode.lancers.vision.CanvasUtil.drawMatOntoCanvas;
import static org.firstinspires.ftc.teamcode.lancers.vision.CanvasUtil.makeGraphicsRect;

// on a control hub, it takes about 2.5 seconds to run this pipeline all-in
// this is pretty good considering it only needs to get 3-5 frames off before it can make a decision
public class TeamScoringElementPipeline implements VisionProcessor { // aka pipeline
    // while RGB works, it indirectly allows more noise to seep into the final heatmap.
    // HSV was way more reliable in our testing and greatly benefitted from the adding of a "low pass" (not quite low pass)
    // filter that excluded pixels that were way too far away from the target color.
    // see VisionUtil.LOW_PASS_THRESHOLD
    private static final ColorSpace COLOR_SPACE_TO_USE = ColorSpace.HSV;

    // probably will be the right size by default, but will be changed when initialized
    int width = 640;
    int height = 480;

    @Getter
    private @Nullable StartPosition.TeamScoringElementLocation teamScoringElementLocation = null;

    private final FullAutonOpMode opMode;
    private final @NotNull Telemetry telemetry;

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
        assert SUBREGION_COUNT >= 3; // must be at least 3
    }

    private @NotNull Rect getZoneForLocation(final @NotNull StartPosition.TeamScoringElementLocation location) {
        switch (location) {
            case LEFT:
                return new Rect(1, 1, (width / SUBREGION_COUNT) - 1, height - 1);
            case CENTER:
                return new Rect(
                        width / SUBREGION_COUNT,
                        height / 3, // don't include the top and bottom of the middle, they don't include the spike strip
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
                // mat is already RGBA, no need to convert
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
        // most processing is done in this method
        // we process in parallel while we can

        final @NotNull Mat heatmapMat = getHeatmap(frame);

        // NOTE: currently, we convert the mat to a heatmap BEFORE we chop it up into rectangles
        // this is fine because our rectangles cover the entire screen. if we ever edit the rectangles to only cover
        // part of the screen, then we should change the order of this code.

        // third step:
        // crop heatmap to the 3 zones
        final @NotNull List<FrameOutputContext.CalculatedDetectionZone> zoneList =
                Arrays.stream(StartPosition.TeamScoringElementLocation.values())
                        .parallel() // evidently, opencv is thread safe
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

    private static final long CONFIDENCE_THRESHOLD_NANOS = 1_000_000_000L; // 1 second
    private long lastKnownLocationTimeNanos = 0;

    private @Nullable StartPosition.TeamScoringElementLocation lastKnownLocation = null;

    private static final long FRAMES_SINCE_LAST_KNOWN_LOCATION_THRESHOLD = 2;
    // make sure we detect the same thing twice, so that it isn't a fluke
    private long framesSinceLastKnownLocation = 0;

    @Contract(pure = false) // there is a side effect
    public boolean findIfEstimateIsGood(final @Nullable StartPosition.TeamScoringElementLocation tse, final long captureTimeNanos) {
        // with both the estimated location and the timestamp of when the frame was captured,
        // we can check to see if we got the same answer for at least 1 second
        // if we did, we can be confident that the answer is correct (return true)
        // if we didn't, we can't be confident that the answer is correct (return false)

        // if the estimated location is null, we can't be confident that the answer is correct,
        // and we should clear our answer
        if (lastKnownLocation != tse) {
            lastKnownLocation = tse;
            lastKnownLocationTimeNanos = captureTimeNanos;
            framesSinceLastKnownLocation = 0;
            return false; // we need to wait longer
        } else {
            framesSinceLastKnownLocation++;
            // if the estimated location is the same as the last known location,
            // we can be confident that the answer is correct if it has remanined the same for a second
            return (framesSinceLastKnownLocation < FRAMES_SINCE_LAST_KNOWN_LOCATION_THRESHOLD)
                    && ((captureTimeNanos - lastKnownLocationTimeNanos) >= CONFIDENCE_THRESHOLD_NANOS);
        }
    }

    final @NotNull Object processLock = new Object();

    // https://media.discordapp.net/attachments/758394776403574834/1196581089863548938/Screenshot_2024-01-15_at_5.25.56_PM.png?ex=65b825fb&is=65a5b0fb&hm=c2c4928fb3b626f0ec75705caaf153a499ed7dda07672dcfa5d16f5e8be73eaf&=&format=webp&quality=lossless&width=1722&height=1294
    @Override
    public Object processFrame(Mat frame, long captureTimeNanos) {
        // we must synchronize this method because we are using shared resources,
        // specifically for the findIfEstimateIsGood method
        synchronized (processLock) {
            try {
                // these are some known attributes of the mat
                assert frame.channels() == 4; // RGBA
                assert frame.type() == CvType.CV_8UC4; // 8 bit unsigned int, 4 channels
                assert frame.height() == height;
                assert frame.width() == width;

                final @NotNull FrameOutputContext ctx = computeContextFromFrame(frame);
                final @Nullable StartPosition.TeamScoringElementLocation tse = ctx.getLocation();
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

    final @NotNull Object drawLock = new Object();

    // https://deltacv.gitbook.io/eocv-sim/vision-portal/drawing-annotations-using-android-canvas
    @Override
    public void onDrawFrame(Canvas canvas, int onscreenWidth, int onscreenHeight, float scaleBmpPxToCanvasPx, float scaleCanvasDensity, Object userContext) {
        // unknown if we need to synchronize this method, but other implementations do so it is best to err on the side of caution
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
            final double confidence = zone.getRelativeLitRatio(ctx);

            final @NotNull android.graphics.Rect graphicsRect = makeGraphicsRect(zone.getRect(), scaleBmpPxToCanvasPx);

            // draw mats first so they go behind the boxes
            drawMatOntoCanvas(canvas, graphicsRect, zone.getGrayscaleMat());

            // translate the confidence value (0-1) to red to green
            // clamped between [0, 255] in case confidence value breaches [0, 1]
            final @NotNull Paint boxPaint = getBoxPaint(confidence);

            canvas.drawRect(graphicsRect, boxPaint);

            // also draw number in center of rect w/ confidence
            canvas.drawText(
                    CanvasUtil.DECIMAL_FORMAT.format(confidence), // limit to 2 decimal places
                    graphicsRect.centerX(),
                    graphicsRect.centerY(),
                    boxPaint
            );
        }
    }

    private static @NotNull Paint getBoxPaint(double confidence) {
        final int red = Math.max(Math.min((int) (255 * (1 - confidence)), 255), 0);
        final int green = Math.max(Math.min((int) (255 * confidence), 255), 0);
        final int blue = 0;

        // make a android graphics paint
        final @NotNull Paint boxPaint = new Paint();
        boxPaint.setARGB(255, red, green, blue);
        boxPaint.setStrokeWidth(5);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setTextAlign(Paint.Align.CENTER);
        boxPaint.setTextSize(30);

        // text border
        boxPaint.setShadowLayer(1, 0, 0, 0xFF000000);
        return boxPaint;
    }

    private enum ColorSpace {
        RGB, // known good
        HSV // known good
    }

    @Data
    private final static class FrameOutputContext {
        public final @NotNull Collection<CalculatedDetectionZone> zones;

        /**
         * @return location with highest relative lit ratio
         */
        private @Nullable StartPosition.TeamScoringElementLocation getLocation() {
            // return the location with the highest confidence if the confidence is above a threshold
            // otherwise, return null
            final @NotNull Optional<FrameOutputContext.CalculatedDetectionZone> maxConfidenceZone = zones.stream().max(Comparator.comparingDouble((z) -> z.getRelativeLitRatio(this)));
            if (maxConfidenceZone.isPresent()) {
                final @NotNull FrameOutputContext.CalculatedDetectionZone detectionZone = maxConfidenceZone.get();
                return detectionZone.getLocation();
            }
            return null;
        }

        @Data
        private final static class CalculatedDetectionZone {
            public final @NotNull Rect rect;
            public final @NotNull StartPosition.TeamScoringElementLocation location;
            public final double sum;
            public final @NotNull Mat grayscaleMat; // potential memory leak. don't let these objects stick around

            /**
             * @return the proportion of the zone that is lit on the grayscale heatmap
             */
            public double getLitRatio() {
                return sum / rect.area();
            }

            /**
             * @return the proportion of the zone that is lit on the grayscale heatmap
             * relative to the other zones
             */
            public double getRelativeLitRatio(final @NotNull FrameOutputContext ctx) {
                // very simple, just shows what proportion of the color space is taken up by this zone
                // guaranteed to be between 0 and 1
                // similar to a "confidence" value
                return getLitRatio() / ctx.getZones().stream().mapToDouble(CalculatedDetectionZone::getLitRatio).sum();
            }
        }
    }
}
