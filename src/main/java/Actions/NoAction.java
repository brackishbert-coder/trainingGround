package Actions;

import FlatLander.FlatLander;

public class NoAction extends Actions {
	private ActionStatus actionStatus;
	
	public NoAction(FlatLander flatLander) {
		super(flatLander);
		actionStatus = ActionStatus.UNSTARTED;
	}

	@Override
	public void act() {
		actionStatus = ActionStatus.STARTED;
		actionStatus = ActionStatus.COMPLETE;
	}

	public ActionStatus getActionStatus() {
		
		return actionStatus;
	}

}
