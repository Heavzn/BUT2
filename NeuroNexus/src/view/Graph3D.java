package view;

import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import model.Data;
import org.fxyz3d.scene.CubeViewer;
import utils.Observable;
import utils.Observer;

import java.util.*;
import java.util.function.Consumer;

public class Graph3D implements Observer {
    final CubeViewer cubeViewer = new CubeViewer(500, 50, false, 5, 2.5, 1);
    final Collection<Data> datas = MainApp.plateforme.getData();
    final Stage primaryStage = new Stage();
    final Stage stageDetails = new Stage();
    final Slider sliderK = new Slider(1, 20, 1);
    final ClassificationController classificationController = new ClassificationController();
    private final Rotate rotateX = new Rotate(150, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(120, Rotate.Y_AXIS);
    private final Translate translateX = new Translate();
    private final Translate translateY = new Translate();
    private final Translate translateZ = new Translate();
    private final ChoiceBox<String> choiceBoxX = new ChoiceBox<>();
    private final ChoiceBox<String> choiceBoxY = new ChoiceBox<>();
    private final ChoiceBox<String> choiceBoxZ = new ChoiceBox<>();
    private final ChoiceBox<String> classifyBy = new ChoiceBox<>();
    private final Text textSlider = new Text();
    private final ChoiceBox<String> methodChoiceBox = new ChoiceBox<>();
    Collection<Double> xOriginalData = new ArrayList<>();
    Collection<Double> yOriginalData = new ArrayList<>();
    Collection<Double> zOriginalData = new ArrayList<>();
    String axeX = "";
    String axeY = "";
    String axeZ = "";
    String classif = "";
    private double scaleFactorX;
    private double scaleFactorY;
    private double scaleFactorZ;
    private Group traitMarkers = new Group();
    private VBox legendBox;
    private Scene scene;
    private boolean isClosing = false;

    public void initialise() {
        setupChoiceBoxes();
        styleChoiceBoxes();

        calculateInitialScaleFactors();
        traitMarkers = createAxisMarkers(scaleFactorX, scaleFactorY, scaleFactorZ);

        Group axes = createAxes();
        final Pane controlPane = createControlPane();
        createDetailsStage(controlPane);

        final Group group = new Group(cubeViewer, axes, traitMarkers);
        setupTransforms(group);
        createMainStage(group);

        setupKeyboardControls();
        update(MainApp.plateforme);
    }

    private void setupChoiceBoxes() {
        final Collection<String> csvHeader = MainApp.plateforme.getCsvHeader();

        final List<String> headers = new ArrayList<>(csvHeader);

        choiceBoxX.getItems().setAll(headers);
        choiceBoxY.getItems().setAll(headers);
        choiceBoxZ.getItems().setAll(headers);


        setupChoiceBoxListener(choiceBoxX, this::updateAxeX);
        setupChoiceBoxListener(choiceBoxY, this::updateAxeY);
        setupChoiceBoxListener(choiceBoxZ, this::updateAxeZ);
        setupChoiceBoxListener(classifyBy, this::updateClassifyBy);

        sliderK.valueProperty().addListener((observable, oldValue, newValue) -> textSlider.setText(String.valueOf((int) sliderK.getValue())));

        methodChoiceBox.getItems().addAll("Euclidean", "Manhattan");
        methodChoiceBox.setValue(methodChoiceBox.getItems().get(0));

    }

    private void setupChoiceBoxListener(final ChoiceBox<String> choiceBox, final Consumer<String> updateMethod) {
        choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                updateMethod.accept(newValue);
            }
        });

    }

    private void styleChoiceBoxes() {
        final String style = "-fx-background-color: white; -fx-border-color: black; -fx-border-radius: 5;";
        choiceBoxX.setStyle(style);
        choiceBoxY.setStyle(style);
        choiceBoxZ.setStyle(style);
        classifyBy.setStyle(style);
    }

    private Group createAxes() {
        final Box boxX = createBox(500, 5, 5, 0, -250, -250, Color.RED);
        final Box boxY = createBox(5, 500, 5, -250, 0, -250, Color.GREEN);
        final Box boxZ = createBox(5, 5, 500, -250, -250, 0, Color.BLUE);
        return new Group(boxX, boxY, boxZ);
    }

    private Box createBox(final double width, final double height, final double depth, final double translateX, final double translateY, final double translateZ, final Color color) {
        final Box box = new Box(width, height, depth);
        box.setTranslateX(translateX);
        box.setTranslateY(translateY);
        box.setTranslateZ(translateZ);
        box.setMaterial(new PhongMaterial(color));
        return box;
    }

    private Pane createControlPane() {
        final VBox pane = new VBox(10);
        pane.setStyle("-fx-padding: 20px 30px");

        final HBox hboxX = createHBox("Axe X : ", choiceBoxX);
        final HBox hboxY = createHBox("Axe Y : ", choiceBoxY);
        final HBox hboxZ = createHBox("Axe Z : ", choiceBoxZ);
        final HBox hboxClassify = createHBox("Classify by : ", classifyBy);
        final HBox hboxK = createHBox("Rayon du KNN : ", sliderK);
        sliderK.setValue(1);
        hboxK.getChildren().add(textSlider);
        textSlider.setText(String.valueOf((int) sliderK.getValue()));
        final HBox hboxMethod = createHBox("Method : ", methodChoiceBox);

        legendBox = new VBox();
        legendBox.setStyle("-fx-padding: 10; -fx-border-color: black; -fx-border-width: 1; -fx-background-color: white;");

        final Button addDataButton = new Button("Ajouter des données");
        addDataButton.setOnAction(event -> addData());

        final Button startClassification = new Button("Start Classification");
        startClassification.setOnAction(event -> {
            try {
                startClassification();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        pane.getChildren().addAll(hboxX, hboxY, hboxZ, hboxClassify, hboxK, hboxMethod, legendBox, addDataButton, startClassification);
        return pane;
    }

    private void startClassification() {
        classificationController.startClassification(sliderK, methodChoiceBox, classif);
        updateScene();
    }

    private HBox createHBox(final String labelText, final Node control) {
        final HBox hbox = new HBox(10);
        hbox.getChildren().addAll(new Text(labelText), control);
        return hbox;
    }

    private void createDetailsStage(final Pane controlPane) {
        final Scene scene = new Scene(controlPane, 340, 900);
        stageDetails.setTitle("NeuroNexus — Détails des données");
        stageDetails.setScene(scene);
        stageDetails.setResizable(false);
        stageDetails.setX(70);
        stageDetails.setY(50);

        stageDetails.setOnCloseRequest(event -> {
            if (!isClosing) {
                isClosing = true;
                closeGraph3DStage();
            }
        });

        stageDetails.show();
    }

    private void setupTransforms(final Group group) {
        translateX.setX(580.0);
        translateY.setY(420.0);
        rotateX.setAngle(160.0);
        rotateY.setAngle(-45.0);

        group.getTransforms().addAll(translateX, translateY, translateZ, rotateX, rotateY);
    }

    private void createMainStage(final Group group) {
        scene = new Scene(group, 1200, 900, false, SceneAntialiasing.BALANCED);
        final Stage primaryStage = new Stage();
        primaryStage.setScene(scene);
        primaryStage.setTitle("Visualisation 3D avec CubeViewer");
        primaryStage.setResizable(false);
        primaryStage.setX(470);
        primaryStage.setY(50);

        primaryStage.setOnCloseRequest(event -> {
            if (!isClosing) {
                isClosing = true;
                closeDetailsStage();
            }
        });

        primaryStage.show();
    }


    private void closeGraph3DStage() {
        if (primaryStage.isShowing()) {
            primaryStage.close();
        }
    }

    private void closeDetailsStage() {
        if (stageDetails.isShowing()) {
            stageDetails.close();
        }
    }


    private void setupKeyboardControls() {
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case UP -> rotateX.setAngle(rotateX.getAngle() - 5);
                case DOWN -> rotateX.setAngle(rotateX.getAngle() + 5);
                case LEFT -> rotateY.setAngle(rotateY.getAngle() - 5);
                case RIGHT -> rotateY.setAngle(rotateY.getAngle() + 5);
            }
        });
    }

    private Iterable<Double> normalizeData(final Iterable<Double> data, final double scaleMax) {
        final List<Double> normalized = new ArrayList<>();
        for (double value : data) {
            double normalizedValue = (double) 0 + (value / scaleMax);
            normalized.add(normalizedValue);
        }
        return normalized;
    }

    private ArrayList<Double> scaleData(final Iterable<Double> data) {
        final ArrayList<Double> scaled = new ArrayList<>();
        for (double value : data) {
            scaled.add(value * (double) 500);
        }
        return scaled;
    }

    private Iterable<Double> centerData(final Iterable<Double> data) {
        final List<Double> centered = new ArrayList<>();
        for (double value : data) {
            centered.add(value - 0.5);
        }
        return centered;
    }

    private void updateAxisMarkers(final Group markers) {
        if (markers != null) {
            markers.getChildren().clear();
        }

        final Group newMarkers = createAxisMarkers(
                scaleFactorX, scaleFactorY, scaleFactorZ
        );

        assert markers != null;
        markers.getChildren().addAll(newMarkers.getChildren());
    }

    public void updateScene() {
        updateAxisData();
        updateSpheres();
        updateAxisMarkers(traitMarkers);
    }

    private void updateAxisData() {
        final ArrayList<Double> scaledX = scaleData(centerData(normalizeData(xOriginalData, scaleFactorX)));
        final ArrayList<Double> scaledY = scaleData(centerData(normalizeData(yOriginalData, scaleFactorY)));
        final ArrayList<Double> scaledZ = scaleData(centerData(normalizeData(zOriginalData, scaleFactorZ)));

        cubeViewer.setxAxisData(scaledX);
        cubeViewer.setyAxisData(scaledY);
        cubeViewer.setzAxisData(scaledZ);
    }

    private void updateSpheres() {
        final ObservableList<Node> listSphere = cubeViewer.scatterDataGroup.getChildren();

        final String selectedClassification = classifyBy.getValue();
        if (!validateClassification(selectedClassification)) {
            return;
        }

        createClassifications(selectedClassification);

        configureSpheres(listSphere);
    }

    private boolean validateClassification(final String selectedClassification) {
        return selectedClassification != null && !selectedClassification.isEmpty();
    }

    private Map<Integer, String> createClassifications(final String selectedClassification) {
        final Map<Integer, String> classifications = new HashMap<>();
        int index = 0;
        for (final Data d : datas) {
            classifications.put(index++, d.getTextData().get(selectedClassification));
        }
        return classifications;
    }

    private void configureSpheres(final ObservableList<Node> listSphere) {
        final ArrayList<Double> scaledX = cubeViewer.getxAxisData();
        final ArrayList<Double> scaledY = cubeViewer.getyAxisData();
        final ArrayList<Double> scaledZ = cubeViewer.getzAxisData();

        final LinkedList<Data> linkedDatas = new LinkedList<>(datas);

        for (int i = 0; i < listSphere.size(); i++) {
            Node s = listSphere.get(i);

            if (s instanceof final Sphere sphere) {
                setupSphere(sphere, i, linkedDatas.get(i), scaledX, scaledY, scaledZ);
            }
        }
    }

    private void setupSphere(final Sphere sphere, final int index, final Data data, final ArrayList<Double> scaledX, final ArrayList<Double> scaledY, final ArrayList<Double> scaledZ) {
        sphere.setRadius(3.0);
        sphere.setUserData(data);

        setSpherePosition(sphere, index, scaledX, scaledY, scaledZ);

        setupSphereMaterial(sphere, data);

        final Map<Integer, String> classifications = createClassifications(classifyBy.getValue());
        assignColorToSphere(sphere, index, classifications);

        configureSphereEvents(sphere);
    }

    private void setupSphereMaterial(final Sphere sphere, final Data data) {
        final PhongMaterial material = new PhongMaterial();
        sphere.setUserData(data);

        sphere.setMaterial(material);
        sphere.setStyle("");
    }

    private void setSpherePosition(final Sphere sphere, final int index, final ArrayList<Double> scaledX, final ArrayList<Double> scaledY, final ArrayList<Double> scaledZ) {
        sphere.setTranslateX(scaledX.get(index));
        sphere.setTranslateY(scaledY.get(index));
        sphere.setTranslateZ(scaledZ.get(index));
    }

    private void configureSphereEvents(final Sphere sphere) {
        final PhongMaterial colorBase = (PhongMaterial) sphere.getMaterial();

        sphere.setOnMouseEntered(event -> {
            final PhongMaterial material = new PhongMaterial();
            material.setDiffuseColor(Color.BLACK);
            sphere.setStyle("-fx-cursor: hand");
            sphere.setMaterial(material);
        });

        sphere.setOnMouseExited(event -> {
            sphere.setStyle("");
            sphere.setMaterial(colorBase);
        });

        sphere.setPickOnBounds(true);
        sphere.setOnMousePressed(event -> showDataDetails((Data) sphere.getUserData()));
    }

    private List<Color> generateDistinctColors(final int n) {
        final List<Color> predefinedColors = List.of(
                Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE,
                Color.DARKGOLDENROD, Color.PINK, Color.GRAY, Color.CYAN, Color.TEAL,
                Color.INDIGO, Color.BROWN, Color.BEIGE, Color.LIME, Color.GOLD,
                Color.MAGENTA, Color.LAVENDER, Color.web("#B2BBA2"), Color.DARKGRAY
        );

        final List<Color> colors = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            colors.add(predefinedColors.get(i % predefinedColors.size()));
        }

        return colors;
    }

    private void assignColorToSphere(final Sphere sphere, final int index, final Map<Integer, String> classifications) {
        final Set<String> uniqueValues = new HashSet<>(classifications.values());

        final List<Color> colors = generateDistinctColors(uniqueValues.size());

        final Map<String, Color> colorMap = new HashMap<>();
        int colorIndex = 0;
        for (final String value : uniqueValues) {
            colorMap.put(value, colors.get(colorIndex++));
        }

        final String classificationValue = classifications.get(index);

        if (classificationValue != null && colorMap.containsKey(classificationValue)) {
            final Color assignedColor = colorMap.get(classificationValue);

            final PhongMaterial material = new PhongMaterial();
            material.setDiffuseColor(assignedColor);
            sphere.setMaterial(material);

        }
        updateLegend(legendBox, colorMap);
    }

    private void showDataDetails(final Data data) {
        classificationController.showDataDetails(data);
    }

    private Group createAxisMarkers(final double scaleX, final double scaleY, final double scaleZ) {
        final Group markers = new Group();
        final int steps = 10;
        final double stepSizeX = scaleX / steps;
        final double stepSizeY = scaleY / steps;
        final double stepSizeZ = scaleZ / steps;

        for (double i = 0; i <= 10; i++) {
            final double translate = -250 + (i * (500.0 / steps));

            markers.getChildren().addAll(createAxisMarker(translate, i * stepSizeX, "X", Color.RED, new Rotate(45, Rotate.X_AXIS), new Rotate(45, Rotate.Y_AXIS)));
            markers.getChildren().addAll(createAxisMarker(translate, i * stepSizeY, "Y", Color.GREEN, new Rotate(90, Rotate.X_AXIS), new Rotate(135, Rotate.Z_AXIS)));
            markers.getChildren().addAll(createAxisMarker(translate, i * stepSizeZ, "Z", Color.BLUE, new Rotate(45, Rotate.Y_AXIS), new Rotate(90, Rotate.Z_AXIS), new Rotate(90, Rotate.X_AXIS)));
        }

        return markers;
    }

    private Group createAxisMarker(final double translate, final double labelValue, final String axis, final Color color, final Rotate... rotations) {
        final Group markerGroup = new Group();


        final Box boxTrait = new Box(5.0 - 2, 50.0 - 2, 5.0 - 2);
        switch (axis) {
            case "X":
                boxTrait.setTranslateX(translate);
                boxTrait.setTranslateY(-250 - 20);
                boxTrait.setTranslateZ(-250 - 20);
                break;
            case "Y":
                boxTrait.setTranslateX(-250 - 20);
                boxTrait.setTranslateY(translate);
                boxTrait.setTranslateZ(-250 - 20);
                break;
            case "Z":
                boxTrait.setTranslateX(-250 - 20);
                boxTrait.setTranslateY(-250 - 20);
                boxTrait.setTranslateZ(translate);
                boxTrait.getTransforms().addAll(new Rotate(0, Rotate.Y_AXIS), new Rotate(90, Rotate.Z_AXIS), new Rotate(90, Rotate.X_AXIS));
                break;
        }

        boxTrait.setMaterial(new PhongMaterial(color));
        boxTrait.getTransforms().addAll(rotations);

        markerGroup.getChildren().add(boxTrait);

        final Text label = new Text(String.format("%.1f", labelValue));
        label.setFill(Color.BLACK);
        label.setStyle("-fx-font-weight: bold;");

        switch (axis) {
            case "X":
                label.setTranslateX(translate);
                label.setTranslateY(-250 - 60);
                label.setTranslateZ(-250 - 60);
                label.getTransforms().add(new Rotate(180, Rotate.X_AXIS));
                break;
            case "Y":
                label.setTranslateX(-250 - 60);
                label.setTranslateY(translate);
                label.setTranslateZ(-250 - 60);
                label.getTransforms().addAll(new Rotate(180, Rotate.X_AXIS), new Rotate(-45, Rotate.Y_AXIS));
                break;
            case "Z":
                label.setTranslateX(-250 - 60);
                label.setTranslateY(-250 - 60);
                label.setTranslateZ(translate);
                label.getTransforms().addAll(new Rotate(180, Rotate.X_AXIS), new Rotate(-90, Rotate.Y_AXIS));
                break;
        }

        markerGroup.getChildren().add(label);

        return markerGroup;
    }


    private void calculateInitialScaleFactors() {
        scaleFactorX = xOriginalData.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        scaleFactorY = yOriginalData.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        scaleFactorZ = zOriginalData.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);

        scaleFactorX = Math.ceil(scaleFactorX) + 2;
        scaleFactorY = Math.ceil(scaleFactorY) + 2;
        scaleFactorZ = Math.ceil(scaleFactorZ) + 2;
    }

    void updateAxeX(final String newValue) {
        updateAxe("X", newValue);
    }

    void updateAxeY(final String newValue) {
        updateAxe("Y", newValue);
    }

    void updateAxeZ(final String newValue) {
        updateAxe("Z", newValue);
    }


    void updateAxe(final String axis, final String newValue) {
        final ArrayList<Double> updatedData = new ArrayList<>();

        if (!newValue.equals(getAxisValue(axis))) {
            setAxisValue(axis, newValue);
            updateChoiceBox(axis, newValue);

            for (final Data d : datas) {
                updatedData.add(d.getNumericData().get(newValue));
            }
            setAxisData(axis, updatedData);
        }

        if (!axeX.isEmpty() && !axeY.isEmpty()
                && !axeZ.isEmpty() && !classif.isEmpty()) {
            calculateInitialScaleFactors();
            updateScene();
        }
    }

    private String getAxisValue(final String axis) {
        return switch (axis) {
            case "X" -> axeX;
            case "Y" -> axeY;
            case "Z" -> axeZ;
            default -> throw new IllegalArgumentException("Invalid axis: " + axis);
        };
    }

    private void setAxisValue(final String axis, final String value) {
        switch (axis) {
            case "X":
                axeX = value;
                break;
            case "Y":
                axeY = value;
                break;
            case "Z":
                axeZ = value;
                break;
            default:
                throw new IllegalArgumentException("Invalid axis: " + axis);
        }
    }

    private void updateChoiceBox(final String axis, final String value) {
        switch (axis) {
            case "X":
                choiceBoxX.setValue(value);
                break;
            case "Y":
                choiceBoxY.setValue(value);
                break;
            case "Z":
                choiceBoxZ.setValue(value);
                break;
            default:
                throw new IllegalArgumentException("Invalid axis: " + axis);
        }
    }

    private void setAxisData(final String axis, final Collection<Double> data) {
        switch (axis) {
            case "X":
                xOriginalData = data;
                break;
            case "Y":
                yOriginalData = data;
                break;
            case "Z":
                zOriginalData = data;
                break;
            default:
                throw new IllegalArgumentException("Invalid axis: " + axis);
        }
    }


    void updateClassifyBy(final String newValue) {
        if (!newValue.equals(this.classif)) {
            classifyBy.setValue(newValue);
            this.classif = newValue;
            updateScene();
        }
    }

    @Override
    public void update(final Observable observable, Object data) {
        update(observable);
    }

    @Override
    public void update(Observable observable) {
        classificationController.setValuesClassifyBy(classifyBy, classif);

        classifyBy.setValue(classifyBy.getItems().get(0));

        updateScene();

        final String xBase = choiceBoxZ.getValue();
        final String yBase = choiceBoxY.getValue();
        final String zBase = choiceBoxX.getValue();

        updateChoiceBoxValue(choiceBoxX, xBase);
        updateChoiceBoxValue(choiceBoxY, yBase);
        updateChoiceBoxValue(choiceBoxZ, zBase);

    }

    private void updateChoiceBoxValue(final ChoiceBox<String> choiceBox, final String baseValue) {
        int baseIndex = choiceBox.getItems().indexOf(baseValue);

        if (baseIndex >= 0 && baseIndex < choiceBox.getItems().size()) {

            if (baseIndex == 0 || baseIndex < choiceBox.getItems().size() - 1) {
                baseIndex++;
            } else if (baseIndex == choiceBox.getItems().size() - 1) {
                baseIndex--;
            }

            choiceBox.setValue(choiceBox.getItems().get(baseIndex));
            choiceBox.setValue(baseValue);
        }
    }

    private void updateLegend(final VBox legendBox, final Map<String, Color> colorMap) {
        legendBox.getChildren().clear();
        for (final Map.Entry<String, Color> entry : colorMap.entrySet()) {
            final HBox legendItem = new HBox();

            final Rectangle colorBox = new Rectangle(20, 20);
            colorBox.setFill(entry.getValue());
            colorBox.setStroke(Color.BLACK);
            colorBox.setStrokeWidth(1);

            final Label label = new Label(entry.getKey());
            label.setStyle("-fx-padding: 0 0 0 10;");
            legendItem.getChildren().addAll(colorBox, label);
            legendBox.getChildren().add(legendItem);
        }
    }

    private void addData() {
        if (choiceBoxX.getValue().isEmpty() || choiceBoxY.getValue().isEmpty() || choiceBoxZ.getValue().isEmpty()) {
            MainApp.errorWindow("Erreur : Pas d'axe X, Y et Z sélectionné.", false);
            return;
        }
        classificationController.pageDataController(classif);

        updateScene();
    }
}
