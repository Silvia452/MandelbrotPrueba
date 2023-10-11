import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Mandelbrot {
    private static final int MAX_ITERATIONS = 300;
    private static int numWorkers = 1;
    private static ExecutorService executor;
    private static int[][] resultados;
    private static double x1 = -2;
    private static double y1 = 1;
    private static double x2 = 1;
    private static double y2 = -1;
    private static JPanel panel;
    private static JSpinner spinner;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Conjunto de Mandelbrot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        panel = new JPanel();
        frame.add(panel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        spinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        controlPanel.add(new JLabel("Número de Trabajadores:"));
        controlPanel.add(spinner);


        JButton startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                numWorkers = (int) spinner.getValue();
                calcularConjuntoMandelbrot();
                pintaMandelbrot();
            }
        });
        controlPanel.add(startButton);
        frame.add(controlPanel, BorderLayout.SOUTH);

        frame.setSize(800, 600);
        frame.setVisible(true);
    }

    private static void calcularConjuntoMandelbrot() {
        executor = Executors.newFixedThreadPool(numWorkers);
        resultados = new int[panel.getWidth()][panel.getHeight()];

        int chunkHeight = panel.getHeight() / numWorkers;

        for (int i = 0; i < numWorkers; i++) {
            int startY = i * chunkHeight;
            int endY = (i + 1) * chunkHeight - 1;
            MandelbrotWorker worker = new MandelbrotWorker(0, startY, panel.getWidth() - 1, endY);
            executor.execute(worker);
        }
        executor.shutdown();
    }

    private static void pintaMandelbrot() {
        Graphics g = panel.getGraphics();
        g.clearRect(0, 0, panel.getWidth(), panel.getHeight());

        for (int i = 0; i < panel.getWidth(); i++) {
            for (int j = 0; j < panel.getHeight(); j++) {
                int velocidad = resultados[i][j];
                g.setColor(Color.getHSBColor((float) velocidad / MAX_ITERATIONS, 1, 1));
                g.drawRect(i, j, 1, 1);
            }
        }
    }

    static class MandelbrotWorker implements Runnable {
        private int startX, endX, startY, endY;

        public MandelbrotWorker(int startX, int startY, int endX, int endY) {
            this.startX = startX;
            this.endX = endX;
            this.startY = startY;
            this.endY = endY;
        }

        @Override
        public void run() {
            for (int i = startX; i <= endX; i++) {
                for (int j = startY; j <= endY; j++) {
                    double x = i * (x2 - x1) / panel.getWidth() + x1;
                    double y = y1 - j * (y1 - y2) / panel.getHeight();
                    int velocidad = mandelbrot(x, y);
                    resultados[i][j] = velocidad;
                }
            }
        }

        private int mandelbrot(double x, double y) {
            double zn1r = 0, zn1i = 0;
            double zn2r = 0, zn2i = 0;
            int contador = 0;

            while (contador < MAX_ITERATIONS && (zn2r * zn2r + zn2i * zn2i) < 10000) {
                zn2r = zn1r * zn1r - zn1i * zn1i + x;
                zn2i = 2 * zn1r * zn1i + y;

                zn1r = zn2r;
                zn1i = zn2i;

                contador++;
            }

            return contador;
        }
    }
}
