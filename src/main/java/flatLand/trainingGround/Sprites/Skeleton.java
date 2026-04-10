package flatLand.trainingGround.Sprites;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import FlatLander.FlatLander;





public class Skeleton implements Sprites{
	
	SpriteType type = SpriteType.Skeleton ;
	
	BufferedImage[] walkRight = new BufferedImage[8];
	BufferedImage[] walkLeft = new BufferedImage[8];
	int state =0;

	private int width=50;

	private int height = 50;;
	
	
	
	public Skeleton(String filePath,String filePath2,Integer scale) throws IOException {
		BufferedImage spriteimageRight = ImageIO.read(new File(filePath));
		BufferedImage spriteimageleft = ImageIO.read(new File(filePath2));
		int wRight = spriteimageRight.getWidth();
		int hRight = spriteimageRight.getHeight();
		int heightRight = (int) ((double) hRight * ((double) scale / 100.0));
		int widthRight = (int) ((double) wRight * ((double) scale / 100.0));
		BufferedImage afterRight = new BufferedImage(widthRight, heightRight, BufferedImage.TYPE_INT_ARGB);
		AffineTransform atRight = new AffineTransform();
		atRight.scale((double) scale / 100.0, (double) scale / 100.0);
		AffineTransformOp scaleOpRight = new AffineTransformOp(atRight, AffineTransformOp.TYPE_BILINEAR);
		afterRight = scaleOpRight.filter(spriteimageRight, afterRight);

		int wLeft = spriteimageleft.getWidth();
		int hLeft = spriteimageleft.getHeight();
		int heightLeft = (int) ((double) hLeft * ((double) scale / 100.0));
		int widthLeft = (int) ((double) wLeft * ((double) scale / 100.0));
		BufferedImage afterLeft = new BufferedImage(widthLeft, heightLeft, BufferedImage.TYPE_INT_ARGB);
		AffineTransform atLeft = new AffineTransform();
		atLeft.scale((double) scale / 100.0, (double) scale / 100.0);
		AffineTransformOp scaleOpLeft = new AffineTransformOp(atLeft, AffineTransformOp.TYPE_BILINEAR);
		afterLeft = scaleOpLeft.filter(spriteimageleft, afterLeft);

		this.width = (int)(50*(scale/100.0));
		this.height = (int)(50*(scale/100.0));
		
		
		
		
		
		populateSpriteAnimation(afterRight,afterLeft,scale);
	}






	private void populateSpriteAnimation(BufferedImage right,BufferedImage left, Integer scale) {
		for (int i = 0; i < walkRight.length; i++) {
			
			int x =0;
			if(i<4)x =i*(int)(50*(scale/100.0));
			else x=(i-4)*(int)(50*(scale/100.0));
			
			int y=0;
			if(i<4)y=0;
			else y=(int)(50*(scale/100.0));
			
			walkRight[i] = right.getSubimage(x, y, (int)(50*(scale/100.0)), (int)(50*(scale/100.0)));
			walkLeft[i] = left.getSubimage(x, y, (int)(50*(scale/100.0)), (int)(50*(scale/100.0)));
		}
		
	}
	
	
	public BufferedImage update(FlatLander actor) {
		BufferedImage walk;
		if(actor.direction>=0 && actor.direction < 1.5708){
			walk = walkRight[state];
		}else if (actor.direction > 4.71239 && actor.direction < 6.28319){
			walk = walkRight[state];
		}else if(actor.direction > 1.5708 && actor.direction < 4.71239 ) {
			walk = walkLeft[state];		
		}else {
			walk = walkLeft[state];
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
		if(state>=8)state=0;
	}











	@Override
	public BufferedImage update(String key, boolean gameMode,boolean prompt) {
		// TODO Auto-generated method stub
		return null;
	}







	
	
	
}
