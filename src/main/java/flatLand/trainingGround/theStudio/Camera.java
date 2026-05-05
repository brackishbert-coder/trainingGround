package flatLand.trainingGround.theStudio;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import Box.Box.PromptObserver;
import Drawing.ImagePile;
import Drawing.ImageStackEntry;
import FlatLandStructure.ViewableFlatLand;
import FlatLander.BoundingBox;
import FlatLander.FlatLandFacebook;
import FlatLander.FlatLander;
import XMLLEVELLOADER.FlatLanderWrper;
import flatLand.trainingGround.EventHandler;
import flatLand.trainingGround.GAMSTATUS;
import flatLand.trainingGround.GameStatus;
import flatLand.trainingGround.Sprites.TerminalSprite;

public class Camera {
	private ViewableFlatLand flatland;
	private int cameraWidth;
	private int cameraHeight;
	private int cameraPosXoncanvas = 0;
	private int cameraPosYoncanvas = 0;
	private int cameraPosXinFlatland = 0;
	private int cameraPosYinFlatland = 0;
	private XMLLEVELLOADER.PlayerWrper play;
	private Graphics graphics;
	private BufferStrategy bufferStrategy;
	private int canvasWidth;
	private int canvasHeight;
	private XMLLEVELLOADER.FlatLanderWrper hud;
	String terminalPath = "/home/wes/gitworkspace/StartAnimation/res/charmap-oldschool_white_sew.png";
	private HashMap<PromptObserver, TerminalSprite> map;
	private int timeout = 150;
	private long previousTime = 0;

	public Camera(ViewableFlatLand flatland, int cameraWidth, int cameraHeight, int posX, int posY,
			XMLLEVELLOADER.PlayerWrper player) {

		cameraPosXoncanvas = posX;
		cameraPosYoncanvas = posY;
		this.flatland = flatland;
		this.cameraWidth = cameraWidth;
		this.cameraHeight = cameraHeight;
		play = player;
		cameraPosXinFlatland = play.getX() - (cameraWidth / 2 + play.getSprite().getWidth() / 2 + cameraPosXoncanvas);
		cameraPosYinFlatland = play.getY() - (cameraHeight / 2 + play.getSprite().getHeight() / 2 + cameraPosYoncanvas);
		buildHud(flatland, null);
	}

	public Camera(ViewableFlatLand flatland, int cameraWidth, int cameraHeight, int canvasWidth, int canvasHeight,
			int posX, int posY, XMLLEVELLOADER.PlayerWrper mel) {

		this.canvasWidth = canvasWidth;
		this.canvasHeight = canvasHeight;
		cameraPosXoncanvas = posX;
		cameraPosYoncanvas = posY;
		this.flatland = flatland;
		this.cameraWidth = cameraWidth;
		this.cameraHeight = cameraHeight;
		play = mel;
		cameraPosXinFlatland = play.getX() - (cameraWidth / 2 + play.getSprite().getWidth() / 2 + cameraPosXoncanvas);
		cameraPosYinFlatland = play.getY() - (cameraHeight / 2 + play.getSprite().getHeight() / 2 + cameraPosYoncanvas);
		buildHud(flatland, null);
	}

	public Camera(ViewableFlatLand flatLandLE, int cameraWidth, int cameraHeight, int canvasWidth, int canvasHeight,
			int posX, int posY) {
		this.canvasWidth = canvasWidth;
		this.canvasHeight = canvasHeight;
		cameraPosXoncanvas = posX;
		cameraPosYoncanvas = posY;
		this.flatland = flatLandLE;
		this.cameraWidth = cameraWidth;
		this.cameraHeight = cameraHeight;
		play = null;
		cameraPosXinFlatland = 0;
		cameraPosYinFlatland = 0;
		buildHud(flatLandLE, null);
	}

	public Graphics takePictureOfFlatland(Graphics graphics2) {

		drawFlatLand(graphics2);

		return graphics2;
	}

