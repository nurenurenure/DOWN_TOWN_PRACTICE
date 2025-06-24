import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Root {
    private final int x;
    private final int y;
    private final Rectangle visual;
    private boolean alive = true;

    public Root(int x, int y) {
        this.x = x;
        this.y = y;
        this.visual = new Rectangle(
                World.CELL_SIZE - 2, World.CELL_SIZE - 2,
                Color.rgb(34, 139, 34)
        );
        this.visual.setX(x * World.CELL_SIZE + 1);
        this.visual.setY(y * World.CELL_SIZE + 1);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public Rectangle getVisual() { return visual; }

    public boolean isAlive() { return alive; }

    public void consume() {
        this.alive = false;
        this.visual.setVisible(false);
    }
}
