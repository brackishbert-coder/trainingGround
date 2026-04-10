package theStart.theView;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.net.Socket;

public class WebcamUpdater implements Runnable {

    private final String serverAddress;
    private final int port;
    private volatile boolean running = true;

    public WebcamUpdater() {
        this.serverAddress = "localhost"; // or remote IP
        this.port = 5000; // must match your WebcamServer port
    }

    @Override
    public void run() {
      //  JFrame frame = new JFrame("Bang Stream Viewer");
       // JLabel imageLabel = new JLabel();
      //  frame.getContentPane().add(imageLabel, BorderLayout.CENTER);
      //  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      //  frame.setSize(640, 480);
      //  frame.setVisible(true);

        while (running) {
            try (Socket socket = new Socket(serverAddress, port)) {
                System.out.println("Connected to WebcamServer at " + serverAddress + ":" + port);

                InputStream in = socket.getInputStream();
                DataInputStream dis = new DataInputStream(in);

                while (running) {
                    // Read length prefix (4 bytes)
                    int length;
                    try {
                        length = dis.readInt();
                    } catch (Exception e) {
                        System.out.println("Server disconnected.");
                        break;
                    }

                    if (length <= 0 || length > 10_000_000) {
                        System.err.println("Invalid frame length: " + length);
                        break;
                    }

                    // Read full JPEG
                    byte[] buf = new byte[length];
                    dis.readFully(buf);

                    // Decode into BufferedImage
                    try {
                        BufferedImage img = ImageIO.read(new ByteArrayInputStream(buf));
                        if (img != null) {
                            SwingUtilities.invokeLater(() -> {
                             //   imageLabel.setIcon(new ImageIcon(img));
                             //   frame.pack();
                                ImageRepository.getInstance().holdToken();
                                ImageRepository.getInstance().setImage(img);
                                ImageRepository.getInstance().releaseToken();
                            });
                        }
                    } catch (Exception ex) {
                        System.err.println("Decode error: " + ex.getMessage());
                    }
                }

                System.out.println("Reconnecting...");
                Thread.sleep(1000);

            } catch (Exception e) {
                System.err.println("Connection error: " + e.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
        }

        System.out.println("WebcamUpdater stopped.");
    }

    public void stop() {
        running = false;
    }

    // For manual testing
    public static void main(String[] args) {
        WebcamUpdater updater = new WebcamUpdater();
        new Thread(updater).start();
    }
}
