import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Worm extends Animal {
    public static final int REPRODUCTION_TIME = 200;
    public static final int MAX_AGE = 2000;
    public static final double REPRODUCTION_PROBABILITY = 0.5;
    private static final int MAX_HUNGER = 300;
    private static final int HUNGER_PER_TICK = 1;


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

        checkForFood(world); // Используем общий метод проверки пищи

        if (Math.random() < 0.01) moveRandomly(world);
        updateVisualPosition();
        // Зимой шанс размножения значительно ниже
        double reproductionProb = REPRODUCTION_PROBABILITY;
        if (world.getSeason() == Season.WINTER) {
            reproductionProb *= 0.2; // Уменьшаем шанс в 5 раз
        }

        if (reproductionCounter >= REPRODUCTION_TIME &&
                Math.random() < reproductionProb) {
            reproduce(world);
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

    public Circle getVisual() {
        return visual;
    }
}

