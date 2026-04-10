package Actions;

import java.util.ArrayList;

public class ActionStack {
	private ArrayList<ActionsInterface> actions;
	private ArrayList<ActionsInterface>completedActions;

	public ActionStack(ArrayList<ActionsInterface> act) {
		actions = act;
		completedActions= new ArrayList<ActionsInterface>();
	}

	public ActionsInterface getPastAction(int index) {
		if(index>=0&&index<completedActions.size()) {
			return completedActions.get(index);
		}
		return new AVaugeSenseOfHavingDoneSomething();
	}
	
	
	public ActionsInterface getFirstPastActionOfClassType() {
		
		for (ActionsInterface action : completedActions) {
			if(action instanceof DrawAProtoCloud)
				return action;
		}
		
		return new AVaugeSenseOfHavingDoneSomething();
	}
	
	
	
	
	public void addActiontoEnd(ActionsInterface act){
		actions.add(act);
	}
	
	public void addActiontoBeginning(ActionsInterface act){
		actions.add(0,act);
	}
	
	
	public void addActionInRandomOrder(ActionsInterface act){
		actions.add((int)(Math.random()*actions.size()-1),act);
	}
	
	
	
	
	public int act() {
		if (actions.size() > 0) {
			ActionsInterface action = actions.get(0);
			action.act();
			if(action.getActionStatus() == ActionStatus.COMPLETE ) {
				actions.remove(0);
				completedActions.add(action);
				
			}
			return 1;
		}else {
			return -1;
		}
	}

}