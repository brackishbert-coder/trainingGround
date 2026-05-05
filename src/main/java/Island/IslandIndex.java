package Island;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import FSM.GameInstance;
import FlatLandStructure.ViewableFlatLand;
import FlatLander.FlatLandFacebook;
import Logging.LOG;
import TheGame.Board;
import TheGame.BoardSpace;
import TheGame.Status;
import View.FlatLandWindow;
import XMLLEVELLOADER.FlatLanderWrper;
import animation.Asset;
import flatLand.trainingGround.GAMSTATUS;
import flatLand.trainingGround.GameStatus;
import flatLand.trainingGround.Sprites.MonopolySpace;
import flatLand.trainingGround.theStudio.Camera;
import theStart.theView.TheControls.GameScreen;
import theStart.theView.TheControls.TheStartCamera;

public class IslandIndex extends LOG {

	static BufferedImage[] spaces = new BufferedImage[46];

	private static FlatLandWindow flatLandWindow;
	private static int cameraWidth = 1300;
	private static int cameraHeight = 1300;
	private static int flatLandWidth = 1300;
	private static int flatLandHeight = 1300;
	private static int canvasWidth = 1300;
	private static int canvasHeight = 1300;
	private static int oncanvasX = 0;
	private static int oncanvasY = 0;
	private static final String FILENAME = "res/level.xml";
	private static ArrayList<Island> islands;
	private static Board board;
	private static GameScreen canvasLE;
	static GameInstance game;



	private static Graphics graphics; 
	private static TheStartCamera theStartCamera;
	private static Camera theEyeInTheSky;

	public static void main(String[] args) throws IOException {
	GameStatus statusInstance = GameStatus.getInstance();
		statusInstance.addStatus(GAMSTATUS.MONOPOLY);
		HashMap<String, String> logs = new HashMap<>();
		logs.put("log", "/res/folder");
		LOG.set_current_working_directory("/home/wes/git/Monopoly_Island_Index/Monopoly_Island_Index");
		LOG.register_output_forLogging(LOG, logs);
		
		ViewableFlatLand flatLandLE = new ViewableFlatLand(flatLandWidth, flatLandHeight, true);

		int posY = (canvasHeight / 2) - cameraHeight / 2;

		theEyeInTheSky = new Camera(flatLandLE, cameraWidth, cameraHeight,canvasWidth,canvasHeight, (canvasWidth / 2) - cameraWidth / 2,
				posY);
		
		
		board = new Board();
		
		canvasLE = new GameScreen(canvasWidth, canvasHeight,statusInstance);
		canvasLE.setTheEyeInTheSky(theEyeInTheSky);
		canvasLE.getGraphics();
		canvasLE.setSize(canvasWidth, canvasHeight);
		canvasLE.setPreferredSize(new Dimension(canvasWidth, canvasHeight));
		flatLandWindow = new FlatLandWindow("hellp", flatLandLE, canvasLE, canvasWidth, canvasHeight);
		islands = new ArrayList<Island>();
		loadIslands();
		canvasLE.setBoard(board);
		canvasLE.getGraphics();
		
		
		
		game = new GameInstance(flatLandLE, canvasLE, board);
		mainLoop();
	}

	private static void mainLoop() {

		game.start();

	}

	private static void loadIslands() throws IOException {

		Path start = Paths.get("/home/wes/gitworkspace/Monopoly_Island_Index/Monopoly_Island_Index/resources/spaces");

		List<String> fileNames = new ArrayList<String>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(start)) {
			for (Path entry : stream) {
				if (!Files.isDirectory(entry)) {
					fileNames.add(entry.getFileName().toString());// add file name in to name list
				}
			}
		}

