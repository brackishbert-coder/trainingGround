package MonopolyActions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import TheGame.BoardSpace;
import TheGame.Status;
import XMLLEVELLOADER.PlayerWrper;

public class Sell implements RoundAction {

	private PlayerWrper player;
	private Status status;
	private ArrayList<BoardSpace> freeSpaces;
	private HashMap<PlayerWrper, ArrayList<BoardSpace>> takenSpaces;
	private boolean resolved;

	public Sell(PlayerWrper player, Status status, ArrayList<BoardSpace> freeSpaces,
			HashMap<PlayerWrper, ArrayList<BoardSpace>> takenSpaces2) {
		this.player = player;
		this.status = status;
		this.freeSpaces = freeSpaces;
		this.takenSpaces = takenSpaces2;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute() {
		if (doesPlayerOwn()) {
			ArrayList<BoardSpace> arrayList = takenSpaces.get(player);
			BoardSpace bsToRemove = null;
			for (BoardSpace boardSpace : arrayList) {
				if (boardSpace.getStatus() == status) {
					bsToRemove = boardSpace;
					break;
				}
			}
			if(bsToRemove!=null) {
				arrayList.remove(bsToRemove);
			}
			takenSpaces.remove(player);
			takenSpaces.put(player, arrayList);
			System.err.println("player "+player.getName()+" sold "+ bsToRemove.getStatus().name());
		}

		resolved = true;

	}

	private boolean doesPlayerOwn() {
		Set<PlayerWrper> keySet = takenSpaces.keySet();
		ArrayList<BoardSpace> arrayList = takenSpaces.get(player);
		if(arrayList!=null) {
		for (BoardSpace boardSpace : arrayList) {
			if (boardSpace.getStatus() == status) {
				return true;
			}

		}}
		return false;
	}

	@Override
	public boolean isResolved() {
		// TODO Auto-generated method stub
		return resolved;
	}

}
