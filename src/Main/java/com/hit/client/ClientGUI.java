package com.hit.client;

import com.hit.dm.RouteResponse;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;
import java.util.*;

public class ClientGUI extends Application {

    private final MyClient client = new MyClient();

    private CityCatalog catalog;

    private ComboBox<CityCatalog.CityItem> startBox;
    private ComboBox<CityCatalog.CityItem> endBox;

    private ListView<CityCatalog.CityItem> stopsList;
    private Button addStopBtn;
    private Button removeStopBtn;

    private Button findBtn;
    private Button clearBtn;
    private Button sampleBtn;

    private Label statusLabel;
    private ProgressIndicator progress;

    private TableView<RouteRow> routeTable;
    private Label totalWeightLabel;
    private Label timeLabel;

    private final Map<Integer, String> idToName = new HashMap<Integer, String>();

    private static final String CITIES_PATH = "src/Main/resources/cities.csv";
    private static final String BG_PATH = "src/Main/resources/bg.jpeg";

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("RouteMaster");

        catalog = new CityCatalog(CITIES_PATH);
        for (CityCatalog.CityItem c : catalog.getCities()) {
            idToName.put(c.getId(), c.getName());
        }

        BorderPane root = new BorderPane();
        root.setTop(buildHeader());
        root.setCenter(buildMainCard());

        applyBackgroundImage(root, BG_PATH);

