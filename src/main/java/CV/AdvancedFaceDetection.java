package CV;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;

public class AdvancedFaceDetection {

    private List<int[][]> haarFeatures;

    public AdvancedFaceDetection(String haarCascadePath) throws IOException {
        // Load Haar Cascade features from XML (simplified for this example)
        this.haarFeatures = loadHaarFeatures(haarCascadePath);
    }

    public boolean detectFaces(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Sliding window approach
        for (int y = 0; y < height - 24; y += 4) { // Window size: 24x24
            for (int x = 0; x < width - 24; x += 4) {
                BufferedImage subImage = image.getSubimage(x, y, 24, 24);
                if (matchesHaarFeatures(subImage)) {
                    System.out.println("Face detected at: (" + x + ", " + y + ")");
                    return true; // Face detected
                }
            }
        }
        System.out.println("No face detected.");
        return false; // No face detected
    }

    private boolean matchesHaarFeatures(BufferedImage subImage) {
        for (int[][] feature : haarFeatures) {
            int sum = 0;
            for (int[] rect : feature) {
                int x = rect[0], y = rect[1], w = rect[2], h = rect[3], weight = rect[4];
                sum += computeRectangleSum(subImage, x, y, w, h) * weight;
            }
            // Adjust the threshold to reduce false positives
            if (sum > 11000) { // Example threshold, adjust as needed
                System.out.println("Feature matched with sum: " + sum);
                return true; // Matches a Haar feature
            }
        }
        return false;
    }

    private int computeRectangleSum(BufferedImage image, int x, int y, int w, int h) {
        int sum = 0;
        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                int rgb = image.getRGB(i, j);
                int gray = (rgb >> 16 & 0xFF) + (rgb >> 8 & 0xFF) + (rgb & 0xFF);
                sum += gray / 3; // Convert to grayscale
            }
        }
        return sum;
    }

    private List<int[][]> loadHaarFeatures(String haarCascadePath) throws IOException {
        // Simplified loading of Haar features from XML
        // In a real implementation, parse the XML file to extract Haar features
        List<int[][]> features = new ArrayList<>();
        features.add(new int[][]{
            {0, 0, 12, 12, 1}, {12, 0, 12, 12, -1} // Example feature
        });
        return features;
    }

    public static void main(String[] args) throws IOException {
        AdvancedFaceDetection detector = new AdvancedFaceDetection("haarcascade.xml");
        BufferedImage image = ImageIO.read(new File("input.jpg"));
        boolean faceDetected = detector.detectFaces(image);
        System.out.println("Face detected: " + faceDetected);
    }
}