package org.firstinspires.ftc.teamcode.lancers.vision;

import org.jetbrains.annotations.NotNull;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.CvType.CV_8UC3;

public final class VisionUtil {
    private final static double COLOR_SIMILARITY_THRESHOLD = 50;

    private VisionUtil() {
    }

    public static android.graphics.Rect makeGraphicsRect(Rect rect, float scaleBmpPxToCanvasPx) {
        int left = Math.round(rect.x * scaleBmpPxToCanvasPx);
        int top = Math.round(rect.y * scaleBmpPxToCanvasPx);
        int right = left + Math.round(rect.width * scaleBmpPxToCanvasPx);
        int bottom = top + Math.round(rect.height * scaleBmpPxToCanvasPx);

        return new android.graphics.Rect(left, top, right, bottom);
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

    public static void convertToSimilarityHeatMapEuclidean(Mat srcMat, Scalar targetColor, Mat destGrayscaleMat) {
        for (int i = 0; i < srcMat.rows(); i++) {
            for (int j = 0; j < srcMat.cols(); j++) {
                final @NotNull double[] pixel = srcMat.get(i, j);
                final double distance = calculateEuclideanDistance(pixel, targetColor.val);
                destGrayscaleMat.put(i, j, distance);
            }
        }
    }

    private static double simpleColorDifferenceHSV(double[] color1, double[] color2) {
        // ignore saturation and value
        // Check if each color channel is within the specified threshold
        if (Math.abs(color1[0] - color2[0]) > COLOR_SIMILARITY_THRESHOLD) {
            return 0;
        }
        return 1;
    }

    public static void convertToSimilarityHeatMapHSV(Mat srcMat, Scalar targetColor, Mat destGrayscaleMat) {
        for (int i = 0; i < srcMat.rows(); i++) {
            for (int j = 0; j < srcMat.cols(); j++) {
                final @NotNull double[] pixel = srcMat.get(i, j);
                final double similarity = simpleColorDifferenceHSV(pixel, targetColor.val);
                destGrayscaleMat.put(i, j, similarity);
            }
        }
    }
}
