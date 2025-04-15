import javax.swing.JPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class Board extends JPanel implements ActionListener {
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;
    private static final int BLOCK_SIZE = 30;
    private static final int MAX_DISCARD = 3;
    private Timer timer;
    private Timer spawnTimer;
    private boolean isFallingFinished = false;
    private boolean isGameOver = false;
    private boolean isSpawning = false;
    private int curX = 0;
    private int curY = 0;
    private Shape currentPiece;
    private Shape nextPiece;
    private int[][] board = new int[BOARD_HEIGHT][BOARD_WIDTH];
    private int score = 0;
    private int currentShapeIndex;
    private int nextShapeIndex;
    private int discardCount = 0;
    private String discardMessage = "";
    private long discardMessageTime = 0;
    private int currentDelay = 400;
    private static final int[][] SPEED_LEVELS = {
        {0, 400}, {501, 300}, {1001, 200}, {2001, 100}
    };
    private static final Color[] SHAPE_COLORS = {
        Color.CYAN,    // I
        Color.YELLOW,  // O
        Color.MAGENTA, // T
        Color.GREEN,   // S
        Color.RED,     // Z
        Color.BLUE,    // J
        Color.ORANGE   // L
    };

    public Board() {
        setFocusable(true);
        timer = new Timer(currentDelay, this);
        spawnTimer = new Timer(100, e -> animateSpawn());
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
                if (isSpawning) {
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
        board = new int[BOARD_HEIGHT][BOARD_WIDTH];
        score = 0;
        discardCount = 0;
        discardMessage = "";
        isGameOver = false;
        isSpawning = false;
        currentDelay = 400;
        timer = new Timer(currentDelay, this);
        currentPiece.setRandomShape();
        currentShapeIndex = currentPiece.getShapeIndex();
        nextPiece.setRandomShape();
        nextShapeIndex = nextPiece.getShapeIndex();
        spawnNewPiece();
    }

    private void spawnNewPiece() {
        currentPiece = nextPiece;
        currentShapeIndex = nextShapeIndex;
        nextPiece = new Shape();
        nextPiece.setRandomShape();
        nextShapeIndex = nextPiece.getShapeIndex();
        curX = BOARD_WIDTH / 2;

        // 計算方塊最大高度，設置初始 curY
        int maxY = 0;
        for (int[] block : currentPiece.getCoordinates()) {
            maxY = Math.max(maxY, block[1]);
        }
        curY = -maxY;

        // 檢查初始位置是否合法
        boolean canPlace = true;
        for (int[] block : currentPiece.getCoordinates()) {
            int x = curX + block[0];
            int y = curY + block[1];
            if (x < 0 || x >= BOARD_WIDTH || y >= BOARD_HEIGHT) {
                canPlace = false;
                break;
            }
            if (y >= 0 && board[y][x] != 0) {
                canPlace = false;
                break;
            }
        }
        if (!canPlace) {
            isGameOver = true;
            timer.stop();
            spawnTimer.stop();
            repaint();
            return;
        }

        // 開始動畫
        isSpawning = true;
        timer.stop();
        spawnTimer.start();
    }

    private void animateSpawn() {
        if (!tryMove(curX, curY + 1)) {
            // 碰撞，結束動畫並檢查遊戲結束
            spawnTimer.stop();
            isSpawning = false;
            for (int[] block : currentPiece.getCoordinates()) {
                int x = curX + block[0];
                int y = curY + block[1];
                if (y >= 0 && y < BOARD_HEIGHT && board[y][x] != 0) {
                    isGameOver = true;
                    repaint();
                    return;
                }
            }
            // 若未結束，固定方塊
            dropPiece();
        } else if (curY >= 0) {
            // 到達 y = 0，結束動畫
            spawnTimer.stop();
            isSpawning = false;
            curY = 0;
            if (!isGameOver) {
                timer.start();
            }
        }
        repaint();
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
        while (tryMove(curX, newY + 1)) {
            newY++;
        }
        curY = newY;
        int dropDistance = curY - originalY;
        score += dropDistance * 2;
        dropPiece();
        updateSpeed();
    }

    private boolean tryMove(int newX, int newY) {
        for (int[] block : currentPiece.getCoordinates()) {
            int x = newX + block[0];
            int y = newY + block[1];
            if (x < 0 || x >= BOARD_WIDTH || y >= BOARD_HEIGHT) {
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
        // 儲存當前座標和 curX, curY
        int[][] oldCoords = new int[4][2];
        for (int i = 0; i < 4; i++) {
            oldCoords[i][0] = currentPiece.getCoordinates()[i][0];
            oldCoords[i][1] = currentPiece.getCoordinates()[i][1];
        }
        int oldX = curX;
        int oldY = curY;

        // 嘗試旋轉
        currentPiece.rotate();

        // 特殊處理 I 形
        if (currentPiece.getShapeIndex() == 0) {
            boolean isHorizontal = true;
            for (int i = 1; i < 4; i++) {
                if (currentPiece.getCoordinates()[i][1] != currentPiece.getCoordinates()[0][1]) {
                    isHorizontal = false;
                    break;
                }
            }
            if (isHorizontal) {
                // 垂直 -> 水平
                curX = oldX - 1;
                curY = oldY + 2;
            } else {
                // 水平 -> 垂直
                curX = oldX + 1;
                curY = oldY - 2;
            }
        }

        // 檢查旋轉後是否合法
        for (int[] block : currentPiece.getCoordinates()) {
            int x = curX + block[0];
            int y = curY + block[1];
            if (x < 0 || x >= BOARD_WIDTH || y >= BOARD_HEIGHT) {
                currentPiece.setCoordinates(oldCoords);
                curX = oldX;
                curY = oldY;
                return false;
            }
            if (y >= 0 && board[y][x] != 0) {
                currentPiece.setCoordinates(oldCoords);
                curX = oldX;
                curY = oldY;
                return false;
            }
        }
        repaint();
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isGameOver && !isSpawning && !tryMove(curX, curY + 1)) {
            dropPiece();
        }
        repaint();
    }

    private void dropPiece() {
        for (int[] block : currentPiece.getCoordinates()) {
            int x = curX + block[0];
            int y = curY + block[1];
            if (y >= 0 && y < BOARD_HEIGHT && x >= 0 && x < BOARD_WIDTH) {
                board[y][x] = currentShapeIndex + 1;
            }
        }
        clearLines();
        if (!isFallingFinished && !isGameOver) {
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
            updateSpeed();
        }
    }

    private void updateSpeed() {
        int newDelay = currentDelay;
        for (int[] level : SPEED_LEVELS) {
            if (score >= level[0]) {
                newDelay = level[1];
            } else {
                break;
            }
        }
        if (newDelay != currentDelay) {
            currentDelay = newDelay;
            timer.stop();
            timer = new Timer(currentDelay, this);
            if (!isGameOver && !isSpawning) {
                timer.start();
            }
        }
    }

    private void restartGame() {
        board = new int[BOARD_HEIGHT][BOARD_WIDTH];
        score = 0;
        discardCount = 0;
        discardMessage = "";
        isGameOver = false;
        isSpawning = false;
        currentDelay = 400;
        timer.stop();
        spawnTimer.stop();
        timer = new Timer(currentDelay, this);
        currentPiece = new Shape();
        nextPiece = new Shape();
        startGame();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 繪製遊戲區域（固定方塊）
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
        // 繪製當前方塊（包括動畫中的 y < 0）
        if (!isGameOver || isSpawning) {
            g.setColor(SHAPE_COLORS[currentShapeIndex]);
            for (int[] block : currentPiece.getCoordinates()) {
                int x = (curX + block[0]) * BLOCK_SIZE;
                int y = (curY + block[1]) * BLOCK_SIZE;
                g.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
            }
        }
        // 繪製預覽
        g.setColor(Color.BLACK);
        g.drawString("Next:", 310, 20);
        g.setColor(SHAPE_COLORS[nextShapeIndex]);
        for (int[] block : nextPiece.getCoordinates()) {
            int x = 310 + (block[0] + 2) * BLOCK_SIZE;
            int y = 40 + (block[1] + 1) * BLOCK_SIZE;
            g.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
        }
        // 繪製分數和訊息
        g.setColor(Color.BLACK);
        g.drawString("Score: " + score, 10, 20);
        g.drawString("Discards Left: " + (MAX_DISCARD - discardCount), 10, 40);
        if (!discardMessage.isEmpty() && System.currentTimeMillis() - discardMessageTime < 2000) {
            g.drawString(discardMessage, 10, 60);
        }
        if (isGameOver) {
            g.setColor(Color.BLACK);
            g.drawString("Game Over - Press R to Restart", 50, 300);
        }
    }
}