	private void drawFlatLand(Graphics graphics) {

		ArrayList<FlatLander> bookOfFlatLanders = FlatLandFacebook.getInstance().getFlatlanderFaceBook();

		GameStatus statusInstance = GameStatus.getInstance();
		if (statusInstance.isStatus(GAMSTATUS.FINPROD)) {
			graphics.clearRect(cameraPosXoncanvas, cameraPosYoncanvas, cameraWidth, cameraHeight);
		}

		for (int i = bookOfFlatLanders.size() - 1; i >= 0; i--) {
			FlatLander flatLander = bookOfFlatLanders.get(i);

			if (flatLander instanceof FlatLanderWrper) {
				BufferedImage bufferedImage = ((FlatLanderWrper) flatLander).getSprite().update(flatLander);
				FlatLanderWrper terminal = ((FlatLanderWrper) flatLander).getTerminal();

				BufferedImage term = null;
				if (terminal != null)
					term = terminal.getSprite().update(flatLander);

				int x = mapFromFlatLandToScreenSpaceX(flatLander.getX());
				int y = mapFromFlatLandToScreenSpaceY(flatLander.getY());

				if ((x >= 0 || x <= cameraWidth) && (y >= 0 || y <= cameraHeight)) {
					if (terminal != null)
						graphics.drawImage(term, x - 150, y - 150, null);
					graphics.drawImage(bufferedImage, x, y, null);
					if (flatLander.isDrawBB()) {
						BoundingBox currentflatLanderBB = flatLander.getCurrentflatLanderBB();
						int x2 = currentflatLanderBB.getTopLeft().getX();
						int x3 = currentflatLanderBB.getTopRight().getX();
						int width = x3 - x2;
						int y2 = currentflatLanderBB.getTopLeft().getY();
						int y3 = currentflatLanderBB.getBottemLeft().getY();
						int height = y3 - y2;
						Color color = graphics.getColor();
						graphics.setColor(Color.GREEN);

						graphics.drawRect(x, y, width, height);
						graphics.setColor(color);
					}

				}
			} else if (flatLander instanceof XMLLEVELLOADER.PlayerWrper) {
				BufferedImage bufferedImage = ((XMLLEVELLOADER.PlayerWrper) flatLander).getSprite().update(flatLander);
				XMLLEVELLOADER.FlatLanderWrper terminal = ((XMLLEVELLOADER.PlayerWrper) flatLander).getTerminal();

				BufferedImage term = null;
				if (terminal != null)
					term = terminal.getSprite().update(flatLander);

				int x = mapFromFlatLandToScreenSpaceX(flatLander.getX());
				int y = mapFromFlatLandToScreenSpaceY(flatLander.getY());

				if ((x >= 0 || x <= cameraWidth) && (y >= 0 || y <= cameraHeight)) {

					if (terminal != null)
						graphics.drawImage(term, x - 150, y - 150, null);
					graphics.drawImage(bufferedImage, x, y, null);
					if (flatLander.isDrawBB()) {
						BoundingBox currentflatLanderBB = flatLander.getCurrentflatLanderBB();
						int x2 = currentflatLanderBB.getTopLeft().getX();
						int x3 = currentflatLanderBB.getTopRight().getX();
						int width = x3 - x2;
						int y2 = currentflatLanderBB.getTopLeft().getY();
						int y3 = currentflatLanderBB.getBottemLeft().getY();
						int height = y3 - y2;
						Color color = graphics.getColor();
						graphics.setColor(Color.GREEN);
						graphics.drawRect(x, y, width, height);
						graphics.setColor(color);
					}

				}
			}
		}

		if (play != null) {
			cameraPosXinFlatland = play.getX()
					- (cameraWidth / 2 + play.getSprite().getWidth() / 2 + cameraPosXoncanvas);
			cameraPosYinFlatland = play.getY()
					- (cameraHeight / 2 + play.getSprite().getHeight() / 2 + cameraPosYoncanvas);
		} else {
			cameraPosXinFlatland = 0;
			cameraPosYinFlatland = 0;
		}
		if (statusInstance.isStatus(GAMSTATUS.FINPROD)) {
			graphics.setColor(Color.blue);
			graphics.fillRect(cameraPosXoncanvas + cameraWidth, 0, canvasWidth, canvasHeight);
			graphics.setColor(Color.yellow);
			graphics.fillRect(0, cameraPosYoncanvas + cameraHeight, canvasWidth, canvasHeight);
			graphics.setColor(Color.red);
			graphics.fillRect(0, 0, cameraPosXoncanvas, canvasHeight);
			graphics.setColor(Color.green);
			graphics.fillRect(0, 0, canvasWidth, cameraPosYoncanvas);
		}

		ImageStackEntry takefromthefront = ImagePile.getInstance().takefromthefront();
		// if (takefromthefront != null) {
		// BufferedImage img = takefromthefront.getImg();

		// graphics.drawImage(img, 0, 0, null);

		int count = 0;
		Set<PromptObserver> keySet = map.keySet();
			for (PromptObserver promptObserver : keySet) {
				if (timeout <= System.currentTimeMillis()-previousTime) {
					previousTime = System.currentTimeMillis();
				promptObserver.notify("\n" + "\n" + "\n" + "\n" + "\n" + "\n");
				promptObserver.notify(flatland.getTime() + " ");
				}

				TerminalSprite terminalSprite = map.get(promptObserver);
				BufferedImage update = terminalSprite.update(hud);
				graphics.drawImage(update, 100 + count * 200, 0, null);
				count++;
			}

		

	}

