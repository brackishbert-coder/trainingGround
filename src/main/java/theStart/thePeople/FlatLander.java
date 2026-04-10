package theStart.thePeople;




import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.IplImage;


import CV.AdvancedFaceDetection;
import FlatLandStructure.ViewableFlatLand;
import Nuron.NuronForDisplay;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import theStart.thePeople.FlatLander.XYPair;
import theStart.thePeople.FlatLander.XYWrapper;
import theStart.theSpace.FlatLand;
import theStart.theStuff.BranchType;
import theStart.theStuff.FlatLanderRandom;
import theStart.theStuff.ClassOfFlatLander;
import theStart.theStuff.Synapse;
import theStart.theStuff.SynapseType;
import theStart.theView.ImageRepository;

public class FlatLander {

	public int xposinflatland;
	public int yposinflatland;
	ViewableFlatLand flatLand;
	FlatLanderRandom generator;
	private ArrayList<XYWrapper> dendrites;
	public XYWrapper axon;
	private long seed;
	public double frequency = 0;
	public double timeFrame = 250;
	private ClassOfFlatLander nuronClass;
	private FlatlanderType nuronType;
	private BufferedImage image;
	private Integer current;
	private Integer previousTime;
	private double backFireCapacity;
	private int backFireThreshold = 25;
	private int negbackFireThreshold = -2;
	public boolean bodyFire = false;
	private int xinImage;
	private int yinImage;
	private boolean isPulse = true;
	private int pulseCount = 0;
	private int timer = 0;
	private int dendCont;
	private int restStateTimer = 0;
	private int restStateTimerThreshold = (int) Math.random() * 50 + 250;
	private static final int MAXTIMERLIMIT = (int) Math.random() * 50 + 10;
	private int redTresold = 150;
	private int onetomanymax = 100;
	private int OneToManyCount = onetomanymax;
	private NuronForDisplay nuronForDisplay;
	private boolean timeStart = false;
	private long timeStartBeginning;
	private long timeStartEnd;

	public class XYWrapper {
		XYPair[] branchh;
		private int lengthRemaining;
		private boolean grow = true;
		public int end = 0;
		private BranchType type;
		public Color baseColor;
		public Color fireColor;
		public Color backFireColor;
		public Color fireAndBackFireColor;
		public Color dendriteColor;
		public Color axonColor;
		public Color dendriteSynapseColor;
		public Color axonSynapseColor;
		private int timer = MAXTIMERLIMIT;
		private int length;

		public XYWrapper(int length) {

			this.lengthRemaining = length;
			this.length = length;
			branchh = new XYPair[length];
		}

		public Color getDendriteColor() {
			return dendriteColor;
		}

		public void setDendriteColor(Color dendriteColor) {
			this.dendriteColor = dendriteColor;
		}

		public Color getAxonColor() {
			return axonColor;
		}

		public void setAxonColor(Color axonColor) {
			this.axonColor = axonColor;
		}

		public void setDendriteSynapseColor(Color dendriteColor2) {
			this.dendriteSynapseColor = dendriteColor2;
		}

		public Color getDendriteSynapseColor() {
			return this.dendriteSynapseColor;
		}

		public void setAxonSynapseColor(Color axonSynapseColor) {
			this.axonSynapseColor = axonSynapseColor;
		}

		public Color getAxonSynapseColor() {
			return this.axonSynapseColor;
		}

		public Color getBackFireColor() {
			return backFireColor;
		}

		public void setBackFireColor(Color backFireColor) {
			this.backFireColor = backFireColor;
		}

		public Color getFireAndBackFireColor() {
			return fireAndBackFireColor;
		}

		public void setFireAndBackFireColor(Color fireAndBackFireColor) {
			this.fireAndBackFireColor = fireAndBackFireColor;
		}

		public Color getBaseColor() {
			return baseColor;
		}

		public void setBaseColor(Color baseColor) {
			this.baseColor = baseColor;
		}

		public Color getFireColor() {
			return fireColor;
		}

		public void setFireColor(Color activationColor) {
			this.fireColor = activationColor;
		}

		public void setType(BranchType branchType) {
			this.type = branchType;
		}

		public BranchType getType() {
			return this.type;
		}

		public int branchLength() {
			return lengthRemaining;
		}

		public void addXYPair(XYPair toadd) {

			branchh[end] = toadd;
			end++;
		}

		public XYPair getXYPairAt(int index) {
			return branchh[index];
		}

		public XYPair getAtBase() {
			return branchh[0];
		}

