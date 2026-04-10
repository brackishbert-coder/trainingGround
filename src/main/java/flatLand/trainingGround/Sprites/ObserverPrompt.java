package flatLand.trainingGround.Sprites;

import Box.Box.Box;
import Box.Box.Observer;
import Box.GameSpaceInterpreter.SandBox;

public class ObserverPrompt implements Observer {

	
	
	private SandBox box;



	public ObserverPrompt(SandBox box) {
		this.box = box;
		}
	
	
	
	@Override
	public void notify(String string) {
		box.notify(string);
	}

}
