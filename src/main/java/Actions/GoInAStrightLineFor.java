package Actions;

import FlatLander.FlatLander;

public class GoInAStrightLineFor extends Actions {

	private ActionStatus actionStatus;
	private  int distance;
	private  int distanceTracker=0;
	public GoInAStrightLineFor(FlatLander actor,int distance) {
		super(actor);
		this.distance = distance;
		actionStatus = ActionStatus.UNSTARTED;
	}

	@Override
	public void act() {
		actionStatus = ActionStatus.STARTED;
		actor.setMoveX((int)Math.round(Math.cos(actor.direction)));
		actor.setMoveY((int)Math.round(Math.sin(actor.direction)));
		
		
		if(distanceTracker<distance) {
			distanceTracker++;
		}else
			actionStatus = ActionStatus.COMPLETE;
	}


	public ActionStatus getActionStatus() {
		
		return actionStatus;
	}


}
