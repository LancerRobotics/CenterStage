package org.firstinspires.ftc.teamcode.lancers.vision.util;

import android.graphics.Canvas;
import android.graphics.Paint;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.text.DecimalFormat;

import static org.firstinspires.ftc.teamcode.lancers.vision.util.VisionUtil.getCoordinateStream;

public final class CanvasUtil {
    // any more decimal places is unweildy
    public static final @NotNull DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    private CanvasUtil() {
    }

    public static android.graphics.Rect makeGraphicsRect(Rect rect, float scaleBmpPxToCanvasPx) {
        int left = Math.round(rect.x * scaleBmpPxToCanvasPx);
        int top = Math.round(rect.y * scaleBmpPxToCanvasPx);
        int right = left + Math.round(rect.width * scaleBmpPxToCanvasPx);
        int bottom = top + Math.round(rect.height * scaleBmpPxToCanvasPx);

        return new android.graphics.Rect(left, top, right, bottom);
    }

    private static android.graphics.Bitmap getBitmapFromMat(final @NotNull Mat mat) {
        final int width = mat.cols();
        final int height = mat.rows();
        final int[] pixels = new int[width * height];
        getCoordinateStream(mat).forEach(pair -> { // don't run in parallel, arrays aren't thread safe
            final int i = pair.getValue0();
            final int j = pair.getValue1();
            final double[] pixel = mat.get(i, j);

            if (mat.channels() >= 4) {
                pixels[i * width + j] = android.graphics.Color.argb((int) pixel[3], (int) pixel[0], (int) pixel[1], (int) pixel[2]);
            } else if (mat.channels() >= 3) {
                pixels[i * width + j] = android.graphics.Color.argb(255, (int) pixel[0], (int) pixel[1], (int) pixel[2]);
            } else if (mat.channels() >= 2) {
                pixels[i * width + j] = android.graphics.Color.argb(255, (int) pixel[0], (int) pixel[1], (int) pixel[1]);
            } else {
                pixels[i * width + j] = android.graphics.Color.argb(255, (int) pixel[0], (int) pixel[0], (int) pixel[0]);
            }
        });
        return android.graphics.Bitmap.createBitmap(pixels, width, height, android.graphics.Bitmap.Config.ARGB_8888);
    }

    private final static Paint BITMAP_PAINT = new Paint();

    static {
        BITMAP_PAINT.setStyle(Paint.Style.FILL);
        BITMAP_PAINT.setARGB(255, 255, 255, 255);
    }

    public static void drawMatOntoCanvas(final @NotNull Canvas canvas, final @NotNull android.graphics.Rect region, final @NotNull Mat mat) {
        final @NotNull android.graphics.Bitmap bitmap = getBitmapFromMat(mat);
        canvas.drawBitmap(bitmap, null, region, BITMAP_PAINT);
    }
}
