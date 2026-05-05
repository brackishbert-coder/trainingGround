package FSM;

import java.awt.Color;

import FlatLandStructure.ViewableFlatLand;
import TheGame.Board;
import XMLLEVELLOADER.PlayerWrper;
import flatLand.trainingGround.theStudio.Camera;
import theStart.theView.TheControls.GameScreen;

public abstract class Level {

	private static Camera theEyeInTheSky;
	private static int cameraWidth = 1300;
	private static int cameraHeight = 1300;
	private static int flatLandWidth = 1300;
	private static int flatLandHeight = 1300;
	private static int canvasWidth = 1300;
	private static int canvasHeight = 1300;
	private static int oncanvasX = 0;
	private static int oncanvasY = 0;

	public LevelStory levelStory;

	public boolean play(ViewableFlatLand flatLandLE, Board board, GameScreen panel) {

		flatLandLE.setFlatLandColor(Color.green);
		while (!board.hasWinner()) {
			for (PlayerWrper player : board.getPlayers()) {

				panel.getGraphics();

				flatLandLE.update();

				panel.repaint();

					if(!board.previousRoundActionResolved() && board.getCurrentTurn() == board.getPlayersTurnOrder(player)) {
						board.executeActions(player);
						
					
					}
					if(board.previousRoundActionResolved()&&board.getCurrentTurn() == board.getPlayersTurnOrder(player)) {
						
						board.interpretRules(player);
						board.setCurrentTurn(board.getCurrentTurn()+1);
						board.collectActions(player);
					}
					
			}

		}

		return false;
	}
}
