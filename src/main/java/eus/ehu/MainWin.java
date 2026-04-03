package eus.ehu;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import eus.ehu.data_access.CurrentUserContext;
import eus.ehu.data_access.DbAccessManager;
import eus.ehu.usermodel.User;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainWin extends Application {

    private record DemoProfile(String displayName, String username, String email, String bio, String location) {
        @Override
        public String toString() {
            return displayName;
        }
    }

    private static final List<DemoProfile> DEMO_PROFILES = List.of(
            new DemoProfile("Ana - Food Hunter", "demo_ana", "ana@sassy.demo", "I rate every brunch spot.", "New York"),
            new DemoProfile("Leo - Movie Nerd", "demo_leo", "leo@sassy.demo", "Cinema nights and reviews.", "Chicago"),
            new DemoProfile("Mia - Gamer Mode", "demo_mia", "mia@sassy.demo", "RPGs, coffee, and chaos.", "Los Angeles"));

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        bootstrapDemoProfiles();
        chooseDemoProfile();
        showFeed();
        primaryStage.setTitle("Sassy-me");
        primaryStage.show();
    }

    public static void showFeed() {
        setRootScene("FeedPage.fxml", 1080, 720);
    }

    public static void showProfile() {
        setRootScene("profile.fxml", 900, 620);
    }

    public static void showCreatePostWindow() {
        setRootScene("createPost.fxml", 560, 700);
    }

    private static void setRootScene(String fxml, double width, double height) {
        try {
            Parent root = loadView(fxml);
            Scene scene = new Scene(root, width, height);
            primaryStage.setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load view: " + fxml, e);
        }
    }

    private static void showModal(String fxml, String title, double width, double height) {
        try {
            Parent root = loadView(fxml);
            Stage modal = new Stage();
            modal.initOwner(primaryStage);
            modal.initModality(Modality.WINDOW_MODAL);
            modal.setTitle(title);
            modal.setScene(new Scene(root, width, height));
            modal.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException("Cannot load view: " + fxml, e);
        }
    }

    private static Parent loadView(String fxml) throws IOException {
        URL resource = MainWin.class.getResource("/eus/ehu/" + fxml);
        if (resource == null) {
            throw new IOException("FXML not found: " + fxml);
        }
        return FXMLLoader.load(resource);
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static void bootstrapDemoProfiles() {
        try (DbAccessManager dbManager = new DbAccessManager()) {
            for (DemoProfile profile : DEMO_PROFILES) {
                User existing = dbManager.findUserByUsername(profile.username());
                if (existing != null) {
                    continue;
                }

                User user = new User(profile.username(), profile.email());
                user.setBio(profile.bio());
                user.setLocation(profile.location());
                dbManager.saveUser(user);
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Database setup failed: " + e.getMessage(), ButtonType.OK);
            alert.setHeaderText("Cannot initialize demo profiles");
            alert.showAndWait();
        }
    }

    private static void chooseDemoProfile() {
        ChoiceDialog<DemoProfile> dialog = new ChoiceDialog<>(DEMO_PROFILES.get(0), DEMO_PROFILES);
        dialog.setTitle("Choose Demo Profile");
        dialog.setHeaderText("Select a profile to simulate app usage");
        dialog.setContentText("Profile:");

        Optional<DemoProfile> selected = dialog.showAndWait();
        DemoProfile activeProfile = selected.orElse(DEMO_PROFILES.get(0));
        CurrentUserContext.setUsername(activeProfile.username());
    }
}
