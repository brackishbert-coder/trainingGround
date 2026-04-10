package flatLand.trainingGround;

import Box.Box.Observer;
import flatLand.trainingGround.Sprites.TerminalSprite;

public class ScoreObserver implements Observer {

	private TerminalSprite sprite2;

	public ScoreObserver(TerminalSprite sprite2) {
		this.sprite2 = sprite2;
		// TODO Auto-generated constructor stub
	}

	public void notify(String string) {
		sprite2.notify(string);
	}
}
