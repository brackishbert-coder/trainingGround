package Playable;

import java.awt.Color;

import FSM.Level;
import FSM.LevelI;
import FlatLand.Physics.Physics;
import FlatLand.Physics.TypeOfEntity;
import FlatLandStructure.ViewableFlatLand;
import FlatLander.FlatLandFacebook;
import userInput.PlayerKeybordHandler;
import TheGame.Board;
import XMLLEVELLOADER.FlatLanderWrper;
import XMLLEVELLOADER.PlayerWrper;
import flatLand.trainingGround.Sprites.TerminalSprite;
import flatLand.trainingGround.theStudio.Camera;
import theStart.theView.TheControls.GameScreen;

public class LevelPlayer extends Level implements LevelI {
	private static Camera theEyeInTheSky;
	private static int cameraWidth = 1300;
	private static int cameraHeight = 1300;
	private static int flatLandWidth = 1300;
	private static int flatLandHeight = 1300;
	private static int canvasWidth = 1300;
	private static int canvasHeight = 1300;
	private static int oncanvasX = 0;
	private static int oncanvasY = 0;
	String terminalPath = "/home/wes/Wisper Tech 1.0/THEORY/GAMES/Monopoly_Island_Index/resources/charmap-oldschool_white.png";
	PlayerWrper player;
	@Override
	public boolean play(ViewableFlatLand flatLandLE, Board board,GameScreen panel) {
		int posY = (canvasHeight / 2) - cameraHeight / 2;
		new Physics(9.8,1, posY, cameraHeight);

		flatLandLE.setFlatLandColor(Color.ORANGE);
		theEyeInTheSky = new Camera(flatLandLE, cameraWidth, cameraHeight,canvasWidth,canvasHeight, (canvasWidth / 2) - cameraWidth / 2,
				posY);

		FlatLanderWrper terminal = new FlatLanderWrper(100, 100, 0, "Terminal", 1, false, false, TypeOfEntity.TERRAIN, Color.MAGENTA);
		PlayerKeybordHandler kh = new PlayerKeybordHandler(terminal);
		kh.buildKeyBindings(panel);
		terminal.setSprite(new TerminalSprite(terminalPath, 128, 64,100));
		Object theRequestie = new Object();
		while (!FlatLandFacebook.getInstance().requestToken(theRequestie)) {
		}
		FlatLandFacebook.getInstance().add(terminal, theRequestie);
		FlatLandFacebook.getInstance().releaseToken(theRequestie);
		
		
		
		
		
		try {
			boolean firstTime = false;

			


			
				while (true) {
					if (firstTime) {
						flatLandLE.update();

					}
					//panel.getBs().show();
					
					Thread.sleep(0);
				}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
				return false;
	}
	
	
	
	
}
