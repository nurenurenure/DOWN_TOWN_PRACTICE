import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;

public class Main extends Application {
    private static final int WIDTH = 80;
    private static final int HEIGHT = 60;
    private static final int MOLES_COUNT = 12;
    private static final int DUNG_COUNT = 5;

    private static final int SPRING_WATER_COUNT = 20;
    private static final int WINTER_WATER_COUNT = 10;

    private static final int MIN_WATER_SIZE = 3;
    private static final int MAX_WATER_SIZE = 8;

    public static final int FROZEN_TOP_LAYERS = 20;

    private World world;
    private AnimationTimer timer;
    private double speedMultiplier = 1.0;
    private boolean isPaused = false; // Флаг паузы

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

    private void startSimulation(Stage stage, String seasonName) {
        Season season = switch (seasonName) {
            case "spring" -> Season.SPRING;
            case "winter" -> Season.WINTER;
            case "summer" -> Season.SUMMER;
            case "autumn" -> Season.AUTUMN;
            default -> Season.SPRING;
        };

        int waterCount = switch (season) {
            case SPRING -> SPRING_WATER_COUNT;
            case WINTER -> WINTER_WATER_COUNT;
            default -> 12;
        };

        world = new World(WIDTH, HEIGHT, MOLES_COUNT,
                waterCount, MIN_WATER_SIZE, MAX_WATER_SIZE, DUNG_COUNT, season);

        BorderPane root = new BorderPane();
        root.setCenter(world);

        // Слайдер скорости
        Slider speedSlider = new Slider(0.1, 3.0, 1.0);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(0.5);
        speedSlider.setMinorTickCount(4);
        speedSlider.setBlockIncrement(0.1);

        Label speedLabel = new Label("Скорость: 1.0x");

        // Кнопка паузы
        Button pauseButton = new Button("Пауза");
        pauseButton.setOnAction(e -> {
            isPaused = !isPaused;
            pauseButton.setText(isPaused ? "Продолжить" : "Пауза");
        });

        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            speedMultiplier = newVal.doubleValue();
            speedLabel.setText(String.format("Скорость: %.1fx", speedMultiplier));
        });

        HBox controls = new HBox(10, speedLabel, speedSlider, pauseButton);
        controls.setStyle("-fx-padding: 10; -fx-alignment: center;");

        root.setBottom(controls);

        Scene scene = new Scene(root, WIDTH * World.CELL_SIZE,
                HEIGHT * World.CELL_SIZE + 50);
        stage.setTitle("Подземная жизнь: " + seasonName);
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

                if (!isPaused) { // Обновляем мир только если не на паузе
                    double deltaTime = (now - lastTime) / 1_000_000_000.0;
                    world.update(deltaTime * speedMultiplier);
                }
                lastTime = now;
            }
        };
        timer.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}