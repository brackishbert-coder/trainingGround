package Math;

public class GameDice {
	
	
	
	private static GameDice gamedice;
	private static Die firstdie = new Die();
	private static Die seconddie = new Die();
	private static int lastRoll = 0;
	private static boolean b =true;
	
	
	
	
	public static GameDice getInstance() {
		if(gamedice==null) {
		gamedice = new GameDice();
		}
		return gamedice;
	}
	
	public static int roll() {
		int roll = firstdie.roll()+seconddie.roll();
		lastRoll=roll;
		return roll;
	}

	public static int getLastRoll() {
		return lastRoll;
	}

	public static boolean nextTurn() {		
		return b;
	}

	

	public static void setNextTurn(boolean b) {
		GameDice.b = b;
	}
	

}
