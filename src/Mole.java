import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.Random;

public class Mole {
    private boolean alive = true;
    public static final int TUNNEL_DURATION = 100;
    private static final double MOVE_DELAY = 0.05;

    private final Random random = new Random();
    private double preferredDirectionX; // Предпочитаемое горизонтальное направление (-1 или 1)
    private double preferredDirectionY; // Небольшие вертикальные отклонения

    private int hunger = 0;
    private static final int MAX_HUNGER = 1500;
    private static final int HUNGER_PER_FRAME = 1;
    private static final int FOOD_VALUE = 100;
    private int gridX, gridY;
    private int targetX, targetY;
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

        this.preferredDirectionX = random.nextBoolean() ? 1 : -1;
        this.preferredDirectionY = 0;

        this.visual = new Rectangle(0, 0, World.CELL_SIZE, World.CELL_SIZE);
        this.visual.setFill(Color.DARKGRAY);
        this.visual.setStroke(Color.BLACK);
        updateVisualPosition();

        // Помечаем начальную позицию
        world.markTunnelCell(gridX, gridY);
    }



    public void move(double deltaTime) {
        if (!alive) return;

        // 1. Обновляем голод
        hunger += HUNGER_PER_FRAME;
        if (hunger >= MAX_HUNGER) {
            die();
            return;
        }

        // 2. Определяем направление с учетом всех факторов
        determineDirection();

        // 3. Обновляем позицию
        updatePosition(deltaTime);

        if (world.isGasChamber(gridX, gridY)) {
            die();
            return;
        }


        // 4. Проверяем червей в текущей клетке
        checkForFood();

        // 5. Обновляем визуализацию
        updateVisuals();
    }

    private void determineDirection() {
        if (progress >= 1.0) {
            int newTargetX, newTargetY;

            if (Math.random() < 0.7) {
                newTargetX = gridX + (int)Math.signum(preferredDirectionX);
                newTargetY = gridY;
            } else {
                newTargetX = gridX;
                newTargetY = gridY + (int)Math.signum(preferredDirectionY);
            }

            // Проверяем границы мира и корректируем, если вышли за пределы
            newTargetX = Math.max(0, Math.min(world.width - 1, newTargetX));
            newTargetY = Math.max(0, Math.min(world.height - 1, newTargetY));

            // Случайные изгибы
            if (Math.random() < 0.1) {
                preferredDirectionX *= (Math.random() < 0.8) ? 1 : -1;
                preferredDirectionY = Math.max(-0.5, Math.min(0.5,
                        preferredDirectionY + (Math.random() - 0.5) * 0.3));
            }

            // Избегание других туннелей
            if (world.hasTunnelAt(newTargetX, newTargetY) && Math.random() < 0.8) {
                preferredDirectionX *= -1;
                newTargetX = gridX + (int)Math.signum(preferredDirectionX);
                newTargetX = Math.max(0, Math.min(world.width - 1, newTargetX));
            }

            // Притяжение к воде
            if (world.getWaterInfluence(gridX, gridY) > 0.5 && Math.random() < 0.6) {
                adjustDirectionToWater(world);
                newTargetX = gridX + (int)Math.signum(preferredDirectionX);
                newTargetY = gridY + (int)Math.signum(preferredDirectionY);

                newTargetX = Math.max(0, Math.min(world.width - 1, newTargetX));
                newTargetY = Math.max(0, Math.min(world.height - 1, newTargetY));
            }

            if (!world.isWater(newTargetX, newTargetY)) {
                // ❄️ Ограничение для зимы: не копать в верхние слои
                if (world.getSeason() == Season.WINTER && newTargetY < Main.FROZEN_TOP_LAYERS) {
                    // Пропускаем эту цель — остаёмся на месте или попробуем позже
                    return;
                }

                targetX = newTargetX;
                targetY = newTargetY;
            } else {
                // Поворачиваем в случайном другом направлении
                int[][] dirs = {
                        {1, 0}, {-1, 0}, {0, 1}, {0, -1}
                };
                for (int[] dir : dirs) {
                    int tx = gridX + dir[0];
                    int ty = gridY + dir[1];
                    if (tx >= 0 && tx < world.width && ty >= 0 && ty < world.height && !world.isWater(tx, ty)) {
                        targetX = tx;
                        targetY = ty;
                        break;
                    }
                }
            }
        }
    }



    private void updatePosition(double deltaTime) {
        if (progress < 1.0) {
            progress = Math.min(1.0, progress + deltaTime / MOVE_DELAY);

            // Плавное перемещение между клетками
            double newX = gridX + (targetX - gridX) * progress;
            double newY = gridY + (targetY - gridY) * progress;

            // Помечаем все промежуточные клетки как туннели
            markTunnelPath(gridX, gridY, (int)newX, (int)newY);

            gridX = (int)newX;
            gridY = (int)newY;
        } else {
            progress = 0;
            gridX = targetX;
            gridY = targetY;
        }
    }

    private void markTunnelPath(int fromX, int fromY, int toX, int toY) {
        // Алгоритм Брезенхема для отметки всех клеток по пути
        int dx = Math.abs(toX - fromX);
        int dy = -Math.abs(toY - fromY);
        int sx = fromX < toX ? 1 : -1;
        int sy = fromY < toY ? 1 : -1;
        int err = dx + dy;

        while (true) {
            world.markTunnelCell(fromX, fromY);
            if (fromX == toX && fromY == toY) break;
            int e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                fromX += sx;
            }
            if (e2 <= dx) {
                err += dx;
                fromY += sy;
            }
        }
    }

    private void checkForFood() {
        Worm worm = world.getWormAt(gridX, gridY);
        if (worm != null && worm.isAlive()) {
            eat(worm);
        }
        // Проверка и поедание корня
        Root root = world.getRootAt(gridX, gridY);
        if (root != null) {
            eat(root);
        }
    }

    private void updateVisuals() {
        updateVisualPosition();
    }


    public boolean isAlive() {
        return alive;
    }

    public void eat(Worm worm) {
        worm.die();
        hunger = Math.max(0, hunger - FOOD_VALUE);
        world.removeWorm(worm);
    }
    public void eat(Root root) {
        hunger = Math.max(0, hunger - 50); // Корни восстанавливают меньше, чем черви
        world.removeRoot(root);
    }

    public void die() {
        alive = false;
        visual.setFill(Color.GRAY);
        world.removeMole(this);  // Уведомляем мир о смерти
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

    private void adjustDirectionToWater(World world) {
        // Простой алгоритм движения к ближайшей воде
        int closestWaterX = 0;
        int closestWaterY = 0;
        double minDistance = Double.MAX_VALUE;

        // Поиск ближайшей воды в радиусе 10 клеток
        for (int dx = -10; dx <= 10; dx++) {
            for (int dy = -10; dy <= 10; dy++) {
                if (world.isWater(gridX + dx, gridY + dy)) {
                    double dist = Math.sqrt(dx*dx + dy*dy);
                    if (dist < minDistance) {
                        minDistance = dist;
                        closestWaterX = dx;
                        closestWaterY = dy;
                    }
                }
            }
        }

        // Корректировка направления
        if (minDistance < Double.MAX_VALUE) {
            preferredDirectionX = Integer.signum(closestWaterX);
            preferredDirectionY = Integer.signum(closestWaterY) * 0.3;
        }
    }
}