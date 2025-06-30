import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Worm {
    public static final int REPRODUCTION_TIME = 200;
    public static final int MAX_AGE = 500;
    public static final double REPRODUCTION_PROBABILITY = 0.3;

    private static final int MAX_HUNGER = 300;
    private static final int HUNGER_PER_TICK = 1;
    private static final int ROOT_FOOD_VALUE = 100;

    private int x, y;
    private int age = 0;
    private int reproductionCounter = 0;
    private int hunger = 0;

    private Circle visual;
    private boolean alive = true;

    public Worm(int x, int y) {
        this.x = x;
        this.y = y;
        this.visual = new Circle(World.CELL_SIZE / 3, Color.PINK);
        updateVisualPosition();
    }

    public void update(World world) {
        if (!alive) return;

        age++;
        hunger += HUNGER_PER_TICK;
        reproductionCounter++;

        // Смерть от старости или голода
        if (age >= MAX_AGE || hunger >= MAX_HUNGER) {
            alive = false;
            return;
        }

        // Поиск и потребление корней
        Root root = world.getRootAt(x, y);
        if (root != null && root.isAlive()) {
            eatRoot(root, world);
        }

        // Случайное движение
        if (Math.random() < 0.01) {
            moveRandomly(world);
        }

        // Размножение
        if (reproductionCounter >= REPRODUCTION_TIME && Math.random() < REPRODUCTION_PROBABILITY) {
            reproduce(world);
        }
    }

    private void eatRoot(Root root, World world) {
        root.consume();
        hunger = Math.max(0, hunger - ROOT_FOOD_VALUE);
        world.removeRoot(root); // Удалить из списка, если такой метод есть
    }

    private void moveRandomly(World world) {
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        int[] dir = directions[(int)(Math.random() * 4)];
        int newX = x + dir[0];
        int newY = y + dir[1];

        if (world.isValidPosition(newX, newY) && !world.isWater(newX, newY)) {
            x = newX;
            y = newY;
            updateVisualPosition();
        }
    }

    private void reproduce(World world) {
        world.addWormNearWater(x, y);
        reproductionCounter = 0;
    }

    private void updateVisualPosition() {
        visual.setCenterX(x * World.CELL_SIZE + World.CELL_SIZE / 2);
        visual.setCenterY(y * World.CELL_SIZE + World.CELL_SIZE / 2);
    }

    public boolean isAlive() { return alive; }
    public Circle getVisual() { return visual; }
    public int getX() { return x; }
    public int getY() { return y; }
    public void die() { alive = false; }
}
