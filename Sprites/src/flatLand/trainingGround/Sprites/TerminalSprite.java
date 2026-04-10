package flatLand.trainingGround.Sprites;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import javax.imageio.ImageIO;

import FlatLander.FlatLander;
import Logging.LOG;

public class TerminalSprite extends LOG implements Sprites {

	int height = 9;
	int width = 7;

	int terminalXCharPosition = 0;
	int terminalYCharPosition = 0;

	private String path;
	private int imageWidth;
	private int imageHeight;
	private BufferedImage[][] characters = new BufferedImage[6][18];
	private BufferedImage[][] TerminalDisplay = null;
	private Character[] terminalBuffer = null;
	private int terminalBufferIndex = 0;
	private BufferedImage TerminalDisplayImg = null;

	HashMap<String, Integer> charXIndex = new HashMap<>();
	HashMap<String, Integer> charYIndex = new HashMap<>();
	private BufferedImage toDisplay;
	private int terminalCharWidth;
	private int terminalLines;
	private ObserverPrompt obvPrompt;
	private int scale;

	public TerminalSprite(String path, int terminalCharWidth, int terminalLines, int scale) {
		this.path = path;
		this.terminalCharWidth = terminalCharWidth;
		this.terminalLines = terminalLines;
		this.scale = scale;
		BufferedImage spriteimageRight;
		try {
			spriteimageRight = ImageIO.read(new File(path));
			this.imageWidth = spriteimageRight.getWidth();
			this.imageHeight = spriteimageRight.getHeight();
			for (int i = 0; i <= 5; i++) {

				for (int j = 0; j <= 17; j++) {
					characters[i][j] = spriteimageRight.getSubimage(j * width, i * height, width, height);
				}

			}
			addXAndY();
			TerminalDisplay = new BufferedImage[terminalLines][terminalCharWidth];
			terminalBuffer = new Character[terminalLines * terminalCharWidth];
			TerminalDisplayImg = new BufferedImage(width * terminalCharWidth, height * terminalLines, 1);
			toDisplay = TerminalDisplayImg;

			BufferedImage before = spriteimageRight;
			int w = before.getWidth();
			int h = before.getHeight();
			BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			AffineTransform at = new AffineTransform();
			at.scale(2.0, 2.0);
			AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
			after = scaleOp.filter(before, after);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void addXAndY() {

		try {
			File myObj = new File("/home/wes/git/Sprites/Sprites/resources/CharMappings");
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNext()) {
				String data = myReader.nextLine();
				int x = Integer.valueOf(myReader.nextLine());
				int y = Integer.valueOf(myReader.nextLine());
				charXIndex.put(data, x);
				charYIndex.put(data, y);

			}
			myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}

	}

	@Override
	public BufferedImage update(FlatLander actor) {

		return toDisplay;
	}

	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getHeight() {
		// TODO Auto-generatedByteArrayOutputStream baos = new ByteArrayOutputStream();
		// method stub
		return 0;
	}

	@Override
	public void updateState() {

	}

	public BufferedImage update(String key, boolean gameMode, boolean prompt) {

		// if (!gameMode) {
		Graphics2D graphics = toDisplay.createGraphics();
		clearGraphics(graphics);

		toDisplay = generateTerminalBufferImage(key, prompt);
		// } else {
		// toDisplay = TerminalDisplayImg;
		// }50

		return null;
	}

	private BufferedImage generateTerminalBufferImage(String key, boolean prompt) {

		if (key.equalsIgnoreCase("\b")) { // Handle backspace
	        if (terminalBufferIndex > 0) {
	            terminalBufferIndex--;
	            terminalBuffer[terminalBufferIndex] = null;

	            // Move cursor back
	            terminalXCharPosition--;
	            if (terminalXCharPosition < 0) {
	                terminalXCharPosition = terminalCharWidth - 1;
	                terminalYCharPosition--;
	                if (terminalYCharPosition < 0) {
	                    terminalYCharPosition = 0;
	                    terminalXCharPosition = 0;
	                }
	            }

	            // Clear the character from the display
	            TerminalDisplay[terminalYCharPosition][terminalXCharPosition] = characters[5][5];

	            // Redraw the terminal
	            Graphics2D g = TerminalDisplayImg.createGraphics();
	            clearGraphics(g);
	            drawTerminal(g);
	            g.dispose();
	        }
	    } else if (key.equalsIgnoreCase("\n") || key.equalsIgnoreCase("\r")) {
			String userCmd = "";
			for (int i = 0; i < terminalBufferIndex; i++) {
				if (terminalBuffer[i] != null)
					userCmd += terminalBuffer[i];
			}

			if (!prompt) {
				System.err.println("CMD: " + userCmd);

				LOG.println(System.currentTimeMillis() + ": " + userCmd);
				obvPrompt.notify(userCmd);
			}

			terminalBuffer = new Character[terminalLines * terminalCharWidth];
			;

			TerminalDisplay[terminalYCharPosition][terminalXCharPosition] = characters[5][5];
			terminalBuffer[terminalBufferIndex] = '\n';
			terminalBufferIndex++;
			if (terminalBufferIndex >= terminalBuffer.length) {
				for (int i = 0; i < terminalBuffer.length; i++) {
					terminalBuffer[i] = null;
				}
				terminalBufferIndex = 0;
				System.err.println("BUFFER PURGE");
			}

			Graphics2D g = TerminalDisplayImg.createGraphics();

			clearGraphics(g);
			drawTerminal(g);

			terminalXCharPosition = 0;
			terminalYCharPosition++;
			if (terminalYCharPosition >= terminalLines) {
				terminalYCharPosition = terminalLines - 1;
				shiftUpAndClearLastLine();
			}
			g.dispose();
		} else {

			int x = charXIndex.get(key);
			int y = charYIndex.get(key);


			TerminalDisplay[terminalYCharPosition][terminalXCharPosition] = characters[x][y];
			terminalBuffer[terminalBufferIndex] = key.charAt(0);
			terminalBufferIndex++;
			Graphics2D g = TerminalDisplayImg.createGraphics();

			clearGraphics(g);
			drawTerminal(g);
			terminalXCharPosition++;

			if (terminalBufferIndex >= terminalBuffer.length) {
				for (int i = 0; i < terminalBuffer.length; i++) {
					terminalBuffer[i] = null;
				}
				terminalBufferIndex = 0;
				System.err.println("BUFFER PURGE");
			}

			if (terminalXCharPosition >= terminalCharWidth) {
				terminalXCharPosition = 0;
				terminalYCharPosition++;

				if (terminalYCharPosition >= terminalLines) {
					terminalYCharPosition = terminalLines - 1;
					shiftUpAndClearLastLine();
				}

			}

			g.dispose();
		}

		BufferedImage spriteimageRight = TerminalDisplayImg;
		int wRight = spriteimageRight.getWidth();
		int hRight = spriteimageRight.getHeight();
		int heightRight = (int) ((double) hRight * ((double) scale / 100.0));
		int widthRight = (int) ((double) wRight * ((double) scale / 100.0));
		BufferedImage afterRight = new BufferedImage(widthRight, heightRight, BufferedImage.TYPE_INT_ARGB);
		AffineTransform atRight = new AffineTransform();
		atRight.scale((double) scale / 100.0, (double) scale / 100.0);
		AffineTransformOp scaleOpRight = new AffineTransformOp(atRight, AffineTransformOp.TYPE_BILINEAR);
		afterRight = scaleOpRight.filter(spriteimageRight, afterRight);

		return afterRight;
	}

	private void drawTerminal(Graphics2D g) {
		for (int i = 0; i < terminalCharWidth; i++) {
			for (int j = 0; j < terminalLines; j++) {

				g.drawImage(TerminalDisplay[j][i], null, i * width, j * height);
			}
		}
	}

	private void clearGraphics(Graphics2D g) {
		for (int i = 0; i < terminalCharWidth; i++) {
			for (int j = 0; j < terminalLines; j++) {

				g.drawImage(characters[5][5], null, i * width, j * height);
			}
		}
	}

	private void shiftUpAndClearLastLine() {
		for (int i = 1; i < terminalLines; i++) {
			for (int j = 0; j < terminalCharWidth; j++) {
				TerminalDisplay[i - 1][j] = TerminalDisplay[i][j];
			}
		}

		for (int j = 0; j < terminalCharWidth; j++) {
			TerminalDisplay[5][j] = characters[5][5];
		}

	}

	public void notify(String string) {

		for (int i = 0; i < string.length(); i++) {
			char charAt = string.charAt(i);
			String str = "" + charAt;
			if (charAt == '\n') {
				Graphics2D g = TerminalDisplayImg.createGraphics();
				g.drawImage(characters[5][5], null, terminalXCharPosition * width, terminalYCharPosition * height);
				terminalXCharPosition = 0;
				terminalYCharPosition++;
				if (terminalYCharPosition >= terminalLines) {
					terminalYCharPosition = terminalLines - 1;
					shiftUpAndClearLastLine();
				}
				terminalBuffer = new Character[terminalLines * terminalCharWidth];
				;
			} else if (charAt == '\r') {
				Graphics2D g = TerminalDisplayImg.createGraphics();
				g.drawImage(characters[5][6], null, terminalXCharPosition * width, terminalYCharPosition * height);
				terminalXCharPosition = 0;
				terminalYCharPosition++;
				if (terminalYCharPosition >= terminalLines) {
					terminalYCharPosition = terminalLines - 1;
					shiftUpAndClearLastLine();
				}
				terminalBuffer = new Character[terminalLines * terminalCharWidth];
				;
			} else
				update(str, false, true);

		}

	}

	public void addObserver(ObserverPrompt obvPrompt) {
		this.obvPrompt = obvPrompt;

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
