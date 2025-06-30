public abstract class Animal {
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

    public abstract void update(World world);
}
