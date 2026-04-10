package Actions;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import Constructs.Point;
import Drawing.ImagePile;
import Drawing.ImageStackEntry;
import Drawing.ImageType;
import FlatLandStructure.ViewableFlatLand;
import FlatLander.FlatLander;

public class DrawArcFasterVersion1 extends Actions {

	private ActionStatus actionStatus;
	private ViewableFlatLand universe;
	private static int originalX = 0;
	private static int originalY = 0;
	private static int localActorOriginalX = 0;
	private static int localActorOriginalY = 0;
	private static int  flatLandImageX = 0;
	private static int flatLandImageY = 0;
	BufferedImage img = null;

	public DrawArcFasterVersion1(FlatLander actor, ViewableFlatLand universe) {
		super(actor);
		this.universe = universe;
		actionStatus = ActionStatus.UNSTARTED;

	}

	public ActionStatus getActionStatus() {

		return actionStatus;
	}

	@Override
	public void act() {
		actionStatus = ActionStatus.STARTED;
		originalX = actor.getX();
		originalY = actor.getY();

		Point point1 = new Point(originalX, originalY);
		Point point2 = new Point(originalX + 10, originalY + 10);

		int distance = 75;
		point1 = new Point(originalX, originalY);

		int widthOfImg = distance * 2 + 100;
		int heightOfImg = distance * 2 + 100;
		flatLandImageX = originalX;
		flatLandImageY = originalY;
		img = new BufferedImage(widthOfImg, heightOfImg, BufferedImage.TYPE_INT_ARGB);

		double degreeOriginal = radiansToDegrees(actor.direction);
		int height = 75;

		double arcAngle = Math.random()*2.79253;

		int flatLandCenterOfTheCircleX = point1.getX() + distance / 2;
		int flatLandCenterOfTheCircleY = point1.getY() + distance / 2;
		int radiusofCircle = height / 2;

		double point2X = flatLandCenterOfTheCircleX + radiusofCircle * Math.cos(actor.direction);
		double point2Y = flatLandCenterOfTheCircleY + radiusofCircle * Math.sin((actor.direction));

		int distanceY = (int) (point2Y - flatLandCenterOfTheCircleY);
		point2Y = point2Y - 2 * distanceY;

		double point3X = flatLandCenterOfTheCircleX + radiusofCircle * Math.cos((actor.direction) + arcAngle);
		double point3Y = flatLandCenterOfTheCircleY + radiusofCircle * Math.sin((actor.direction) + arcAngle);

		distanceY = (int) (point3Y - flatLandCenterOfTheCircleY);
		point3Y = point3Y - 2 * distanceY;

		Graphics graphics = img.getGraphics();
		
		//graphics.fillRect(0, 0, img.getWidth(), img.getHeight());
		graphics.setColor(Color.green);
		graphics.drawArc(getLocalX(point1.getX()), getLocalY(point1.getY()), distance, height,
				(int) radiansToDegrees(actor.direction), (int) radiansToDegrees(arcAngle));
		graphics.setColor(Color.green);
		graphics.fillRect(getLocalX(flatLandCenterOfTheCircleX), getLocalY(flatLandCenterOfTheCircleY), 5, 5);
		graphics.setColor(Color.BLUE);
		graphics.fillRect(getLocalX((int) point2X), getLocalY((int) point2Y), 5, 5);

		graphics.setColor(Color.RED);
		graphics.drawLine(getLocalX(flatLandCenterOfTheCircleX), getLocalY(flatLandCenterOfTheCircleY),
				getLocalX((int) point2X), getLocalY((int) point2Y));
		graphics.setColor(Color.MAGENTA);
		graphics.drawLine(getLocalX(flatLandCenterOfTheCircleX), getLocalY(flatLandCenterOfTheCircleY),
				getLocalX((int) point3X), getLocalY((int) point3Y));
		graphics.setColor(Color.RED);
		graphics.fillRect(getLocalX((int) point3X), getLocalY((int) point3Y), 5, 5);

		graphics.dispose();

		if (img != null) {
			ImagePile.getInstance()
					.addToPile(new ImageStackEntry(img, flatLandImageX, flatLandImageY,ImageType.MAGICMEDIUM, Integer.MAX_VALUE));
			actionStatus = ActionStatus.COMPLETE;
			
		}

	}

	private double radiansToDegrees(double toconvert) {
		return (double) (toconvert) * 180 / Math.PI;
	}

	private int getLocalX(int nonLocal) {
		return nonLocal - originalX;
	}

	private int getLocalY(int nonLocal) {
		return nonLocal - originalY;
	}

	private int getflatLandImageY(int Y) {
		return Y - localActorOriginalY;
	}

	private int getflatLAndImageX(int X) {
		return X - localActorOriginalX;
	}

}
