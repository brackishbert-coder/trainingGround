package MonopolyActions;

import Player.Player;
import TheGame.Board;
import XMLLEVELLOADER.PlayerWrper;

public class Move implements RoundAction {

	private PlayerWrper player;
	private Board board;
	private int xFinal;
	private int yFinal;
	private int nex;
	private int ney;
	private  boolean resolved = false;

	public Move(int x, int y, PlayerWrper player, Board board) {
		this.xFinal = x;
		this.yFinal = y;
		this.player = player;
		this.board = board;
		nex = xFinal - board.getPlayerCurrentPositionX(player);
		ney = yFinal - board.getPlayerCurrentPositionY(player);
	}

	@Override
	public void execute() {
		if (player.getX() != xFinal) {
				player.setX(xFinal);;

		}

		if (player.getY() != yFinal) {
				player.setY(yFinal);
		}

		if (player.getX() == xFinal && player.getY() == yFinal)
			resolved = true;
	}

	public boolean isResolved() {
		return resolved;
	}

}
