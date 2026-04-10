package Actions;

import FlatLander.FlatLander;

public class MoveByXY extends Actions {

	private ActionStatus actionStatus;
	private int moveByX;
	private int moveByY;
	private double newAngle;

	public MoveByXY(FlatLander actor,int moveByX,int moveByY,double newAngle) {
		super(actor);
		this.moveByX = moveByX;
		this.moveByY = moveByY;
		this.newAngle=newAngle;
		actionStatus = ActionStatus.UNSTARTED;

	}

	public ActionStatus getActionStatus() {

		return actionStatus;
	}

	@Override
	public void act() {

		actionStatus = ActionStatus.STARTED;
		actor.direction=this.newAngle;
		actor.setMoveX(moveByX);
		actor.setMoveY(moveByY);
		actionStatus = ActionStatus.COMPLETE;
	}

}
