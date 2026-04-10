package Drawing;

import java.awt.image.BufferedImage;

public class ImageStackEntry {
	private BufferedImage img;
	private int flatLandPositionX;
	private int flatLandPositionY;
	private int width;
	private int height;
	private String MetaData;
	private Integer timeTillDeath;
	private Boolean hasBeenDrawn = false;
	private Boolean redraw =true;
	private ImageType imgType;
	
	public ImageStackEntry(BufferedImage imageTorender,int flx,int fly,ImageType imgType,Integer dtd) {
		this.imgType = imgType;
		setImg(imageTorender);
		setWidth(getImg().getWidth());
		setHeight(getImg().getHeight());
		setFlatLandPositionX(flx);
		setFlatLandPositionY(fly);
		setTimeTillDeath(dtd);
		
	}

	public BufferedImage getImg() {
		return img;
	}

	public void setImg(BufferedImage img) {
		this.img = img;
	}

	public int getFlatLandPositionX() {
		return flatLandPositionX;
	}

	public void setFlatLandPositionX(int flatLandPositionX) {
		this.flatLandPositionX = flatLandPositionX;
	}

	public int getFlatLandPositionY() {
		return flatLandPositionY;
	}

	public void setFlatLandPositionY(int flatLandPositionY) {
		this.flatLandPositionY = flatLandPositionY;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getMetaData() {
		return MetaData;
	}

	public void setMetaData(String metaData) {
		MetaData = metaData;
	}

	public Integer getTimeTillDeath() {
		return timeTillDeath;
	}

	public void setTimeTillDeath(Integer timeTillDeath) {
		this.timeTillDeath = timeTillDeath;
	}

	public Boolean getHasBeenDrawn() {
		return hasBeenDrawn;
	}

	public void setHasBeenDrawn(Boolean hasBeenDrawn) {
		this.hasBeenDrawn = hasBeenDrawn;
	}

	public Boolean getRedraw() {
		return redraw;
	}

	public void setRedraw(Boolean redraw) {
		this.redraw = redraw;
	}

	public ImageType getImgType() {
		return imgType;
	} 
	
	

}
