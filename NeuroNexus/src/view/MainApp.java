package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import model.Plateforme;

import java.util.Objects;

public class MainApp extends Application {

    public static Plateforme plateforme = new Plateforme();

    public static final double WINDOW_WIDTH = 763.0;
    public static final double WINDOW_HEIGHT = 497.0;

    public static void errorWindow(final String message, final boolean forceExit) {
        final Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("NeuroNexus — Erreur");

        final Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();

        stage.getIcons().add(new Image(Objects.requireNonNull(MainApp.class.getResource("/view/icons/logo/logo_black_64.png")).toString()));

        alert.setHeaderText("Une erreur est survenue !");
        alert.setContentText(message);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && forceExit) {
                System.exit(0);
            }
        });
    }

    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) {
        try {
            final Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/pages/accueil.fxml")));

            primaryStage.setTitle("NeuroNexus");

            primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/view/icons/logo/logo_black_64.png")).toString()));

            final Scene scene = new Scene(root);

            primaryStage.setScene(scene);

            primaryStage.show();
        } catch (Exception e) {
            errorWindow(e.getMessage(), true);
        }
    }
}
