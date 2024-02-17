package org.firstinspires.ftc.teamcode.lancers.vision.util;

import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.opencv.core.CvType.CV_8UC3;

public final class VisionUtil {
    private VisionUtil() {
    }

    public static @NotNull Stream<Pair<Integer, Integer>> getCoordinateStream(final @NotNull Mat mat) {
        return IntStream.range(0, mat.rows()).boxed().flatMap(i -> IntStream.range(0, mat.cols()).mapToObj(j -> new Pair<>(i, j)));
    }

    public static @NotNull Scalar rgbScalarToHSVScalar(final @NotNull Scalar rgbScalar) {
        // translate this C++ to Java
        // https://stackoverflow.com/questions/43217964/opencv-convert-scalar-to-different-color-space
        // Scalar ScalarHSV2BGR(uchar H, uchar S, uchar V) {
        //    Mat rgb;
        //    Mat hsv(1,1, CV_8UC3, Scalar(H,S,V));
        //    cvtColor(hsv, rgb, CV_HSV2BGR);
        //    return Scalar(rgb.data[0], rgb.data[1], rgb.data[2]);
        //}

        final @NotNull Mat rgbMat = new Mat(1, 1, CV_8UC3, rgbScalar);
        final @NotNull Mat hsvMat = new Mat(1, 1, CV_8UC3);
        Imgproc.cvtColor(rgbMat, hsvMat, Imgproc.COLOR_RGB2HSV);
        return new Scalar(hsvMat.get(0, 0));
    }

    public static double calculateEuclideanDistance(double[] color1, double[] color2) {
        // Euclidean distance formula
        return Math.sqrt(Math.pow(color1[0] - color2[0], 2)
                + Math.pow(color1[1] - color2[1], 2)
                + Math.pow(color1[2] - color2[2], 2));
    }

    public static void convertToSimilarityHeatMapEuclideanRGB(Mat srcMat, Scalar targetColor, Mat destGrayscaleMat) {
        getCoordinateStream(srcMat).parallel().forEach(pair -> {
            final int i = pair.getValue0();
            final int j = pair.getValue1();
            final @NotNull double[] pixel = srcMat.get(i, j);
            final double similarity = 255.0d - calculateEuclideanDistance(pixel, targetColor.val);
            destGrayscaleMat.put(i, j, similarity);
        });
    }

    private final static double OPENCV_HUE_MAX = 179.0d;  // OpenCV hue only goes to 179
    // if the left coefficient of this value is 0.2, then it means that the color can be 20% different

    // https://stackoverflow.com/questions/35113979/calculate-distance-between-colors-in-hsv-space
    private static double colorDifferenceHSVNormalized0_1(double[] color1, double[] color2) {
        final double h1 = color1[0];
        final double h2 = color2[0];

        // remember, 0 and 360 degrees are the same hue, so we will account for that by checking which is closer
        final double hueDifferenceNormalized0_1 = Math.min(Math.abs(h1 - h2), OPENCV_HUE_MAX - Math.abs(h1 - h2)) / OPENCV_HUE_MAX;

        final double s1 = color1[1];
        final double s2 = color2[1];

        final double saturationDifferenceNormalized0_1 = Math.abs(s1 - s2) / 255.0d;

        final double v1 = color1[2];
        final double v2 = color2[2];

        final double valueDifferenceNormalized0_1 = Math.abs(v1 - v2) / 255.0d;

        return Math.sqrt(Math.pow(hueDifferenceNormalized0_1, 2) +
                Math.pow(saturationDifferenceNormalized0_1, 2) +
                Math.pow(valueDifferenceNormalized0_1, 2));
    }

    private static final double LOW_PASS_THRESHOLD = 100.0d;

    private static double getGrayscaleColorFromDifference(final double normalizedDifference0_1) {
        final double grayscaleColor = 255.0d * (1.0d - normalizedDifference0_1);
        return grayscaleColor > LOW_PASS_THRESHOLD ? grayscaleColor : 0.0d;
    }

    public static void convertToSimilarityHeatMapHSV(Mat srcMat, Scalar targetColor, Mat destGrayscaleMat) {
        getCoordinateStream(srcMat).parallel().forEach(pair -> {
            final int i = pair.getValue0();
            final int j = pair.getValue1();
            final @NotNull double[] pixel = srcMat.get(i, j);
            final double differenceHSV = colorDifferenceHSVNormalized0_1(pixel, targetColor.val);
            final double grayscaleColor = getGrayscaleColorFromDifference(differenceHSV);
            destGrayscaleMat.put(i, j, grayscaleColor);
        });
    }
}
