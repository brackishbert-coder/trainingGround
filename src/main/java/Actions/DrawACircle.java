package Actions;

import java.awt.Color;

import FlatLandStructure.ViewableFlatLand;
import FlatLander.FlatLander;

public class DrawACircle extends Actions {

	private ViewableFlatLand universe;
	private ActionStatus actionStatus;

	public DrawACircle(FlatLander actor, ViewableFlatLand universe) {
		super(actor);
		this.universe = universe;
		actionStatus = ActionStatus.UNSTARTED;
	}

	@Override
	public void act() {
		actionStatus = ActionStatus.STARTED;
		int originalX = actor.getX();
		int originalY = actor.getY();

		double theta = 0.0;
		double step = .01;

		int r = (int) (Math.random() * 90)+10;

		while (theta < 6.28319) {

			actor.setX( (int) (originalX + r * Math.cos(theta)));
			actor.setY((int) (originalY + r * Math.sin(theta)));

			universe.setColor(actor, Color.RED);
			theta += step;
		}
		
		
		actor.setX(originalX);
		actor.setY(originalY);
		actionStatus = ActionStatus.COMPLETE;
	}

	public ActionStatus getActionStatus() {
		
		return actionStatus;
	}

	
}
