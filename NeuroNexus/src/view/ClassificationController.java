package view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.Data;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Observable;
import utils.Observer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class ClassificationController implements Observer {

    final NumberAxis xAxis = new NumberAxis(0, 10, 1);
    final NumberAxis yAxis = new NumberAxis(0, 10, 1);

    final List<XYChart.Series<Number, Number>> series = new ArrayList<>();

    @FXML
    Button buttonAddData;
    @FXML
    Button buttonAddView;
    String axeX = "";
    String axeY = "";
    String classif = "";

    @FXML
    private ChoiceBox<String> choiceBoxX;
    @FXML
    private ChoiceBox<String> choiceBoxY;
    @FXML
    private ChoiceBox<String> classifyBy = new ChoiceBox<>();
    @FXML
    private Slider sliderK;
    @FXML
    private ChoiceBox<String> methodChoiceBox = new ChoiceBox<>();
    @FXML
    private Text textSlider = new Text();
    @FXML
    private ScatterChart<Number, Number> scatterChart = new ScatterChart<>(xAxis, yAxis);
    private Graph3D graph3D;

    @FXML
    public void initialize() {

        choiceBoxX.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                updateAxeX(newValue);
            }
        });

        classifyBy.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                updateClassifyBy(newValue);
            }
        });


        choiceBoxY.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                updateAxeY(newValue);
            }
        });

        sliderK.valueProperty().addListener((observable, oldValue, newValue) -> {
            textSlider.setText(String.valueOf((int) sliderK.getValue()));
        });

        methodChoiceBox.getItems().addAll("Euclidean", "Manhattan");
        methodChoiceBox.setValue(methodChoiceBox.getItems().get(0));
        update(MainApp.plateforme);
    }

    public void addSerieToScatter(final XYChart.Series<Number, Number> serie) {
        final XYChart.Data<Number, Number> dummyData = new XYChart.Data<>(0, 0);

        serie.getData().add(dummyData);
        scatterChart.getData().add(serie);
    }


    public final void addSeriesToScatter(final List<XYChart.Series<Number, Number>> series) {
        for (final XYChart.Series<Number, Number> serie : series) {
            addSerieToScatter(serie);
        }
    }

    @FXML
    void updateAxeX(final String newValue) {
        if (!newValue.equals(this.axeX)) {
            this.axeX = newValue;
            choiceBoxX.setValue(axeX);
            displayGraph();
        }
    }

    void updateClassifyBy(final String newValue) {
        if (!newValue.equals(classif)) {
            classifyBy.setValue(newValue);
            classif = newValue;
            updateSeries();

            displayGraph();
        }
    }

    @FXML
    void updateAxeY(final String newValue) {
        if (!newValue.equals(this.axeY)) {
            this.axeY = newValue;
            choiceBoxY.setValue(axeY);
            displayGraph();
        }
    }

    @FXML
    private void addData() {
        if (choiceBoxX.getValue().isEmpty() || choiceBoxY.getValue().isEmpty()) {
            MainApp.errorWindow("Erreur : Pas d'axe X et Y sélectionné.", false);
            return;
        }
        pageDataController(classif);
    }

    public void pageDataController(final String classification) {

        final AddDataController addDataController = new AddDataController();

        final Pane pane = new Pane();
        pane.setPrefSize(MainApp.WINDOW_WIDTH, MainApp.WINDOW_HEIGHT);
        pane.setStyle("-fx-background-color: white;");

        addDataController.initializeInterface(pane, classification);

        MainApp.plateforme.attach(addDataController);

        final Scene scene = new Scene(pane);
        final Stage stage = new Stage();

        final String css = Objects.requireNonNull(this.getClass().getResource("/view/styles/styles.css")).toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("NeuroNexus — Ajouter une donnée");

        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/view/icons/logo/logo_black_64.png")).toString()));

        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void update(Observable observable) {
        final Collection<String> csvHeader = MainApp.plateforme.getCsvHeader();

        if (csvHeader != null) {
            choiceBoxX.getItems().setAll(csvHeader);
            choiceBoxY.getItems().setAll(csvHeader);
            classifyBy.getItems().setAll(Data.getCanClassifyBy().keySet());

            final String axeX = this.axeX;
            final String axeY = this.axeY;

            if (axeX == null) {
                updateAxeX(csvHeader.stream().toList().get(0));
            } else {
                choiceBoxX.setValue(axeX);
            }
            if (classif.isEmpty()) {
                if (!Data.getCanClassifyBy().isEmpty()) {
                    updateClassifyBy((String) Data.getCanClassifyBy().keySet().toArray()[0]);
                }
            } else {
                classifyBy.setValue(classif);
            }
            if (axeY == null) {
                updateAxeY(csvHeader.stream().toList().get(0));
            } else {
                choiceBoxY.setValue(axeY);
            }
            setValuesClassifyBy(classifyBy, classif);
            displayGraph();
        }

    }

    protected void setValuesClassifyBy(final ChoiceBox<String> hisClassifyBy, final String hisClassif) {
        final List<String> validClassifications = new ArrayList<>();
        for (final String header : Data.getCanClassifyBy().keySet()) {
            if (hasDuplicateValues(MainApp.plateforme.getData(), header)) {
                validClassifications.add(header);
            }
        }

        if (validClassifications.isEmpty()) {
            hisClassifyBy.getItems().clear();
            hisClassifyBy.setDisable(true);
        } else {
            hisClassifyBy.getItems().setAll(validClassifications);
            hisClassifyBy.setDisable(false);
            if (hisClassif.isEmpty() || !validClassifications.contains(hisClassif)) {
                updateClassifyBy(validClassifications.get(0));
            } else {
                hisClassifyBy.setValue(hisClassif);
                if (graph3D != null) {
                    graph3D.updateClassifyBy(hisClassif);
                }
            }
        }
    }


    public void updateSeries() {
        final Set<String> possibleValues = Data.getCanClassifyBy().get(classif);

        series.clear();
        scatterChart.getData().clear();

        if (possibleValues == null || possibleValues.isEmpty()) {
            return;
        }
        possibleValues.add("Inconnu");
        possibleValues.forEach(v -> {
            final XYChart.Series<Number, Number> serie = new XYChart.Series<>();
            serie.setName(v + " ");
            series.add(serie);
        });
        addSeriesToScatter(series);
    }

    public XYChart.Data<Number, Number> getDataFromAxis(final Data d) {
        if (d.getNumericData().get(this.axeX) == null || d.getNumericData().get(this.axeY) == null) return null;

        final double x = d.getNumericData().get(this.axeX);
        final double y = d.getNumericData().get(this.axeY);


        return new XYChart.Data<>(x, y);
    }

    @Override
    public void update(final Observable observable, Object data) {
        update(observable);
    }

    public void clearSeries() {
        series.forEach(s -> {
            s.getData().clear();
        });
    }

    public void displayGraph() {
        this.clearSeries();
        if (axeX == null || axeY == null) return;
        for (final Data d : MainApp.plateforme.getData()) {
            final XYChart.Data<Number, Number> data = this.getDataFromAxis(d);

            if (data != null) {
                if (d.getTextData().get(classif) != null && d.getTextData().get(classif).equals("NULL")) {
                    for (final XYChart.Series<Number, Number> serie : series) {
                        if (serie.getName().trim().equalsIgnoreCase("Inconnu")) {
                            serie.getData().add(data);
                        }
                    }
                    data.getNode().setUserData(d);
                } else {
                    series.forEach(s -> {
                        if (s.getName().trim().equals(d.getTextData().get(classif))) {
                            s.getData().add(data);
                            data.getNode().setUserData(d);
                        }
                    });
                }
            }
        }
        this.showPointsInfo();
    }

    public void addEntryValue(final Map.Entry<String, ?> entry, final VBox vBox) {
        if (!entry.getValue().equals("")) {
            final Label title = new Label(entry.getKey());
            final Label value = new Label(String.valueOf(entry.getValue()));

            if (classif.equals(entry.getKey())) {
                title.setStyle("-fx-color: red");
            }

            title.setStyle("-fx-font-weight: bold");
            title.setStyle("-fx-font-size: 20px");
            value.setStyle("-fx-padding: 0 0 14 0");
            value.setStyle("-fx-font-size: 14px");

            vBox.getChildren().addAll(title, value);
        }
    }

    public void showPointsInfo() {
        for (final XYChart.Series<Number, Number> ser : series) {
            for (final XYChart.Data<Number, Number> data : ser.getData()) {
                data.getNode().setOnMouseClicked(e -> {
                    final Data originalData = (Data) data.getNode().getUserData();
                    showDataDetails(originalData);
                });

                final String colorBase = data.getNode().getStyle();
                data.getNode().setOnMouseEntered(e -> {
                    data.getNode().setStyle("-fx-background-color: #101010");
                });
                data.getNode().setOnMouseExited(e -> {
                    data.getNode().setStyle(colorBase);
                });
            }
        }
    }

    public void showDataDetails(final Data data) {

        final ScrollPane pane = new ScrollPane();
        pane.setStyle("-fx-hbar-policy: never");

        final VBox vBox = new VBox();
        vBox.setStyle("-fx-padding: 20px 30px");

        for (final Map.Entry<String, String> entry : data.getTextData().entrySet()) {
            addEntryValue(entry, vBox);
        }

        for (final Map.Entry<String, Double> entry : data.getNumericData().entrySet()) {
            addEntryValue(entry, vBox);
        }

        try {
             String query = constructSearchQuery(data, MainApp.plateforme.getData());
             vBox.getChildren().add(fetchFirstImage(query));
        }catch (Exception e){
            vBox.getChildren().add(new ImageView());
        }


        pane.setContent(vBox);
        final Stage stage = new Stage();
        final Scene scene = new Scene(pane);

        stage.getIcons().add(new Image(
                Objects.requireNonNull(getClass().getResource("/view/icons/logo/logo_black_64.png")).toString()));
        stage.setTitle("NeuroNexus — Détails des données");
        stage.setScene(scene);
        stage.show();
    }

    private String constructSearchQuery(final Data data, final Collection<Data> dataCollection) {
        final String csvName = MainApp.plateforme.getCsvName();

        String targetType = "";
        for (final String header : Data.getCanClassifyBy().keySet()) {
            if (!hasDuplicateValues(dataCollection, header)) {
                targetType = data.getTextData().get(header);
                return "image " + csvName + " " + targetType;
            }
        }
        return "image " + csvName + " " + targetType;
    }

    public boolean hasDuplicateValues(final Collection<Data> dataCollection, final String classification) {
        final Set<String> uniqueValues = new HashSet<>();
        for (final Data data : dataCollection) {
            final String value = data.getTextData().get(classification);
            if (value != null) {
                if (!uniqueValues.add(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    private ImageView fetchFirstImage(final String query) {
        try {
            final String API_KEY = "AIzaSyB2cTS6IpXA6yI_c5jbI4zLM2DmgsFvjrM";
            final String CX = "15ffec16728c54657";
            return researchQuery(query, API_KEY, CX);
        }catch (Exception e) {
            final String API_KEY = "AIzaSyDdI7AglFaQzdlA7KfvgqTIKVz6t0kakyM";
            final String CX = "90d6fdffe036c4afe";

            try {
                return researchQuery(query, API_KEY, CX);
            } catch (IOException ex) {
                MainApp.errorWindow("Une erreur est survenue lors de la recherche d'images.", false);
            }
        }
        return null;
    }

    private ImageView researchQuery(final String query, final String API, final String CX) throws IOException {
        try {
            final String searchUrl = "https://www.googleapis.com/customsearch/v1?q=" + query.replace(" ", "+")
                    + "&cx=" + CX
                    + "&searchType=image"
                    + "&key=" + API;

            final HttpURLConnection conn = (HttpURLConnection) new URL(searchUrl).openConnection();
            conn.setRequestMethod("GET");

            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            final StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();


            final JSONObject jsonResponse = new JSONObject(response.toString());

            final JSONArray items = jsonResponse.getJSONArray("items");

            if (items.isEmpty()) {
                System.err.println("Aucune image trouvée.");
                return null;
            }

            final JSONObject firstImageItem = items.getJSONObject(0);
            final String imageUrl = firstImageItem.getString("link");

            final ImageView imageView = new ImageView(new Image(imageUrl));
            imageView.setFitWidth(150);
            imageView.setFitHeight(150);
            return imageView;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    @FXML
    public void addView() {
        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/pages/classification.fxml"));

            final Parent classificationPage = loader.load();
            final ClassificationController classificationController = loader.getController();
            MainApp.plateforme.attach(classificationController);

            final Stage stage = new Stage();
            final Scene scene = new Scene(classificationPage);

            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/view/icons/logo/logo_black_64.png")).toString()));

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    public void startClassification() {
        startClassification(sliderK, methodChoiceBox, classif);
    }


    public void startClassification(final Slider sliderK, final ChoiceBox<String> methode, final String classif) {
        try {
            final List<Data> datas = new ArrayList<>();
            for (final Data data : MainApp.plateforme.getData()) {
                if (data.getTextData().get(classif).equals("NULL")) datas.add(data);
            }
            if (methode.getValue().equals("Euclidean")) {
                for (final Data data : datas) {
                    //MainApp.plateforme.classifDataEuclidean(data, (int) sliderK.getValue(), classif);
                    MainApp.plateforme.classifData(data, (int) sliderK.getValue(), classif, false);
                }

            } else if (methode.getValue().equals("Manhattan")) {
                for (final Data data : datas) {
                    //MainApp.plateforme.classifDataManhattan(data, (int) sliderK.getValue(), classif);
                    MainApp.plateforme.classifData(data, (int) sliderK.getValue(), classif, true);
                }
            } else {
                MainApp.errorWindow("Erreur : Pas de méthode de classification choisie. (valeur : " + methodChoiceBox.getValue() + ")", false);
            }
            displayGraph();
        }catch (Exception e){
            MainApp.errorWindow(e.getMessage(), false);
        }try {
                graph3D.updateScene();
        } catch (Exception ignored) {}
    }

    @FXML
    public void test3DPage() {
        graph3D = new Graph3D();
        graph3D.initialise();
        MainApp.plateforme.attach(graph3D);
    }

}

