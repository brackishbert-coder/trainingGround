package flatLand.trainingGround.Sprites;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import FlatLander.FlatLander;

public class ItemObject implements Sprites {

	int width = 32;
	int height = 32;

	
	private BufferedImage after;

	int state = 0;

	public ItemObject(String filePath, Integer scale) throws IOException {
		BufferedImage spriteimageRight = ImageIO.read(new File(filePath));

		populateSpriteAnimation(spriteimageRight, scale);
	}

	private void populateSpriteAnimation( BufferedImage spriteimageRight, Integer scale) {
		BufferedImage platform = new BufferedImage(16,16,BufferedImage.TYPE_INT_ARGB);
		int[] left = new int[16 * 16];
		 spriteimageRight.getRGB(0, 0, 16, 16, left, 0, 8);
		 platform.setRGB(0, 0, 16, 16, left, 0, 8);

		 after = platform;
	}

	public BufferedImage update(FlatLander actor) {
		return after;
	}

	public int getWidth() {

		return width;
	}

	public int getHeight() {

		return height;
	}

	public void updateState() {

	}

	@Override
	public BufferedImage update(String key, boolean gameMode, boolean prompt) {
		return after;
	}

}
