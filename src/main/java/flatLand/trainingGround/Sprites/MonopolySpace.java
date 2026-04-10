package flatLand.trainingGround.Sprites;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import FlatLander.FlatLander;

public class MonopolySpace implements Sprites {

	int width = 32;
	int height = 64;

	private BufferedImage bufferedImageResult;

	public MonopolySpace(String filePath, Integer scale) throws IOException {
		BufferedImage spriteimageRight = ImageIO.read(new File(filePath));
		bufferedImageResult = new BufferedImage(32, 64, spriteimageRight.getType());
		Graphics2D g2d = bufferedImageResult.createGraphics();
		g2d.drawImage(spriteimageRight, 0, 0, 32, 64, null);
		g2d.dispose();

	}

	public BufferedImage update(FlatLander actor) {

		return bufferedImageResult;
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
	public BufferedImage update(String key, boolean gameMode,boolean prompt) {

		return null;
	}

}
