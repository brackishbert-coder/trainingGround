package theStart.theView.TheControls;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import FlatLandStructure.ViewableFlatLand;
import Nuron.NuronForDisplay;

import theStart.thePeople.FlatLander;
import theStart.thePeople.FlatLander.XYPair;
import theStart.thePeople.FlatLander.XYWrapper;
import theStart.thePeople.FlatLanderFaceBook;
import theStart.thePeople.FlatlanderType;
import theStart.theSpace.FlatLandWindow;
import theStart.theStuff.BranchType;
import theStart.theStuff.FlatLanderRandom;
import theStart.theStuff.ClassOfFlatLander;
import theStart.theStuff.SynapseFaceBook;
import theStart.theStuff.SynapsePair;
import theStart.theView.TheControls.KeyBoardHandler;
import vectorization.vector;
import vec.VectorServer;
public class TheStartCamera implements CameraContract {

	private int width;
	private int height;
	private int posxinflatland = 0;
	private int posyinflatland = 0;
	private KeyBoardHandler cameraKeybordHandler;
	private ViewableFlatLand flatland;
	private BufferStrategy bufferStrategy;
	private Integer currentTime;
	boolean display = true;
	boolean displayContacts = true;
	boolean displayBody = true;
	boolean displayBranches = true;
	private int seed;
	private ArrayList<FlatLander> flatlanderFaceBook;
	private ArrayList<FlatLander> flatlanderFaceBookPool;
	private int nuroncount = 0;
	private boolean runonce = false;

	private ArrayList<NuronForDisplay> nuronList = new ArrayList<NuronForDisplay>();
	private long timerCount = 0;
	private VectorServer vectorServer;

	public TheStartCamera(int width, int height, int posxinflatland, int posyinflatland, ViewableFlatLand flatLand2,
			int seed, int nroncount, Canvas canvas) {

		this.flatland = flatLand2;
		this.seed = seed;
		this.nuroncount = nroncount;
		this.setWidth(width);
		this.setHeight(height);
		this.setPosxinflatland(posxinflatland);
		this.setPosyinflatland(posyinflatland);
		this.cameraKeybordHandler = new KeyBoardHandler(this);
		generateFlatlandersAKANurons();
		if (canvas != null) {
			canvas.createBufferStrategy(2);
			bufferStrategy = canvas.getBufferStrategy();
		}
		
		vectorServer = new VectorServer();
		Thread thread = new Thread(vectorServer);
		thread.start();
		
		
	}

