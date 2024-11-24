import javax.swing.*;

/**
 * @author Munseon Chang
 * @version Nov 16 2024
 * The starter main class for the classic snake game that runs on GUI
 */
public class SnakeGame extends JFrame {
    public SnakeGame() {
        this.add(new GamePanel());
        this.setTitle("Snake Game");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.pack();
        this.setVisible(true);
        this.setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        new SnakeGame();
    }
}