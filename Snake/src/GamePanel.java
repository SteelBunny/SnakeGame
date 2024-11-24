import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

/**
 * @author Munseon Chang
 * @version Nov 18 2024
 * The game panel class for the snake game.
 */

enum GameState {
    START_SCREEN, PLAYING, GAME_OVER, HOW_TO_PLAY
}

public class GamePanel extends JPanel implements ActionListener {
    private static final int SCREEN_WIDTH = 700;
    private static final int SCREEN_HEIGHT = 700;
    private static final int UNIT_SIZE = 20;
    private static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / UNIT_SIZE;
    private static final int DELAY = 55;
    private final int[] x = new int[GAME_UNITS];
    private final int[] y = new int[GAME_UNITS];
    private int bodyParts = 6;
    private int applesEaten;
    private int appleX;
    private int appleY;
    private char direction = 'R';
    private char lastDirection = 'R';
    private boolean directionChanged = false;
    private boolean running = false;
    private Timer timer;
    private final Random random;
    private GameState currentState = GameState.START_SCREEN;
    private final JPanel buttonPanel;
    private final JButton playButton;
    private final JButton howToPlayButton;
    private final JButton restartButton;

    public GamePanel() {
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());

        buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setOpaque(false);

        playButton = new JButton("Play");
        howToPlayButton = new JButton("How to Play");
        restartButton = new JButton("Restart");

        playButton.addActionListener(e -> {
            currentState = GameState.PLAYING;
            startGame();
            hideButtons();
        });

        howToPlayButton.addActionListener(e -> {
            currentState = GameState.HOW_TO_PLAY;
            hideButtons();
        });

        restartButton.addActionListener(e -> {
            restartGame();
        });
        restartButton.setVisible(false);
        this.add(restartButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        buttonPanel.add(playButton, gbc);
        buttonPanel.add(howToPlayButton, gbc);

        this.setLayout(new BorderLayout());
        this.add(buttonPanel, BorderLayout.CENTER);
    }

    private void hideButtons() {
        buttonPanel.setVisible(false);
        this.requestFocusInWindow();
    }

    private void showButtons() {
        buttonPanel.setVisible(true);
        this.revalidate();
        this.repaint();
    }

    private void returnToMainMenu() {
        currentState = GameState.START_SCREEN;
        showButtons();
    }

    public void startGame() {
        newApple();
        running = true;
        timer = new Timer(DELAY, this);
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        switch (currentState) {
            case START_SCREEN:
                drawStartScreen(g);
                showButtons();
                break;
            case PLAYING:
                draw(g);
                break;
            case GAME_OVER:
                draw(g);
                gameOver(g);
                restartButton.setVisible(true);
                break;
            case HOW_TO_PLAY:
                drawHowToPlay(g);
                break;

        }
    }