        Pane overlay = new Pane();
        overlay.setMouseTransparent(true);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.35);");
        overlay.prefWidthProperty().bind(root.widthProperty());
        overlay.prefHeightProperty().bind(root.heightProperty());

        StackPane stacked = new StackPane(root, overlay);

        Scene scene = new Scene(stacked, 900, 560);
        scene.getStylesheets().add(writeCssToTempFile());

        stage.setScene(scene);
        stage.show();

        startClock();
    }

    private Pane buildHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(10, 16, 10, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("header-bar");

        Text appTitle = new Text("RouteMaster");
        appTitle.getStyleClass().add("app-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        timeLabel = new Label();
        timeLabel.getStyleClass().add("time-label");

        header.getChildren().addAll(appTitle, spacer, timeLabel);
        return header;
    }

    private Pane buildMainCard() {
        HBox main = new HBox(18);
        main.setPadding(new Insets(18));

        // ---------------- Left (Table) ----------------
        VBox left = new VBox(10);
        left.getStyleClass().add("card");
        left.setPadding(new Insets(12));
        left.setPrefWidth(520);

        HBox leftTitleBar = new HBox();
        leftTitleBar.getStyleClass().add("card-title-bar");
        leftTitleBar.setPadding(new Insets(8, 12, 8, 12));
        Label tableTitle = new Label("Route Details");
        tableTitle.getStyleClass().add("card-title");
        leftTitleBar.getChildren().add(tableTitle);

        routeTable = new TableView<RouteRow>();
        routeTable.getStyleClass().add("route-table");
        routeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<RouteRow, String> colFrom = new TableColumn<RouteRow, String>("From");
        colFrom.setCellValueFactory(new PropertyValueFactory<RouteRow, String>("from"));

        TableColumn<RouteRow, String> colTo = new TableColumn<RouteRow, String>("To");
        colTo.setCellValueFactory(new PropertyValueFactory<RouteRow, String>("to"));

        // Force text color in table cells
        colFrom.setCellFactory(tc -> new TableCell<RouteRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item);
                setStyle("-fx-text-fill: black; -fx-font-weight: 700;");
            }
        });
        colTo.setCellFactory(tc -> new TableCell<RouteRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item);
                setStyle("-fx-text-fill: black; -fx-font-weight: 700;");
            }
        });

        routeTable.getColumns().addAll(colFrom, colTo);
        routeTable.setPrefHeight(330);

        // ✅ Total weight below the table (this is what you want)
        totalWeightLabel = new Label("Total weight: -");
        totalWeightLabel.getStyleClass().add("total-weight-label");

        left.getChildren().addAll(leftTitleBar, routeTable, totalWeightLabel);

        // ---------------- Right (Controls) ----------------
        VBox right = new VBox(12);
        right.getStyleClass().add("card");
        right.setPadding(new Insets(12));
        right.setPrefWidth(330);

        HBox rightTitleBar = new HBox();
        rightTitleBar.getStyleClass().add("card-title-bar");
        rightTitleBar.setPadding(new Insets(8, 12, 8, 12));
        Label controlsTitle = new Label("Plan Your Route");
        controlsTitle.getStyleClass().add("card-title");
        rightTitleBar.getChildren().add(controlsTitle);

        startBox = new ComboBox<CityCatalog.CityItem>(catalog.getCities());
        startBox.setPromptText("Choose start city");
        startBox.getStyleClass().add("input");

        endBox = new ComboBox<CityCatalog.CityItem>(catalog.getCities());
        endBox.setPromptText("Choose destination");
        endBox.getStyleClass().add("input");

        VBox startBlock = labeled("Start city", startBox);
        VBox endBlock = labeled("Destination", endBox);

        stopsList = new ListView<CityCatalog.CityItem>();
        stopsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        stopsList.setPrefHeight(160);
        stopsList.getStyleClass().add("input");

        addStopBtn = new Button("➕ Add stop");
        addStopBtn.getStyleClass().add("soft-btn");
        addStopBtn.setOnAction(e -> onAddStop());

        removeStopBtn = new Button("🗑 Remove selected");
        removeStopBtn.getStyleClass().add("soft-btn");
        removeStopBtn.setOnAction(e -> onRemoveStops());

        HBox stopBtns = new HBox(10, addStopBtn, removeStopBtn);

        VBox stopsBlock = new VBox(6);
        Label stopsLabel = new Label("Stops (any order)");
        stopsLabel.getStyleClass().add("field-label");
        stopsBlock.getChildren().addAll(stopsLabel, stopsList, stopBtns);

        findBtn = new Button("🔎 Find Best Route");
        findBtn.getStyleClass().add("primary-btn");

        sampleBtn = new Button("✨ Fill example");
        sampleBtn.getStyleClass().add("accent-btn");

        clearBtn = new Button("🧽 Clear");
        clearBtn.getStyleClass().add("danger-btn");

        HBox actionRow = new HBox(10, findBtn, sampleBtn, clearBtn);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        progress = new ProgressIndicator();
        progress.setMaxSize(18, 18);
        progress.setVisible(false);

        statusLabel = new Label("Ready.");
        statusLabel.getStyleClass().add("status-ok");

        HBox statusRow = new HBox(8, progress, statusLabel);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        findBtn.setOnAction(e -> onFind());
        sampleBtn.setOnAction(e -> fillExample());
        clearBtn.setOnAction(e -> clearAll());

        right.getChildren().addAll(
                rightTitleBar,
                startBlock, endBlock,
                stopsBlock,
                actionRow,
                statusRow
        );

        main.getChildren().addAll(left, right);
        return main;
    }

    private VBox labeled(String label, Control control) {
        Label l = new Label(label);
        l.getStyleClass().add("field-label");
        return new VBox(6, l, control);
    }

    private void onAddStop() {
        CityCatalog.CityItem selected = chooseCityDialog("Select a stop city", catalog.getCities());
        if (selected == null) return;

        for (CityCatalog.CityItem item : stopsList.getItems()) {
            if (item.getId() == selected.getId()) {
                setStatus("Stop already exists.", true);
                return;
            }
        }
        stopsList.getItems().add(selected);
        setStatus("Stop added: " + selected.getName(), false);
    }

    private void onRemoveStops() {
        ObservableList<CityCatalog.CityItem> sel = stopsList.getSelectionModel().getSelectedItems();
        if (sel == null || sel.isEmpty()) return;

        List<CityCatalog.CityItem> toRemove = new ArrayList<CityCatalog.CityItem>(sel);
        stopsList.getItems().removeAll(toRemove);
        setStatus("Removed selected stops.", false);
    }

    private void onFind() {
        routeTable.getItems().clear();
        totalWeightLabel.setText("Total weight: -");

        CityCatalog.CityItem start = startBox.getValue();
        CityCatalog.CityItem end = endBox.getValue();

        if (start == null || end == null) {
            setStatus("Please choose start and destination.", true);
            return;
        }

        List<Integer> stops = new ArrayList<Integer>();
        for (CityCatalog.CityItem s : stopsList.getItems()) {
            stops.add(s.getId());
        }

        setLoading(true);
        setStatus("Calculating best route...", false);

        final int startId = start.getId();
        final int endId = end.getId();

        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    RouteResponse resp = client.requestRoute(startId, endId, stops);

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (resp.getRoute() == null || resp.getRoute().isEmpty()) {
                                setStatus("No route found.", true);
                                routeTable.getItems().clear();
                                totalWeightLabel.setText("Total weight: -");
                            } else {
                                renderRoute(resp.getRoute(), resp.getCost());
                                setStatus("Done.", false);
                            }
                            setLoading(false);
                        }
                    });

                } catch (Exception ex) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            setStatus("Error: " + ex.getMessage(), true);
                            setLoading(false);
                        }
                    });
                }
            }
        });
        worker.setDaemon(true);
        worker.start();
    }

    private void renderRoute(List<Integer> route, int totalCost) {
        routeTable.getItems().clear();

        for (int i = 0; i < route.size() - 1; i++) {
            int u = route.get(i);
            int v = route.get(i + 1);
            routeTable.getItems().add(new RouteRow(nameOf(u), nameOf(v)));
        }

        // ✅ show total weight from server (this is the shortest route cost)
        totalWeightLabel.setText("Total weight: " + totalCost);
    }

    private String nameOf(int id) {
        String n = idToName.get(id);
        return (n != null) ? n : String.valueOf(id);
    }

    private void fillExample() {
        startBox.setValue(findCityById(1));
        endBox.setValue(findCityById(5));
        stopsList.getItems().clear();
        stopsList.getItems().add(findCityById(2));
        stopsList.getItems().add(findCityById(3));
        setStatus("Example filled. Click Find Best Route.", false);
    }

    private CityCatalog.CityItem findCityById(int id) {
        for (CityCatalog.CityItem c : catalog.getCities()) {
            if (c.getId() == id) return c;
        }
        return null;
    }

    private void clearAll() {
        startBox.setValue(null);
        endBox.setValue(null);
        stopsList.getItems().clear();
        routeTable.getItems().clear();
        totalWeightLabel.setText("Total weight: -");
        setStatus("Ready.", false);
    }

    private void setLoading(boolean loading) {
        progress.setVisible(loading);
        findBtn.setDisable(loading);
        clearBtn.setDisable(loading);
        sampleBtn.setDisable(loading);
        startBox.setDisable(loading);
        endBox.setDisable(loading);
        stopsList.setDisable(loading);
        addStopBtn.setDisable(loading);
        removeStopBtn.setDisable(loading);
    }

    private void setStatus(String msg, boolean error) {
        statusLabel.setText(msg);
        statusLabel.getStyleClass().removeAll("status-ok", "status-err");
        statusLabel.getStyleClass().add(error ? "status-err" : "status-ok");
    }

    private void startClock() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss   EEE, dd MMM yyyy");
                while (true) {
                    final String s = fmt.format(new Date());
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            timeLabel.setText(s);
                        }
                    });
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private CityCatalog.CityItem chooseCityDialog(String title, ObservableList<CityCatalog.CityItem> options) {
        Dialog<CityCatalog.CityItem> dialog = new Dialog<CityCatalog.CityItem>();
        dialog.setTitle(title);

        ButtonType ok = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        ComboBox<CityCatalog.CityItem> box = new ComboBox<CityCatalog.CityItem>(options);
        box.setPrefWidth(260);
        box.setPromptText("Choose city");

        VBox content = new VBox(10, box);
        content.setPadding(new Insets(12));
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(bt -> bt == ok ? box.getValue() : null);

        Optional<CityCatalog.CityItem> res = dialog.showAndWait();
        return res.isPresent() ? res.get() : null;
    }

    private void applyBackgroundImage(Pane root, String filePath) {
        try {
            Image bg = new Image("file:" + filePath);
            BackgroundSize size = new BackgroundSize(100, 100, true, true, true, false);
            BackgroundImage bgi = new BackgroundImage(
                    bg,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    size
            );
            root.setBackground(new Background(bgi));
        } catch (Exception e) {
            root.setStyle("-fx-background-color: linear-gradient(to bottom, #0b5aa5, #bfe6ff);");
        }
    }

    private String writeCssToTempFile() {
        try {
            String css =
                    ".label { -fx-text-fill: white; }" +

                            ".header-bar { -fx-background-color: rgba(255,255,255,0.92); -fx-background-radius: 14; }" +
                            ".app-title { -fx-font-size: 20px; -fx-font-weight: 900; -fx-fill: #0b3f7a; }" +
                            ".time-label { -fx-font-weight: 800; -fx-text-fill: #0b3f7a; }" +

                            ".card { -fx-background-color: rgba(0,0,0,0.35); -fx-background-radius: 16; -fx-border-radius: 16;" +
                            "        -fx-border-color: rgba(255,255,255,0.18); -fx-border-width: 1;" +
                            "        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 18, 0.25, 0, 8); }" +

                            ".card-title-bar { -fx-background-color: rgba(11,63,122,0.92); -fx-background-radius: 12; }" +
                            ".card-title { -fx-font-size: 16px; -fx-font-weight: 900; -fx-text-fill: white; }" +

                            ".field-label { -fx-font-weight: 800; -fx-text-fill: white; }" +

                            ".input { -fx-background-radius: 10; -fx-border-radius: 10;" +
                            "         -fx-border-color: rgba(0,0,0,0.18); -fx-border-width: 1; }" +

                            ".primary-btn { -fx-background-color: #f4c542; -fx-font-weight: 900; -fx-background-radius: 10; }" +
                            ".accent-btn  { -fx-background-color: #ffd166; -fx-font-weight: 800; -fx-background-radius: 10; }" +
                            ".danger-btn  { -fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-weight: 900; -fx-background-radius: 10; }" +
                            ".soft-btn    { -fx-background-color: rgba(11,90,165,0.12); -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 10; }" +

                            ".status-ok  { -fx-text-fill: lime; -fx-font-weight: 900; }" +
                            ".status-err { -fx-text-fill: red;  -fx-font-weight: 900; }" +

                            ".route-table { -fx-background-color: rgba(255,255,255,0.98); }" +
                            ".route-table .column-header-background { -fx-background-color: rgba(11,63,122,0.92); }" +
                            ".route-table .column-header, .route-table .label { -fx-text-fill: white; -fx-font-weight: 900; }" +

                            ".total-weight-label { -fx-font-size: 16px; -fx-font-weight: 900; -fx-text-fill: white; }";

            java.io.File f = java.io.File.createTempFile("routemaster", ".css");
            f.deleteOnExit();

            try (java.io.FileWriter fw = new java.io.FileWriter(f)) {
                fw.write(css);
            }

            return f.toURI().toString();
        } catch (Exception e) {
            // fallback: no stylesheet
            return null;
        }
    }


    public static class RouteRow {
        private final String from;
        private final String to;

        public RouteRow(String from, String to) {
            this.from = from;
            this.to = to;
        }

        public String getFrom() { return from; }
        public String getTo() { return to; }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
