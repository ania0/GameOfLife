import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {
    private GridManager gridManager;

    public GamePanel(GridManager gridManager) {
        this.gridManager = gridManager;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int[][] grid = gridManager.getGrid();
        int[][] threadOwnership = gridManager.getThreadOwnership();
        int rows = grid.length;
        int cols = grid[0].length;
        int cellWidth = getWidth() / cols;
        int cellHeight = getHeight() / rows;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int threadId = threadOwnership[i][j];
                Color threadColor = getThreadColor(threadId);
                g.setColor(threadColor);
                g.fillRect(j * cellWidth, i * cellHeight, cellWidth, cellHeight);

                if (grid[i][j] == 1) {
                    g.setColor(Color.BLACK);
                } else {
                    g.setColor(new Color(255, 255, 255, 120));
                }
                g.fillRect(j * cellWidth, i * cellHeight, cellWidth, cellHeight);

                g.setColor(Color.GRAY);
                g.drawRect(j * cellWidth, i * cellHeight, cellWidth, cellHeight);
            }
        }
    }

    private Color getThreadColor(int threadId) {
        int r = (10 + threadId * 40) % 256;
        int g = (50 + threadId * 60) % 256;
        int b = (150 + threadId * 30) % 256;
        return new Color(r, g, b, 180);
    }
}
