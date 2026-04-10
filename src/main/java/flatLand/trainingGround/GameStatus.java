package flatLand.trainingGround;

import java.util.ArrayList;

import FlatLander.FlatLander;

public class GameStatus {

	
	
	private static GameStatus instance;
	private ArrayList<GAMSTATUS> status = new ArrayList<GAMSTATUS>(); 
	
	public static GameStatus getInstance() {
		if (instance == null) {
			instance = new GameStatus();
		}
		return instance;
	}

	public boolean isStatus(GAMSTATUS stat) {
		
		for (GAMSTATUS gamstatus : status) {
			if(gamstatus==stat)
				return true;
		} 
		return false;
	}

	public void setStatus(ArrayList<GAMSTATUS> status) {
		this.status = status;
	}
	public void addStatus(GAMSTATUS status) {
		this.status.add(status);
	}

	
	public boolean containsStatus(GAMSTATUS stat) {
		return status.contains(stat);
	}
	
	
	
}
