package org.firstinspires.ftc.teamcode.lancers.vision;

import android.graphics.Canvas;
import android.graphics.Paint;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import static org.firstinspires.ftc.teamcode.lancers.vision.VisionUtil.getCoordinateStream;

public final class CanvasUtil {
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
        getCoordinateStream(mat).forEach(pair -> {
            final int i = pair.getValue0();
            final int j = pair.getValue1();
            final int pixel = (int) mat.get(i, j)[0];
            pixels[i * width + j] = android.graphics.Color.argb(255, pixel, pixel, pixel);
        });
        return android.graphics.Bitmap.createBitmap(pixels, width, height, android.graphics.Bitmap.Config.ARGB_8888);
    }

    final static Paint paint = new Paint();

    static {
        paint.setStyle(Paint.Style.FILL);
        paint.setARGB(255, 255, 255, 255);
    }

    public static void drawMatOntoCanvas(final @NotNull Canvas canvas, final @NotNull android.graphics.Rect region, final @NotNull Mat mat) {
        final @NotNull android.graphics.Bitmap bitmap = getBitmapFromMat(mat);
        canvas.drawBitmap(bitmap, null, region, paint);
    }
}
