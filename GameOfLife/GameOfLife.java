import javax.swing.*;
import java.awt.*;
import java.util.concurrent.*;

public class GameOfLife {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java GameOfLife <input_file> <num_threads>");
            return;
        }

        String inputFile = args[0];
        int numThreads;

        try {
            numThreads = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Error: Number of threads must be an integer.");
            return;
        }

        GridManager gridManager = new GridManager(inputFile, numThreads);

        JFrame frame = new JFrame("Conway's Game of Life - Multithreading Visualization");
        GamePanel gamePanel = new GamePanel(gridManager);
        JTextArea threadReportArea = new JTextArea(6, 50);
        threadReportArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(threadReportArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        frame.setLayout(new BorderLayout());
        frame.add(gamePanel, BorderLayout.CENTER);
        frame.add(scrollPane, BorderLayout.SOUTH);
        frame.setSize(800, 900);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        gridManager.startGame(threadReportArea, gamePanel);
    }
}
