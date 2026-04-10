package flatLand.trainingGround.Sprites;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import FlatLander.FlatLander;


public class SceneObject implements Sprites {
	private int width;
	private int height;
	private BufferedImage spriteimageRight;
	private BufferedImage after;

	public SceneObject(String filePath, Integer scale, String type, Integer width2, Integer totalHeight) throws IOException {

		spriteimageRight = ImageIO.read(new File(filePath));
		generateImage(scale, type,width2,totalHeight);
	}

	private void generateImage(Integer scale, String type, Integer width2, Integer totalHeight) {
		if (type.equalsIgnoreCase("enemy")) {
			int w = spriteimageRight.getWidth();
			int h = spriteimageRight.getHeight();
			int height = (int) ((double) h * ((double) scale / 100.0));
			int width = (int) ((double) w * ((double) scale / 100.0));
			after = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			AffineTransform at = new AffineTransform();
			at.scale((double) scale / 100.0, (double) scale / 100.0);
			AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
			after = scaleOp.filter(spriteimageRight, after);

			this.width = after.getWidth();
			this.height = after.getHeight();
		}else if(type.equalsIgnoreCase("ground_dirt")) {
			BufferedImage ground = new BufferedImage(width2+10,totalHeight,BufferedImage.TYPE_INT_ARGB);
			int[] left = new int[10 * 5];
			 spriteimageRight.getRGB(0, 0, 5, 10, left, 0, 5);
			 ground.setRGB(0, 0, 5, 10, left, 0, 5);
			 int[] right = new int[10 * 5];
			 spriteimageRight.getRGB(43, 0, 5, 10, right, 0, 5);
			 ground.setRGB(width2, 0, 5, 10, right, 0, 5);
			 
			 
			 double timesToLoop = (double)(width2 /38.0);
			 int[] mid = new int[38 * 10];
			 int i = 0;
			 for(;i<(int)timesToLoop;i++) {
			 spriteimageRight.getRGB(5, 0, 38, 10, mid, 0, 38);
			 ground.setRGB(5+i*38, 0, 38, 10, mid, 0, 38);
			 }
			 double remainder = timesToLoop%1;
			 double remainingPiels = 38 *remainder;
			 long round = Math.round(remainingPiels);
			 int[] fin = new int[(int)round * 10];
			 spriteimageRight.getRGB(5, 0, (int)round, 10, fin, 0, (int)round);
			 ground.setRGB(i*38, 0, (int)round, 10, fin, 0, (int)round);
			 double increment = (double)(totalHeight / 10.0);
			 int count =0;
			 for(int j = 0 ;j<increment-1;j++) {
				 left = new int[10 * 5];
				 int j2 = 10+count*10;
				 if(j2<48) {
					 count =0;
					 j2 = 10+count*10;
				 }
				spriteimageRight.getRGB(0, j2, 5, 10, left, 0, 5);
				 ground.setRGB(0, 10+j*10, 5, 10, left, 0, 5);
				 right = new int[10 * 5];
				 spriteimageRight.getRGB(43, j2, 5, 10, right, 0, 5);
				 ground.setRGB(width2, 10+j*10, 5, 10, right, 0, 5);
				 
				 
				  timesToLoop = (double)(width2 /38.0);
				 mid = new int[38 * 10];
				 i = 0;
				 for(;i<(int)timesToLoop;i++) {
				 spriteimageRight.getRGB(5, j2, 38, 10, mid, 0, 38);
				 ground.setRGB(5+i*38, 10+j*10, 38, 10, mid, 0, 38);
				 }
				 remainder = timesToLoop%1;
				 remainingPiels = 38 *remainder;
				 round = Math.round(remainingPiels);
				 fin = new int[(int)round * 10];
				 spriteimageRight.getRGB(5, j2, (int)round, 10, fin, 0, (int)round);
				 ground.setRGB(i*38, 10+j*10, (int)round, 10, fin, 0, (int)round);
				 count++;
				 
			 }
			 
			 
			 
			 after = ground;
			 this.width = after.getWidth();
			this.height = after.getHeight();
		}else if(type.equalsIgnoreCase("ground_stone")) {
			BufferedImage ground = new BufferedImage(width2+10,totalHeight,BufferedImage.TYPE_INT_ARGB);
			int[] left = new int[7 * 8];
			 spriteimageRight.getRGB(96, 0, 8, 7, left, 0, 8);
			 ground.setRGB(0, 0, 8, 7, left, 0, 8);
			 int[] right = new int[7 * 8];
			 spriteimageRight.getRGB(96+32, 0, 8, 7, right, 0, 8);
			 ground.setRGB(width2, 0, 8, 7, right, 0, 8);
			 
			 
			 double timesToLoop = (double)(width2 /32.0);
			 int[] mid = new int[32 * 7];
			 int i = 0;
			 for(;i<(int)timesToLoop;i++) {
			 spriteimageRight.getRGB(96+8, 0, 32, 7, mid, 0, 32);
			 ground.setRGB(8+i*32, 0, 32, 7, mid, 0, 32);
			 }
			 double remainder = timesToLoop%1;
			 double remainingPiels = 32 *remainder;
			 long round = Math.round(remainingPiels);
			 int[] fin = new int[(int)round * 10];
			 spriteimageRight.getRGB(96+8, 0, (int)round, 7, fin, 0, (int)round);
			 ground.setRGB(i*32, 0, (int)round, 7, fin, 0, (int)round);
			 double increment = (double)(totalHeight / 7);
			 int count =0;
			 for(int j = 0 ;j<increment-1;j++) {
				 int j2 = 7+count*7;
				 if(j2<48) {
					 count =0;
					 j2 = 7+count*7;
				 }
				 left = new int[7 * 8];
				spriteimageRight.getRGB(96, j2, 8, 7, left, 0, 8);
				 ground.setRGB(0, 7+j*7, 8, 7, left, 0, 8);
				 right = new int[7 * 8];
				 spriteimageRight.getRGB(96+32+8, j2, 8, 7, right, 0, 8);
				 ground.setRGB(width2, 7+j*7, 8, 7, right, 0, 8);
				 
				 
				  timesToLoop = (double)(width2 /32.0);
				 mid = new int[32 * 7];
				 i = 0;
				 for(;i<(int)timesToLoop;i++) {
				 spriteimageRight.getRGB(96+8, j2, 32, 7, mid, 0, 32);
				 ground.setRGB(8+i*32, 7+j*7, 32, 7, mid, 0, 32);
				 }
				 remainder = timesToLoop%1;
				 remainingPiels = 32 *remainder;
				 round = Math.round(remainingPiels);
				 fin = new int[(int)round * 7];
				 spriteimageRight.getRGB(96+8, j2, (int)round, 7, fin, 0, (int)round);
				 ground.setRGB(i*32, 7+j*7, (int)round, 7, fin, 0, (int)round);
				 count++;
				 
			 }
			 
			 
			 
			 after = ground;
			 this.width = after.getWidth();
			this.height = after.getHeight();
		}else if(type.equalsIgnoreCase("platform_dirt")) {
			BufferedImage platform = new BufferedImage(width2+10,16,BufferedImage.TYPE_INT_ARGB);
			int[] left = new int[16 * 5];
			 spriteimageRight.getRGB(0, 48, 5, 16, left, 0, 5);
			 platform.setRGB(0, 0, 5, 16, left, 0, 5);
			 int[] right = new int[16 * 5];
			 spriteimageRight.getRGB(43, 48, 5, 16, right, 0, 5);
			 platform.setRGB(width2, 0, 5, 16, right, 0, 5);
			 
			 
			 double timesToLoop = (double)(width2 /38.0);
			 int[] mid = new int[38 * 16];
			 int i = 0;
			 for(;i<(int)timesToLoop;i++) {
			 spriteimageRight.getRGB(5, 48, 38, 16, mid, 0, 38);
			 platform.setRGB(5+i*38, 0, 38, 16, mid, 0, 38);
			 }
			 double remainder = timesToLoop%1;
			 double remainingPiels = 38 *remainder;
			 long round = Math.round(remainingPiels);
			 int[] fin = new int[(int)round * 16];
			 spriteimageRight.getRGB(5, 48, (int)round, 16, fin, 0, (int)round);
			 platform.setRGB(i*38, 0, (int)round, 16, fin, 0, (int)round);
			 
			 
			 
			 
			 after = platform;
			 this.width = after.getWidth();
			this.height = after.getHeight();
		}else if(type.equalsIgnoreCase("Item")) {
			BufferedImage platform = new BufferedImage(width2+10,16,BufferedImage.TYPE_INT_ARGB);
			int[] left = new int[16 * 5];
			 spriteimageRight.getRGB(0,0, 5, 16, left, 0, 5);
			 platform.setRGB(0, 0, 5, 16, left, 0, 5);
			 
			 
			 
			 after = platform;
			 this.width = after.getWidth();
			this.height = after.getHeight();
		}
	}

	public BufferedImage update(FlatLander actor) {

		return after;
	}

	public int getWidth() {

		return this.width;
	}

	public int getHeight() {

		return this.height;
	}

	public void updateState() {
	}


	@Override
	public BufferedImage update(String key, boolean gameMode,boolean prompt) {
		// TODO Auto-generated method stub
		return null;
	}



}
