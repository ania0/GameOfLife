import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.*;

public class GridManager {
    private static final int ALIVE = 1;
    private static final int DEAD = 0;
    private int rows, cols, iterations, numThreads;
    private int[][] grid, nextGrid;
    private int[][] threadOwnership;
    private JTextArea threadReportArea;
    private GamePanel gamePanel;
    private ExecutorService executorService;

    public GridManager(String inputFile, int numThreads) {
        this.numThreads = numThreads;
        executorService = Executors.newFixedThreadPool(numThreads);
        if (!initializeGridFromFile(inputFile)) {
            throw new IllegalStateException("Grid initialization failed");
        }
    }

    public boolean initializeGridFromFile(String inputFile) {
        try (Scanner scanner = new Scanner(new File(inputFile))) {
            if (!scanner.hasNextInt()) return false;
            rows = scanner.nextInt();
            if (!scanner.hasNextInt()) return false;
            cols = scanner.nextInt();
            if (!scanner.hasNextInt()) return false;
            iterations = scanner.nextInt();
            if (!scanner.hasNextInt()) return false;
            int liveCells = scanner.nextInt();

            grid = new int[rows][cols];
            nextGrid = new int[rows][cols];
            threadOwnership = new int[rows][cols];

            for (int i = 0; i < liveCells; i++) {
                if (!scanner.hasNextInt()) return false;
                int x = scanner.nextInt() - 1;
                if (!scanner.hasNextInt()) return false;
                int y = scanner.nextInt() - 1;

                if (x >= 0 && x < rows && y >= 0 && y < cols) {
                    grid[x][y] = ALIVE;
                } else {
                    return false;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error: Input file not found.");
            return false;
        } catch (Exception e) {
            System.out.println("Error: Invalid file format.");
            return false;
        }
        return true;
    }

    public void startGame(JTextArea threadReportArea, GamePanel gamePanel) {
        this.threadReportArea = threadReportArea;
        this.gamePanel = gamePanel;

        for (int iter = 0; iter < iterations; iter++) {
            CountDownLatch latch = new CountDownLatch(numThreads);
            int rowsPerThread = rows / numThreads;
            int remainder = rows % numThreads;
            threadReportArea.append("Iteration: " + (iter + 1) + "\n");

            int startRow = 0;
            for (int t = 0; t < numThreads; t++) {
                final int endRow = startRow + rowsPerThread + (t < remainder ? 1 : 0);
                final int threadId = t;
                final int finalStartRow = startRow;

                executorService.submit(() -> {
                    computeNextState(finalStartRow, endRow, latch, threadId);
                });

                startRow = endRow;
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            synchronized (grid) {
                int[][] temp = grid;
                grid = nextGrid;
                nextGrid = temp;
            }

            SwingUtilities.invokeLater(gamePanel::repaint);

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        executorService.shutdown();
    }

    private void computeNextState(int startRow, int endRow, CountDownLatch latch, int threadId) {
        int cellsProcessed = 0;
        for (int i = startRow; i < endRow; i++) {
            for (int j = 0; j < cols; j++) {
                int liveNeighbors = countLiveNeighbors(i, j);
                if (grid[i][j] == ALIVE) {
                    nextGrid[i][j] = (liveNeighbors == 2 || liveNeighbors == 3) ? ALIVE : DEAD;
                } else {
                    nextGrid[i][j] = (liveNeighbors == 3) ? ALIVE : DEAD;
                }
                threadOwnership[i][j] = threadId;
                cellsProcessed++;
            }
        }

        synchronized (threadReportArea) {
            threadReportArea.append(String.format("Thread ID: %d | Rows: %d-%d | Cells Processed: %d\n",
                    threadId, startRow, endRow - 1, cellsProcessed));
        }

        latch.countDown();
    }

    private int countLiveNeighbors(int row, int col) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                int r = (row + i + rows) % rows;
                int c = (col + j + cols) % cols;
                count += grid[r][c];
            }
        }
        return count;
    }

    public int[][] getGrid() {
        return grid;
    }
    public int[][] getThreadOwnership() {
        return threadOwnership;
    }

}
