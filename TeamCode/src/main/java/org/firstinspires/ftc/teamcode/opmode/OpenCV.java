package org.firstinspires.ftc.teamcode.opmode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.opmode.auton.AutonStartMode;
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
 * Holds common OpenCV code shared between different auton modes.
 */
// https://www.youtube.com/watch?v=547ZUZiYfQE&t=37s
    //NOTE: THIS IS ONLY FOR RECOGNIZING THE COLOR RED, TO RECOGNIZE THE COLOR BLUE THIS CODE WILL HAVE TO BE SLIGHTLY MODIFIED
    //To change color, look at line 62 to change rgb
public class OpenCV extends OpMode{
    final OpMode opMode;
    final AutonStartMode startMode;

    public OpenCV(final OpMode opMode, final AutonStartMode startMode) {
        this.opMode = opMode;
        this.startMode = startMode;
    }
    OpenCvWebcam webcam = null;
    public void init() {
        WebcamName webcamName = hardwareMap.get(WebcamName.class, "webcam"); //gets webcam from hardwaremap
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        webcam = OpenCvCameraFactory.getInstance().createWebcam(webcamName, cameraMonitorViewId); //retrieves the ID of the webcam
        webcam.setPipeline(new examplePipeline()); //ExamplePipeline is a temporary placeholder name
        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() { //opens camera webcam
            @Override
            public void onOpened() {
                webcam.startStreaming(720, 720, OpenCvCameraRotation.UPRIGHT); //Sets width and height of the camera as well as its orientation
            }

            @Override
            public void onError(int errorCode) {

            }
        });
    }

    public void loop() {

    }
    class examplePipeline extends OpenCvPipeline{ //place where any variables that are members of the pipeline should be defined
    //all variables must be declared at the beginning of the pipeline
        Mat YCbCr = new Mat();
        Mat leftCrop;
        Mat rightCrop;
        double leftavgfin;
        double rightavgfin;
        Mat outPut = new Mat();
        Scalar rectColor = new Scalar (255.0, 0.0, 0.0); //RGB COLORS FOR RED
        @Override
        public Mat processFrame(Mat input) {

            telemetry.addLine("pipeline running");

            Rect leftRect = new Rect(1, 1, 359, 719); //These two lines of code set parameters for boxes of recognition inside of camera view
            Rect rightRect = new Rect(360, 1, 359, 719); //Our camera is 720 x 720, so these two lines of code have divided the field of view in half.
                                                                            //Height and width are self-explanatory, the x and y refer to the x coordinate of the top-left corner and the y coordinate of the top-left corner of the rectangle.
            input.copyTo(outPut);
            Imgproc.rectangle(outPut, leftRect, rectColor, 2);  //These two lines of code allow you to see your boundary boxes on the driver station
            Imgproc.rectangle(outPut, rightRect, rectColor, 2);

            leftCrop = YCbCr.submat(leftRect); //These submats are the subframes of the camera, their boundaries were defined in lines 68 and 69 of the code above. Their parameters can thus be changed.
            rightCrop = YCbCr.submat(rightRect);

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
                telemetry.addLine("left"); //If the average amount of color that is being sought after is larger in the left box than the right box, the robot identifies it as being on the left
            }
            else{
                telemetry.addLine("right");//otherwise, the robot knows that there is a majority of the sought after color on the right

            }

            return(outPut);
        }
    }
}
