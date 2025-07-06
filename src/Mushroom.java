import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

public class Mushroom {
    private int x;
    private int y;
    private boolean alive;
    private final Circle visual;

    public Mushroom(int x, int y) {
        this.x = x;
        this.y = y;
        this.alive = true;

        this.visual = new Circle(
                x * World.CELL_SIZE + World.CELL_SIZE/2,
                y * World.CELL_SIZE + World.CELL_SIZE/2,
                World.CELL_SIZE/3,
                Color.rgb(200, 50, 50) // Красноватый цвет гриба
        );
    }

    public Circle getVisual() {
        return visual;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isAlive() { return alive; }

    public void consume() {
        this.alive = false;
        this.visual.setVisible(false);
    }

    // Шанс распространения гриба на соседние клетки
    public void trySpread(World world) {
        if (Math.random() < 0.001) { // 0.1% шанс распространения
            int dx = (int)(Math.random() * 3) - 1; // -1, 0 или 1
            int dy = (int)(Math.random() * 3) - 1;

            int newX = x + dx;
            int newY = y + dy;

            if (world.isValidPosition(newX, newY) &&
                    !world.hasMushroomAt(newX, newY) &&
                    world.isNearWater(newX, newY)) {

                world.addMushroom(new Mushroom(newX, newY));
            }
        }
    }

}