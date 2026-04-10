package flatLand.trainingGround.Sprites;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import FlatLander.FlatLander;
import animation.Animations;
import animation.Asset;



public class GenericSprite implements Sprites{

	
	
	private int width;
	private int height;
	private Map<String,BufferedImage[]> animationMap = new HashMap<String,BufferedImage[]>();
	private Map<String,framesAndCurrentState> framesAndStateMap = new HashMap<String,framesAndCurrentState>();
	private Asset asset;
	private String currentAnimation="";
	
	
	public GenericSprite(Asset asset) throws IOException {
		this.asset = asset;
		BufferedImage spriteimageRight = ImageIO.read(new File(asset.getFile()));
		int w = spriteimageRight.getWidth();
		int h = spriteimageRight.getHeight();
		int height = (int) ((double) h * ((double) 100 / 100.0));
		int width = (int) ((double) w * ((double) 100 / 100.0));
		BufferedImage after = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		AffineTransform at = new AffineTransform();
		at.scale((double) 100 / 100.0, (double) 100 / 100.0);
		AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		after = scaleOp.filter(spriteimageRight, after);
		this.width = (int)(asset.getxSize()*(100/100.0));
		this.height = (int)(asset.getySize()*(100/100.0));
		populateAnimationMap( after);
	}
	
	
	
	private void populateAnimationMap(BufferedImage after) {
		ArrayList<Animations> animations = asset.getAnimations();
		for (Animations anim : animations) {
			String direction = anim.getDirection();
			String substring = direction.substring(0, 1);
			
			int frames = anim.getFrames();
			int startX = anim.getStartX();
			int startY = anim.getStartY();
			int width = asset.getxSize();
			int height = asset.getySize();
			BufferedImage[] img = new BufferedImage[frames];
			if(substring.equals("-")) {
				for(int i = frames-1;i<0;i--) {
					img[i]= after.getSubimage(i*startX, startY, width, height);
				}
			}else {
				for(int i =0;i<frames;i++) {
					if(i==0)
					img[i]= after.getSubimage(startX, startY, width, height);
					else
						img[i]= after.getSubimage(i*startX, startY, width, height);
				}
			}
			animationMap.put(anim.getName(), img);
			framesAndStateMap.put(anim.getName(), new framesAndCurrentState(0, frames));
			
		}
		
	}



	public int getWidth() {
		
		return width;
	}


	public int getHeight() {
		
		return height;
	}


	public void updateState() {
		framesAndCurrentState framesAndCurrentState = framesAndStateMap.get(currentAnimation);
		framesAndCurrentState.setState(framesAndCurrentState.getState()+1);
		if(framesAndCurrentState.getState()>=framesAndCurrentState.getFrames())
			framesAndCurrentState.setState(0);
	}
public class framesAndCurrentState{
	
	private int frames;
	private int state;
	
	public framesAndCurrentState(int state,int frames) {
		this.state = state;
		this.frames = frames;
	}
	
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public int getFrames() {
		return frames;
	}
	public void setFrames(int frames) {
		this.frames = frames;
	}
}
@Override
public BufferedImage update(FlatLander actor) {
	Set<String> keySet = animationMap.keySet();
	
	BufferedImage walk=null;
		if(actor.direction>=0 && actor.direction < 1.5708){
			for (String string : keySet) {
				if(string.contains("walk")&&string.contains("right")) {
					currentAnimation = string;
					BufferedImage[] bufferedImages = animationMap.get(currentAnimation);
					framesAndCurrentState framesAndCurrentState = framesAndStateMap.get(currentAnimation);
					
					walk = bufferedImages[framesAndCurrentState.getState()];
				}
			}
			
		}else if (actor.direction > 4.71239 && actor.direction < 6.28319){
			for (String string : keySet) {
				if(string.contains("walk")&&string.contains("up")) {
					currentAnimation = string;
					BufferedImage[] bufferedImages = animationMap.get(currentAnimation);
					framesAndCurrentState framesAndCurrentState = framesAndStateMap.get(currentAnimation);
					
					walk = bufferedImages[framesAndCurrentState.getState()];
				}
			}
		}else if(actor.direction > 1.5708 && actor.direction < 4.71239 ) {
			for (String string : keySet) {
				if(string.contains("walk")&&string.contains("left")) {
					currentAnimation = string;
					BufferedImage[] bufferedImages = animationMap.get(currentAnimation);
					framesAndCurrentState framesAndCurrentState = framesAndStateMap.get(currentAnimation);
					
					walk = bufferedImages[framesAndCurrentState.getState()];
				}
			}	
		}else {
			for (String string : keySet) {
				if(string.contains("walk")&&string.contains("down")) {
					currentAnimation = string;
					BufferedImage[] bufferedImages = animationMap.get(currentAnimation);
					framesAndCurrentState framesAndCurrentState = framesAndStateMap.get(currentAnimation);
					
					walk = bufferedImages[framesAndCurrentState.getState()];
				}
			}
		}
		

		
	
	return walk;
}







@Override
public BufferedImage update(String key, boolean gameMode,boolean prompt) {
	// TODO Auto-generated method stub
	return null;
}




}