		String path = "";
		for (int i = 0; i < start.getNameCount(); i++) {
			path += "/" + start.getName(i);
		}

		
		loadSidesOfBoard();

	}


	
	
	
	
	
	private static void loadSidesOfBoard() {
		try {
			
			
			int i =0;
			for (int j = 1; j <=4; j++) {
			
				File myObj = new File("/home/wes/git/Monopoly_Island_Index/Monopoly_Island_Index/resources/spaces/Side"+j);
				Scanner myReader = new Scanner(myObj);
				int k =i*10 ;
				while (myReader.hasNextLine()) {
					String name = myReader.nextLine();
					System.out.println(name);
					board.addToBoardSpace(new BoardSpace(k,j,name,board.getWidth(),board.getHeight(),Status.getStatus(name) ));
					k++;
				}
				i++;
				myReader.close();
			}
	      } catch (FileNotFoundException e) {
	        System.out.println("An error occurred.");
	        e.printStackTrace();
	      }
	}

	
	
	

	
	
	

	
	
	
	
	
	private static void buildAndPlaceTriplets(String path, int i, String string, String filename) throws IOException {

		int x = getXBasedOfffName(filename);
		int y = getYBasedOfffName(filename);

		FlatLanderWrper mel = new FlatLanderWrper(x, y, 0, filename, 1, false, false, FlatLand.Physics.TypeOfEntity.TERRAIN, Color.BLACK);
		Asset asset = new Asset();
		asset.setFile(path + "/" + string);
		mel.setSprite(new MonopolySpace(path + "/" + string, 100));
		Object theRequestie = new Object();
		while (!FlatLandFacebook.getInstance().requestToken(theRequestie)) {
		}
		FlatLandFacebook.getInstance().add(mel, theRequestie);
		FlatLandFacebook.getInstance().releaseToken(theRequestie);
	}

	private static int getYBasedOfffName(String filename2) {
		if (filename2.contains("0I")) {
			if (filename2.contains("0I0")) {
				return 0;
			} else if (filename2.contains("0I1")) {
				return 0;

			} else if (filename2.contains("0I2")) {

				return 0;
			}
		} else if (filename2.contains("1I")) {
			if (filename2.contains("1I0")) {
				return 0;

			} else if (filename2.contains("1I1")) {

				return 0;
			} else if (filename2.contains("1I2")) {

				return 0;
			}
		} else if (filename2.contains("2I")) {
			if (filename2.contains("2I0")) {
				return 100;

			} else if (filename2.contains("2I1")) {
				return 175;

			} else if (filename2.contains("2I2")) {
				return 250;

			}
		} else if (filename2.contains("3I")) {
			if (filename2.contains("3I0")) {
				return 500;

			} else if (filename2.contains("3I1")) {
				return 575;

			} else if (filename2.contains("3I2")) {
				return 650;

			}
		} else if (filename2.contains("4I")) {
			if (filename2.contains("4I0")) {
				return 725;

			} else if (filename2.contains("4I1")) {
				return 725;

			} else if (filename2.contains("4I2")) {
				return 725;

			}
		} else if (filename2.contains("5I")) {
			if (filename2.contains("5I0")) {
				return 725;

			} else if (filename2.contains("5I1")) {
				return 725;

			} else if (filename2.contains("5I2")) {
				return 725;

			}
		} else if (filename2.contains("6I")) {
			if (filename2.contains("6I0")) {
				return 650;

			} else if (filename2.contains("6I1")) {
				return 575;

			} else if (filename2.contains("6I2")) {
				return 500;

			}
		}
		return 0;
	}

	private static int getXBasedOfffName(String filename2) {
		if (filename2.contains("0I")) {
			if (filename2.contains("0I0")) {
				return 100;
			} else if (filename2.contains("0I1")) {
				return 200;

			} else if (filename2.contains("0I2")) {

				return 300;
			}
		} else if (filename2.contains("1I")) {
			if (filename2.contains("1I0")) {
				return 600;

			} else if (filename2.contains("1I1")) {

				return 700;
			} else if (filename2.contains("1I2")) {

				return 800;
			}
		} else if (filename2.contains("2I")) {
			if (filename2.contains("2I0")) {
				return 980;

			} else if (filename2.contains("2I1")) {
				return 980;

			} else if (filename2.contains("2I2")) {
				return 980;

			}
		} else if (filename2.contains("3I")) {
			if (filename2.contains("3I0")) {
				return 980;

			} else if (filename2.contains("3I1")) {
				return 980;

			} else if (filename2.contains("3I2")) {
				return 980;

			}
		} else if (filename2.contains("4I")) {
			if (filename2.contains("4I0")) {
				return 800;

			} else if (filename2.contains("4I1")) {
				return 700;

			} else if (filename2.contains("4I2")) {
				return 600;

			}
		} else if (filename2.contains("5I")) {
			if (filename2.contains("5I0")) {
				return 300;

			} else if (filename2.contains("5I1")) {
				return 200;

			} else if (filename2.contains("5I2")) {
				return 100;

			}
		} else if (filename2.contains("6I")) {
			if (filename2.contains("6I0")) {
				return 0;

			} else if (filename2.contains("6I1")) {
				return 0;

			} else if (filename2.contains("6I2")) {
				return 0;

			}
		}
		return 0;
	}

	public static String getFileExtension(String filePath) {
		// Create a Path object from the file path
		Path path = Paths.get(filePath);

		// Get the file name and its extension
		String fileName = path.getFileName().toString();
		int dotIndex = fileName.lastIndexOf('.');

		// Check if the file has an extension
		if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
			return "No extension found";
		} else {
			// Extract and return the file extension
			return fileName.substring(dotIndex + 1);
		}
	}

	public static String getFileName(String filePath) {
		// Create a Path object from the file path
		Path path = Paths.get(filePath);

		// Get the file name and its extension
		String fileName = path.getFileName().toString();
		int dotIndex = fileName.lastIndexOf('.');

		// Check if the file has an extension
		if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
			return "No extension found";
		} else {
			// Extract and return the file extension
			return fileName.substring(0, dotIndex);
		}
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
