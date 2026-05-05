package MonopolyActions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import Player.Player;
import TheGame.BoardSpace;
import TheGame.Status;
import XMLLEVELLOADER.PlayerWrper;

public class Morgatge implements RoundAction {

	private PlayerWrper player;
	private Status status;
	private ArrayList<BoardSpace> freeSpaces;
	private HashMap<PlayerWrper, ArrayList<BoardSpace>> takenSpaces;
	private boolean resolved;

	public Morgatge(PlayerWrper player, Status status, ArrayList<BoardSpace> freeSpaces,
			HashMap<PlayerWrper, ArrayList<BoardSpace>> takenSpaces) {
		this.player = player;
		this.status = status;
		this.freeSpaces = freeSpaces;
		this.takenSpaces = takenSpaces;
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
			if (bsToRemove != null) {
				System.err.println("player " + player.getName() + " Morgatged " + bsToRemove.getStatus().name());
			}
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
		return resolved;
	}

}
