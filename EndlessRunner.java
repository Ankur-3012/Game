import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class EndlessRunner extends JPanel implements ActionListener, KeyListener {
    private final int width = 800;
    private final int height = 400;
    private Timer timer;
    private Player player;
    private ArrayList<Obstacle> obstacles;
    private int score;
    private boolean gameOver;
    private Random random = new Random();
    private final Color skyColor = new Color(135, 206, 235);
    private final Color groundColor = new Color(34, 139, 34);
    private JButton retryButton;
    private int backgroundOffset = 0;
    private Color cloudColor = Color.WHITE; // Define cloudColor

    public EndlessRunner() {
        setPreferredSize(new Dimension(width, height));
        setBackground(skyColor);
        setFocusable(true);
        addKeyListener(this);

        player = new Player(50, height - 50);
        obstacles = new ArrayList<>();
        score = 0;
        gameOver = false;

        retryButton = new JButton("Retry");
        retryButton.setBounds(width / 2 - 50, height / 2 + 50, 100, 30);
        retryButton.addActionListener(e -> resetGame());
        retryButton.setVisible(false);
        setLayout(null);
        add(retryButton);

        timer = new Timer(30, this);
        timer.start();
    }

    private void resetGame() {
        player = new Player(50, height - 50);
        obstacles.clear();
        score = 0;
        gameOver = false;
        retryButton.setVisible(false);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            player.update();
            updateObstacles();
            checkCollisions();
            score++;
            backgroundOffset -= 1;
            if (backgroundOffset <= -width) {
                backgroundOffset = 0;
            }
        }
        repaint();
    }

    private void updateObstacles() {
        // Adjusting obstacle generation frequency based on score
        int obstacleGap = Math.max(200 - score / 10, 50); // Minimum gap is 50

        if (obstacles.isEmpty() || obstacles.get(obstacles.size() - 1).getX() < width - obstacleGap) {
            obstacles.add(new Obstacle(width, height - 50, random.nextInt(30) + 20));
        }

        for (int i = obstacles.size() - 1; i >= 0; i--) {
            Obstacle obstacle = obstacles.get(i);
            obstacle.update();
            if (obstacle.getX() + obstacle.getWidth() < 0) {
                obstacles.remove(i);
            }
        }
    }

    private void checkCollisions() {
        for (Obstacle obstacle : obstacles) {
            if (player.getBounds().intersects(obstacle.getBounds())) {
                gameOver = true;
                break;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        drawBackground(g2d);

        player.draw(g2d);
        for (Obstacle obstacle : obstacles) {
            obstacle.draw(g2d);
        }

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Score: " + score, 10, 30);

        if (gameOver) {
            g2d.setFont(new Font("Arial", Font.BOLD, 40));
            g2d.drawString("Game Over!", width / 2 - 100, height / 2 - 20);
            retryButton.setVisible(true);
        }
    }

    private void drawBackground(Graphics2D g2d) {
        g2d.setColor(skyColor);
        g2d.fillRect(0, 0, width, height - 50); // Restored sky height

        g2d.setColor(groundColor);
        // Restored ground height back to 50
        g2d.fillRect(0, height - 50, width, 50); // Updated ground position

        drawClouds(g2d);
    }

    private void drawClouds(Graphics2D g2d) {
        for (int i = 0; i < 5; i++) { // Changed from previous number to 5
            int x = (int) (Math.random() * width);
            int y = (int) (Math.random() * (height - 100));
            g2d.setColor(cloudColor);
            g2d.fillOval(x, y, 60, 30); // Draw cloud
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            player.jump();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Endless Runner");
        EndlessRunner game = new EndlessRunner();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }
}

class Player {
    private int x, y;
    private final int width = 40, height = 30;
    private int yVelocity = 0;
    private boolean isJumping = false;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        if (isJumping) {
            y += yVelocity;
            yVelocity += 1;  // Gravity effect
            if (y >= 400 - 50) {  // Ground level
                y = 400 - 50;
                isJumping = false;
            }
            if (y < 0) {
                y = 0;
                isJumping = false;
            }
        }

        x += 1;  // Decreased horizontal movement from 2 to 1

        if (x > 800 - width) {
            x = 800 - width;
        }

        if (x < 0) {
            x = 0;
        }
    }

    public void jump() {
        if (!isJumping) {
            yVelocity = -15;
            isJumping = true;
        }
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(200, 150, 100));
        g2d.fillOval(x, y, width, height);

        int earSize = 10;
        int[] xPoints = {x + 5, x + 15, x + 25};
        int[] yPoints = {y - earSize, y, y - earSize};
        g2d.fillPolygon(xPoints, yPoints, 3);

        g2d.setColor(Color.BLACK);
        g2d.fillOval(x + 10, y + 5, 5, 5);  // Left eye
        g2d.fillOval(x + 25, y + 5, 5, 5);  // Right eye
        g2d.drawLine(x + 15, y + 15, x + 25, y + 15);  // Mouth
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}

class Obstacle {
    private int x, y;
    private final int width;
    private final int height;
    private Random random;

    public Obstacle(int x, int y, int height) {
        this.x = x;
        this.y = y;
        this.width = 20;
        this.height = height;
        this.random = new Random();
    }

    public void update() {
        x -= 2; // Decreased speed from 3 to 2
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(0, 100, 0));
        g2d.fillRoundRect(x, y, width, height, 10, 10);

        g2d.fillRoundRect(x - 10, y + 15, 15, 10, 5, 5);
        g2d.fillRoundRect(x + width - 5, y + 25, 15, 10, 5, 5);

        g2d.setColor(Color.WHITE);
        for (int i = 0; i < 10; i++) {
            int spineX = x + random.nextInt(width);
            int spineY = y + random.nextInt(height);
            g2d.drawLine(spineX, spineY, spineX + 2, spineY + 2);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public int getX() {
        return x;
    }

    public int getWidth() {
        return width;
    }
}
