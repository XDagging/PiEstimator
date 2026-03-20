import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;


public class PiEstimator extends JFrame {

    private final JLabel actualPiLabel;
    private final JLabel estimateLabel;
    private final JLabel trialsLabel;
    private final JButton runPauseButton;

    private long totalTrials = 0;
    private long pointsInside = 0;

    private volatile boolean running = false;
    private final Object lock = new Object();

    public PiEstimator() {
        super("Pi Estimator");

    
		
        actualPiLabel = new JLabel("Actual value of pi: " + Math.PI);


        estimateLabel = new JLabel("Current Estimate: 0");
        trialsLabel = new JLabel("Number of Trials: 0");


        runPauseButton = new JButton("Run");



		
        Font labelFont = new Font("Monospaced", Font.BOLD, 18);
        actualPiLabel.setFont(labelFont);


        estimateLabel.setFont(labelFont);
        trialsLabel.setFont(labelFont);


        runPauseButton.setFont(new Font("SansSerif", Font.PLAIN, 14));

        setLayout(new BorderLayout(10, 10));


        JPanel labelPanel = new JPanel(new GridLayout(3, 1, 10, 10));



        labelPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        labelPanel.add(actualPiLabel);



        labelPanel.add(estimateLabel);
        labelPanel.add(trialsLabel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(runPauseButton);

        add(labelPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        runPauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleRunning();
            }
        });

        // Initialize and start computation thread
        Thread computationThread = new Thread(new ComputationTask());
        computationThread.setDaemon(true);
        computationThread.start();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void toggleRunning() {
        synchronized (lock) {
            running = !running;
            if (running) {
                runPauseButton.setText("Pause");
                lock.notifyAll(); // Wake up computation thread
            } else {
                runPauseButton.setText("Run");
            }
        }
    }




    private void updateDisplay() {
        double estimate = 4.0 * pointsInside / totalTrials;
        SwingUtilities.invokeLater(() -> {
            estimateLabel.setText("Current Estimate: " + estimate);
            trialsLabel.setText("Number of Trials: " + totalTrials);
        });
    }

    private class ComputationTask implements Runnable {
        @Override
        public void run() {
            while (true) {
                synchronized (lock) {
                    while (!running) {
                        try {
                            lock.wait(); // Pause thread
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }

                // Perform a batch of 1 million trials
                for (int i = 0; i < 1000000; i++) {
                    double x = Math.random();
                    double y = Math.random();
                    if (x * x + y * y < 1.0) {
                        pointsInside++;
                    }
                    totalTrials++;
                }

                updateDisplay();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PiEstimator());
    }
}
