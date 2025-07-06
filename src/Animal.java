public abstract class Animal {


    protected static final int WORM_FOOD_VALUE = 100;
    protected static final int ROOT_FOOD_VALUE = 50;
    protected static final int MUSHROOM_FOOD_VALUE = 30;

    protected int x, y;
    protected boolean alive = true;
    protected int hunger = 0;
    protected int reproductionCounter = 0;

    public Animal(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean isAlive() {
        return alive;
    }

    public void die() {
        alive = false;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    protected void moveRandomly(World world) {
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        int[] dir = directions[(int)(Math.random() * directions.length)];
        int newX = x + dir[0];
        int newY = y + dir[1];
        if (world.isValidPosition(newX, newY) && !world.isWater(newX, newY)) {
            x = newX;
            y = newY;
        }
    }

    protected void checkForFood(World world) {
        // Проверяем и едим червей (если текущее животное может их есть)
        Worm worm = world.getWormAt(x, y);
        if (worm != null && worm.isAlive() && this.canEatWorms()) {
            eat(worm, world);
        }

        // Проверяем и едим корни
        Root root = world.getRootAt(x, y);
        if (root != null && root.isAlive() && this.canEatRoots()) {
            eat(root, world);
        }

        // Проверяем и едим грибы
        Mushroom mushroom = world.getMushroomAt(x, y);
        if (mushroom != null && mushroom.isAlive() && this.canEatMushrooms()) {
            eat(mushroom, world);
        }
    }

    protected boolean canEatWorms() {
        return false;
    }

    protected boolean canEatRoots() {
        return true;
    }

    protected boolean canEatMushrooms() {
        return true;
    }

    protected void eat(Worm worm, World world) {
        worm.die();
        hunger = Math.max(0, hunger - WORM_FOOD_VALUE);
        world.removeWorm(worm);
    }

    protected void eat(Root root, World world) {
        root.consume();
        hunger = Math.max(0, hunger - ROOT_FOOD_VALUE);
        world.removeRoot(root);
    }

    protected void eat(Mushroom mushroom, World world) {
        mushroom.consume();
        hunger = Math.max(0, hunger - MUSHROOM_FOOD_VALUE);
        world.removeMushroom(mushroom);
    }


    public abstract void update(World world);
}
