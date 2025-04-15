import javax.swing.JFrame;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TetrisGame extends JFrame {
    public TetrisGame() {
        setTitle("Tetris");
        setSize(450, 650); // �W�[�e�ץH�e�ǹw���ϰ�
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Board board = new Board();
        add(board);
        board.startGame();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TetrisGame().setVisible(true));
    }
}