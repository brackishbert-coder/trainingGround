package Drawing;

import java.util.ArrayList;

public class ImagePile {

	private static ImagePile instance;

	private static ArrayList<ImageStackEntry> pile = new ArrayList<ImageStackEntry>();

	public ImagePile() {

	}
	
	public static ImagePile getInstance() {
		if (instance == null) {
			instance = new ImagePile();
		}
		return instance;
	}
	
	public ImageStackEntry takefromthefront() {
		if (pile.size() > 0) {
			ImageStackEntry imageStackEntry = pile.get(0);
			pile.remove(0);
			return imageStackEntry;
		}
		return null;
	}

	public ArrayList<ImageStackEntry> getPile() {
		return (ArrayList<ImageStackEntry>) pile.clone();
	}

	public void clearPile() {
		pile.clear();
	}

	public void addToPile(ImageStackEntry image) {

		if (pile.size() > 0) {
			int index = (int) (Math.random() * (pile.size() - 1));
			pile.add(index, image);
		} else {
			pile.add(image);
		}
	}



}
