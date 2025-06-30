import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {
    private static final int WIDTH = 80;
    private static final int HEIGHT = 60;
    private static final int MOLES_COUNT = 34;

    private static final int SPRING_WATER_COUNT = 20;
    private static final int WINTER_WATER_COUNT = 10;

    private static final int MIN_WATER_SIZE = 3;
    private static final int MAX_WATER_SIZE = 8;

    private World world;
    private AnimationTimer timer;

    @Override
    public void start(Stage primaryStage) {
        showSeasonSelection(primaryStage);
    }

    private void showSeasonSelection(Stage stage) {
        Button springBtn = new Button("Весна");
        Button winterBtn = new Button("Зима");

        springBtn.setOnAction(e -> startSimulation(stage, "spring"));
        winterBtn.setOnAction(e -> startSimulation(stage, "winter"));

        VBox root = new VBox(20, springBtn, winterBtn);
        root.setStyle("-fx-padding: 40; -fx-alignment: center; -fx-background-color: linen;");

        Scene scene = new Scene(root, 300, 200);
        stage.setTitle("Выбор времени года");
        stage.setScene(scene);
        stage.show();
    }

    private void startSimulation(Stage stage, String season) {
        int waterCount = season.equals("spring") ? SPRING_WATER_COUNT : WINTER_WATER_COUNT;

        world = new World(WIDTH, HEIGHT, MOLES_COUNT,
                waterCount, MIN_WATER_SIZE, MAX_WATER_SIZE);

        Scene scene = new Scene(world, WIDTH * World.CELL_SIZE,
                HEIGHT * World.CELL_SIZE);
        stage.setTitle("Подземная жизнь: " + (season.equals("spring") ? "Весна" : "Зима"));
        stage.setScene(scene);
        stage.show();

        timer = new AnimationTimer() {
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
    }

    public static void main(String[] args) {
        launch(args);
    }
}
