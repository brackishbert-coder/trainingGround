package testing;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class main2 {


	public static final void main(String[] args) {
		Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
		BufferedImage capture;

		try {
			Thread.sleep(10000);
			
			capture = new Robot().createScreenCapture(screenRect);

			File imageFile = new File("single-screen.bmp");
			ImageIO.write(capture, "bmp", imageFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AWTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		System.out.println("Welcome to OpenCV " + Core.VERSION);
		Mat m = new Mat(5, 10, CvType.CV_8UC1, new Scalar(0));
		System.out.println("OpenCV Mat: " + m);
		Mat mr1 = m.row(1);
		mr1.setTo(new Scalar(1));
		Mat mc5 = m.col(5);
		mc5.setTo(new Scalar(5));
		System.out.println("OpenCV Mat data:\n" + m.dump());

		
		
		
		
		Mat img = loadImage("single-screen.bmp");
		Mat greyImg = new Mat();
		
		
		
		Mat threshImg = new Mat();
		Imgproc.cvtColor(img, greyImg, Imgproc.COLOR_RGB2GRAY);
		
//		Core.bitwise_not(greyImg, gray);
//		
//		Imgcodecs.imwrite("/home/wes/Wisper Tech 1.0/THEORY/GAMES/ScreenIntegration/text/ReversedGrey.jpg",
//				gray);
//		
//		
		
		Imgproc.threshold(greyImg, threshImg, 255,255, Imgproc.THRESH_BINARY);
//		Mat kernal = Mat.ones(3,3, CvType.CV_8UC1);
//		Mat morph = new Mat();
//		Mat morph1 = new Mat();
		
		
		Mat kern = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(3,3));

		Mat imgfinal = new Mat();
		Core.bitwise_not(greyImg, imgfinal,threshImg);
		Mat newimg = new Mat();
		Mat dilateimg = new Mat();
		Imgproc.threshold(imgfinal, newimg, 255,255, Imgproc.THRESH_BINARY);
		Imgproc.dilate(newimg, dilateimg, kern);
		
//		Imgproc.morphologyEx(threshImg, morph, Imgproc.MORPH_CLOSE, kernal);
//		Imgcodecs.imwrite("/home/wes/Wisper Tech 1.0/THEORY/GAMES/ScreenIntegration/text/morph1.jpg",
//				morph);
//		Imgproc.morphologyEx(morph, morph1, Imgproc.MORPH_OPEN, kernal);
//		Imgcodecs.imwrite("/home/wes/Wisper Tech 1.0/THEORY/GAMES/ScreenIntegration/text/morph2.jpg",
//				morph1);
//		kernal = Mat.ones(3,3, CvType.CV_8UC1);
//
//		Imgproc.morphologyEx(morph1, morph, Imgproc.MORPH_ERODE, kernal);
//		Imgcodecs.imwrite("/home/wes/Wisper Tech 1.0/THEORY/GAMES/ScreenIntegration/text/morph3.jpg",
//				morph);
		Mat cannyOutput = new Mat();

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(dilateimg, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		System.out.println("contours: "+contours.size());
for (int i = 0; i < contours.size(); i++) {
	MatOfPoint matOfPoint = contours.get(i);
	Rect boundingRect = Imgproc.boundingRect(matOfPoint);
	
	
	Imgproc.rectangle(img, boundingRect.br(),boundingRect.tl(), new Scalar(128, 255, 255));
	
}
		
Imgcodecs.imwrite("/home/wes/Wisper Tech 1.0/THEORY/GAMES/ScreenIntegration/text/aaaaa.jpg",
		img);
		
		double areaThresh = 0;
		MatOfPoint bigContour = null;

//		if (contours.size() == 2) {
//			contours = new ArrayList<>(Collections.singletonList(contours.get(0)));
//		} else if (contours.size() == 1) {
//			contours = new ArrayList<>(Collections.singletonList(contours.get(0)));
//
//		} else if (contours.size() <= 0) {
//			contours = new ArrayList<>();
//
//		} else {
//			contours = new ArrayList<>(Collections.singletonList(contours.get(1)));
//		}

		double area_thresh = 0;
		MatOfPoint big_contour = null;
		for (MatOfPoint c : contours) {
			double area = Imgproc.contourArea(c);
			Rect rect = Imgproc.boundingRect(c);
			double aspect = (double) rect.height / rect.width;
			if (area > area_thresh && aspect > 1) {
				big_contour = c;
				area_thresh = area;
			}
		}

		int count = 0;
		for (Iterator iterator = contours.iterator(); iterator.hasNext();) {
			MatOfPoint matOfPoint = (MatOfPoint) iterator.next();
			Rect rect = Imgproc.boundingRect(matOfPoint);
			Mat text = img.submat(rect.y, rect.y + rect.height, rect.x, rect.x + rect.width);
			
				Imgcodecs.imwrite("/home/wes/Wisper Tech 1.0/THEORY/GAMES/ScreenIntegration/text/text" + count + ".jpg",
						text);
			count++;
		}

//		if (!contours.isEmpty()&& big_contour!=null) {
//			Rect rect = Imgproc.boundingRect(big_contour);
//			Mat text = img.submat(rect.y, rect.y + rect.height, rect.x, rect.x + rect.width);
//
//			 Mat binartText = threshImg.submat(rect.y, rect.y + rect.height, rect.x, rect.x + rect.width);
//
//			boolean imwrite = Imgcodecs.imwrite("/home/wes/Wisper Tech 1.0/THEORY/GAMES/ScreenIntegration/thresh.jpg",
//					threshImg);
//			boolean imwrite2 = Imgcodecs.imwrite("/home/wes/Wisper Tech 1.0/THEORY/GAMES/ScreenIntegration/mhh.jpg",
//					morph);
//			boolean imwrite3 = Imgcodecs.imwrite("/home/wes/Wisper Tech 1.0/THEORY/GAMES/ScreenIntegration/hmm.jpg",
//					morph1);
//			boolean imwrite4 = Imgcodecs.imwrite("/home/wes/Wisper Tech 1.0/THEORY/GAMES/ScreenIntegration/text.jpg",
//					text);
//			boolean imwrite5 = Imgcodecs.imwrite("/home/wes/Wisper Tech 1.0/THEORY/GAMES/ScreenIntegration/binartText.jpg",
//				binartText);
//			boolean imwrite6 = Imgcodecs.imwrite("/home/wes/Wisper Tech 1.0/THEORY/GAMES/ScreenIntegration/greyText.jpg",
//					greyImg);
//			if (imwrite &&
//			// imwrite2&&imwrite3&&
//					imwrite4
//			// && imwrite5
//			) {
//				System.out.println("SUCESS: ");
//			}
//		} else {
//			System.out.println("No Text: ");
//		}

	}

	public static Mat loadImage(String imagePath) {
		Imgcodecs imageCodecs = new Imgcodecs();
		return imageCodecs.imread(imagePath);
	}

	public static void saveImage(Mat imageMatrix, String targetPath) {
		Imgcodecs imgcodecs = new Imgcodecs();
		imgcodecs.imwrite(targetPath, imageMatrix);
	}

	public static BufferedImage Mat2BufferedImage(Mat mat) throws IOException {
		// Encoding the image
		MatOfByte matOfByte = new MatOfByte();
		Imgcodecs.imencode(".jpg", mat, matOfByte);
		// Storing the encoded Mat in a byte array
		byte[] byteArray = matOfByte.toArray();
		// Preparing the Buffered Image
		ByteArrayInputStream in = new ByteArrayInputStream(byteArray);
		BufferedImage bufImage = ImageIO.read(in);
		return bufImage;
	}
}