	public void buildHud(ViewableFlatLand flatLand2, EventHandler events) {
		TerminalSprite sprite2 = new TerminalSprite(terminalPath, 10, 6, 250);
		PromptObserver promptOb = new PromptObserver(sprite2);
		promptOb.notify("level-N ");
		TerminalSprite sprite3 = new TerminalSprite(terminalPath, 10, 6, 250);
		PromptObserver promptOb3 = new PromptObserver(sprite3);
		promptOb3.notify("lives-M ");

		TerminalSprite sprite4 = new TerminalSprite(terminalPath, 10, 6, 250);
		PromptObserver promptOb4 = new PromptObserver(sprite4);
		promptOb4.notify(flatLand2.getTime() + "");

		TerminalSprite sprite5 = new TerminalSprite(terminalPath, 10, 6, 250);
		PromptObserver promptOb5 = new PromptObserver(sprite5);
		promptOb5.notify(flatLand2.getTime() + "");
		
		
		map = new HashMap<PromptObserver, TerminalSprite>();
		map.put(promptOb, sprite2);
		map.put(promptOb3, sprite3);
		map.put(promptOb4, sprite4);
		map.put(promptOb5, sprite5);

		previousTime= System.currentTimeMillis();

	}

	public int mapFromFlatLandToScreenSpaceX(int input) {
		int input_end = cameraPosXinFlatland + cameraWidth;
		int input_start = cameraPosXinFlatland;
		int output_start = 0;
		int output_end = cameraWidth;

		double slope = 1.0 * (output_end - output_start) / (input_end - input_start);
		return output_start + (int) Math.round(slope * (input - input_start));
	}

	public int mapFromFlatLandToScreenSpaceY(int input) {
		int input_end = cameraPosYinFlatland + cameraHeight;
		int input_start = cameraPosYinFlatland;
		int output_start = 0;
		int output_end = cameraHeight;
		double slope = 1.0 * (output_end - output_start) / (input_end - input_start);
		return output_start + (int) Math.round(slope * (input - input_start));
	}

	public int mapFromScreenSpaceToFlatLAndX(int input) {
		int input_end = cameraPosXoncanvas + cameraWidth;
		int input_start = cameraPosXoncanvas;
		int output_start = 0;
		int output_end = cameraWidth;
		double slope = 1.0 * (output_end - output_start) / (input_end - input_start);
		return output_start + (int) Math.round(slope * (input - input_start));
	}

	public int mapFromScreenSpaceFlatLandY(int input) {
		int input_end = cameraPosYoncanvas + cameraHeight;
		int input_start = cameraPosYoncanvas;
		int output_start = 0;
		int output_end = cameraHeight;
		double slope = 1.0 * (output_end - output_start) / (input_end - input_start);
		return output_start + (int) Math.round(slope * (input - input_start));
	}

	public int getFlatLandOnCanvasX() {
		if (play != null)
			return play.getX();
		else
			return 0;
	}

	public int getFlatLandOnCanvasY() {
		if (play != null)
			return play.getY();
		else
			return 0;
	}

	public int getCameraPosX() {
		return cameraPosXoncanvas;
	}

	public int getCameraPosY() {
		return cameraPosYoncanvas;
	}

	public Graphics getGraphics() {
		// TODO Auto-generated method stub
		return this.graphics;
	}

	public BufferStrategy getBufferStrategy() {
		return bufferStrategy;
	}

	public void setBufferStrategy(BufferStrategy bufferStrategy) {
		this.bufferStrategy = bufferStrategy;
	}

	public ViewableFlatLand getFlatland() {
		return flatland;
	}

}
