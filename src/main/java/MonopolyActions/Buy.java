package MonopolyActions;

import java.util.ArrayList;
import java.util.HashMap;

import TheGame.BoardSpace;
import TheGame.Status;
import XMLLEVELLOADER.PlayerWrper;

public class Buy implements RoundAction {



	private Status status;
	private ArrayList<BoardSpace> freeSpaces;
	private HashMap<PlayerWrper, ArrayList<BoardSpace>> takenSpaces;
	private PlayerWrper player;
	private boolean resolved;

	public Buy(PlayerWrper player, Status status, ArrayList<BoardSpace> freeSpaces, HashMap<PlayerWrper, ArrayList<BoardSpace>> takenSpaces2) {
		this.player = player;
		this.status = status;
		this.freeSpaces = freeSpaces;
		this.takenSpaces = takenSpaces2;
	}

	
	
	private int indexOfSpace(Status status) {
		for (int i = 0; i < freeSpaces.size(); i++) {
			if(freeSpaces.get(i).getStatus()==status)
				return i;
		}
		return -1;
	}
	
	
	
	@Override
	public void execute() {
		int i = indexOfSpace(status);
		if(i>=0) {
			BoardSpace remove = freeSpaces.remove(i);
			ArrayList<BoardSpace> arrayList = takenSpaces.get(player);
			if(arrayList==null) {
				ArrayList<BoardSpace> newArr = new ArrayList<BoardSpace>();
				newArr.add(remove);
				takenSpaces.put(player,newArr);
			}else {
				arrayList.add(remove);
				takenSpaces.remove(player);
				takenSpaces.put(player, arrayList);
			}
			System.out.println("player "+player.getName()+" purchased "+ remove.getStatus().name());
			resolved = true;
		}else {
			resolved = true;
		}

	}

	@Override
	public boolean isResolved() {
		return resolved;
	}

}
