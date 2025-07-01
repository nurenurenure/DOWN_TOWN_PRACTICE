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

    private final List<Mole> moles = new ArrayList<>();
    private final List<Worm> worms = new ArrayList<>();

    public final int[][] tunnelMap;
    public final boolean[][] waterMap;
    public final boolean[][] emptyMap;

    private boolean[][] gasChambers;


    private double rootGrowthTimer = 0;
    private final double ROOT_GROWTH_INTERVAL = 0.5; // корни растут раз в 0.5 секунды
    private final int MAX_ROOT_DEPTH = 12;
    private final boolean[][] dungMap;




    private final Random random = new Random();

    private final List<Root> roots = new ArrayList<>();


    private Season season;

    public World(int width, int height, int moleCount,
                 int waterCount, int minWaterSize, int maxWaterSize,int dungCount, Season season) {
        this.season = season;
        this.width = width;
        this.height = height;

        this.tunnelMap = new int[width][height];
        this.waterMap = new boolean[width][height];
        this.emptyMap = new boolean[width][height];
        this.dungMap = new boolean[width][height]; // Инициализируем здесь


        // Создаем слои отрисовки
        this.backgroundCanvas = new Canvas(width * CELL_SIZE, height * CELL_SIZE);
        this.tunnelsCanvas = new Canvas(width * CELL_SIZE, height * CELL_SIZE);
        this.gridCanvas = new Canvas(width * CELL_SIZE, height * CELL_SIZE);

        this.getChildren().addAll(backgroundCanvas, tunnelsCanvas, gridCanvas);

        // Создаем кротов
        for (int i = 0; i < moleCount; i++) {
            int x = random.nextInt(width);
            int y;

            if (season == Season.WINTER) {
                y = random.nextInt(height - Main.FROZEN_TOP_LAYERS) + Main.FROZEN_TOP_LAYERS;
            } else {
                y = random.nextInt(height);
            }

            Mole mole = new Mole(x, y, this);
            moles.add(mole);  // теперь список, не массив
            getChildren().add(mole.getVisual());
        }


        // Генерируем воду
        generateWater(waterCount, minWaterSize, maxWaterSize);

        // Генерируем червей около воды
        generateInitialWorms(waterCount * 2);

        generateRoots();

        gasChambers = new boolean[width][height];
        generateGasChambers();

        // Инициализируем графику
        drawBackground();
        drawGrid();
    }


    public Season getSeason() {
        return season;
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

    private void generateRoots() {
        for (int x = 0; x < width; x++) {
            if (isNearWater(x, 0) && !hasRootAt(x, 0)) {
                if (random.nextDouble() < 1) { // было 0.05 — увеличили до 40%
                    Root root = new Root(x, 0);
                    roots.add(root);
                    getChildren().add(root.getVisual());
                }
            }
        }
    }
    private void growRootsDownward() {
        List<Root> newRoots = new ArrayList<>();
        for (Root root : roots) {
            if (!root.isAlive()) continue;

            int belowY = root.getY() + 1;
            int x = root.getX();

            // Ограничим максимальную глубину
            if (belowY >= height || belowY > MAX_ROOT_DEPTH) continue;

            if (!hasRootAt(x, belowY) && !isWater(x, belowY)) {
                if (random.nextDouble() < 0.1) { // шанс роста вниз 10%
                    Root newRoot = new Root(x, belowY);
                    newRoots.add(newRoot);
                    getChildren().add(newRoot.getVisual());
                }
            }
        }
        roots.addAll(newRoots);
    }





    public boolean hasRootAt(int x, int y) {
        for (Root root : roots) {
            if (root.getX() == x && root.getY() == y && root.isAlive()) {
                return true;
            }
        }
        return false;
    }

    public Root getRootAt(int x, int y) {
        for (Root root : roots) {
            if (root.getX() == x && root.getY() == y && root.isAlive()) {
                return root;
            }
        }
        return null;
    }

    public void removeRoot(Root root) {
        root.consume();  // Делаем корень "мертвым" и невидимым
    }


    public void update(double deltaTime) {
        List<Mole> molesToUpdate = new ArrayList<>(moles);
        for (Mole mole : molesToUpdate) {
            if (mole != null && mole.isAlive()) {
                mole.update(deltaTime);
            }
        }

        // Обновляем червей
        updateWorms();

        // Обновляем туннели
        updateTunnels();

        // Таймер роста корней
        rootGrowthTimer += deltaTime;
        if (rootGrowthTimer >= ROOT_GROWTH_INTERVAL) {
            growRootsDownward();
            rootGrowthTimer = 0;
        }


        // Очищаем и перерисовываем
        GraphicsContext gc = tunnelsCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width * CELL_SIZE, height * CELL_SIZE);

        // Рисуем туннели
        drawAllTunnels();


    }

    public void updateWorms() {
        List<Worm> wormsToUpdate = new ArrayList<>(worms);

        for (Worm worm : wormsToUpdate) {
            worm.update(this);
            if (!worm.isAlive()) {
                // Удаляем из основного списка и со сцены
                worms.remove(worm);
                getChildren().remove(worm.getVisual());
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

        // Рисуем газовые камеры
        gc.setFill(Color.rgb(0, 255, 0, 0.25)); // Ядовито-зелёный полупрозрачный

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (gasChambers[x][y]) {
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

        if (season == Season.WINTER) {
            gc.setFill(Color.rgb(180, 220, 255, 0.3)); // голубоватый туман
            gc.fillRect(
                    0,
                    0,
                    width * CELL_SIZE,
                    Main.FROZEN_TOP_LAYERS * CELL_SIZE
            );
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
            moles.remove(mole);
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
    public double getWaterInfluence(int x, int y) {
        int waterCells = 0;
        int totalCells = 0;

        for (int dx = -5; dx <= 5; dx++) {
            for (int dy = -5; dy <= 5; dy++) {
                if (isValidPosition(x+dx, y+dy)) {
                    totalCells++;
                    if (isWater(x+dx, y+dy)) waterCells++;
                }
            }
        }

        return (double)waterCells / totalCells;
    }
    public boolean hasTunnelAt(int x, int y) {
        if (!isValidPosition(x, y)) return false;
        return tunnelMap[x][y] > 0;
    }

    private void generateGasChambers() {
        Random random = new Random();
        int clusterCount = (width * height) / 500; // Примерно 1 газовая камера на 500 клеток

        for (int i = 0; i < clusterCount; i++) {
            int clusterSize = 4 + random.nextInt(5); // 4–8 клеток
            int startX = random.nextInt(width);
            int startY = random.nextInt(height);

            for (int j = 0; j < clusterSize; j++) {
                int dx = startX + random.nextInt(3) - 1;
                int dy = startY + random.nextInt(3) - 1;

                if (isInBounds(dx, dy) && !isWater(dx, dy)) {
                    gasChambers[dx][dy] = true;
                }
            }
        }
    }

    private boolean isInBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    public boolean isGasChamber(int x, int y) {
        return isInBounds(x, y) && gasChambers[x][y];
    }

    public void addMole(Mole mole) {
        moles.add(mole);
        getChildren().add(mole.getVisual());
    }
    public boolean hasMoleAt(int x, int y) {
        for (Mole mole : moles) {
            if (mole.isAlive() && mole.getX() == x && mole.getY() == y) {
                return true;
            }
        }
        return false;
    }



}