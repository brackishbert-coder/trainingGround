package flatLand.trainingGround.Sprites;

import java.awt.image.BufferedImage;
import java.util.Random;

import FlatLander.FlatLander;

public class Destructibles implements Sprites {
    private BufferedImage spriteImage;
    private int width;
    private int height;
    private boolean destroyed;
    private Sprites spawnedItem;

    public Destructibles(BufferedImage image, int width, int height) {
        this.spriteImage = image;
        this.width = width;
        this.height = height;
        this.destroyed = false;
    }

    @Override
    public BufferedImage update(FlatLander actor) {
        if (!destroyed) {
            // Update logic before destruction (e.g., animation, position)
            return spriteImage;
        } else if (spawnedItem != null) {
            // Trigger spawning another item
            return spawnedItem.update(actor);
        }
        return null;
    }

    @Override
    public BufferedImage update(String key, boolean gameMode, boolean prompt) {
        // Example logic to change sprite or check destruction state
        if (!destroyed) {
            // Update logic based on inputs
            if ("destroy".equalsIgnoreCase(key)) {
                destroyed = true;
                spawnNewItem();
            }
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
        if (destroyed && spawnedItem != null) {
            spawnedItem.updateState();
        }
    }

    private void spawnNewItem() {
        // Logic to spawn a new item
        BufferedImage newItemImage = generateNewItemImage(); // Placeholder for item sprite
        spawnedItem = new Collectible(newItemImage, 20, 20); // Example spawned item
        System.out.println("Spawned a new item!");
    }

    private BufferedImage generateNewItemImage() {
        // Placeholder method for creating an image for the spawned item
        return spriteImage; // Reuse the same image for simplicity
    }
}