		public XYPair getAtEnd() {
			int index = end - 1;
			if (end == 0)
				index = 0;
			if (branchh.length == 0)
				System.out.println();

			return branchh[index];
		}

		public int getEndIndex() {
			return end;
		}

		public boolean getGrow() {
			return grow;
		}

		public void setGrow(boolean b) {
			this.grow = false;
		}

		public void setBrancLength(int i) {
			lengthRemaining = i;

		}

		public boolean hasBeenBackFired() {
			for (int i = 0; i < end - 1; i++) {
				if (branchh[i].hasbeenbackfire || branchh[i].backFire)
					return true;
			}
			return false;
		}

		public boolean hasBeenFired() {
			for (int i = 0; i < end - 1; i++) {
				if (branchh[i].hasbeenfire || branchh[i].fire)
					return true;
			}
			return false;
		}

	}

	public class XYPair {

		private int x;
		private int y;
		private double direction;
		private long identifier;

		boolean fire = false;
		boolean hasbeenbackfire = false;
		boolean hasbeenfire = false;
		int fireCount = 0;
		boolean backFire = false;
		int backFireCount = 0;
		int length;

		public Synapse dendriteSynapse;
		public Synapse axonSynapse;

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}

		public double getDirection() {
			return direction;
		}

		public void setDirection(double direction) {
			this.direction = direction;
		}

		public long getIdentifier() {
			return identifier;
		}

		public void setIdentifier(long identifier) {
			this.identifier = identifier;
		}

		public boolean isFire() {

			return this.fire;
		}

		public void setFire(boolean fire) {
			this.fire = fire;
		}

		public int getFireCount() {
			return fireCount;
		}

		public void setFireCount(int fireCount) {
			this.fireCount = fireCount;
		}

		public void setBackFire(boolean backFire) {
			this.backFire = backFire;
		}

		public boolean getBackFire() {
			return backFire;
		}

		public int getBackFireCount() {

			return this.backFireCount;
		}

		public void setBackFireCount(int count) {
			this.backFireCount = count;
		}

		public void setLength(int length) {
			this.length = length;
		}

		public int getLength() {
			return this.length;
		}

		public void formDenriteSynapse() {
			this.setDendriteSynapse(new Synapse(SynapseType.Dendrite));
		}

		public void formAxionSynapse() {
			this.setAxonSynapse(new Synapse(SynapseType.Axon));
		}

		public Synapse getDendriteSynapse() {
			return dendriteSynapse;
		}

		public void setDendriteSynapse(Synapse dendriteSynapse) {
			this.dendriteSynapse = dendriteSynapse;
		}

		public Synapse getAxonSynapse() {
			return axonSynapse;
		}

		public void setAxonSynapse(Synapse axonSynapse) {
			this.axonSynapse = axonSynapse;
		}

		public boolean isHasbeenbackfire() {
			return hasbeenbackfire;
		}

		public void setHasBeenBackFire(boolean hasbeenfire) {
			this.hasbeenbackfire = hasbeenfire;
		}

		public boolean isHasbeenfire() {
			return hasbeenfire;
		}

