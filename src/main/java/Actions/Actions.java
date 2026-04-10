package Actions;

import FlatLander.FlatLander;

public abstract class Actions implements ActionsInterface {

	FlatLander actor;

	public Actions(FlatLander actor) {
		this.actor = actor;
		
	}



	public abstract void act();

}
