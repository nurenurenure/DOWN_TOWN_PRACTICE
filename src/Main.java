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
    private static final int MIN_MOLES = 10;
    private static final int MAX_MOLES = 50;
    private static final int DEFAULT_MOLES = 20;
    private static final int MIN_WATER = 5;
    private static final int MAX_WATER = 30;
    private static final int MIN_WATER_SIZE = 3;
    private static final int MAX_WATER_SIZE = 8;
    public static final int FROZEN_TOP_LAYERS = 20;

    private World world;
    private AnimationTimer timer;
    private double speedMultiplier = 1.0;
    private boolean isPaused = false;
    private Season selectedSeason;

    @Override
    public void start(Stage primaryStage) {
        showSeasonSelection(primaryStage);
    }

    private void showSeasonSelection(Stage stage) {
        Label titleLabel = new Label("Выберите время года:");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button springBtn = new Button("Весна");
        Button summerBtn = new Button("Лето");
        Button autumnBtn = new Button("Осень");
        Button winterBtn = new Button("Зима");

        // Обработчики выбора сезона
        springBtn.setOnAction(e -> {
            selectedSeason = Season.SPRING;
            showParameterSettings(stage);
        });
        summerBtn.setOnAction(e -> {
            selectedSeason = Season.SUMMER;
            showParameterSettings(stage);
        });
        autumnBtn.setOnAction(e -> {
            selectedSeason = Season.AUTUMN;
            showParameterSettings(stage);
        });
        winterBtn.setOnAction(e -> {
            selectedSeason = Season.WINTER;
            showParameterSettings(stage);
        });

        VBox seasonBox = new VBox(10, springBtn, summerBtn, autumnBtn, winterBtn);
        seasonBox.setStyle("-fx-alignment: center;");

        VBox root = new VBox(20, titleLabel, seasonBox);
        root.setStyle("-fx-padding: 30; -fx-alignment: center; -fx-background-color: #f5f5f5;");

        Scene scene = new Scene(root, 300, 300);
        stage.setScene(scene);
        stage.setTitle("Выбор времени года");
        stage.show();
    }

    private void showParameterSettings(Stage stage) {
        Label titleLabel = new Label("Настройте параметры симуляции");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Слайдер для кротов
        Label molesLabel = new Label("Количество кротов: " + DEFAULT_MOLES);
        Slider molesSlider = new Slider(MIN_MOLES, MAX_MOLES, DEFAULT_MOLES);
        molesSlider.setBlockIncrement(1);
        molesSlider.setMajorTickUnit(10);
        molesSlider.setMinorTickCount(1);
        molesSlider.setShowTickMarks(true);
        molesSlider.setSnapToTicks(true);
        molesSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            molesLabel.setText("Количество кротов: " + newVal.intValue());
        });

        // Слайдер для водоемов
        int defaultWater = selectedSeason == Season.WINTER ? 10 :
                selectedSeason == Season.SPRING ? 20 : 15;

        Label waterLabel = new Label("Количество водоемов: " + defaultWater);
        Slider waterSlider = new Slider(MIN_WATER, MAX_WATER, defaultWater);
        waterSlider.setBlockIncrement(1);
        waterSlider.setMajorTickUnit(5);
        waterSlider.setMinorTickCount(1);
        waterSlider.setShowTickMarks(true);
        waterSlider.setSnapToTicks(true);
        waterSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            waterLabel.setText("Количество водоемов: " + newVal.intValue());
        });

        // Кнопка старта
        Button startButton = new Button("Начать симуляцию");
        startButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 16;");
        startButton.setOnAction(e -> startSimulation(
                stage,
                selectedSeason,
                (int)molesSlider.getValue(),
                (int)waterSlider.getValue()
        ));

        VBox paramsBox = new VBox(10,
                molesLabel, molesSlider,
                waterLabel, waterSlider,
                startButton
        );
        paramsBox.setStyle("-fx-padding: 20; -fx-spacing: 15;");

        VBox root = new VBox(20, titleLabel, paramsBox);
        root.setStyle("-fx-alignment: center; -fx-background-color: #f5f5f5;");

        Scene scene = new Scene(root, 350, 300);
        stage.setScene(scene);
        stage.setTitle("Настройка параметров - " + selectedSeason);
    }

    private void startSimulation(Stage stage, Season season, int moleCount, int waterCount) {
        world = new World(WIDTH, HEIGHT, moleCount,
                waterCount, MIN_WATER_SIZE, MAX_WATER_SIZE, season);

        BorderPane root = new BorderPane();
        root.setCenter(world);

        // Панель управления
        Label speedLabel = new Label("Скорость: 1.0x");
        Slider speedSlider = new Slider(0.1, 3.0, 1.0);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);

        Button pauseButton = new Button("Пауза");
        pauseButton.setOnAction(e -> {
            isPaused = !isPaused;
            pauseButton.setText(isPaused ? "Продолжить" : "Пауза");
        });

        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            speedMultiplier = newVal.doubleValue();
            speedLabel.setText(String.format("Скорость: %.1fx", speedMultiplier));
        });

        HBox controls = new HBox(15, speedLabel, speedSlider, pauseButton);
        controls.setStyle("-fx-padding: 10; -fx-alignment: center;");
        root.setBottom(controls);

        Scene scene = new Scene(root, WIDTH * World.CELL_SIZE,
                HEIGHT * World.CELL_SIZE + 60);
        stage.setScene(scene);
        stage.setTitle("Подземная жизнь: " + season + " | Кротов: " + moleCount + " | Водоемов: " + waterCount);
        stage.show();

        // Запуск таймера симуляции
        timer = new AnimationTimer() {
            private long lastTime = 0;

            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                if (!isPaused) {
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