		public void setHasbeenfire(boolean hasbeenfire) {
			this.hasbeenfire = hasbeenfire;
		}
	}

	public FlatLander(int x, int y, ClassOfFlatLander nuronType, FlatlanderType type, FlatLanderRandom generator2,
			long seed, ViewableFlatLand flatland) {
		this.setNuronType(nuronType);
		this.seed = seed;
		this.flatLand = flatland;
		previousTime = flatland.getTime();
		this.generator = generator2;
		xposinflatland = x;
		yposinflatland = y;
		this.nuronType = type;

	}

	public FlatLander(int x, int y, ClassOfFlatLander nuronType, FlatlanderType type, FlatLanderRandom generator2,
			long seed, ViewableFlatLand flatland, int axon, int axonDirection, int axonLength, int dendrites,
			int dendriteDirection, int dendriteLength, Color color, Color fireColor, Color backFireColor,
			Color fireAndBackFireColor, Color dendriteSynapseColor, Color axonSynapeColor, Color dendriteColor,
			Color axonColor) {
		this.nuronType = type;
		dendCont = dendrites;
		this.setNuronType(nuronType);
		this.seed = seed;
		this.flatLand = flatland;
		previousTime = flatland.getTime();
		this.generator = generator2;
		xposinflatland = x;
		yposinflatland = y;
		buildNurons(nuronType, axonDirection, axonLength, dendrites, dendriteDirection, dendriteLength, color,
				fireColor, backFireColor, fireAndBackFireColor, dendriteSynapseColor, axonSynapeColor, dendriteColor,
				axonColor);
	}

	public double getbackFireCapacity() {
		return backFireCapacity;
	}

	private void buildNurons(ClassOfFlatLander nuronType, int axonDirection, int axonLength, int dendrites,
			int dendriteDirection, int dendriteLength, Color color, Color fireColor, Color backFireColor,
			Color fireAndBackFireColor, Color dendriteSynapseColor, Color axonSynapeColor, Color dendriteColor,
			Color axonColor) {
		if (nuronType == ClassOfFlatLander.BipolarNuron) {
			this.axon = buildAnAxon(axonDirection, axonLength, color, fireColor, backFireColor, fireAndBackFireColor,
					dendriteSynapseColor, axonSynapeColor, dendriteColor, axonColor);
			buildDendrites(1, axonDirection + 180, dendriteLength, 0, color, fireColor, backFireColor,
					fireAndBackFireColor, dendriteSynapseColor, axonSynapeColor, dendriteColor, axonColor);
		} else if (nuronType == ClassOfFlatLander.MultipolarNuron) {
			this.axon = buildAnAxon(axonDirection, axonLength, color, fireColor, backFireColor, fireAndBackFireColor,
					dendriteSynapseColor, axonSynapeColor, dendriteColor, axonColor);
			buildDendrites(6, axonDirection + 90, dendriteLength, 25, color, fireColor, backFireColor,
					fireAndBackFireColor, dendriteSynapseColor, axonSynapeColor, dendriteColor, axonColor);
		} else if (nuronType == ClassOfFlatLander.PyramidalNuron) {
			this.axon = buildAnAxon(axonDirection, axonLength, color, fireColor, backFireColor, fireAndBackFireColor,
					dendriteSynapseColor, axonSynapeColor, dendriteColor, axonColor);
			buildDendrites(2, axonDirection, dendriteLength, 90, color, fireColor, backFireColor, fireAndBackFireColor,
					dendriteSynapseColor, axonSynapeColor, dendriteColor, axonColor);
		} else if (nuronType == ClassOfFlatLander.PurkinjeNuron) {
			this.axon = buildAnAxon(axonDirection, axonLength, color, fireColor, backFireColor, fireAndBackFireColor,
					dendriteSynapseColor, axonSynapeColor, dendriteColor, axonColor);
			buildDendrites(2, axonDirection, dendriteLength, 90, color, fireColor, backFireColor, fireAndBackFireColor,
					dendriteSynapseColor, axonSynapeColor, dendriteColor, axonColor);
		} else {
			this.axon = buildAnAxon(axonDirection, axonLength, color, fireColor, backFireColor, fireAndBackFireColor,
					dendriteSynapseColor, axonSynapeColor, dendriteColor, axonColor);
			buildDendrites(dendrites, dendriteDirection, dendriteLength, 35, color, fireColor, backFireColor,
					fireAndBackFireColor, dendriteSynapseColor, axonSynapeColor, dendriteColor, axonColor);

		}
	}

	public FlatLander(int x, int y, ClassOfFlatLander nuronType, int xinImage2, int yinImage2,
			FlatLanderRandom generator2, long seed2, ViewableFlatLand flatland2, int axon, int direction, int length,
			int dendrites, int direction2, int length2, Color color, Color fireColor, Color backFireColor,
			Color fireAndBackFireColor, Color dendriteSynapseColor, Color axonSynapseColor, Color dendriteColor,
			Color axonColor) {
		this.xinImage = xinImage2;
		this.yinImage = yinImage2;
		this.setNuronType(nuronType);
		this.seed = seed2;
		this.flatLand = flatland2;
		previousTime = flatland2.getTime();
		this.generator = generator2;
		xposinflatland = x;
		yposinflatland = y;
		this.axon = buildAnAxon(direction, length, color, fireColor, backFireColor, fireAndBackFireColor,
				dendriteSynapseColor, axonSynapseColor, dendriteColor, axonColor);
		buildDendrites(dendrites, direction2, length2, 45, color, fireColor, backFireColor, fireAndBackFireColor,
				dendriteSynapseColor, axonSynapseColor, dendriteColor, axonColor);
	}

	private void buildDendrites(int dendrites2, int dendriteDirection, int dendriteLength, int dendriteAngle,
			Color color, Color fireColor, Color backFireColor, Color fireAndBackFireColor, Color dendriteSynapseColor,
			Color axonSynapseColor, Color dendriteColor, Color axonColor) {
		setDentrites(new ArrayList<XYWrapper>());
		int count = 0;
		while (dendrites2 > count) {
			XYWrapper XYwrap = new XYWrapper(dendriteLength);
			XYPair xyPair = new XYPair();
			XYwrap.setType(BranchType.Dendrite);
			XYwrap.setBaseColor(color);
			XYwrap.setFireColor(fireColor);
			XYwrap.setBackFireColor(backFireColor);
			XYwrap.setFireAndBackFireColor(fireAndBackFireColor);
			XYwrap.setDendriteColor(dendriteColor);
			XYwrap.setDendriteSynapseColor(dendriteSynapseColor);
			XYwrap.setAxonColor(axonColor);
			XYwrap.setAxonSynapseColor(axonSynapseColor);
			xyPair.setIdentifier(seed);
			xyPair.setLength(dendriteLength);

			int angle = count * dendriteAngle;
			dendriteDirection = dendriteDirection + dendriteAngle;
			double directionInRadians = Math.toRadians(dendriteDirection);
			xyPair.setDirection(directionInRadians);
			double round = 10 * Math.cos(directionInRadians);

			round = Math.round(round);

			xyPair.setX((int) (xposinflatland + round));
			double round2 = 10 * Math.sin(directionInRadians);
			round2 = Math.round(round2);
			xyPair.setY((int) (yposinflatland + round2));
			XYwrap.addXYPair(xyPair);
			getDendrites().add(XYwrap);
			count++;
		}

	}

	private XYWrapper buildADendrite(int dendriteDirection, int dendriteLength, int dendriteAngle, Color color,
			Color fireColor, Color backFireColor, Color fireAndBackFireColor, Color dendriteSynapseColor,
			Color axonSynapseColor, Color dendriteColor, Color axonColor, int seed) {
		XYWrapper xywrapper = new XYWrapper(dendriteLength);
		XYPair xyPair = new XYPair();
		xywrapper.setType(BranchType.Dendrite);
		xywrapper.setBaseColor(color);
		xywrapper.setFireColor(fireColor);
		xywrapper.setBackFireColor(backFireColor);
		xywrapper.setFireAndBackFireColor(fireAndBackFireColor);
		xywrapper.setDendriteColor(dendriteColor);
		xywrapper.setDendriteSynapseColor(dendriteSynapseColor);
		xywrapper.setAxonColor(axonColor);
		xywrapper.setAxonSynapseColor(axonSynapseColor);
		xyPair.setIdentifier(seed);
		xyPair.setLength(dendriteLength);

		int angle = dendriteAngle;
		dendriteDirection = dendriteDirection + dendriteAngle;
		double directionInRadians = Math.toRadians(dendriteDirection);
		xyPair.setDirection(directionInRadians);
		double round = 10 * Math.cos(directionInRadians);

		round = Math.round(round);

		xyPair.setX((int) (xposinflatland + round));
		double round2 = 10 * Math.sin(directionInRadians);
		round2 = Math.round(round2);
		xyPair.setY((int) (yposinflatland + round2));
		xywrapper.addXYPair(xyPair);
		return xywrapper;

	}

	private XYWrapper buildAnAxon(int axonDirection, int axonLength, Color color, Color fireColor, Color backFireColor,
			Color fireAndBackFireColor, Color dendriteSynapseColor, Color axonSynapseColor, Color dendriteColor,
			Color axonColor) {

		XYWrapper xywrapper = new XYWrapper(axonLength);
		XYPair xyPair = new XYPair();
		xywrapper.setType(BranchType.Axon);
		xyPair.setLength(axonLength);
		xywrapper.setBaseColor(color);
		xywrapper.setFireColor(fireColor);
		xywrapper.setBackFireColor(backFireColor);
		xywrapper.setFireAndBackFireColor(fireAndBackFireColor);
		xywrapper.setDendriteColor(dendriteColor);
		xywrapper.setDendriteSynapseColor(dendriteSynapseColor);
		xywrapper.setAxonSynapseColor(axonSynapseColor);
		xywrapper.setAxonColor(axonColor);
		xyPair.setIdentifier(seed);

		double directionInRadians = Math.toRadians(axonDirection);
		xyPair.setDirection(directionInRadians);
		double round = 10 * Math.cos(directionInRadians);
		if (round < 0) {
			round = Math.floor(round);
		} else if (round > 0) {
			round = Math.round(round);
		}
		xyPair.setX((int) (xposinflatland + round));
		double round2 = 10 * Math.sin(directionInRadians);
		if (round2 < 0) {
			round2 = Math.floor(round2);
		} else if (round2 > 0) {
			round2 = Math.round(round2);
		}
		xyPair.setY((int) (yposinflatland + round2));
		xywrapper.addXYPair(xyPair);

		return xywrapper;

	}

	public boolean fire() {
		if (getNuronType() != ClassOfFlatLander.InputNuron) {
			double nextDouble = Math.random() * 2;
			if (nextDouble < 1) {
				this.bodyFire = true;
				return this.bodyFire;
			}
			this.bodyFire = false;
			return this.bodyFire;

		} else {

			BufferedImage image2 = getImage();
			if (image2 != null) {
//				AdvancedFaceDetection detector;
//				try {
//					detector = new AdvancedFaceDetection("/home/wes/git/theStart/theStart/res/folder/haarcascade.xml");
//					boolean faceDetected = detector.detectFaces(image2);
//					if (faceDetected) {
//						this.bodyFire = true;
//						return this.bodyFire;
//					} else {
//						this.bodyFire = false;
//						return this.bodyFire;
//					}	
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//		       
//				
				
				
				double measureCompleteness = measureCompleteness(image2);
				double computeEdgeThreshold = computeEdgeThreshold(image2);
				
				



					if (measureCompleteness>.5 || computeEdgeThreshold>2.3 ) {
						this.bodyFire = true;
						return this.bodyFire;
					} else {
						this.bodyFire = false;
						return this.bodyFire;
					}	
				
			}
			this.bodyFire = false;
			}

		
		return this.bodyFire;
		}
	
	
    private static final Map<Character, Integer> EXPECTED_COUNTS = Map.of(
            'P', 8,
            'R', 2,
            'B', 2,
            'N', 2,
            'K', 1,
            'Q', 1
        );
