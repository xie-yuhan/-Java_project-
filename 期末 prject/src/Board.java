import javax.swing.JPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class Board extends JPanel implements ActionListener {
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;
    private static final int BLOCK_SIZE = 30;
    private static final int MAX_DISCARD = 3; // 最大丟棄次數
    private Timer timer;
    private boolean isFallingFinished = false;
    private boolean isGameOver = false;
    private int curX = 0;
    private int curY = 0;
    private Shape currentPiece;
    private Shape nextPiece; // 下一個方塊
    private int[][] board = new int[BOARD_HEIGHT][BOARD_WIDTH];
    private int score = 0;
    private int currentShapeIndex;
    private int nextShapeIndex;
    private int discardCount = 0; // 已使用的丟棄次數
    private String discardMessage = ""; // 丟棄提示訊息
    private long discardMessageTime = 0; // 提示訊息顯示時間
    private static final Color[] SHAPE_COLORS = {
        Color.CYAN, Color.YELLOW, Color.MAGENTA, Color.GREEN,
        Color.RED, Color.BLUE, Color.ORANGE
    };

    public Board() {
        setFocusable(true);
        timer = new Timer(400, this);
        currentPiece = new Shape();
        nextPiece = new Shape();
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
                    case KeyEvent.VK_UP -> tryRotate();
                    case KeyEvent.VK_D -> discardCurrentPiece();
                    case KeyEvent.VK_SPACE -> hardDrop();
                }
                repaint();
            }
        });
    }

    public void startGame() {
        currentPiece.setRandomShape();
        currentShapeIndex = new Random().nextInt(SHAPE_COLORS.length);
        nextPiece.setRandomShape();
        nextShapeIndex = new Random().nextInt(SHAPE_COLORS.length);
        curX = BOARD_WIDTH / 2;
        curY = 0;
        timer.start();
    }

    private void spawnNewPiece() {
        currentPiece = nextPiece;
        currentShapeIndex = nextShapeIndex;
        nextPiece = new Shape();
        nextPiece.setRandomShape();
        nextShapeIndex = new Random().nextInt(SHAPE_COLORS.length);
        curX = BOARD_WIDTH / 2;
        curY = 0;

        if (!tryMove(curX, curY)) {
            boolean isTopBlocked = false;
            for (int[] block : currentPiece.getCoordinates()) {
                int y = curY + block[1];
                if (y < 0) {
                    isTopBlocked = true;
                    break;
                }
            }
            if (isTopBlocked) {
                isGameOver = true;
                timer.stop();
            }
        }
    }

    private void discardCurrentPiece() {
        if (discardCount >= MAX_DISCARD) {
            discardMessage = "No discards left!";
            discardMessageTime = System.currentTimeMillis();
            return;
        }
        discardCount++;
        spawnNewPiece();
        discardMessage = "Discarded! " + (MAX_DISCARD - discardCount) + " left";
        discardMessageTime = System.currentTimeMillis();
    }

    private void hardDrop() {
        int originalY = curY;
        int newY = curY;
        // 找到最底部合法位置
        while (tryMove(curX, newY + 1)) {
            newY++;
        }
        curY = newY; // 更新 curY 為最終位置
        // 計算掉落距離並加分
        int dropDistance = curY - originalY;
        score += dropDistance * 2;
        // 固定方塊
        dropPiece();
    }

    private boolean tryMove(int newX, int newY) {
        for (int[] block : currentPiece.getCoordinates()) {
            int x = newX + block[0];
            int y = newY + block[1];
            // 嚴格檢查邊界
            if (x < 0 || x >= BOARD_WIDTH || y < -2 || y >= BOARD_HEIGHT) {
                return false;
            }
            if (y >= 0 && board[y][x] != 0) {
                return false;
            }
        }
        curX = newX;
        curY = newY;
        return true;
    }

    private boolean tryRotate() {
        int[][] oldCoords = currentPiece.getCoordinates();
        int[][] newCoords = new int[4][2];
        for (int i = 0; i < 4; i++) {
            newCoords[i][0] = -oldCoords[i][1];
            newCoords[i][1] = oldCoords[i][0];
        }
        // 檢查 O 形（不旋轉）
        boolean isOShape = true;
        for (int i = 0; i < 4; i++) {
            if (newCoords[i][0] != oldCoords[i][0] || newCoords[i][1] != oldCoords[i][1]) {
                isOShape = false;
                break;
            }
        }
        if (isOShape) {
            return false;
        }
        // 檢查旋轉後位置
        for (int[] block : newCoords) {
            int x = curX + block[0];
            int y = curY + block[1];
            if (x < 0 || x >= BOARD_WIDTH || y < -2 || y >= BOARD_HEIGHT) {
                return false;
            }
            if (y >= 0 && board[y][x] != 0) {
                return false;
            }
        }
        currentPiece.setCoordinates(newCoords);
        repaint();
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isGameOver && !tryMove(curX, curY + 1)) {
            dropPiece();
        }
        repaint();
    }

    private void dropPiece() {
        for (int[] block : currentPiece.getCoordinates()) {
            int x = curX + block[0];
            int y = curY + block[1];
            // 確保 y 在合法範圍
            if (y >= 0 && y < BOARD_HEIGHT && x >= 0 && x < BOARD_WIDTH) {
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
        discardCount = 0;
        discardMessage = "";
        isGameOver = false;
        currentPiece = new Shape();
        nextPiece = new Shape();
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
        g.drawString("Next:", 310, 20);
        g.setColor(SHAPE_COLORS[nextShapeIndex]);
        for (int[] block : nextPiece.getCoordinates()) {
            int x = 310 + (block[0] + 2) * BLOCK_SIZE;
            int y = 40 + (block[1] + 1) * BLOCK_SIZE;
            g.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
        }
        g.setColor(Color.BLACK);
        g.drawString("Score: " + score, 10, 20);
        g.drawString("Discards Left: " + (MAX_DISCARD - discardCount), 10, 40);
        if (!discardMessage.isEmpty() && System.currentTimeMillis() - discardMessageTime < 2000) {
            g.drawString(discardMessage, 10, 60);
        }
        if (isGameOver) {
            g.drawString("Game Over - Press R to Restart", 50, 300);
        }
    }
}