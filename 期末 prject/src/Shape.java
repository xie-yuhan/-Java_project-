import java.util.Random;

public class Shape {
    private int[][] coords;
    private int shapeIndex;
    private static final int[][][] SHAPES = {
        {{0,1}, {0,2}, {0,3}, {0,4}}, // I
        {{0,0}, {0,1}, {1,0}, {1,1}}, // O
        {{0,0}, {0,1}, {0,2}, {1,1}}, // T
        {{0,0}, {0,1}, {1,1}, {1,2}}, // S
        {{0,0}, {0,1}, {1,0}, {1,-1}}, // Z
        {{0,0}, {0,1}, {0,2}, {1,0}}, // J
        {{0,0}, {0,1}, {0,2}, {-1,0}} // L
    };
    private static final int[][] CENTERS = {
        {0, 2}, // I: 垂直時上往下第2格 (0,2)
        {0, 0}, // O: 不使用
        {0, 1}, // T
        {1, 1}, // S
        {1, 0}, // Z
        {0, 1}, // J
        {0, 1}  // L
    };
    private boolean isHorizontal = false;

    public Shape() {
        coords = new int[4][2];
        setRandomShape();
    }

    public void setRandomShape() {
        Random r = new Random();
        shapeIndex = r.nextInt(SHAPES.length);
        for (int i = 0; i < 4; i++) {
            coords[i][0] = SHAPES[shapeIndex][i][0];
            coords[i][1] = SHAPES[shapeIndex][i][1];
        }
        isHorizontal = false;
    }

    public void rotate() {
        // O 形不旋轉
        if (shapeIndex == 1) {
            return;
        }

        // 旋轉每個座標
        int[][] newCoords = new int[4][2];
        if (shapeIndex == 0 && isHorizontal) {
            // 水平 -> 垂直
            for (int i = 0; i < 4; i++) {
                int x = coords[i][0];
                int y = coords[i][1];
                newCoords[i][0] = y + 1; // x = y + 1
                newCoords[i][1] = x - 1; // y = x - 1
            }
            // 校正：x-=1, y+=2
            for (int i = 0; i < 4; i++) {
                newCoords[i][0] -= 1;
                newCoords[i][1] += 2;
            }
            isHorizontal = false;
        } else {
            // 其他形狀或 I 形垂直 -> 水平
            int cx = CENTERS[shapeIndex][0];
            int cy = CENTERS[shapeIndex][1];
            for (int i = 0; i < 4; i++) {
                int x = coords[i][0];
                int y = coords[i][1];
                int xPrime = x - cx;
                int yPrime = y - cy;
                newCoords[i][0] = -yPrime + cx;
                newCoords[i][1] = xPrime + cy;
            }
            if (shapeIndex == 0) {
                // 垂直 -> 水平
                int minX = Integer.MAX_VALUE;
                for (int[] block : newCoords) {
                    minX = Math.min(minX, block[0]);
                }
                int offsetX = 0 - minX;
                for (int i = 0; i < 4; i++) {
                    newCoords[i][0] += offsetX;
                    newCoords[i][1] -= 2;
                }
                isHorizontal = true;
            }
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

    public int getShapeIndex() {
        return shapeIndex;
    }
}