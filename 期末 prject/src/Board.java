import javax.swing.JPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class Board extends JPanel implements ActionListener {
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;
    private static final int BLOCK_SIZE = 30;
    private Timer timer;
    private boolean isFallingFinished = false;
    private boolean isGameOver = false;
    private int curX = 0;
    private int curY = 0;
    private Shape currentPiece;
    private int[][] board = new int[BOARD_HEIGHT][BOARD_WIDTH];
    private int score = 0;
    private int currentShapeIndex;
    private static final Color[] SHAPE_COLORS = {
        Color.CYAN, Color.YELLOW, Color.MAGENTA, Color.GREEN,
        Color.RED, Color.BLUE, Color.ORANGE
    };

    public Board() {
        setFocusable(true);
        timer = new Timer(400, this);
        currentPiece = new Shape();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (isGameOver) {
                    if (e.getKeyCode() == KeyEvent.VK_R) {
                        restartGame();
                    }
                    return;
                }
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT -> tryMove(curX - 1, curY);
                    case KeyEvent.VK_RIGHT -> tryMove(curX + 1, curY);
                    case KeyEvent.VK_DOWN -> tryMove(curX, curY + 1);
                    case KeyEvent.VK_UP -> {
                        if (tryRotate()) {
                            repaint();
                        }
                    }
                }
                repaint();
            }
        });
    }

    public void startGame() {
        spawnNewPiece();
        timer.start();
    }

    private void spawnNewPiece() {
        currentPiece = new Shape();
        currentPiece.setRandomShape();
        currentShapeIndex = new Random().nextInt(SHAPE_COLORS.length);
        curX = BOARD_WIDTH / 2;
        curY = 0;

        // 檢查新方塊是否可以放置在初始位置
        if (!tryMove(curX, curY)) {
            // 只有當方塊堆積到頂部（y=0 或更低）時才結束遊戲
            boolean isTopBlocked = false;
            for (int[] block : currentPiece.getCoordinates()) {
                int y = curY + block[1];
                if (y < 0) { // 方塊超出頂部
                    isTopBlocked = true;
                    break;
                }
            }
            if (isTopBlocked) {
                isGameOver = true;
                timer.stop();
            } else {
                // 如果頂部未被阻塞，可能是其他碰撞，遊戲繼續
                // 這裡可以選擇調整初始位置或直接生成新方塊
                spawnNewPiece(); // 簡單起見，重新生成
            }
        }
    }

    private boolean tryMove(int newX, int newY) {
        for (int[] block : currentPiece.getCoordinates()) {
            int x = newX + block[0];
            int y = newY + block[1];
            // 允許方塊部分超出頂部（y < 0），只要不與其他方塊碰撞
            if (x < 0 || x >= BOARD_WIDTH || y >= BOARD_HEIGHT || (y >= 0 && board[y][x] != 0)) {
                return false;
            }
        }
        curX = newX;
        curY = newY;
        return true;
    }

    private boolean tryRotate() {
        Shape rotatedPiece = new Shape();
        int[][] oldCoords = currentPiece.getCoordinates();
        rotatedPiece.setRandomShape();
        for (int i = 0; i < 4; i++) {
            rotatedPiece.getCoordinates()[i][0] = oldCoords[i][0];
            rotatedPiece.getCoordinates()[i][1] = oldCoords[i][1];
        }
        rotatedPiece.rotate();

        for (int[] block : rotatedPiece.getCoordinates()) {
            int x = curX + block[0];
            int y = curY + block[1];
            if (x < 0 || x >= BOARD_WIDTH || y >= BOARD_HEIGHT || (y >= 0 && board[y][x] != 0)) {
                return false;
            }
        }

        currentPiece = rotatedPiece;
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!tryMove(curX, curY + 1)) {
            dropPiece();
        }
        repaint();
    }

    private void dropPiece() {
        for (int[] block : currentPiece.getCoordinates()) {
            int x = curX + block[0];
            int y = curY + block[1];
            if (y >= 0) { // 僅記錄在遊戲區域內的方塊
                board[y][x] = currentShapeIndex + 1;
            }
        }
        clearLines();
        if (!isFallingFinished) {
            spawnNewPiece();
        }
    }

    private void clearLines() {
        int linesCleared = 0;
        for (int y = BOARD_HEIGHT - 1; y >= 0; y--) {
            boolean isFull = true;
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (board[y][x] == 0) {
                    isFull = false;
                    break;
                }
            }
            if (isFull) {
                linesCleared++;
                for (int i = y; i > 0; i--) {
                    board[i] = board[i - 1].clone();
                }
                board[0] = new int[BOARD_WIDTH];
                y++;
            }
        }
        if (linesCleared > 0) {
            score += linesCleared * 100;
        }
    }

    private void restartGame() {
        board = new int[BOARD_HEIGHT][BOARD_WIDTH];
        score = 0;
        isGameOver = false;
        currentPiece = new Shape();
        startGame();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (board[y][x] != 0) {
                    g.setColor(SHAPE_COLORS[board[y][x] - 1]);
                    g.fillRect(x * BLOCK_SIZE, y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
                }
                g.setColor(Color.BLACK);
                g.drawRect(x * BLOCK_SIZE, y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
            }
        }
        g.setColor(SHAPE_COLORS[currentShapeIndex]);
        for (int[] block : currentPiece.getCoordinates()) {
            int x = (curX + block[0]) * BLOCK_SIZE;
            int y = (curY + block[1]) * BLOCK_SIZE;
            g.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
        }
        g.setColor(Color.BLACK);
        g.drawString("Score: " + score, 10, 20);
        if (isGameOver) {
            g.drawString("Game Over - Press R to Restart", 50, 300);
        }
    }
}