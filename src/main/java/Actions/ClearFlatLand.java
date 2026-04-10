package Actions;

import FlatLandStructure.ViewableFlatLand;
import FlatLander.FlatLander;

public class ClearFlatLand extends Actions {

	private ViewableFlatLand universe;
	private ActionStatus actionStatus;
	
	public ClearFlatLand(FlatLander actor,ViewableFlatLand universe) {
		super(actor);
		this.universe = universe;
		actionStatus = ActionStatus.UNSTARTED;
		
	}

	@Override
	public void act() {
		actionStatus = ActionStatus.STARTED;
		universe.clearFlatLand();
		actionStatus = ActionStatus.COMPLETE;
	}

	public ActionStatus getActionStatus() {
		
		return actionStatus;
	}

	

}
