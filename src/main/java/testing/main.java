package testing;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import FlatLandStructure.ViewableFlatLand;
import View.FlatLandWindow;
import flatLand.trainingGround.GameStatus;
import flatLand.trainingGround.theStudio.Camera;
import theStart.theView.TheControls.GameScreen;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class main {

	private static final int CHAIN_APPROX_SIMPLE = Imgproc.CHAIN_APPROX_SIMPLE;
	private static final int RETR_EXTERNAL = Imgproc.RETR_EXTERNAL;
	private static Camera theEyeInTheSky;
	private static int resize=4;
	private static int sigmaX=0;
	private static int sigmaY=0;
	private static int ddepth=-1;
	private static int kSizeLaplacian=7;
	private static int kernelSizeRight=3;
	private static int kernelSizeLeft=3;
	private static int pageSegMode=12;
	private static int ocrEngineMode=1;

	public static void main(String[] args) {
		GameStatus statusInstance = GameStatus.getInstance();
		// statusInstance.addStatus(GAMSTATUS.DEBUG);
		String windowName = "screen integration";
		int flatLandWidth = 685;
		int flatLandHeight = 700;
		ViewableFlatLand flatland = new ViewableFlatLand(flatLandWidth, flatLandHeight, true);

		theEyeInTheSky = new Camera(flatland, flatLandWidth, flatLandHeight, flatLandWidth, flatLandHeight, 0, 0);

		GameScreen canvas = new GameScreen(flatLandWidth, flatLandHeight, statusInstance);
		canvas.setTheEyeInTheSky(theEyeInTheSky);
		canvas.getGraphics();
		canvas.setSize(flatLandWidth, flatLandHeight);
		canvas.setPreferredSize(new Dimension(flatLandWidth, flatLandHeight));
		FlatLandWindow flatLandWindow = new FlatLandWindow(windowName, flatland, canvas, flatLandWidth, flatLandHeight);

		Graphics drarb = canvas.getGraphics();

		while (!flatLandWindow.isClose()) {
			long start = System.currentTimeMillis();
			BufferedImage capture = null;
			try {

				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				GraphicsDevice[] screens = ge.getScreenDevices();

				Rectangle allScreenBounds = new Rectangle();
				for (GraphicsDevice screen : screens) {
					Rectangle screenBounds = screen.getDefaultConfiguration().getBounds();
					allScreenBounds.width = Math.max(allScreenBounds.width, screenBounds.width);
					allScreenBounds.height += screenBounds.height;
				}

				Robot robert = new Robot();
				capture = robert.createScreenCapture(allScreenBounds);

				File imageFile = new File("single-screen.bmp");
				ImageIO.write(capture, "bmp", imageFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (AWTException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			System.load("/home/wes/TIMEVIBE/opencv-3.4.13/build/lib//libopencv_java3413.so");
			Mat img = loadImage("single-screen.bmp");
			Mat greyImg = new Mat();

			Mat threshImg = new Mat();
			Mat threshImgW = new Mat();
			Imgproc.cvtColor(img, greyImg, Imgproc.COLOR_RGB2GRAY);

			Mat gray = new Mat();

			Core.bitwise_not(greyImg, gray);


			
			

			Mat blurredImage = new Mat();
			
			Imgproc.GaussianBlur(img, blurredImage, new Size(kernelSizeRight, kernelSizeLeft), sigmaX,sigmaY);

			
			
			// Kernel size for Laplacian filter (adjust as needed)
			Mat sharpenedImage = new Mat();

			

			Imgproc.Laplacian(blurredImage, sharpenedImage, ddepth, kSizeLaplacian);

			
			Mat dst = new Mat();
			
			Imgproc.resize(sharpenedImage, dst ,new Size(sharpenedImage.width()*resize, sharpenedImage.height()*resize),resize,resize,Imgproc.INTER_AREA);
			
			
			
			BufferedImage gray1 = new BufferedImage(dst.cols(), dst.rows(), BufferedImage.TYPE_3BYTE_BGR);

			// Get the BufferedImage's backing array and copy the pixels directly into it
			byte[] data = new byte[dst.channels()*dst.cols()*dst.rows()];
			dst.get(0, 0, data);
			final byte[] targetPixels = ((DataBufferByte) gray1.getRaster().getDataBuffer()).getData();
			System.arraycopy(data, 0, targetPixels, 0, data.length);
			 
			
			processImg(gray1, 10f, 0); 

			Rect chatbox = new Rect(VariableRepository.getInstance().getPoint1(),
					VariableRepository.getInstance().getPoint2(), VariableRepository.getInstance().getPoint3(),
					VariableRepository.getInstance().getPoint4());

			Imgproc.rectangle(img, chatbox.br(),chatbox.tl(), new Scalar(255, 255, 0));

			MatOfByte mob = new MatOfByte();
			Imgcodecs.imencode(".jpg", img, mob);
			byte ba[] = mob.toArray();


			BufferedImage read;
			try {
				BufferedImage bi = ImageIO.read(new ByteArrayInputStream(ba));

				int w = bi.getWidth();
				int h = bi.getHeight();
				BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
				AffineTransform at = new AffineTransform();
				at.scale(.5, .50);
				AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
				after = scaleOp.filter(bi, after);

				drarb.drawImage(after, 0, 0, null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			long end = System.currentTimeMillis();

			long length = end - start;
			if (16 - (length / 1000) > 0)
				try {
					Thread.sleep(16 - (length / 1000));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		}
	}

	private static void processImg(BufferedImage gray1, float scaleFactor, float offset) {
		try {
			
			
			
			
			
			BufferedImage opimage 
		    = new BufferedImage(gray1.getWidth(), 
		                        gray1.getHeight(), 
		                        gray1.getType()); 
			 Graphics2D graphic 
		        = opimage.createGraphics(); 
			 graphic.drawImage(gray1, 0, 0, 
					 gray1.getWidth(), gray1.getHeight(), null); 
			 graphic.dispose(); 
			 
			 
//			RescaleOp rescale 
//		        = new RescaleOp(scaleFactor, offset, null); 
//  
//		    BufferedImage fopimage 
//		    = rescale.filter(opimage, null); 
		ImageIO 
		    .write(opimage, 
		           "jpg", 
		           new File("/home/wes/git/ScreenIntegration/ScreenIntegration/output.jpg")); 
  
		Tesseract it = new Tesseract(); 
		it.setDatapath("/home/wes/Documents/Tess4J/tessdata");
		it.setLanguage("eng");
		it.setPageSegMode(pageSegMode);
		it.setOcrEngineMode(ocrEngineMode);
		
		// doing OCR on the image 
		// and storing result in string str 
		Rectangle rectangle = new Rectangle(VariableRepository.getInstance().getPoint1()*resize, VariableRepository.getInstance().getPoint2()*resize, VariableRepository.getInstance().getPoint3()*resize,
				VariableRepository.getInstance().getPoint4()*resize);
		String str = it.doOCR(opimage, "temp.png", Arrays.asList(rectangle));
		System.out.println(str); 

		} catch (TesseractException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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
		// Storing the encoded Mat in       a byte array
		byte[] byteArray = matOfByte.toArray();
		// Preparing the Buffered Image
		ByteArrayInputStream in = new ByteArrayInputStream(byteArray);
		BufferedImage bufImage = ImageIO.read(in);
		return bufImage;
	}

}
