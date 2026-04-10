package Actions;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import Drawing.ImagePile;
import Drawing.ImageStackEntry;
import Drawing.ImageType;
import FlatLandStructure.ViewableFlatLand;
import FlatLander.FlatLander;

public class DrawAProtoCloud extends Actions {

	private ViewableFlatLand universe;
	private ActionStatus actionStatus;
	
	private int orininglYforBox;
	private int orininglXforBox;

	public DrawAProtoCloud(FlatLander actor, ViewableFlatLand universe) {
		super(actor);
		this.universe = universe;
		actionStatus = ActionStatus.UNSTARTED;
	}

	@Override
	public void act() {
		
		actionStatus = ActionStatus.STARTED;
		int originalX = actor.getX();
		int originalY = actor.getY();

		double thetaBig = 0.0;
		double stepBig = .25;
		int rBig = (int) (Math.random() * 90) + 10;
		int rSmallMax = (int) (rBig / 3) + 5;

		int rTotal = rBig + rSmallMax;

		Color workZoneColor = Color.cyan;
		Color cloudColor = Color.ORANGE;
		Color workLineColor = Color.RED;

		BufferedImage img = demarkateWorkZone(rTotal, workZoneColor);
		Graphics graphics = img.getGraphics();
		
		while (thetaBig < 360) {
			graphics.setColor(workLineColor);
			int x = (int)(rTotal+rBig * Math.cos(thetaBig));
			int y = (int)(rTotal+rBig * Math.sin(thetaBig));
			graphics.drawRect(x, y, 1, 1);
	
			thetaBig += stepBig;
			actor.setX((int) (originalX + rBig * Math.cos(thetaBig)));
			actor.setY((int) (originalY + rBig * Math.sin(thetaBig)));
			drawSmallCircle(thetaBig, 20, rBig, cloudColor,graphics,originalX,originalY,rTotal);

		}
		

		graphics.dispose();
		actor.setX( originalX);
		actor.setY(originalY);
		ImagePile.getInstance().addToPile(new ImageStackEntry(img, orininglXforBox, orininglYforBox,ImageType.MAGICMEDIUM,10));
		
		actionStatus = ActionStatus.COMPLETE;
	}




	private BufferedImage demarkateWorkZone(int rTotal, Color workZoneColor) {

		actor.setX(actor.getX()- rTotal);
		actor.setY(actor.getY() - rTotal);
		this.orininglXforBox = universe.getXBoundedAndWraped(actor);
		this.orininglYforBox = universe.getYBoundedAndWraped(actor);

		BufferedImage img = new BufferedImage(2 * rTotal, 2 * rTotal, BufferedImage.TYPE_INT_ARGB);
		Graphics graphics =img.getGraphics();
		graphics.setColor(workZoneColor);
		graphics.fillRect(0, 0, 2 * rTotal, 2 * rTotal);
		graphics.dispose();
		
		return img;
		

	}

	private void drawSmallCircle(double thetaBig, int mod, int rBig, Color cloudColor, Graphics graphics, int originalX, int originalY, int rTotal) {
		double thetaSmall = 0.0;
		double stepSmall = .01;
		int rSmall = (int) (Math.random() * (rBig / 3)) + 5;

		int tempOriginalX = actor.getX();
		int tempOriginalY = actor.getY();

		if (thetaBig % mod == 0) {
			while (thetaSmall < 360) {
				graphics.setColor(cloudColor);
				graphics.drawRect((int)((tempOriginalX-originalX) +rTotal+rSmall * Math.cos(thetaSmall)),(int)((tempOriginalY-originalY)+rTotal+ rSmall * Math.sin(thetaSmall)), 1, 1);
				thetaSmall += stepSmall;
			}
		}

	}

	public ActionStatus getActionStatus() {

		return actionStatus;
	}

	public int getOrininglXforBox() {
		return orininglXforBox;
	}


	public int getOrininglYforBox() {
		return orininglYforBox;
	}

	public void setOrininglYforBox(int orininglYforBox) {
		this.orininglYforBox = orininglYforBox;
	}



}
