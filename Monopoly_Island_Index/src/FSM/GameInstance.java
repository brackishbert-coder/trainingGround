package FSM;

import java.util.ArrayList;

import FlatLandStructure.ViewableFlatLand;
import Playable.LevelZero;
import TheGame.Board;
import theStart.theView.TheControls.GameScreen;

public class GameInstance {

	private ArrayList<LevelI> level = new ArrayList<LevelI>();
	private LevelI currentLevel = null;
	private int levelCounter = 0;

	private ViewableFlatLand flatLandLE;
	private Board board;
	private GameScreen graphics;

	public GameInstance(ViewableFlatLand flatLandLE,GameScreen canvasLE, Board board) {
		this.flatLandLE = flatLandLE;
		this.graphics = canvasLE;
		
		this.board = board;
		
		LevelZero lzero = new LevelZero();
		level.add(lzero);
		
		
		
		
	}

	public void start() {

		
		while (levelCounter < level.size()) {

			loadLevel();
			currentLevel.play(flatLandLE, board,graphics);
			levelCounter++;
		}
	}


	private void loadLevel() {
		currentLevel = level.get(levelCounter);
	}

}
