package flatLand.trainingGround.Sprites;


import java.awt.image.BufferedImage;

import FlatLander.FlatLander;

public class Collectible implements Sprites {
    private BufferedImage spriteImage;
    private int width;
    private int height;
    private boolean collected;

    public Collectible(BufferedImage image, int width, int height) {
        this.spriteImage = image;
        this.width = width;
        this.height = height;
        this.collected = false;
    }

    @Override
    public BufferedImage update(FlatLander actor) {
        if (!collected) {
            // Logic for collectible before being collected
            return spriteImage;
        }
        return null;
    }

    @Override
    public BufferedImage update(String key, boolean gameMode, boolean prompt) {
        if ("collect".equalsIgnoreCase(key)) {
            collected = true;
            System.out.println("Item collected!");
        }
        return spriteImage;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void updateState() {
        if (collected) {
            System.out.println("Collectible is no longer active.");
        }
    }
}
