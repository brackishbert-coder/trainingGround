package theStart;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;

import FlatLandStructure.ViewableFlatLand;
import FlatLander.FlatLandFacebook;
import Notes.Notes;
import View.FlatLandWindow;

import XMLLEVELLOADER.PlayerWrper;
import XMLLEVELLOADER.XmlLevelLoader;
import flatLand.trainingGround.EventHandler;
import flatLand.trainingGround.FlatLandSelector;
import flatLand.trainingGround.GAMSTATUS;
import flatLand.trainingGround.GameStatus;
import flatLand.trainingGround.Level;
import flatLand.trainingGround.Sprites.SkeletonTwo;
import flatLand.trainingGround.theStudio.Camera;
import theStart.theView.WebcamUpdater;
import theStart.theView.TheControls.GameScreen;
import theStart.theView.TheControls.TheStartCamera;

public class StartingPoint {

	private static FlatLandWindow flatLandWindow;
	private static ViewableFlatLand flatLand;
	private static int cameraWidth = 512;
	private static int cameraHeight = 400;
	private static int flatLandWidth = 1024;
	private static int flatLandHeight = 800;
	private static int canvasWidth = 1024;
	private static int canvasHeight = 800;;
	private static Camera theEyeInTheSky;
	private static TheStartCamera theStartCamera;
	private static FlatLandSelector flatLandSelector;
	private static PlayerWrper player0;
	private static GameScreen panel;
	private static EventHandler events;
	private static WebcamUpdater webcamUpdater = new WebcamUpdater();

	public static void main(String[] args) {
		ArrayList<GAMSTATUS> list =new ArrayList<GAMSTATUS>();
		list.add(GAMSTATUS.BRAIN);
		list.add(GAMSTATUS.DEBUG);
		
		GameStatus.getInstance().setStatus(list);
		Thread thread = new Thread(webcamUpdater);
		thread.start();
		events = new EventHandler();

		int conut = 0;
		flatLand = initializeFlatLand();
		player0 = initializePlayer(flatLand);
		initializeCamera(flatLand, player0);
		panel = initializePanel(flatLand, player0);

		new XmlLevelLoader("res/level" + conut + ".xml", events, flatLand, panel);
		Notes NOTES = new Notes();
		NOTES.pack();
		NOTES.setVisible(true);
		refreshNotes(NOTES);
		flatLandWindow = new FlatLandWindow("Hello World", flatLand, panel, canvasWidth, canvasHeight);
		while (conut <= 3) {
			Level level = new Level(flatLandWindow);

			level.loadLevel(events, flatLand, player0, panel, cameraWidth, cameraHeight, canvasHeight, theEyeInTheSky);

			level.start();
			conut++;
			if (conut <= 3) {
				FlatLandFacebook inst = FlatLandFacebook.getInstance();
				inst.clear();
				events.clearEvents();
				flatLand = initializeFlatLand();
				player0 = initializePlayer(flatLand);
				initializeCamera(flatLand, player0);
				panel = initializePanel(flatLand, player0);
				flatLandWindow.setflatLand(flatLand);
				flatLandWindow.setPanel(panel);

			
				new XmlLevelLoader("res/level" + conut + ".xml", events, flatLand, panel);

			}

		}
		
		flatLandWindow.dispose();
		NOTES.dispose();
	System.exit(0);

	}

	
	
	
	private static void refreshNotes(Notes NOTES) {
		NOTES.repaint();
	}
	private static ViewableFlatLand initializeFlatLand() {
		return new ViewableFlatLand(flatLandWidth, flatLandHeight, true);
	}

	private static PlayerWrper initializePlayer(ViewableFlatLand flatLand2) {
		PlayerWrper mel = new PlayerWrper(Color.BLUE, 50, 0, "wes", 0, true);
		mel.buildTerminal(flatLand2, events);
		try {
			mel.setSprite(new SkeletonTwo("res/zombie_n_skeleton2.png", 100));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mel;
	}

	private static void initializeCamera(ViewableFlatLand flatLand, PlayerWrper mel) {
		int posX = (canvasWidth / 2) - cameraWidth / 2;
		int posY = (canvasHeight / 2) - cameraHeight / 2;
		theEyeInTheSky = new Camera(flatLand, cameraWidth, cameraHeight, canvasWidth, canvasHeight, posX, posY, mel);
		theStartCamera = new TheStartCamera(cameraWidth, cameraHeight, posX, posY, flatLand, 16777214, 25, null);
	}

	private static GameScreen initializePanel(ViewableFlatLand flatLand, PlayerWrper mel) {
		GameScreen panel = new GameScreen(canvasWidth, canvasHeight, GameStatus.getInstance());
		panel.setTheEyeInTheSky(theEyeInTheSky);
		panel.setTheStartCamera(theStartCamera);

		return panel;
	}
}