public static double measureCompleteness(BufferedImage img) {
        final int w = img.getWidth();
        final int h = img.getHeight();
        final int threshold = 140; // adjust if letters are lighter/darker

        int darkCount = 0;
        int transitions = 0;
        int prevRowDark = 0;

        // Scan lines to measure letter-like segmentation density
        for (int y = 0; y < h; y++) {
            int rowDark = 0;
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                int gray = (r * 30 + g * 59 + b * 11) / 100;
                if (gray < threshold) rowDark++;
            }
            darkCount += rowDark;
            if (y > 0) {
                // detect contrast between rows (like letter edges)
                int diff = Math.abs(rowDark - prevRowDark);
                if (diff > w * 0.05) transitions++;
            }
            prevRowDark = rowDark;
        }

        double pixelRatio = (double) darkCount / (w * h);
        double transRatio = (double) transitions / h;

        // Expected density range for typical uppercase sets
        // (e.g., chess notation letters, solid grid of uppercase glyphs)
        double densityScore = Math.min(1.0, pixelRatio / 0.12);
        double edgeScore = Math.min(1.0, transRatio / 0.2);

        // Weighted blend — can tune empirically
        double completeness = 0.6 * densityScore + 0.4 * edgeScore;
        return Math.min(1.0, completeness);
    }
	