    public void draw(Graphics g) {
        if (running) {
            g.setColor(Color.red);
            g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    g.setColor(Color.green);
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                } else {
                    g.setColor(new Color(45, 180, 0));
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                }
            }
            g.setColor(Color.white);
            g.setFont(new Font("Monospaced", Font.BOLD, 40));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Score: " + applesEaten, (SCREEN_WIDTH - metrics.stringWidth("Score: " + applesEaten)) / 2, g.getFont().getSize());
        } else {
            gameOver(g);
        }
    }

    public void newApple() {
        int tempX, tempY;
        boolean validLocation;

        do {
            validLocation = true;
            tempX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            tempY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;

            // Check if the new location overlaps with the snake
            for (int i = 0; i < bodyParts; i++) {
                if (tempX == x[i] && tempY == y[i]) {
                    validLocation = false;
                    break;
                }
            }
        } while (!validLocation);

        appleX = tempX;
        appleY = tempY;
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i-1];
            y[i] = y[i-1];
        }

        switch (direction) {
            case 'U':
                y[0] = y[0] - UNIT_SIZE;
                break;
            case 'D':
                y[0] = y[0] + UNIT_SIZE;
                break;
            case 'L':
                x[0] = x[0] - UNIT_SIZE;
                break;
            case 'R':
                x[0] = x[0] + UNIT_SIZE;
                break;
        }
        lastDirection = direction;
        directionChanged = false;
    }

    public void checkApple() {
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            newApple();
        }
    }

    public void checkCollisions() {
        // Check if head collides with body
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
                return;
            }
        }
        // Check if head touches left border
        if (x[0] < 0) {
            running = false;
        }
        // Check if head touches right border
        if (x[0] >= SCREEN_WIDTH) {
            running = false;
        }
        // Check if head touches top border
        if (y[0] < 0) {
            running = false;
        }
        // Check if head touches bottom border
        if (y[0] >= SCREEN_HEIGHT) {
            running = false;
        }

        if (!running) {
            timer.stop();
            currentState = GameState.GAME_OVER;
            restartButton.setVisible(true);
            restartButton.setBounds(SCREEN_WIDTH/2 - 50, SCREEN_HEIGHT/2 + 50, 100, 30);
        }
    }

    public void gameOver(Graphics g) {
        // Score
        g.setColor(Color.white);
        g.setFont(new Font("Monospaced", Font.BOLD, 40));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Score: " + applesEaten, (SCREEN_WIDTH - metrics1.stringWidth("Score: " + applesEaten)) / 2, g.getFont().getSize());
        // Game Over text
        g.setColor(Color.white);
        g.setFont(new Font("Monospaced", Font.BOLD, 75));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Game Over", (SCREEN_WIDTH - metrics2.stringWidth("Game Over")) / 2, SCREEN_HEIGHT / 2);
    }

    private void restartGame() {
        bodyParts = 6;
        applesEaten = 0;
        direction = 'R';
        for (int i = 0; i < bodyParts; i++) {
            x[i] = 0;
            y[i] = 0;
        }
        currentState = GameState.PLAYING;
        restartButton.setVisible(false);
        startGame();
    }

    private void drawStartScreen(Graphics g) {
        g.setColor(Color.white);
        g.setFont(new Font("Monospaced", Font.BOLD, 75));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString("Snake Game", (SCREEN_WIDTH - metrics.stringWidth("Snake Game")) / 2, SCREEN_HEIGHT / 4);
    }

    private void drawHowToPlay(Graphics g) {
        g.setColor(Color.white);
        g.setFont(new Font("Monospaced", Font.BOLD, 40));
        g.drawString("How to Play", SCREEN_WIDTH / 4, SCREEN_HEIGHT / 4);
        g.setFont(new Font("Monospaced", Font.PLAIN, 20));
        g.drawString("Use arrow keys to control the snake", SCREEN_WIDTH / 4, SCREEN_HEIGHT / 2);
        g.drawString("Eat apples to grow", SCREEN_WIDTH / 4, SCREEN_HEIGHT / 2 + 30);
        g.drawString("Don't hit the walls or yourself!", SCREEN_WIDTH / 4, SCREEN_HEIGHT / 2 + 60);
        g.drawString("Press any key to return to main menu", SCREEN_WIDTH / 4, SCREEN_HEIGHT / 2 + 120);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (directionChanged) {
                return;
            }

            if (currentState == GameState.HOW_TO_PLAY) {
                returnToMainMenu();
                return;
            }

            if (currentState != GameState.PLAYING) return;

            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (lastDirection != 'R') {
                        direction = 'L';
                        directionChanged = true;
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (lastDirection != 'L') {
                        direction = 'R';
                        directionChanged = true;
                    }
                    break;
                case KeyEvent.VK_UP:
                    if (lastDirection != 'D') {
                        direction = 'U';
                        directionChanged = true;
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (lastDirection != 'U') {
                        direction = 'D';
                        directionChanged = true;
                    }
                    break;
            }
        }
    }
}