import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Worm extends Animal {
    public static final int REPRODUCTION_TIME = 200;
    public static final int MAX_AGE = 2000;
    public static final double REPRODUCTION_PROBABILITY = 0.5;
    private static final int MAX_HUNGER = 300;
    private static final int HUNGER_PER_TICK = 1;
    private static final int ROOT_FOOD_VALUE = 100;
    private static final int MUSHROOM_FOOD_VALUE = 100;


    private int age = 0;
    private Circle visual;

    public Worm(int x, int y) {
        super(x, y);
        visual = new Circle(World.CELL_SIZE / 3, Color.PINK);
        updateVisualPosition();
    }

    @Override
    public void update(World world) {
        if (!alive) return;

        age++;
        hunger += HUNGER_PER_TICK;
        reproductionCounter++;

        if (age >= MAX_AGE || hunger >= MAX_HUNGER) {
            die();
            return;
        }
        if (world.isGasChamber(x, y)) {
            die();
            return;
        }

        Root root = world.getRootAt(x, y);
        if (root != null && root.isAlive()) {
            eatRoot(root, world);
        }

        if (Math.random() < 0.01) moveRandomly(world);
        updateVisualPosition();

        if (reproductionCounter >= REPRODUCTION_TIME &&
                Math.random() < REPRODUCTION_PROBABILITY) {
            reproduce(world);
        }
    }

    private void eatRoot(Root root, World world) {
        root.consume();
        hunger = Math.max(0, hunger - ROOT_FOOD_VALUE);
        world.removeRoot(root);
    }

    private void reproduce(World world) {
        world.addWormNearWater(x, y);
        reproductionCounter = 0;
    }

    private void updateVisualPosition() {
        visual.setCenterX(x * World.CELL_SIZE + World.CELL_SIZE / 2);
        visual.setCenterY(y * World.CELL_SIZE + World.CELL_SIZE / 2);
    }

    public Circle getVisual() {
        return visual;
    }
}

