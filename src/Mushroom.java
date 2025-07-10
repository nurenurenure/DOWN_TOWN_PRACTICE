import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
public class Mushroom {
    private int x;
    private int y;
    private boolean alive;
    private final ImageView visual;

    public Mushroom(int x, int y) {
        this.x = x;
        this.y = y;
        this.alive = true;
        // Загружаем изображение гриба
        Image mushroomImage = new Image(getClass().getResourceAsStream("/mushroom.png"));
        this.visual = new ImageView(mushroomImage);

        // Настраиваем размер и позицию
        this.visual.setFitWidth(World.CELL_SIZE * 0.8);
        this.visual.setFitHeight(World.CELL_SIZE * 0.8);
        this.visual.setX(x * World.CELL_SIZE + World.CELL_SIZE * 0.1);
        this.visual.setY(y * World.CELL_SIZE + World.CELL_SIZE * 0.1);
    }
    public ImageView getVisual() {
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