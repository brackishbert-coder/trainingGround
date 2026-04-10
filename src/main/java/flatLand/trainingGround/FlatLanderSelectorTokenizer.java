package flatLand.trainingGround;

import FlatLander.FlatLander;



public class FlatLanderSelectorTokenizer {
	private static FlatLanderSelectorTokenizer instance;
	
	
	private static FlatLander flatlanderTokenized = null;
	
	
	public static FlatLanderSelectorTokenizer getInstance() {
		if (instance == null) {
			instance = new FlatLanderSelectorTokenizer();
		}
		return instance;
	}
	
	
	public static FlatLander getFlatLanderTokenized() {
		return flatlanderTokenized;
	}
	public void set(FlatLander flatlander) {
		flatlanderTokenized=flatlander;
	}
}
