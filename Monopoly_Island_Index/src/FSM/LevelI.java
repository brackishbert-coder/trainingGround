package FSM;

import FlatLandStructure.ViewableFlatLand;
import TheGame.Board;
import theStart.theView.TheControls.GameScreen;

public interface LevelI {

	boolean play(ViewableFlatLand flatLandLE, Board board, GameScreen canvasLE);

}
