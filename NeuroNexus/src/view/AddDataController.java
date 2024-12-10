package view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import model.Data;
import utils.Observable;
import utils.Observer;

import java.util.*;

public class AddDataController implements Observer {
    private final List<Node> boxPoints = new ArrayList<>();
    int lengthHeader = MainApp.plateforme.getLengthHeader();
    boolean bug=false;
    List<String> listHeader = Data.getHeader();
    LinkedHashMap<String, String> textData = new LinkedHashMap<>();
    LinkedHashMap<String, Double> numericData = new LinkedHashMap<>();

    @FXML
    private Button buttonAdd;

    @FXML
    private Button buttonCancel;

    public void initializeInterface(final Pane pane, final String classif) {
        final ScrollPane scrollPane = createScrollPane();
        final VBox vbox = createVBox(classif);
        scrollPane.setContent(vbox);
        scrollPane.setPrefSize(MainApp.WINDOW_WIDTH - 20, MainApp.WINDOW_HEIGHT - 100);

        final VBox mainContainer = createMainContainer(scrollPane);
        pane.getChildren().add(mainContainer);
    }

    private ScrollPane createScrollPane() {
        final ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        return scrollPane;
    }

    private VBox createVBox(final String classif) {
        final VBox vbox = new VBox(10);
        vbox.setId("vboxContainer");
        vbox.setPadding(new Insets(10));

        for (int i = 0; i < lengthHeader; i++) {
            final String header = listHeader.get(i);
            final HBox fieldContainer = createFieldContainer(header, classif);
            vbox.getChildren().add(fieldContainer);
        }

        vbox.getChildren().addAll(boxPoints);
        return vbox;
    }

    private HBox createFieldContainer(final String header, final String classif) {
            final HBox fieldContainer = new HBox(header.equalsIgnoreCase(classif) ? 0 : 10);
            fieldContainer.setAlignment(Pos.CENTER_LEFT);

            if (header.equalsIgnoreCase("name") || header.equalsIgnoreCase("Nom")) {
                fieldContainer.getChildren().add(new Text(header));
                final TextField textField = new TextField();
                fieldContainer.getChildren().add(textField);
            } else if (Data.getCanClassifyBy().containsKey(header)) {
            final ChoiceBox<String> choiceBox = createChoiceBox(header, classif);
            fieldContainer.getChildren().add(new Text(header));
            fieldContainer.getChildren().add(choiceBox);
        } else {
            fieldContainer.getChildren().add(new Text(header));
            final Spinner<Double> spinner = createSpinner();
            fieldContainer.getChildren().add(spinner);
        }

        return fieldContainer;
    }

    private ChoiceBox<String> createChoiceBox(final String header, final String classif) {
                final ChoiceBox<String> choiceBox = new ChoiceBox<>();
                if (!classif.trim().equalsIgnoreCase(header.trim())) {
                    final Set<String> classificationOptions = new HashSet<>(Data.getCanClassifyBy().get(header));
                    classificationOptions.remove("Inconnu");
                    choiceBox.getItems().addAll(classificationOptions);
                    choiceBox.setValue(classificationOptions.isEmpty() ? null : classificationOptions.iterator().next());
                } else {
                    choiceBox.getItems().addAll(Data.getCanClassifyBy().get(header));
                    choiceBox.setValue("NULL");
                    choiceBox.setVisible(false);
                }
        return choiceBox;
    }

    private Spinner<Double> createSpinner() {
                final SpinnerValueFactory<Double> valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0, 0.1);
                valueFactory.setConverter(new DoubleStringConverter());
                final Spinner<Double> spinner = new Spinner<>(valueFactory);
                spinner.setEditable(true);
        return spinner;
        }

    private VBox createMainContainer(final ScrollPane scrollPane) {
        final VBox mainContainer = new VBox(10);
        mainContainer.setPadding(new Insets(10));
        mainContainer.setAlignment(Pos.CENTER);

        final HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(10);

        buttonAdd = createButton("Ajouter", 122, 42, "-fx-background-color: black; -fx-background-radius: 8;", javafx.scene.paint.Color.WHITE, e -> add());
        buttonCancel = createButton("Annuler", 122, 45, "-fx-background-color: white; -fx-border-color: black; -fx-border-radius: 8;", javafx.scene.paint.Color.BLACK, e -> cancel());

        hBox.getChildren().addAll(buttonAdd, buttonCancel);
        mainContainer.getChildren().addAll(scrollPane, hBox);
        return mainContainer;
    }

    private Button createButton(String text, double width, double height, String style, javafx.scene.paint.Color textColor, EventHandler<ActionEvent> eventHandler) {
        final Button button = new Button(text);
        button.setPrefSize(width, height);
        button.setStyle(style);
        button.setTextFill(textColor);
        button.setOnAction(eventHandler);
        return button;
    }


    @FXML
    private void add() {
        bug=false;
        textData.clear();
        numericData.clear();

        final VBox vbox = getVBoxContainer();
        if (vbox == null) {
            System.err.println("VBox not found! Check the ID and hierarchy.");
            return;
        }

        extractDataFromVBox(vbox);
        if(!bug){
            final Data d = new Data(numericData, textData);
            MainApp.plateforme.add(d);
        }


        closeAddData();
    }

    private VBox getVBoxContainer() {
        return (VBox) buttonAdd.getParent().getParent().getParent().lookup("#vboxContainer");
    }

    private void extractDataFromVBox(final VBox vbox) {
        int index = 0;
        for (final Node node : vbox.getChildren()) {
            if (node instanceof HBox fieldContainer) {
                extractDataFromFieldContainer(fieldContainer, index);
                index++;
            }
        }
    }

    private void extractDataFromFieldContainer(final HBox fieldContainer, int index) {
                for (final Node fieldNode : fieldContainer.getChildren()) {
                    if (fieldNode instanceof TextField textField) {
                        if(!textField.getText().isEmpty()){
                            for (Data d:MainApp.plateforme.getData()){
                                try{
                                    if(!d.getTextData().get("name").equals(textField.getText())){
                                        textData.put(listHeader.get(index), textField.getText());
                                    }else {
                                        MainApp.errorWindow("Ce nom éxiste déja",false);
                                        bug=true;
                                    }
                                }catch (NullPointerException e){
                                    if(!d.getTextData().get("Nom").equals(textField.getText()) ){
                                        textData.put(listHeader.get(index), textField.getText());
                                    }else {
                                        MainApp.errorWindow("Ce nom éxiste déja",false);
                                        bug=true;
                                    }
                                }

                            }
                        }else {
                            MainApp.errorWindow("Vous ne pouvez pas créer un pokemon sans nom !",false);
                            bug=true;
                        }


                    } else if (fieldNode instanceof ChoiceBox<?> choiceBox) {
                        final Object value = choiceBox.getValue();
                        if (value != null) {
                            textData.put(listHeader.get(index), value.toString());
                        }
                    } else if (fieldNode instanceof Spinner) {
                        @SuppressWarnings("unchecked")
                        final Spinner<Double> spinner = (Spinner<Double>) fieldNode;
                        final Double value = spinner.getValue();
                        if (value != null) {
                            numericData.put(listHeader.get(index), value);
                        }
                    }
                }
    }


    @FXML
    private void cancel() {
        closeAddData();
    }

    private void closeAddData() {
        final Stage stage = (Stage) buttonAdd.getScene().getWindow();
        stage.close();
    }

    @Override
    public void update(Observable observable) {
    }

    @Override
    public void update(Observable observable, Object data) {
        // Traitement des mises à jour spécifiques
    }
}
