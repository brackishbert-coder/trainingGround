package flatLand.trainingGround.Sprites;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import FlatLander.FlatLander;


public class ZombieBaby implements Sprites {

	int width =32;
	int height=64;
	
	BufferedImage[] walkRight = new BufferedImage[3];
	BufferedImage[] walkDown = new BufferedImage[3];
	
	BufferedImage[] walkUp = new BufferedImage[3];
	BufferedImage[] walkLeft = new BufferedImage[3];
	
	int state =0;
	
	public ZombieBaby(String filePath,Integer scale) throws IOException {
		BufferedImage spriteimageRight = ImageIO.read(new File(filePath));
		int w = spriteimageRight.getWidth();
		int h = spriteimageRight.getHeight();
		int height = (int) ((double) h * ((double) scale / 100.0));
		int width = (int) ((double) w * ((double) scale / 100.0));
		BufferedImage after = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		AffineTransform at = new AffineTransform();
		at.scale((double) scale / 100.0, (double) scale / 100.0);
		AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		after = scaleOp.filter(spriteimageRight, after);

		this.width = (int)(32.0*(scale/100.0));
		this.height = (int)(64.0*(scale/100.0));
		
		populateSpriteAnimation(after,scale); 
	}
	
	private void populateSpriteAnimation(BufferedImage right, Integer scale) {

		for (int i = 0; i < walkRight.length; i++) {
			
			walkDown[i] = right.getSubimage(i*width, 0, width, height);
			int j = (int)(64.0*(scale/100.0));
			walkLeft[i] = right.getSubimage(i*width, j, width, height);
			int j2 = (int)(128.0*(scale/100.0));
			walkRight[i] = right.getSubimage(i*width, j2, width, height);
			int k = (int)(192.0*(scale/100.0));
			walkUp[i] = right.getSubimage(i*width, k, width, height);
		}
		
	}
	
	public BufferedImage update(FlatLander actor) {
		BufferedImage walk;
		if(actor.direction>=0 && actor.direction < 1.5708){
			walk = walkRight[state];
		}else if (actor.direction > 4.71239 && actor.direction < 6.28319){
			walk = walkUp[state];
		}else if(actor.direction > 1.5708 && actor.direction < 4.71239 ) {
			walk = walkLeft[state];		
		}else {
			walk = walkDown[state];
		}
		
		return walk;
	}

	public int getWidth() {
		
		return width;
	}

	public int getHeight() {
		
		return height;
	}

	public void updateState() {
		state++;
		if(state>=2)state=0;
	}



	@Override
	public BufferedImage update(String key, boolean gameMode,boolean prompt) {
		// TODO Auto-generated method stub
		return null;
	}




}
