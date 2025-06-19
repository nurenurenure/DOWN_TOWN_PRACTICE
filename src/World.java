import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class World extends Pane {
    public static final int CELL_SIZE = 10;
    public final int width;
    public final int height;

    private final Canvas backgroundCanvas;
    private final Canvas tunnelsCanvas;
    private final Canvas gridCanvas;

    private final Mole[] moles;
    private final List<Worm> worms = new ArrayList<>();

    public final int[][] tunnelMap;
    public final boolean[][] waterMap;
    public final boolean[][] emptyMap;

    private final Random random = new Random();

    public World(int width, int height, int moleCount,
                 int waterCount, int minWaterSize, int maxWaterSize) {
        this.width = width;
        this.height = height;

        this.tunnelMap = new int[width][height];
        this.waterMap = new boolean[width][height];
        this.emptyMap = new boolean[width][height];

        // Создаем слои отрисовки
        this.backgroundCanvas = new Canvas(width * CELL_SIZE, height * CELL_SIZE);
        this.tunnelsCanvas = new Canvas(width * CELL_SIZE, height * CELL_SIZE);
        this.gridCanvas = new Canvas(width * CELL_SIZE, height * CELL_SIZE);

        this.getChildren().addAll(backgroundCanvas, tunnelsCanvas, gridCanvas);

        // Создаем кротов
        this.moles = new Mole[moleCount];  // Массив изначально заполнен null
        for (int i = 0; i < moles.length; i++) {
            moles[i] = new Mole(  // Создаем новых кротов
                    random.nextInt(width),
                    random.nextInt(height),
                    this
            );
            this.getChildren().add(moles[i].getVisual());
        }

        // Генерируем воду
        generateWater(waterCount, minWaterSize, maxWaterSize);

        // Генерируем червей около воды
        generateInitialWorms(waterCount * 2);

        // Инициализируем графику
        drawBackground();
        drawGrid();
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
                if (Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2)) <= size/2.0) {
                    waterMap[x][y] = true;
                }
            }
        }
    }
    public void markTunnelCell(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            tunnelMap[x][y] = Mole.TUNNEL_DURATION;
        }
    }

    private void generateInitialWorms(int wormCount) {
        for (int i = 0; i < wormCount; i++) {
            int x, y;
            do {
                x = random.nextInt(width);
                y = random.nextInt(height);
            } while (!isNearWater(x, y) || isWater(x, y) || hasWormAt(x, y));

            Worm worm = new Worm(x, y);
            worms.add(worm);
            this.getChildren().add(worm.getVisual());
        }
    }

    public void update(double deltaTime) {
        // Обновляем кротов (учитываем живых)
        for (int i = 0; i < moles.length; i++) {
            Mole mole = moles[i];
            if (mole != null && mole.isAlive()) {  // Добавляем проверку на null
                mole.move(deltaTime);
            }
        }
        // Обновляем кротов
        for (Mole mole : moles) {
            if (mole.isAlive()) {
                mole.move(deltaTime);
            }
        }

        // Обновляем червей
        updateWorms();

        // Обновляем туннели
        updateTunnels();


        // Очищаем и перерисовываем
        GraphicsContext gc = tunnelsCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width * CELL_SIZE, height * CELL_SIZE);

        // Рисуем туннели
        drawAllTunnels();
    }

    private void updateWorms() {
        Iterator<Worm> iterator = worms.iterator();
        while (iterator.hasNext()) {
            Worm worm = iterator.next();
            worm.update(this);
            if (!worm.isAlive()) {
                getChildren().remove(worm.getVisual());
                iterator.remove();
            }
        }
    }

    private void updateTunnels() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (tunnelMap[i][j] > 0) {
                    tunnelMap[i][j]--;
                }
            }
        }
    }


    private void drawBackground() {
        GraphicsContext gc = backgroundCanvas.getGraphicsContext2D();

        // Рисуем землю
        gc.setFill(Color.rgb(60, 30, 10));
        gc.fillRect(0, 0, width * CELL_SIZE, height * CELL_SIZE);

        // Рисуем водоёмы
        gc.setFill(Color.rgb(30, 144, 255, 0.7));
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

        // Рисуем пустоты
        gc.setFill(Color.BLACK);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (emptyMap[x][y]) {
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

    private void drawAllTunnels() {
        GraphicsContext gc = tunnelsCanvas.getGraphicsContext2D();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (tunnelMap[x][y] > 0) {
                    double alpha = 0.2 + 0.8 * (tunnelMap[x][y] / (double)Mole.TUNNEL_DURATION);
                    gc.setFill(Color.rgb(139, 69, 19, alpha));
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

    // Вспомогательные методы
    public boolean isWater(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height && waterMap[x][y];
    }

    public boolean isEmpty(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height && emptyMap[x][y];
    }

    public boolean isNearWater(int x, int y) {
        for (int i = -3; i <= 3; i++) {
            for (int j = -3; j <= 3; j++) {
                if (isWater(x + i, y + j)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasWormAt(int x, int y) {
        for (Worm worm : worms) {
            if (worm.getX() == x && worm.getY() == y && worm.isAlive()) {
                return true;
            }
        }
        return false;
    }

    public Worm getWormAt(int x, int y) {
        for (Worm worm : worms) {
            if (worm.getX() == x && worm.getY() == y && worm.isAlive()) {
                return worm;
            }
        }
        return null;
    }

    public void removeWorm(Worm worm) {
        worms.remove(worm);
        getChildren().remove(worm.getVisual());
    }
    public void removeMole(Mole mole) {
        if (mole != null) {
            getChildren().remove(mole.getVisual());
            // Удаляем крота из массива, устанавливая null
            for (int i = 0; i < moles.length; i++) {
                if (moles[i] == mole) {
                    moles[i] = null;
                    break;
                }
            }
        }
    }


    public void addWormNearWater(int nearX, int nearY) {
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                int x = nearX + i;
                int y = nearY + j;

                if (isValidPosition(x, y) && !isWater(x, y) &&
                        !hasWormAt(x, y) && isNearWater(x, y)) {
                    Worm worm = new Worm(x, y);
                    worms.add(worm);
                    getChildren().add(worm.getVisual());
                    return;
                }
            }
        }
    }

    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
}