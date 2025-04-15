import java.util.Random;
public class Shape {
    private int[][] coords;
    private static final int[][][] SHAPES = {
        {{0,0}, {0,1}, {0,2}, {0,3}}, // I
        {{0,0}, {0,1}, {1,0}, {1,1}}, // O
        {{0,0}, {0,1}, {0,2}, {1,1}}, // T
        {{0,0}, {0,1}, {1,1}, {1,2}}, // S
        {{0,0}, {0,1}, {1,0}, {1,-1}}, // Z
        {{0,0}, {0,1}, {0,2}, {1,0}}, // J
        {{0,0}, {0,1}, {0,2}, {-1,0}} // L
    };

    public Shape() {
        coords = new int[4][2];
        setRandomShape();
    }

    public void setRandomShape() {
        Random r = new Random();
        int shapeIndex = r.nextInt(SHAPES.length);
        for (int i = 0; i < 4; i++) {
            coords[i][0] = SHAPES[shapeIndex][i][0];
            coords[i][1] = SHAPES[shapeIndex][i][1];
        }
    }

    public void rotate() {
        // 檢查是否為 O 形（不旋轉）
        boolean isOShape = true;
        for (int i = 0; i < 4; i++) {
            int newX = -coords[i][1];
            int newY = coords[i][0];
            if (newX != coords[i][0] || newY != coords[i][1]) {
                isOShape = false;
                break;
            }
        }
        if (isOShape) {
            return;
        }
        // 執行旋轉
        int[][] newCoords = new int[4][2];
        for (int i = 0; i < 4; i++) {
            newCoords[i][0] = -coords[i][1];
            newCoords[i][1] = coords[i][0];
        }
        coords = newCoords;
    }

    public int[][] getCoordinates() {
        return coords;
    }

    public void setCoordinates(int[][] newCoords) {
        for (int i = 0; i < 4; i++) {
            coords[i][0] = newCoords[i][0];
            coords[i][1] = newCoords[i][1];
        }
    }
}