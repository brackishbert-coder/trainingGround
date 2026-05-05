package MonopolyActions;

import java.util.ArrayList;
import java.util.HashMap;

import Player.Player;
import TheGame.BoardSpace;
import TheGame.Status;
import XMLLEVELLOADER.PlayerWrper;

public class CCSquare implements EventAction {

	private boolean resolved;
	private PlayerWrper player;
	private Status status;
	private ArrayList<BoardSpace> freeSpaces;
	private HashMap<PlayerWrper, ArrayList<BoardSpace>> takenSpaces;

	public CCSquare(PlayerWrper player, Status status, ArrayList<BoardSpace> freeSpaces,
			HashMap<PlayerWrper, ArrayList<BoardSpace>> takenSpaces) {
				this.player = player;
				this.status = status;
				this.freeSpaces = freeSpaces;
				this.takenSpaces = takenSpaces;
		// TODO Auto-generated constructor stub
	}

	public RoundAction get(Player player) {
		// TODO Auto-generated method stub
		return null;
	}

	public void execute() {
		System.out.println("landed on "+status.name());
resolved=true;
	}

	@Override
	public boolean isResolved() {
		// TODO Auto-generated method stub
		return resolved;
	}

}
