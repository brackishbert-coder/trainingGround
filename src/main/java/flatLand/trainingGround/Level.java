
package flatLand.trainingGround;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import Actions.ActionsInterface;
import Actions.DrawABlob;
import Actions.DrawAProtoCloud;
import Actions.DrawArc;
import Actions.DrawArcFasterVersion1;
import Actions.GoInAStrightLineFor;
import Actions.Wonder;
import Box.GameSpaceInterpreter.SandBox;
import FlatLand.Physics.Physics;
import FlatLand.Physics.UpdateTimeSingleton;
import FlatLandStructure.ViewableFlatLand;
import FlatLander.FlatLandFacebook;
import FlatLander.FlatLander;
import Logging.LOG;
import View.FlatLandWindow;
import XMLLEVELLOADER.PlayerWrper;
import dialogManagement.DialogManager;
import flatLand.trainingGround.theStudio.Camera;

import src.ANT;
import src.Direction;
import src.Simulation;
import src.Tiles;
import src.tile;
import theStart.theView.TheControls.GameScreen;

public class Level extends LOG {
	private static FlatLandWindow flatLandWindow;
	private static String FILENAME = "res/level.xml";
	private static Simulation simulation;
	private static ArrayList<ANT> ant_s_;
	private static Tiles tiles;
	private static ArrayList<Graphics> gQueue = new ArrayList<>();
	private static BigDecimal fPS = BigDecimal.ZERO;
	static FlatLandSelector flatLandSelector;
	protected static int xInitiial;
	protected static int yInitial;
	private static int targetFrameRate = 66;
	private static EventHandler events;
	private static ByteArrayOutputStream baos;
	private static SandBox box;
	static ViewableFlatLand flatLand;
	private static boolean objectiveComplete=false;
	private static int cameraWidth;
	private static int cameraHeight;
	private static int canvasHeight;
	private Physics physics;
	private PlayerWrper player0;
	private GameScreen panel;
	private Camera theEyeInTheSky;



	public Level(FlatLandWindow flw) {
		this.flatLandWindow = flw;

	}

	
	public void loadLevel(EventHandler events2, ViewableFlatLand flatLand2, PlayerWrper player02,
			GameScreen panel,int camWidth,int camHeight,int canvasHeight,Camera theEyeInTheSky) {
		cameraWidth = camWidth;
		cameraHeight = camHeight;
		this.panel = panel;
		this.theEyeInTheSky = theEyeInTheSky;
		initializeLogging();
		flatLandSelector = new FlatLandSelector(theEyeInTheSky, flatLand);
		initializeMouseListeners(panel);
		physics = initializePhysics();
		initializeSimulation(flatLand, player02, panel);
		events = events2;
		player0 = player02;
		flatLand = flatLand2;
	}
	private static Physics initializePhysics() {
		int posY = (canvasHeight / 2) - cameraHeight / 2;
		return new Physics(9.8, 3, posY, cameraHeight);
	}
	
	



	public boolean start() {
		runSimulationLoop(physics, flatLand, player0, panel);
		LOG.close();
		return objectiveComplete;
	}

	private static void initializeLogging() {
//		GameStatus statusInstance = GameStatus.getInstance();
//		statusInstance.addStatus(GAMSTATUS.BRAIN);
		HashMap<String, String> logs = new HashMap<>();
		logs.put("log", "res/folder/");
		LOG.set_current_working_directory("");
		LOG.register_output_forLogging(LOG, logs);
	}


	

	

	

	

	private static void initializeMouseListeners(GameScreen panel) {
		panel.addMouseListener(new MouseHandler());
		panel.addMouseMotionListener(new MouseMotionHandler());
	}

	private static void initializeSimulation(ViewableFlatLand flatLand, PlayerWrper mel, GameScreen panel) {
		
		mel.buildKeyBoardHandler(panel);
		panel.requestFocus();
		ant_s_ = new ArrayList<>();
		tiles = new Tiles(cameraWidth, cameraHeight, Color.WHITE, ant_s_);
		generateBoard();
		generateAnts();
		simulation = new Simulation(true, new Point(0, 0), 0, flatLandWindow, null, cameraWidth, cameraWidth,
				cameraHeight, tiles);
		panel.setFlatLandWindow(flatLandWindow);
		panel.setSimulation(simulation);
	}

	private static void runSimulationLoop(Physics physics, ViewableFlatLand flatLand, PlayerWrper mel,
			GameScreen panel) {
		Object theRequestie = new Object();
		while (!FlatLandFacebook.getInstance().requestToken(theRequestie)) {
		}
		FlatLandFacebook.getInstance().add(mel, theRequestie);
		FlatLandFacebook.getInstance().releaseToken(theRequestie);
		loadandrunlevel1(physics, flatLand, mel, panel);
	}

