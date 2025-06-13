import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.HashSet;
import java.util.Set;

public class Mole {
    public static final int TUNNEL_DURATION = 1000;
    private static final double MOVE_DELAY = 0.7;

    private int gridX, gridY;
    private double targetX, targetY;
    private double progress = 0;
    private Rectangle visual;
    private int[][] tunnelMemory;
    private double timeSinceLastMove = 0;
    private Set<String> visitedCells = new HashSet<>(); // Для отслеживания посещенных клеток

    public Mole(int startX, int startY, int gridWidth, int gridHeight) {
        this.gridX = startX;
        this.gridY = startY;
        this.targetX = startX;
        this.targetY = startY;

        this.visual = new Rectangle(0, 0, World.CELL_SIZE, World.CELL_SIZE);
        this.visual.setFill(Color.DARKGRAY);
        this.visual.setStroke(Color.BLACK);
        updateVisualPosition();

        this.tunnelMemory = new int[gridWidth][gridHeight];
        markTunnelCell(gridX, gridY); // Помечаем начальную позицию
    }

    private void markTunnelCell(int x, int y) {
        if (x >= 0 && x < tunnelMemory.length && y >= 0 && y < tunnelMemory[0].length) {
            tunnelMemory[x][y] = TUNNEL_DURATION;
            visitedCells.add(x + "," + y); // Запоминаем посещенную клетку
        }
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
                    targetX = Math.min(tunnelMemory.length - 1, gridX + 1);
                    targetY = gridY;
                } else if (dir < 0.9) {
                    targetX = gridX;
                    targetY = Math.max(0, gridY - 1);
                } else {
                    targetX = gridX;
                    targetY = Math.min(tunnelMemory[0].length - 1, gridY + 1);
                }
                timeSinceLastMove = 0;
            }
        }

        if (progress < 1.0) {
            progress = Math.min(1.0, progress + deltaTime / MOVE_DELAY);

            // Помечаем все клетки по пути движения
            int currentCellX = (int)(gridX + (targetX - gridX) * progress);
            int currentCellY = (int)(gridY + (targetY - gridY) * progress);
            markTunnelCell(currentCellX, currentCellY);

            updateVisualPosition();
        }
    }

    private void updateVisualPosition() {
        double currentX = gridX + (targetX - gridX) * progress;
        double currentY = gridY + (targetY - gridY) * progress;

        visual.setX(currentX * World.CELL_SIZE);
        visual.setY(currentY * World.CELL_SIZE);
    }

    public void updateTunnels() {
        for (int i = 0; i < tunnelMemory.length; i++) {
            for (int j = 0; j < tunnelMemory[i].length; j++) {
                if (tunnelMemory[i][j] > 0) {
                    tunnelMemory[i][j]--;
                }
            }
        }
    }

    public Rectangle getVisual() {
        return visual;
    }

    public int[][] getTunnelMemory() {
        return tunnelMemory;
    }
}