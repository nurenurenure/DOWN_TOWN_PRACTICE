import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static final int WIDTH = 80;
    private static final int HEIGHT = 60;
    private static final int MOLES_COUNT = 12;

    private World world;

    @Override
    public void start(Stage primaryStage) {
        world = new World(WIDTH, HEIGHT, MOLES_COUNT);

        AnimationTimer timer = new AnimationTimer() {
            private long lastTime = 0;

            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                double deltaTime = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                world.update(deltaTime);
            }
        };
        timer.start();

        Scene scene = new Scene(world, WIDTH * World.CELL_SIZE,
                HEIGHT * World.CELL_SIZE);
        primaryStage.setTitle("Подземная жизнь");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}