	private static void intiilizeEvents(ArrayList<GameEvent> events2, PlayerWrper mel) {

		for (GameEvent event : events2) {
			if (event.getType().equalsIgnoreCase("LevelEnd")) {
				if(event.getX()!= mel.x && event.getY() != mel.y) {
                    objectiveComplete = false;
                }
			}
		}
		
		
		
		
		
	}


	private static void loadandrunlevel1(Physics physics, ViewableFlatLand flatLand, PlayerWrper mel,
			GameScreen panel) {
		intiilizeEvents(events.getEvents(),mel);
		FlatLandFacebook instance = FlatLandFacebook.getInstance();
		instance.getFlatlanderFaceBook();
	
		try {
			boolean firstTime = false;

			int fpscount = 0;
			long[] FPSframes = new long[targetFrameRate - 1];
			long[] timeSpentOnUpdate = new long[targetFrameRate - 1];
			long[] timeSpentOnTakingPictureAndDeveloping = new long[targetFrameRate - 1];
			long[] timeSpentOnOther = new long[targetFrameRate - 1];
			LOG.println("run the simulation");
			while (!objectiveComplete) {
				long start = System.currentTimeMillis();
				long startUpdate = System.currentTimeMillis();
				if (firstTime) {
					physics.applyPhysics();
					flatLand.update();
				}
				checkEvents(mel, events);

				flatLandWindow.notifyX("" + mel.x);
				flatLandWindow.notifyY("" + mel.y);
				long endUpdate = System.currentTimeMillis();
				long startPicAndDevelop = System.currentTimeMillis();
				panel.repaint();
				long endPicAndDevelop = System.currentTimeMillis();
				long startOther = System.currentTimeMillis();
				long endOther = System.currentTimeMillis();
				long end = System.currentTimeMillis();
				long length = end - start;
				long lengthUpdate = endUpdate - startUpdate;
				long lengthPicAndDevelop = endPicAndDevelop - startPicAndDevelop;
				long lengthOther = endOther - startOther;
				fpscount++;
				firstTime = true;
				fpscount = calculateFPS(mel, fpscount, FPSframes, timeSpentOnUpdate,
						timeSpentOnTakingPictureAndDeveloping, timeSpentOnOther, length, lengthUpdate,
						lengthPicAndDevelop, lengthOther);
			}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
			LOG.println(e.getMessage());
		}
	}

	private static int calculateFPS(PlayerWrper mel, int fpscount, long[] FPSframes, long[] timeSpentOnUpdate,
			long[] timeSpentOnTakingPictureAndDeveloping, long[] timeSpentOnOther, long length, long lengthUpdate,
			long lengthPicAndDevelop, long lengthOther) throws InterruptedException {
		if (fpscount < targetFrameRate - 1) {
			timeSpentOnUpdate[fpscount] = lengthUpdate;
			timeSpentOnTakingPictureAndDeveloping[fpscount] = lengthPicAndDevelop;
			timeSpentOnOther[fpscount] = lengthOther;
			if (length < 1000 / targetFrameRate) {
				FPSframes[fpscount] = 1000 / targetFrameRate;
				Thread.sleep(1000 / targetFrameRate - length);
			} else {
				FPSframes[fpscount] = length;
				Thread.sleep(length);
			}
		} else {
			long total = length;
			for (long l : FPSframes) {
				total += l;
			}
			long totalUpdate = lengthUpdate;
			for (long l : timeSpentOnUpdate) {
				totalUpdate += l;
			}
			long totalPicAndDevelop = lengthPicAndDevelop;
			for (long l : timeSpentOnTakingPictureAndDeveloping) {
				totalPicAndDevelop += l;
			}
			long totalOther = lengthOther;
			for (long l : timeSpentOnOther) {
				totalOther += l;
			}
			BigDecimal totalMAX = BigDecimal.valueOf(total).divide(BigDecimal.valueOf(1000));
			BigDecimal totalMAXU = BigDecimal.valueOf(totalUpdate).divide(BigDecimal.valueOf(1000));
			BigDecimal totalMAXP = BigDecimal.valueOf(totalPicAndDevelop).divide(BigDecimal.valueOf(1000));
			BigDecimal totalMAXO = BigDecimal.valueOf(totalOther).divide(BigDecimal.valueOf(1000));
			if (totalMAX == BigDecimal.ZERO)
				totalMAX = BigDecimal.ONE;
			fPS = BigDecimal.valueOf(targetFrameRate).multiply(totalMAX);
			if (totalMAX != BigDecimal.valueOf(0)) {
				flatLandWindow.notify("" + fPS);
			}
			if (totalMAXU != BigDecimal.valueOf(0)) {
				flatLandWindow.notifyUpdate("" + totalMAXU);
			}
			if (totalMAXP != BigDecimal.valueOf(0)) {
				flatLandWindow.notifyPic("" + totalMAXP);
			}
			if (totalMAXO != BigDecimal.valueOf(0)) {
				flatLandWindow.notifyOther("" + totalMAXO);
			}
			UpdateTimeSingleton.getInstance()
					.setCurrentTime(totalMAXU.doubleValue() + totalMAXP.doubleValue() + totalMAXO.doubleValue());
			if (totalMAX.longValue() / targetFrameRate < 1000 / targetFrameRate)
				Thread.sleep(1000 / targetFrameRate - totalMAX.longValue() / targetFrameRate);
			else
				Thread.sleep(totalMAX.longValue() / targetFrameRate);
			fpscount = 0;
			LOG.println(mel.getName() + ": " + mel.x + " , " + mel.y + " , " + mel.direction);
		}
		return fpscount;
	}



