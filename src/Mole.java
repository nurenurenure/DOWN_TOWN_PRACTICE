import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Mole {
    public static final int TUNNEL_DURATION = 100;
    private static final double MOVE_DELAY = 0.05;

    private int gridX, gridY;
    private double targetX, targetY;
    private double progress = 0;
    private Rectangle visual;
    private double timeSinceLastMove = 0;
    private World world; // Ссылка на мир

    public Mole(int startX, int startY, World world) {
        this.gridX = startX;
        this.gridY = startY;
        this.targetX = startX;
        this.targetY = startY;
        this.world = world;

        this.visual = new Rectangle(0, 0, World.CELL_SIZE, World.CELL_SIZE);
        this.visual.setFill(Color.DARKGRAY);
        this.visual.setStroke(Color.BLACK);
        updateVisualPosition();

        // Помечаем начальную позицию
        world.markTunnelCell(gridX, gridY);
    }

    public void move(double deltaTime) {
        timeSinceLastMove += deltaTime;

        if (progress >= 1.0) {
            progress = 0;
            gridX = (int)targetX;
            gridY = (int)targetY;

            if (timeSinceLastMove > MOVE_DELAY && Math.random() < 0.2) {
                double dir = Math.random();
                if (dir < 0.4) {
                    targetX = Math.max(0, gridX - 1);
                    targetY = gridY;
                } else if (dir < 0.8) {
                    targetX = Math.min(world.width - 1, gridX + 1);
                    targetY = gridY;
                } else if (dir < 0.9) {
                    targetX = gridX;
                    targetY = Math.max(0, gridY - 1);
                } else {
                    targetX = gridX;
                    targetY = Math.min(world.height - 1, gridY + 1);
                }
                timeSinceLastMove = 0;
            }
        }

        if (progress < 1.0) {
            progress = Math.min(1.0, progress + deltaTime / MOVE_DELAY);

            // Проверяем, не движемся ли мы в воду
            int nextX = (int)(gridX + (targetX - gridX) * progress);
            int nextY = (int)(gridY + (targetY - gridY) * progress);

            if (world.isWater(nextX, nextY)) {
                // Избегаем воды - меняем направление
                targetX = gridX;
                targetY = gridY;
                progress = 1.0;
                timeSinceLastMove = MOVE_DELAY * 2; // Дольше думаем у воды
            } else {
                // Помечаем туннели как обычно
                int steps = 3;
                for (int i = 0; i <= steps; i++) {
                    double p = progress * i / steps;
                    int currentX = (int)(gridX + (targetX - gridX) * p);
                    int currentY = (int)(gridY + (targetY - gridY) * p);
                    world.markTunnelCell(currentX, currentY);
                }
            }

            updateVisualPosition();
        }
    }

    private void updateVisualPosition() {
        double currentX = gridX + (targetX - gridX) * progress;
        double currentY = gridY + (targetY - gridY) * progress;

        visual.setX(currentX * World.CELL_SIZE);
        visual.setY(currentY * World.CELL_SIZE);
    }

    public Rectangle getVisual() {
        return visual;
    }
}