package testing;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImageProcessing {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        // load image
        Mat img = Imgcodecs.imread("rock.jpg");

        // convert to gray
        Mat gray = new Mat();
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);

        // threshold image
        Mat thresh = new Mat();
        Imgproc.threshold(gray, thresh, 150, 255, Imgproc.THRESH_BINARY);

        // apply morphology to clean up small white or black regions
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
        Mat morph = new Mat();
        Imgproc.morphologyEx(thresh, morph, Imgproc.MORPH_CLOSE, kernel);
        Imgproc.morphologyEx(morph, morph, Imgproc.MORPH_OPEN, kernel);

        // thin region to remove excess black border
        kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.morphologyEx(morph, morph, Imgproc.MORPH_ERODE, kernel);

        // find contours
        List<MatOfPoint> cntrs = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(morph, cntrs, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Contour filtering -- keep largest, vertically oriented object (h/w > 1)
        double areaThresh = 0;
        MatOfPoint bigContour = null;
        for (MatOfPoint c : cntrs) {
            double area = Imgproc.contourArea(c);
            Rect rect = Imgproc.boundingRect(c);
            double aspect = (double) rect.height / rect.width;
            if (area > areaThresh && aspect > 1) {
                bigContour = c;
                areaThresh = area;
            }
        }

        // extract region of text contour from image
        Rect rect = Imgproc.boundingRect(bigContour);
        Mat text = new Mat(img, rect);

        // extract region from thresholded image
        Mat binaryText = new Mat(thresh, rect);

        // write result to disk
        Imgcodecs.imwrite("rock_thresh.jpg", thresh);
        Imgcodecs.imwrite("rock_morph.jpg", morph);
        Imgcodecs.imwrite("rock_text.jpg", text);
        Imgcodecs.imwrite("rock_binary_text.jpg", binaryText);

        
    }
}