	private static void checkEvents(PlayerWrper mel, EventHandler events2) {
		ArrayList<GameEvent> events3 = events2.getEvents();
		for (GameEvent event : events3) {
			if (event.isEventWithinDistsnce(mel.x, mel.y, 50)) {
				if (event.getName().equalsIgnoreCase("theMid")) {
					DialogManager.getInstance();
					DialogManager.notifyAllPO("Welcome to the Mid");
				}
				if (event.getName().equalsIgnoreCase("theStart")) {
					DialogManager.getInstance();
					DialogManager.notifyAllPO("Welcome to the Start");
				}
				
				if (event.getType().equalsIgnoreCase("LevelEnd")) {
					if(event.getX()<= mel.x && event.getY() <= mel.y) {
	                    objectiveComplete = true;
	                }
				}
				
				
				
				
			}
		}
	}

	public static void generateAnts() {
		for (int i = 0; i < 1; i++) {
			ant_s_.add(new ANT((int) (Math.random() * (cameraWidth - 1)), (int) (Math.random() * (cameraHeight - 1)),
					Direction.random(), cameraWidth, cameraHeight, new Color(255, 255, 0)));
		}
	}

	public static void generateBoard() {
		tile[][] tilez = tiles.getTiles();
		for (int i = 0; i < cameraWidth; i++) {
			for (int j = 0; j < cameraHeight; j++) {
				tilez[i][j] = new tile(i, j, Color.WHITE);
			}
		}
	}



	private static void buildMelsActs(ArrayList<ActionsInterface> acts, FlatLander flatlander,
			ViewableFlatLand flatLand) {
		DrawArc drawAArcMel = new DrawArc(flatlander, flatLand);
		DrawAProtoCloud drawAProtoCloudMel = new DrawAProtoCloud(flatlander, flatLand);
		Wonder wonderMel = new Wonder(flatlander);
		new DrawABlob(flatlander, flatLand);
		for (int i = 0; i < 200; i++) {
			for (int j = 0; j < 10; j++) {
				acts.add(drawAArcMel);
				acts.add(drawAArcMel);
				acts.add(new GoInAStrightLineFor(flatlander, (int) (Math.random() * 50)));
				acts.add(wonderMel);
			}
			for (int j = 0; j < 10; j++) {
				acts.add(wonderMel);
				acts.add(new GoInAStrightLineFor(flatlander, (int) (Math.random() * 50)));
				acts.add(new DrawArcFasterVersion1(flatlander, flatLand));
			}
			acts.add(drawAProtoCloudMel);
		}
		acts.add(new GoInAStrightLineFor(flatlander, (int) (Math.random() * 500)));
	}

	@Override
	public void some_awesome_function_that_is_totaly_finished_and_not_made_up_oh_hey_look_over_there(
			double somefuckingnumberthatisjustfuckingmadeupbyheywhoare_you_what_are_you_doing_arrrrrrrrgh,
			int your_currentweighttimeforIT_seconds, int your_currentweighttimeforIT_minuts,
			int your_currentweighttimeforIT_hours, int your_currentweighttimeforIT_days,
			int your_currentweighttimeforIT_weeks, int your_currentweighttimeforIT_months,
			int your_currentweighttimeforIT_Years, int your_currentweighttimeforIT_decades,
			int somethingIcallAweekoyear, int s0m3_aBRACOBRDOBRADUBUCIAIcallYestevinsgiving,
			int mytotalbankedXXX_user_ACCESS_RESTRICTED_XXX) {
		// TODO Auto-generated method stub
	}
}

class MouseHandler implements MouseListener {
	@Override
	public void mouseClicked(MouseEvent arg0) {
		System.err.println("Clicked");
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		System.err.println("entered");
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		System.err.println("exited");
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		Level.xInitiial = arg0.getX();
		Level.yInitial = arg0.getY();
		Level.flatLandSelector.select(Level.xInitiial, Level.yInitial);
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}
}

class MouseMotionHandler implements MouseMotionListener {
	@Override
	public void mouseMoved(MouseEvent arg0) {
		// App.updateHUD(App.flatLand.getXUnWraped(arg0.getX())-500,App.flatLand.getYUnWraped(arg0.getY())-500);
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		Level.flatLandSelector.reposition(Level.flatLand.getXUnWraped(arg0.getX()),
				Level.flatLand.getYUnWraped(arg0.getY()), Level.xInitiial, Level.yInitial);
	}
}
