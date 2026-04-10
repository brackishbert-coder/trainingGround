package Actions;

import FlatLander.FlatLander;

public class Wonder extends Actions {
	
	private ActionStatus actionStatus;

	public Wonder(FlatLander actor) {
		super(actor);
		actionStatus=ActionStatus.UNSTARTED;
	}
	
	@Override
	public void act() {
		actionStatus=ActionStatus.STARTED;
		
		double input = Math.random()*6.28319;
		actor.direction=input;
		actor.setMoveX((int)Math.round(Math.cos(actor.direction)));
		actor.setMoveY((int)Math.round(Math.sin(actor.direction)));
		
		
		actionStatus=ActionStatus.COMPLETE;

	}

	public ActionStatus getActionStatus() {
		
		return actionStatus;
	}

}
