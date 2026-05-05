package Rules;

import MonopolyActions.RoundAction;
import Player.Player;
import TheGame.Board;
import TheGame.BoardSpace;
import TheGame.PlayerChance;
import TheGame.PlayerCommieChest;
import TheGame.PlayerPositions;
import TheGame.PlayerSpaces;
import TheGame.Status;
import XMLLEVELLOADER.PlayerWrper;

public class RuleInterpreter {

	private BoardRules rules;

	public RuleInterpreter(BoardRules rules) {
		this.rules = rules;
	}

	public void checkRules(PlayerWrper player,BoardSpace boardSpace,Board board) {

		rules.check(player,boardSpace,board);
		
		
		
		
	}

}