public static double computeEdgeThreshold(BufferedImage img) {
        if (img == null) return 0;
        int w = img.getWidth();
        int h = img.getHeight();
        if (w < 3 || h < 3) return 0;

        // Convert image to byte array (BGR layout)
        byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        boolean hasAlpha = img.getAlphaRaster() != null;
        int pixelLength = hasAlpha ? 4 : 3;

        double totalGradient = 0;
        int count = 0;

        // Precompute gray values for all pixels (1D array)
        int[] gray = new int[w * h];
        int index = 0;
        for (int y = 0; y < h; y++) {
            int rowStart = y * w * pixelLength;
            for (int x = 0; x < w; x++) {
                int pos = rowStart + x * pixelLength;
                int b = pixels[pos] & 0xFF;
                int g = pixels[pos + 1] & 0xFF;
                int r = pixels[pos + 2] & 0xFF;
                gray[index++] = (r * 299 + g * 587 + b * 114) / 1000; // integer grayscale
            }
        }

        // Fast Sobel
        for (int y = 1; y < h - 1; y++) {
            int row = y * w;
            for (int x = 1; x < w - 1; x++) {
                int i = row + x;
                int g00 = gray[i - w - 1], g01 = gray[i - w], g02 = gray[i - w + 1];
                int g10 = gray[i - 1],     g11 = gray[i],     g12 = gray[i + 1];
                int g20 = gray[i + w - 1], g21 = gray[i + w], g22 = gray[i + w + 1];

                int gradX = (g02 + 2 * g12 + g22) - (g00 + 2 * g10 + g20);
                int gradY = (g20 + 2 * g21 + g22) - (g00 + 2 * g01 + g02);

                totalGradient += Math.sqrt(gradX * gradX + gradY * gradY);
                count++;
            }
        }

        double avgGradient = totalGradient / count;
        return Math.min(255, Math.max(0, avgGradient / 8.0));
    }

    private static int getGray(Raster raster, int x, int y) {
        int[] pixel = new int[3];
        raster.getPixel(x, y, pixel);
        // Basic luminance approximation
        return (int)(0.299 * pixel[0] + 0.587 * pixel[1] + 0.114 * pixel[2]);
    }
	private BufferedImage getImage() {
		if (ImageRepository.getInstance().isImageToken()) {
			return ImageRepository.getInstance().getPreviousImage();
		} else {
			return ImageRepository.getInstance().getImage();
		}
	}

	public static BufferedImage IplImageToBufferedImage(IplImage src) {
		OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
		Java2DFrameConverter paintConverter = new Java2DFrameConverter();
		Frame frame = grabberConverter.convert(src);
		grabberConverter.close();
		BufferedImage bufferedImage = paintConverter.getBufferedImage(frame, 1);
		paintConverter.close();
		return bufferedImage;
	}

	public int getXposinflatland() {
		return xposinflatland;
	}

	public void setXposinflatland(int xposinflatland) {
		this.xposinflatland = xposinflatland;
	}

	public int getYposinflatland() {
		return yposinflatland;
	}

	public void setYposinflatland(int yposinflatland) {
		this.yposinflatland = yposinflatland;
	}

	public void growBranches() {

		if (dendrites == null) {
			setDentrites(new ArrayList<XYWrapper>());
			int tobranch = (int) (Math.random() * 8) + 1;

			int direction = (int) (generator.nextDouble(xposinflatland, yposinflatland, flatLand.getTime(), 0, 360));
			int length = (int) (generator.nextDouble(xposinflatland, yposinflatland, flatLand.getTime(), 1000, 3000));
			int count = 0;
			Color color = Color.black;
			Color fireColor = Color.ORANGE;
			Color backFireColor = Color.CYAN;
			Color fireAndBackFireColor = Color.BLUE;
			Color dendriteColor = Color.CYAN;
			Color axonColor = Color.MAGENTA;
			while (tobranch > count) {
				XYWrapper XYwrap = new XYWrapper(length);
				XYPair xyPair = new XYPair();
				XYwrap.setType(BranchType.Dendrite);
				XYwrap.setBaseColor(color);
				XYwrap.setFireColor(fireColor);
				XYwrap.setBackFireColor(backFireColor);
				XYwrap.setFireAndBackFireColor(fireAndBackFireColor);
				XYwrap.setDendriteColor(dendriteColor);
				XYwrap.setAxonColor(axonColor);
				xyPair.setIdentifier(seed);
				xyPair.setLength(length);

				double directionInRadians = Math.toRadians(direction + (count * (360 / tobranch)));
				xyPair.setDirection(directionInRadians);
				double round = Math.cos(directionInRadians);
				if (round < 0) {
					round = Math.floor(round);
				} else if (round > 0) {
					round = Math.round(round);
				}
				xyPair.setX((int) (xposinflatland + round));
				double round2 = Math.sin(directionInRadians);
				if (round2 < 0) {
					round2 = Math.floor(round2);
				} else if (round2 > 0) {
					round2 = Math.round(round2);
				}
				xyPair.setY((int) (yposinflatland + round2));
				XYwrap.addXYPair(xyPair);
				dendrites.add(XYwrap);
				count++;
			}

		} else {

			ArrayList<XYWrapper> dendrites2 = new ArrayList<XYWrapper>(getDendrites());
			for (XYWrapper xywrap : dendrites2) {
				if (xywrap != null)
					goToEnd(xywrap);

			}

		}

		XYWrapper axon2 = getAxon();
		if (axon2 != null)
			goToEnd(axon2);

	}

	private void goToEnd(XYWrapper branch) {

		if (branch.grow) {
			if (branch.branchLength() > 1) {
				XYPair xyPair = new XYPair();
				branch.setBrancLength(branch.branchLength() - 1);
				if (branch.branchLength() == 0)
					branch.setGrow(false);
				xyPair.setIdentifier(seed);

				int x = branch.getAtEnd().getX();
				int y = branch.getAtEnd().getY();
				do {
					int direction = (int) (Math.random() * 90);
					int plusMinus = (int) (Math.random() * 360);
					if (plusMinus > 180) {
						plusMinus = 1;
					} else {
						plusMinus = -1;
					}
					direction = (int) (averageOfParents(branch, Math.toDegrees(branch.getXYPairAt(0).direction), 0)
							+ direction * plusMinus);

					double directionInRadians2 = Math.toRadians(direction);
					xyPair.setDirection(directionInRadians2);

					double round3 = Math.cos(directionInRadians2);
					if (round3 < 0) {
						round3 = Math.floor(round3);
					} else if (round3 > 0) {
						round3 = Math.round(round3);
					}
					double round4 = Math.sin(directionInRadians2);
					if (round4 < 0) {
						round4 = Math.floor(round4);
					} else if (round4 > 0) {
						round4 = Math.round(round4);
					}
					x = (int) (branch.getAtEnd().getX() + round3);
					y = (int) (branch.getAtEnd().getY() + round4);
					xyPair.setX(x);
					xyPair.setY(y);
				} while (checkToSeeIfAnyPreviousHadXY(branch, x, y));

				branch.addXYPair(xyPair);

			}
		} else {
			branch.timer--;
			XYWrapper aDend = null;
			XYWrapper aaxon = null;
			if (branch.timer == 0) {
				branch.timer = MAXTIMERLIMIT;
				if (branch.type == BranchType.Dendrite) {

					if (!hasBeenBackFired(branch)) {
						aDend = buildADendrite(2, branch.length, 25, branch.baseColor, branch.fireColor,
								branch.backFireColor, branch.fireAndBackFireColor, branch.dendriteSynapseColor,
								branch.axonSynapseColor, branch.dendriteColor, branch.axonColor,
								(int) (Math.random() * 10000000));

						ArrayList<XYWrapper> daaa = new ArrayList<XYWrapper>(dendrites);

						daaa.remove(branch);

						daaa.add(aDend);
						dendrites.clear();
						dendrites.addAll(daaa);
					}

				}
				if (branch.type == BranchType.Axon) {
					if ((!hasBeenFired(branch))) {
						aaxon = buildAnAxon(2, branch.length, branch.baseColor, branch.fireColor, branch.backFireColor,
								branch.fireAndBackFireColor, branch.dendriteSynapseColor, branch.axonSynapseColor,
								branch.dendriteColor, branch.axonColor);
						axon = aaxon;
					}
				}

			}

		}
	}

	private boolean hasBeenBackFired(XYWrapper xyPair2) {

		return xyPair2.hasBeenBackFired();
	}

	private boolean hasBeenFired(XYWrapper xyPair2) {

		return xyPair2.hasBeenFired();
	}

	private double averageOfParents(XYWrapper pair, double direction, int i) {
		if (i >= pair.end) {
			return direction / i;
		} else {
			direction = direction + Math.toDegrees(pair.getXYPairAt(i).getDirection());
			i = i + 1;
			return averageOfParents(pair, direction, i);
		}
	}

	private boolean checkToSeeIfAnyPreviousHadXY(XYWrapper pair, int x, int y) {

		if (pair.getAtEnd().getX() != x && pair.getAtEnd().getY() != y) {
			return false;
		} else
			return true;
	}

	public int mapFromFlatLandToDirectionX(int input) {
		int input_end = 360;
		int input_start = 0;
		int output_start = -1;
		int output_end = 1;
		double slope = 1.0 * (output_end - output_start) / (input_end - input_start);
		return output_start + (int) Math.round(slope * (input - input_start));
	}

	public int mapFromFlatLandToDirectionY(int input) {
		int input_end = 360;
		int input_start = 0;
		int output_start = -1;
		int output_end = 1;
		double slope = 1.0 * (output_end - output_start) / (input_end - input_start);
		return output_start + (int) Math.round(slope * (input - input_start));
	}

	public ArrayList<XYWrapper> getDendrites() {
		return dendrites;
	}

	public void setDentrites(ArrayList<XYWrapper> branches) {
		this.dendrites = branches;
	}

	public XYWrapper getAxon() {
		return axon;
	}

	public void setAxon(XYWrapper axon) {
		this.axon = axon;
	}

	public ClassOfFlatLander getNuronType() {
		return nuronClass;
	}

	public void setNuronType(ClassOfFlatLander nuronType) {
		this.nuronClass = nuronType;
	}

	public void updateBranches() {
		pulseCount++;

		if (getDendrites() != null) {
			for (XYWrapper xyPair : getDendrites()) {

				updatefromEnd(xyPair);

			}
		}

		if (nuronClass == ClassOfFlatLander.InputNuron) {
			fire();
			if (this.bodyFire) {
				axon.getAtBase().setFire(true);
			}
		} else {
			if (this.bodyFire) {

				axon.getAtBase().setFire(true);
			}
		}
		updatefromEnd(axon);
	}

	private void updatefromEnd(XYWrapper pair) {

		updateFire(pair);
		updateBackFire(pair);

		if (pair.getEndIndex() >= pair.length && pair.getGrow()) {
			pair.grow = false;
		}

	}

	private void updateFire(XYWrapper pair) {
		boolean update[] = new boolean[pair.end];
		boolean update2[] = new boolean[pair.end];

		for (int i = 0; i < pair.end; i++) {
			update[i] = pair.getXYPairAt(i).fire;
			if (update[i])
				pair.getXYPairAt(i).hasbeenfire = true;

		}

		for (int i = 0; i + 1 < pair.end; i++) {
			pair.getXYPairAt(i).fire = false;
			update2[i] = false;
		}
		for (int i = 0; i + 1 < pair.end; i++) {
			update2[i + 1] = update[i];
		}

		for (int i = 0; i < pair.end; i++) {
			pair.getXYPairAt(i).fire = update2[i];
		}

	}

	private void updateBackFire(XYWrapper pair) {
		boolean update[] = new boolean[pair.end];
		boolean update2[] = new boolean[pair.end];

		for (int i = 0; i < pair.end; i++) {
			update[i] = pair.getXYPairAt(i).backFire;
			if (update[i])
				pair.getXYPairAt(i).hasbeenbackfire = true;

		}

		for (int i = 0; i + 1 < pair.end; i++) {
			pair.getXYPairAt(i).backFire = false;
			update2[i] = false;
		}
		for (int i = 0; i + 1 < pair.end; i++) {
			update2[i] = update[i + 1];
		}

		for (int i = 0; i < pair.end; i++) {
			pair.getXYPairAt(i).backFire = update2[i];
		}
	}

	public void updateBody() {

		if (!timeStart) {
			this.timeStartBeginning = System.currentTimeMillis();
			timeStart = true;
		}

		ArrayList<XYWrapper> dendrites2 = dendrites;
		for (XYWrapper xywrap : dendrites2) {
			if (xywrap.getXYPairAt(0).getBackFire()) {
				this.backFireCapacity += 1;
			}
		}

		if (timeStart && System.currentTimeMillis() - this.timeStartBeginning > timeFrame) {

			this.timeStartEnd = System.currentTimeMillis();
			long l = this.timeStartEnd - this.timeStartBeginning;
			l = l ;
			frequency = this.backFireCapacity / l;
			timeStart = false;
		}

		if (this.nuronType == FlatlanderType.OneToOne && this.backFireCapacity > this.backFireThreshold) {
			this.bodyFire = true;
			this.backFireCapacity = 0;
		} else if (this.nuronType == FlatlanderType.OneToOne && this.backFireCapacity < this.negbackFireThreshold) {
//			this.bodyFire = true;
			this.backFireCapacity = 0;
		} else if (this.nuronType == FlatlanderType.ManyToOne && this.backFireCapacity > this.backFireThreshold) {
			this.bodyFire = true;
			this.backFireCapacity = 0;
		} else if (this.nuronType == FlatlanderType.ManyToOne && this.backFireCapacity < this.negbackFireThreshold) {
//			this.bodyFire = true;
			this.backFireCapacity = 0;
		} else if (this.nuronType == FlatlanderType.ManyToMany && this.backFireCapacity > this.backFireThreshold) {
			this.bodyFire = true;
			this.backFireCapacity = 0;
		} else if (this.nuronType == FlatlanderType.ManyToMany && this.backFireCapacity < this.negbackFireThreshold) {
//			this.bodyFire = true;
			this.backFireCapacity = 0;
		} else if (this.nuronType == FlatlanderType.OneToMany && this.backFireCapacity > this.backFireThreshold) {
			this.bodyFire = true;
			if (this.OneToManyCount <= 0)
				this.backFireCapacity = 0;
			this.OneToManyCount--;
		} else if (this.nuronType == FlatlanderType.OneToMany && this.backFireCapacity < this.negbackFireThreshold) {
//			this.bodyFire = true;
			this.backFireCapacity = 0;
		} else {
			this.OneToManyCount = onetomanymax;
			this.bodyFire = false;
		}
	}

	public void setNuronForDisplay(NuronForDisplay nuronForDisplay) {
		this.nuronForDisplay = nuronForDisplay;
		// TODO Auto-generated method stub

	}

	public NuronForDisplay getNuronForDisplay() {
		return nuronForDisplay;
	}

	public double getFrequency() {
		return frequency;
	}

}
