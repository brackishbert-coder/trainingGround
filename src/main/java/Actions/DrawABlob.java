package Actions;

import java.awt.Color;

import FlatLandStructure.ViewableFlatLand;
import FlatLander.FlatLander;

public class DrawABlob extends Actions {

	private ViewableFlatLand universe;
	private ActionStatus actionStatus;

	public DrawABlob(FlatLander actor,ViewableFlatLand universe) {
		super(actor);
		this.universe = universe;
		actionStatus = ActionStatus.UNSTARTED;
	}

	@Override
	public void act() {
		actionStatus = ActionStatus.STARTED;
		Wonder wonder = new Wonder(actor);

		universe.setColor(actor, Color.RED);
		wonder.act();
		wonder.act();
		wonder.act();
		wonder.act();
		wonder.act();
		universe.setColor(actor, Color.GREEN);
		wonder.act();
		wonder.act();
		wonder.act();
		wonder.act();
		wonder.act();
		wonder.act();
		universe.setColor(actor, Color.PINK);
		actionStatus = ActionStatus.COMPLETE;
	}

	public ActionStatus getActionStatus() {
		
		return actionStatus;
	}


}
