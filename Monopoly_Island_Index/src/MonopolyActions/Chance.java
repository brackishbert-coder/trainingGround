package MonopolyActions;

import java.util.ArrayList;
import java.util.HashMap;

import Player.Player;
import TheGame.BoardSpace;
import TheGame.Status;
import XMLLEVELLOADER.PlayerWrper;
import testing.VariableRepository;

public class Chance implements RoundAction {



	private boolean resolved;
	private PlayerWrper player;
	private Status status;
	private ArrayList<BoardSpace> freeSpaces;
	private HashMap<PlayerWrper, ArrayList<BoardSpace>> takenSpaces;

	public Chance(PlayerWrper player, Status status, ArrayList<BoardSpace> freeSpaces,
			HashMap<PlayerWrper, ArrayList<BoardSpace>> takenSpaces) {
				this.player = player;
				this.status = status;
				this.freeSpaces = freeSpaces;
				this.takenSpaces = takenSpaces;
				
	}

	@Override
	public void execute() {
		System.out.println("landed on "+status.name());
		VariableRepository instance = VariableRepository.getInstance();
		boolean but0 = instance.isBut0();
		boolean but1 = instance.isBut1();
		while(!but0 && !but1) {
			
			
			but0 = instance.isBut0();
			but1 = instance.isBut1();
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
		if(but0)
			System.out.println("played Chance");
		else if(but1)
			System.out.println("didnt play Chance");
		instance.setBut0(false);
		instance.setBut1(false);
	resolved = true;

	}

	@Override
	public boolean isResolved() {
		// TODO Auto-generated method stub
		return resolved;
	}

}
