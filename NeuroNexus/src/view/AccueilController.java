package view;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class AccueilController {

    MediaPlayer logoAnimationPlayer;
    MediaPlayer logoAnimationReversePlayer;
    @FXML
    private Button uploadDataButton;
    @FXML
    private MediaView logoAnimation;

    @FXML
    public void initialize() {
        try {
            logoAnimationPlayer = new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource("/view/videos/logo.mp4")).toString()));
            logoAnimationReversePlayer = new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource("/view/videos/logo_reversed.mp4")).toString()));

            logoAnimation.setMediaPlayer(logoAnimationPlayer);
            logoAnimationPlayer.setAutoPlay(true);
        } catch (Exception e) {
            System.out.println("Player non initialisé : problème de dépendances.");
        }
    }

    @FXML
    void replayLogoAnimation() {
        if (logoAnimationPlayer.getCurrentTime().equals(logoAnimationPlayer.getTotalDuration())) {
            logoAnimation.setMediaPlayer(logoAnimationReversePlayer);
            logoAnimationReversePlayer.play();

            logoAnimationReversePlayer.setOnEndOfMedia(() -> {
                logoAnimationReversePlayer.stop();

                logoAnimation.setMediaPlayer(logoAnimationPlayer);
                logoAnimationPlayer.seek(Duration.ZERO);
                logoAnimationPlayer.play();
            });
        }
    }

    @FXML
    void uploadData() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
        final Stage stage = (Stage) uploadDataButton.getScene().getWindow();
        final File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                final FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/pages/classification.fxml"));
                final Parent classificationPage = loader.load();
                final ClassificationController classificationController = loader.getController();

                MainApp.plateforme.attach(classificationController);
                MainApp.plateforme.setCsvData(selectedFile);

                final Scene classificationScene = new Scene(classificationPage);
                stage.setScene(classificationScene);
                stage.show();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @FXML
    void exitApp() {
        final Stage stage = (Stage) uploadDataButton.getScene().getWindow();
        stage.close();
    }
}
