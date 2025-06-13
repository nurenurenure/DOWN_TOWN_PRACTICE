import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class World extends Pane {
    public static final int CELL_SIZE = 10;
    private final int width, height;
    private final Canvas canvas;
    private final Mole[] moles;

    public World(int width, int height, int moleCount) {
        this.width = width;
        this.height = height;

        this.canvas = new Canvas(width * CELL_SIZE, height * CELL_SIZE);
        this.getChildren().add(canvas);

        this.moles = new Mole[moleCount];
        for (int i = 0; i < moles.length; i++) {
            moles[i] = new Mole(
                    (int)(Math.random() * width),  // Теперь передаем номера клеток
                    (int)(Math.random() * height),
                    width, height
            );
            this.getChildren().add(moles[i].getVisual());
        }

        drawGrid();
    }

    private void drawGrid() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.rgb(60, 30, 10));
        gc.fillRect(0, 0, width*CELL_SIZE, height*CELL_SIZE);

        gc.setStroke(Color.rgb(100, 70, 40));
        gc.setLineWidth(0.5);

        for (int x = 0; x <= width; x++) {
            gc.strokeLine(x * CELL_SIZE, 0, x * CELL_SIZE, height * CELL_SIZE);
        }

        for (int y = 0; y <= height; y++) {
            gc.strokeLine(0, y * CELL_SIZE, width * CELL_SIZE, y * CELL_SIZE);
        }
    }

    public void update(double deltaTime) { // Теперь принимает deltaTime
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Очищаем туннели
        gc.setFill(Color.rgb(60, 30, 10));
        gc.fillRect(0, 0, width*CELL_SIZE, height*CELL_SIZE);

        // Обновляем кротов
        for (Mole mole : moles) {
            mole.move(deltaTime);
            mole.updateTunnels();
        }

        // Рисуем туннели
        drawTunnels();

        // Рисуем сетку поверх всего
        drawGrid();
    }

    private void drawTunnels() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.rgb(139, 69, 19, 0.7)); // Основной цвет туннелей

        for (Mole mole : moles) {
            int[][] tunnels = mole.getTunnelMemory();
            for (int i = 0; i < tunnels.length; i++) {
                for (int j = 0; j < tunnels[i].length; j++) {
                    if (tunnels[i][j] > 0) {
                        // Более плавное исчезновение туннелей
                        double alpha = 0.2 + 0.8 * (tunnels[i][j] / (double)Mole.TUNNEL_DURATION);
                        gc.setFill(Color.rgb(139, 69, 19, alpha));
                        gc.fillRect(
                                i * CELL_SIZE + 1,
                                j * CELL_SIZE + 1,
                                CELL_SIZE - 2,
                                CELL_SIZE - 2
                        );
                    }
                }
            }
        }
    }
}