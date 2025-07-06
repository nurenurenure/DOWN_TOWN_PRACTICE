import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.Random;

public class Mole extends Animal {

    private static final int REPRODUCTION_TIME = 600;
    private static final double REPRODUCTION_PROBABILITY = 0.04;
    private static final int MAX_HUNGER = 1500;
    private static final int HUNGER_PER_FRAME = 1;
    private static final int FOOD_VALUE = 100;
    public static final int TUNNEL_DURATION = 1000;
    private static final double MOVE_DELAY = 0.05;

    private final Random random = new Random();
    private double preferredDirectionX;
    private double preferredDirectionY;

    private int targetX, targetY;
    private double progress = 0;
    private Rectangle visual;
    private World world;

    public Mole(int startX, int startY, World world) {
        super(startX, startY);
        this.targetX = startX;
        this.targetY = startY;
        this.world = world;

        this.preferredDirectionX = random.nextBoolean() ? 1 : -1;
        this.preferredDirectionY = 0;

        this.visual = new Rectangle(0, 0, World.CELL_SIZE, World.CELL_SIZE);
        this.visual.setFill(Color.DARKGRAY);
        this.visual.setStroke(Color.BLACK);
        updateVisualPosition();

        world.markTunnelCell(x, y);
    }

    public void update(double deltaTime) {
        if (!alive) return;

        hunger += HUNGER_PER_FRAME;
        if (hunger >= MAX_HUNGER) {
            die();
            return;
        }

        determineDirection();
        updatePosition(deltaTime);

        if (world.isGasChamber(x, y)) {
            die();
            return;
        }

        reproductionCounter++;
        if (reproductionCounter >= REPRODUCTION_TIME && Math.random() < REPRODUCTION_PROBABILITY) {
            reproduce();
            reproductionCounter = 0;
        }

        checkForFood();
        updateVisuals();
    }

    @Override
    public void update(World world) {
        // Не используется, т.к. крот обновляется через update(deltaTime)
    }

    private void reproduce() {
        // Зимой шанс размножения значительно ниже
        if (world.getSeason() == Season.WINTER && Math.random() > 0.2) {
            return;
        }

        int[][] directions = {{0,1},{1,0},{0,-1},{-1,0}};
        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];
            if (world.isValidPosition(newX, newY)
                    && !world.isWater(newX, newY)
                    && !world.hasMoleAt(newX, newY)) {
                world.addMole(new Mole(newX, newY, world));
                break;
            }
        }
    }

    private void determineDirection() {
        if (progress >= 1.0) {
            int newTargetX = x, newTargetY = y;

            if (Math.random() < 0.7) {
                newTargetX += (int)Math.signum(preferredDirectionX);
            } else {
                newTargetY += (int)Math.signum(preferredDirectionY);
            }

            newTargetX = Math.max(0, Math.min(world.width - 1, newTargetX));
            newTargetY = Math.max(0, Math.min(world.height - 1, newTargetY));

            if (Math.random() < 0.1) {
                preferredDirectionX *= (Math.random() < 0.8) ? 1 : -1;
                preferredDirectionY = Math.max(-0.5, Math.min(0.5,
                        preferredDirectionY + (Math.random() - 0.5) * 0.3));
            }

            if (world.hasTunnelAt(newTargetX, newTargetY) && Math.random() < 0.8) {
                preferredDirectionX *= -1;
                newTargetX = x + (int)Math.signum(preferredDirectionX);
                newTargetX = Math.max(0, Math.min(world.width - 1, newTargetX));
            }

            if (world.getWaterInfluence(x, y) > 0.5 && Math.random() < 0.6) {
                adjustDirectionToWater();
                newTargetX = x + (int)Math.signum(preferredDirectionX);
                newTargetY = y + (int)Math.signum(preferredDirectionY);

                newTargetX = Math.max(0, Math.min(world.width - 1, newTargetX));
                newTargetY = Math.max(0, Math.min(world.height - 1, newTargetY));
            }

            if (!world.isWater(newTargetX, newTargetY)) {
                if (world.getSeason() == Season.WINTER && newTargetY < Main.FROZEN_TOP_LAYERS) return;
                targetX = newTargetX;
                targetY = newTargetY;
            } else {
                for (int[] dir : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                    int tx = x + dir[0];
                    int ty = y + dir[1];
                    if (world.isValidPosition(tx, ty) && !world.isWater(tx, ty)) {
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
            double newX = x + (targetX - x) * progress;
            double newY = y + (targetY - y) * progress;
            markTunnelPath(x, y, (int)newX, (int)newY);
            x = (int)newX;
            y = (int)newY;
        } else {
            progress = 0;
            x = targetX;
            y = targetY;
        }
    }

    private void markTunnelPath(int fromX, int fromY, int toX, int toY) {
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


    @Override
    protected boolean canEatWorms() {
        return true; // Кроты могут есть червей
    }

    private void checkForFood() {
        checkForFood(world); // Используем общий метод из Animal
    }

    @Override
    public void die() {
        super.die();
        visual.setFill(Color.GRAY);
        world.removeMole(this);
    }

    private void updateVisuals() {
        updateVisualPosition();
    }

    private void updateVisualPosition() {
        double currentX = x + (targetX - x) * progress;
        double currentY = y + (targetY - y) * progress;
        visual.setX(currentX * World.CELL_SIZE);
        visual.setY(currentY * World.CELL_SIZE);
    }

    public Rectangle getVisual() {
        return visual;
    }

    private void adjustDirectionToWater() {
        int bestDx = 0, bestDy = 0;
        double minDist = Double.MAX_VALUE;

        for (int dx = -10; dx <= 10; dx++) {
            for (int dy = -10; dy <= 10; dy++) {
                if (world.isWater(x + dx, y + dy)) {
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist < minDist) {
                        minDist = dist;
                        bestDx = dx;
                        bestDy = dy;
                    }
                }
            }
        }

        if (minDist < Double.MAX_VALUE) {
            preferredDirectionX = Integer.signum(bestDx);
            preferredDirectionY = Integer.signum(bestDy) * 0.3;
        }
    }
}
