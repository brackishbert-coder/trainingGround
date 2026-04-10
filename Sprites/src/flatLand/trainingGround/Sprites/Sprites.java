package flatLand.trainingGround.Sprites;

import java.awt.image.BufferedImage;

import FlatLander.FlatLander;

public interface Sprites {
	BufferedImage update(FlatLander actor);
	BufferedImage update(String key, boolean gameMode,boolean prompt);

	int getWidth();

	int getHeight();

	void updateState();


}
