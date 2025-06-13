import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import java.util.Random;

public class World extends Pane {
    public static final int CELL_SIZE = 10;
    public final int width;
    public final int height;
    private final Canvas backgroundCanvas;
    private final Canvas tunnelsCanvas;
    private final Canvas gridCanvas;
    private final Mole[] moles;
    private final int[][] tunnelMap;
    private final boolean[][] waterMap;
    private final Random random = new Random();

    // Основной конструктор с параметрами воды
    public World(int width, int height, int moleCount,
                 int waterCount, int minWaterSize, int maxWaterSize) {
        this.width = width;
        this.height = height;
        this.tunnelMap = new int[width][height];
        this.waterMap = new boolean[width][height];

        this.backgroundCanvas = new Canvas(width * CELL_SIZE, height * CELL_SIZE);
        this.tunnelsCanvas = new Canvas(width * CELL_SIZE, height * CELL_SIZE);
        this.gridCanvas = new Canvas(width * CELL_SIZE, height * CELL_SIZE);

        this.getChildren().addAll(backgroundCanvas, tunnelsCanvas, gridCanvas);

        this.moles = new Mole[moleCount];
        for (int i = 0; i < moles.length; i++) {
            moles[i] = new Mole(
                    (int)(Math.random() * width),
                    (int)(Math.random() * height),
                    this
            );
            this.getChildren().add(moles[i].getVisual());
        }

        generateWater(waterCount, minWaterSize, maxWaterSize);
        drawBackground();
        drawGrid();
    }

    // Старый конструктор для совместимости
    public World(int width, int height, int moleCount) {
        this(width, height, moleCount, 10, 3, 8); // Значения по умолчанию для воды
    }

    private void generateWater(int waterCount, int minSize, int maxSize) {
        for (int i = 0; i < waterCount; i++) {
            int centerX = random.nextInt(width);
            int centerY = random.nextInt(height);
            int size = minSize + random.nextInt(maxSize - minSize + 1);
            createWaterBlob(centerX, centerY, size);
        }
    }

    private void createWaterBlob(int centerX, int centerY, int size) {
        for (int x = Math.max(0, centerX - size); x < Math.min(width, centerX + size); x++) {
            for (int y = Math.max(0, centerY - size); y < Math.min(height, centerY + size); y++) {
                // Круглая форма водоёма
                if (Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2)) <= size/2.0) {
                    waterMap[x][y] = true;
                }
            }
        }
        drawInitialWater(); // Отрисовываем водоёмы
    }
    private void drawInitialWater() {
        GraphicsContext gc = backgroundCanvas.getGraphicsContext2D();
        gc.setFill(Color.rgb(30, 144, 255, 0.7)); // Синий цвет воды

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (waterMap[x][y]) {
                    gc.fillRect(
                            x * CELL_SIZE + 1,
                            y * CELL_SIZE + 1,
                            CELL_SIZE - 2,
                            CELL_SIZE - 2
                    );
                }
            }
        }
    }
    public boolean isWater(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height && waterMap[x][y];
    }

    private void drawBackground() {
        GraphicsContext gc = backgroundCanvas.getGraphicsContext2D();

        // Рисуем землю
        gc.setFill(Color.rgb(60, 30, 10));
        gc.fillRect(0, 0, width * CELL_SIZE, height * CELL_SIZE);

        // Рисуем водоёмы поверх земли
        drawInitialWater();
    }

    public void markTunnelCell(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            tunnelMap[x][y] = Mole.TUNNEL_DURATION;
            // Немедленно отрисовываем новый туннель
            drawTunnelCell(x, y);
        }
    }

    public void update(double deltaTime) {
        // 1. Обновляем позиции кротов
        for (Mole mole : moles) {
            mole.move(deltaTime);
        }

        // 2. Обновляем время жизни туннелей
        updateTunnels();
    }

    private void updateTunnels() {
        GraphicsContext gc = tunnelsCanvas.getGraphicsContext2D();

        // Очищаем только старые туннели
        gc.clearRect(0, 0, width * CELL_SIZE, height * CELL_SIZE);

        // Рисуем все активные туннели
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (tunnelMap[i][j] > 0) {
                    drawTunnelCell(i, j);
                }
            }
        }
    }

    private void drawTunnelCell(int x, int y) {
        GraphicsContext gc = tunnelsCanvas.getGraphicsContext2D();
        double alpha = 0.3 + 0.7 * (tunnelMap[x][y] / (double)Mole.TUNNEL_DURATION);
        gc.setFill(Color.rgb(160, 100, 60, alpha));
        gc.fillRect(
                x * CELL_SIZE + 1,
                y * CELL_SIZE + 1,
                CELL_SIZE - 2,
                CELL_SIZE - 2
        );
    }

    private void drawGrid() {
        GraphicsContext gc = gridCanvas.getGraphicsContext2D();
        gc.setStroke(Color.rgb(100, 70, 40, 0.7));
        gc.setLineWidth(0.5);

        for (int x = 0; x <= width; x++) {
            gc.strokeLine(x * CELL_SIZE, 0, x * CELL_SIZE, height * CELL_SIZE);
        }

        for (int y = 0; y <= height; y++) {
            gc.strokeLine(0, y * CELL_SIZE, width * CELL_SIZE, y * CELL_SIZE);
        }
    }
}