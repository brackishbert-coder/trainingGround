package Rules;

import TheGame.Board;
import TheGame.BoardSpace;
import XMLLEVELLOADER.PlayerWrper;

public interface TheRules {
	public void check(PlayerWrper player,BoardSpace boardSpace,Board board);
}
