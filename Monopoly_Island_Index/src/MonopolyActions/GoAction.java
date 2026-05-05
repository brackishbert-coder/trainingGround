package MonopolyActions;

import java.util.ArrayList;
import java.util.HashMap;

import TheGame.BoardSpace;
import TheGame.Status;
import XMLLEVELLOADER.PlayerWrper;

public class GoAction implements Actn {

	private PlayerWrper player;
	private Status status;
	private ArrayList<BoardSpace> freeSpaces;
	private HashMap<PlayerWrper, ArrayList<BoardSpace>> takenSpaces;
	private boolean resolved;

	public GoAction(PlayerWrper player, Status status, ArrayList<BoardSpace> freeSpaces,
			HashMap<PlayerWrper, ArrayList<BoardSpace>> takenSpaces) {
				this.player = player;
				this.status = status;
				this.freeSpaces = freeSpaces;
				this.takenSpaces = takenSpaces;
				
	}

	@Override
	public void execute() {System.out.println("landed on "+status.name());
		resolved = true;
	}

	@Override
	public boolean isResolved() {
		return resolved;
	}

}
