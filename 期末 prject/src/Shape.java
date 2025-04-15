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
        if (coords == SHAPES[1]) return; // O§Î¤£±ÛÂà
        for (int i = 0; i < 4; i++) {
            int temp = coords[i][0];
            coords[i][0] = -coords[i][1];
            coords[i][1] = temp;
        }
    }

    public int[][] getCoordinates() {
        return coords;
    }
}