	public KeyBoardHandler setKeyBindingsForPlayer(FlatLandWindow flatLandWindow) {
		return cameraKeybordHandler.buildKeyBindings(flatLandWindow, this);
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getPosxinflatland() {
		return posxinflatland;
	}

	public void setPosxinflatland(int posxinflatland) {
		this.posxinflatland = posxinflatland;
	}

	public int getPosyinflatland() {
		return posyinflatland;
	}

	public void setPosyinflatland(int posyinflatland) {
		this.posyinflatland = posyinflatland;
	}

	public void takePictureOfFlatLand() {

		Graphics graphics = bufferStrategy.getDrawGraphics();
		currentTime = flatland.getTime();
		if (this.display) {
			graphics.setColor(Color.green);
			graphics.drawString("BLAH", 20, 20);
			graphics.setColor(Color.black);
			graphics.fillRect(0, 0, width, height);
			runonce = false;
		} else if (!this.display && !runonce) {
			graphics.setColor(Color.green);
			graphics.drawString("BLAH", 20, 20);
			graphics.setColor(Color.black);
			graphics.fillRect(0, 0, width, height);
			runonce = true;
		}

		renderNurons(graphics);

		graphics.dispose();
		bufferStrategy.show();
	}

	private void renderNurons(Graphics graphics) {
		int count = 0;

		for (NuronForDisplay nuronForDisplay : nuronList) {
			
			
				count++;
			
		}
		double[] outputVector = new double[count];
		int count2 = 0;
		for (NuronForDisplay nuronForDisplay : nuronList) {
			nuronForDisplay.renderNurons(graphics, flatlanderFaceBook, flatlanderFaceBookPool, display, displayBody,
					displayBranches, displayContacts, posxinflatland, posyinflatland);
			if ( count2 < count) {
				if (nuronForDisplay.getFlatLander().getFrequency() > 0.0) {
				//	System.out.println("Output Nuron Frequency: " + nuronForDisplay.getFlatLander().getFrequency());
					outputVector[count2] = nuronForDisplay.getFlatLander().getFrequency();
					count2++;
				}
			}

			if (System.currentTimeMillis() - timerCount >= 250) {
				vector vector = new vector( outputVector);
				vectorServer.addVector(vector);
				timerCount = System.currentTimeMillis();

			}

		}

	}

	private void generateFlatlandersAKANurons() {
		new FlatLanderRandom(seed);
		Color color = Color.blue;
		Color fireColor = Color.red;
		Color backFireColor = Color.YELLOW;
		Color fireAndBackFireColor = Color.YELLOW;
		Color dendriteSynapseColor = Color.CYAN;
		Color axonSynapseColor = Color.MAGENTA;
		Color dendriteColor = Color.blue;
		Color axonColor = Color.gray;
		int maxPerColumn = 2;
		int maxPerRow = 5;
		int initialColumn = posyinflatland - (height / 2);
		int initialrow = posxinflatland - (width / 2);
		int outputcount = 0;
		for (int i = posxinflatland - (width / 2); i < posxinflatland + (width / 2); i += 2) {
			int rowcont = 0;
			int columncount = 0;

			for (int j = posyinflatland - (height / 2); j < posyinflatland
					+ (height / 2); j += 2) {
				int j2 = (int) (Math.random() * 15);
				if (j2 == 1 && columncount < maxPerColumn && rowcont < maxPerRow
						&& (initialrow + 20 < i || initialColumn + 20 < j)) {
					if (nuroncount > 0) {
						initialrow = i;
						initialColumn = j;
						int dendritesStatic = (int) Math.random() * (6) + 1;
						int direction1 = (int) (Math.random() * 90);
						int length1 = (int) (Math.random() * 200) + 20;
						int direction2 = (int) (Math.random() * 90);
						int length2 = (int) (Math.random() * 300) + 30;

						int ofclass = (int) (1 + Math.random() * ClassOfFlatLander.values().length - 1);
						if (ClassOfFlatLander.ofType(ofclass) == ClassOfFlatLander.OutputNuron ) {
							outputcount++;
						}


						int ofType = (int) Math.random() * 1;
//						if (ofType >= 5)
//							ofType = 4;
						if (!FlatLanderFaceBook.getInstance().check(i, j)) {
							FlatLander flatlander = new FlatLander(i, j, ClassOfFlatLander.ofType(ofclass),
									FlatlanderType.ofType(ofType), new FlatLanderRandom(seed), seed, flatland, 1,
									direction2, length2, dendritesStatic, direction1, length1, color, fireColor,
									backFireColor, fireAndBackFireColor, dendriteSynapseColor, axonSynapseColor,
									dendriteColor, axonColor);

							if (ClassOfFlatLander.ofType(ofclass) == ClassOfFlatLander.InputNuron) {
								FlatLanderFaceBook.getInstance().addInputNuron(flatlander);
							}

							nuronList.add(new NuronForDisplay(height, width, flatlander));
							FlatLanderFaceBook.getInstance().add(flatlander);

							nuroncount--;
							columncount++;
							rowcont++;
						}
					}
				}
			}
		}

		Math.random();

		flatlanderFaceBookPool = FlatLanderFaceBook.getInstance().getFlatlanderFaceBookPool();
		flatlanderFaceBook = FlatLanderFaceBook.getInstance().getFlatlanderFaceBook();
	}

	public Graphics takePictureOfFlatLand(Graphics graphics) {
		currentTime = flatland.getTime();

		renderNurons(graphics);

		return graphics;
	}

	public int mapFromFlatLandToScreenSpaceX(int input) {
		int input_end = posxinflatland + width / 2;
		int input_start = posxinflatland - width / 2;
		int output_start = 0;
		int output_end = width;
		double slope = 1.0 * (output_end - output_start) / (input_end - input_start);
		int i = output_start + (int) Math.round(slope * (input - input_start));

		return i;
	}

	public int mapFromFlatLandToScreenSpaceY(int input) {
		int input_end = posyinflatland + height / 2;
		int input_start = posyinflatland - height / 2;
		int output_start = 0;
		int output_end = height;
		double slope = 1.0 * (output_end - output_start) / (input_end - input_start);
		int i = output_start + (int) Math.round(slope * (input - input_start));

		return i;
	}

	public static void writeVectorToFile(double[] vector, String filePath) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) { // 'true' enables appending
			for (int i = 0; i < vector.length; i++) {
				writer.write(Double.toString(vector[i]));
				if (i < vector.length - 1) {
					writer.write(" , "); // Separate values with spaces and commas
				}
			}
			writer.newLine(); // Add a newline after writing the vector
		}
	}

}
