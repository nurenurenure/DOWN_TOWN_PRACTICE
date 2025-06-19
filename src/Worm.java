import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Worm {
    public static final int REPRODUCTION_TIME = 200;
    public static final int MAX_AGE = 500;
    public static final double REPRODUCTION_PROBABILITY = 0.05;

    private int x, y;
    private int age = 0;
    private int reproductionCounter = 0;
    private Circle visual;
    private boolean alive = true;

    public Worm(int x, int y) {
        this.x = x;
        this.y = y;
        this.visual = new Circle(World.CELL_SIZE/3, Color.PINK);
        updateVisualPosition();
    }

    public void update(World world) {
        if (!alive) return;

        age++;
        reproductionCounter++;

        // Смерть от старости
        if (age >= MAX_AGE) {
            alive = false;
            return;
        }

        // Случайное движение
        if (Math.random() < 0.2) {
            moveRandomly(world);
        }

        // Размножение
        if (reproductionCounter >= REPRODUCTION_TIME && Math.random() < REPRODUCTION_PROBABILITY) {
            reproduce(world);
        }
    }

    private void moveRandomly(World world) {
        int[][] directions = {{0,1}, {1,0}, {0,-1}, {-1,0}};
        int[] dir = directions[(int)(Math.random()*4)];
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
        visual.setCenterX(x * World.CELL_SIZE + World.CELL_SIZE/2);
        visual.setCenterY(y * World.CELL_SIZE + World.CELL_SIZE/2);
    }

    public boolean isAlive() { return alive; }
    public Circle getVisual() { return visual; }
    public int getX() { return x; }
    public int getY() { return y; }
    public void die() { alive = false; }
}