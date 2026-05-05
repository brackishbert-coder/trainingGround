package Rules;

import TheGame.Board;
import TheGame.BoardSpace;
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
