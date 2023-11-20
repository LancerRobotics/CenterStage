package org.firstinspires.ftc.teamcode.lancers.auton;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.lancers.LancersBotConfig;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;
import org.openftc.easyopencv.OpenCvWebcam;

/**
 * Holds common code shared between different auton modes.
 * Implements OpenCV & Roadrunner.
 */
public class AutonCommon {
    // Constants
    // The webcam view window may need to be tuned for performance later
    final int WEBCAM_WIDTH = 720;
    final int WEBCAM_HEIGHT = 720;

    // Initialization code
    final OpMode opMode;
    final AutonStartMode startMode;

    public AutonCommon(final OpMode opMode, final AutonStartMode startMode) {
        this.opMode = opMode;
        this.startMode = startMode;
    }

    // OpenCV Code from tutorial https://www.youtube.com/watch?v=547ZUZiYfQE&t=37s

    OpenCvWebcam webcam = null;

    public void init() {
        final WebcamName webcamName = opMode.hardwareMap.get(WebcamName.class, LancersBotConfig.WEBCAM);
        final int cameraMonitorViewId = opMode.hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", opMode.hardwareMap.appContext.getPackageName());

        webcam = OpenCvCameraFactory.getInstance().createWebcam(webcamName, cameraMonitorViewId);
        webcam.setPipeline(new LancerBotPipeline());

        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() { //opens camera webcam
            @Override
            public void onOpened() {

                webcam.startStreaming(WEBCAM_WIDTH, WEBCAM_HEIGHT, OpenCvCameraRotation.UPRIGHT); //Sets width and height of the camera as well as its orientation
            }

            @Override
            public void onError(int errorCode) {

            }
        });
    }

    boolean loopReached = false;

    public void loop() {
        loopReached = true;
    }

    class LancerBotPipeline extends OpenCvPipeline {
        final @NotNull Scalar RED_SCALAR = new Scalar(255.0d, 0.0d, 0.0d);
        final @NotNull Scalar BLUE_SCALAR = new Scalar(0.0d, 0.0d, 255.0d);
        final Scalar allianceColor; // This will be the color

        public LancerBotPipeline() {
            super();
            switch (AutonCommon.this.startMode.getAllianceColor()) {
                case RED:
                    allianceColor = RED_SCALAR;
                    break;
                case BLUE:
                default: // already exhaustive
                    allianceColor = BLUE_SCALAR;
                    break;
            }
        }

        Mat leftCrop;
        Mat rightCrop;
        double leftavgfin;
        double rightavgfin;

        @Override
        public @NotNull Mat processFrame(@NotNull Mat input) {
            // TODO: Find which spikestrip the TSE is located on top of

            final Mat output = new Mat();
            final Mat yCbCrMat = new Mat();
            input.copyTo(output);

            // Remember! This code needs to be fast! The CPU in the control hub is a piece of garbage.
            // Do not block this code! This thread needs to be spinning for fast I/O.

            if (!AutonCommon.this.loopReached) {
                // We cannot move around until the loop is reached,
                // and until we can move around we will not be able to detect where the TSE is located.
                return output;
            }

            AutonCommon.this.opMode.telemetry.addLine("pipeline running");

            Rect leftRect = new Rect(1, 1, WEBCAM_WIDTH / 2, WEBCAM_HEIGHT); //These two lines of code set parameters for boxes of recognition inside of camera view
            Rect rightRect = new Rect(WEBCAM_WIDTH / 2, 1, WEBCAM_WIDTH / 2, WEBCAM_HEIGHT); //Our camera is 720 x 720, so these two lines of code have divided the field of view in half.
            //Height and width are self-explanatory, the x and y refer to the x coordinate of the top-left corner and the y coordinate of the top-left corner of the rectangle.
            input.copyTo(output);
            Imgproc.rectangle(output, leftRect, allianceColor, 2);  //These two lines of code allow you to see your boundary boxes on the driver station
            Imgproc.rectangle(output, rightRect, allianceColor, 2);

            leftCrop = yCbCrMat.submat(leftRect); //These submats are the subframes of the camera, their boundaries were defined in lines 68 and 69 of the code above. Their parameters can thus be changed.
            rightCrop = yCbCrMat.submat(rightRect);

            Core.extractChannel(leftCrop, leftCrop, 2); //These two lines choose which color to extract based on their YCbCr values. In this case, the YCbCr value for red is 2.
            Core.extractChannel(rightCrop, rightCrop, 2); //YCbCr (the color streaming of this camera), has three channels: Y, Cb, and Cr. There's a bunch of formulas and stuff behind them, but the extract channel asks you for a number between 0-2 for its final parameter.
            // What I got from research is that 2 represents red, and 1 represents blue, but I could be wrong about the blue.
            //The final parameter is called coi, and asks for the index of the channel. It starts from 0. https://docs.opencv.org/3.4/javadoc/org/opencv/core/Core.html

            Scalar leftavg = Core.mean(leftCrop); //takes average of the extracted channel to be used in comparison later on
            Scalar rightavg = Core.mean(rightCrop);

            leftavgfin = leftavg.val[0]; //average value is equal to the first digit of the scalar
            rightavgfin = rightavg.val[0];

            //logic compares the average amount of color shown between the two specific zones.
            //if you wanted to split it into more boxes, you would have to specify parameters at lines 66-67,
            //then modify everything else and assign different scalar values.
            //You would then modify this statement to include an else-if.
            if (leftavgfin > rightavgfin){
                AutonCommon.this.opMode.telemetry.addLine("left"); //If the average amount of color that is being sought after is larger in the left box than the right box, the robot identifies it as being on the left
            }
            else{
                AutonCommon.this.opMode.telemetry.addLine("right");//otherwise, the robot knows that there is a majority of the sought after color on the right
            }

            return output;
        }
    }
}
