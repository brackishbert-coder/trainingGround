package Actions;

import java.awt.Color;
import java.util.EmptyStackException;

import Constructs.Construct;
import Constructs.Point;
import FlatLandStructure.ViewableFlatLand;
import FlatLander.FlatLander;


public class DrawArc extends Actions {
	private ViewableFlatLand universe;
	private ActionStatus actionStatus;

	public DrawArc(FlatLander actor, ViewableFlatLand universe) {
		super(actor);
		this.universe = universe;
		actionStatus= ActionStatus.UNSTARTED;
	}

	@Override
	public void act() {
		actionStatus = ActionStatus.STARTED;
		int originalX = actor.getX();
		int originalY = actor.getY();
		try {
			Construct popMemory1 = actor.popMemory();
			Construct popMemory2 = actor.popMemory();
			Point point1=new Point(0,0);
			Point point2=new Point(0,0);
			if(popMemory1 instanceof Point) {
				point1= (Point)popMemory1;
			}
			if(popMemory2 instanceof Point) {
				point2= (Point)popMemory2;
			}
			
			double distForPoint1 = Math.sqrt(Math.pow((point1.getX()-originalX), 2)+Math.pow((point1.getY()-originalY), 2));
			double distForPoint2 = Math.sqrt(Math.pow((point2.getX()-originalX), 2)+Math.pow((point2.getY()-originalY), 2));
			
			int r = 0;
			double angleRelativeToX=0.0;
			int point1X=0;
			int point1Y=0;
			if(distForPoint1<distForPoint2) {
				angleRelativeToX = Math.atan2(point1.getY()-originalY, point1.getX()-originalX);
				r=(int)distForPoint1;
				point1X = point1.getX();
				point1Y = point1.getY();
				
			}else {
				angleRelativeToX = Math.atan2(point2.getY()-originalY, point2.getX()-originalX);
				r=(int)distForPoint2;
				point1X = point2.getX();
				point1Y = point2.getY();
			}
			
			
			double angle1 = angleRelativeToX;
			double angle2 = angle1 + Math.random() * 6.28319;
			
			int point2X = (int) (originalX + r * Math.cos(angle2));
			int point2Y = (int) (originalY + r * Math.sin(angle2));


			drawArc(r, angle1, angle2, originalX, originalY, .01);
			actor.pushtoMemory(new Point(point1X, point1Y));
			actor.pushtoMemory(new Point(point2X, point2Y));
			
			
			

		} catch (EmptyStackException e) {
			int r = (int)(Math.random()*90)+10;
			
			
			double angle1 = Math.random() * 6.28319;
			double angle2 = angle1 + Math.random() * 6.28319;
			
			int point1X = (int) (originalX + r * Math.cos(angle1));
			int point1Y = (int) (originalY + r * Math.sin(angle1));
			
			int point2X = (int) (originalX + r * Math.cos(angle2));
			int point2Y = (int) (originalY + r * Math.sin(angle2));


			drawArc(r, angle1, angle2, originalX, originalY, .01);
			actor.pushtoMemory(new Point(point1X, point1Y));
			actor.pushtoMemory(new Point(point2X, point2Y));
		}

		

		actor.setX(originalX);
		actor.setY(originalY);
		
		actionStatus = ActionStatus.COMPLETE;

	}



	private void drawArc(int r, double theta, double angle2, int originalX, int originalY, double step) {
		while (theta < angle2) {

			actor.setX( (int) (originalX + r * Math.cos(theta)));
			actor.setY((int) (originalY + r * Math.sin(theta)));

			Color color = new Color((int)(Math.random()*255),(int)(Math.random()*255),(int)(Math.random()*255),255);
			while(color == Color.white){
				color = new Color((int)(Math.random()*255),(int)(Math.random()*255),(int)(Math.random()*255),255);
			}
			
			
			universe.setColor(actor, color);
			theta += step;
		}
	}

	public ActionStatus getActionStatus() {
		
		return actionStatus;
	}

}
