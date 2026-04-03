package eus.ehu.controllers;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import eus.ehu.MainWin;
import eus.ehu.data_access.CurrentUserContext;
import eus.ehu.data_access.DbAccessManager;
import eus.ehu.usermodel.Post;
import eus.ehu.usermodel.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

public class ProfileController {
    private static final ObservableList<String> LOCATION_OPTIONS = FXCollections.observableArrayList(
            "New York", "Los Angeles", "Chicago", "Houston", "Miami");

    @FXML private TextField usernameField;
    @FXML private TextField bioField;
    @FXML private ComboBox<String> locationComboBox;
    @FXML private ListView<String> postsListView;
    @FXML private Label postsCountLabel;
    @FXML private Label errorLabel;
    @FXML private ImageView profileImageView; 
    @FXML private ImageView favourite1;
    @FXML private ImageView favourite2;
    @FXML private ImageView favourite3;

    @FXML private Button editButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private ImageView editIconView;

    //private ManageProfileUseCase manageProfileUseCase;
    private User currentUser;
    private boolean isEditing = false;

    @FXML
    public void initialize() {
        setupLocationComboBox();
        //manageProfileUseCase = new ManageProfileUseCase(); (TO SOLVE: Use case integration)
        loadUserProfile();
        setEditMode(false);
        makeCircular(profileImageView);
        setupHoverEffect();
    }

    private void setupLocationComboBox() {
        locationComboBox.setItems(LOCATION_OPTIONS);
        locationComboBox.setVisibleRowCount(5);
        locationComboBox.setOnMousePressed(event -> {
            if (!locationComboBox.isShowing()) {
                locationComboBox.show();
            }
        });
    }

    private void loadUserProfile() {
        try {
            String username = CurrentUserContext.getUsername();

            try (DbAccessManager dbManager = new DbAccessManager()) {
                currentUser = dbManager.findUserByUsername(username);
                if (currentUser == null) {
                    currentUser = new User(username, username + "@example.com");
                    currentUser.setBio("");
                    currentUser.setLocation("New York");
                    dbManager.saveUser(currentUser);
                }
            }

            usernameField.setText(currentUser.getUsername());
            bioField.setText(currentUser.getBio() == null ? "" : currentUser.getBio());
            locationComboBox.setValue(currentUser.getLocation());
            if (currentUser.getProfilePicturePath() != null) {
                profileImageView.setImage(new Image(currentUser.getProfilePicturePath()));
            }
            refreshPostsSection();
        } catch (Exception e) {
            errorLabel.setText("Error loading profile: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditProfile() {
        setEditMode(true);
    } //TO SOLVE: Put it in the EditButton action in the FXML file

    @FXML
    private void handleSaveProfile() { //Again TO SOLVE with database integration
        errorLabel.setText("");
        if (usernameField.getText().isEmpty()) {
            errorLabel.setText("Username cannot be empty.");
            return;
        }
        if (bioField.getText().length() > 150) {
            errorLabel.setText("Bio cannot exceed 150 characters.");
            return;
        }
        if (locationComboBox.getValue() == null) {
            errorLabel.setText("Please select a location.");
            return;
        }

        try {
            currentUser.setUsername(usernameField.getText());
            currentUser.setBio(bioField.getText());
            currentUser.setLocation(locationComboBox.getValue());

            try (DbAccessManager dbManager = new DbAccessManager()) {
                currentUser = dbManager.updateUser(currentUser);
            }

            CurrentUserContext.setUsername(currentUser.getUsername());
            refreshPostsSection();
            setEditMode(false);
        } catch (Exception e) {
            errorLabel.setText("Error saving profile: " + e.getMessage());
        }
    }

    @FXML
    private void setEditMode(boolean editing) {
        isEditing = editing;

        //Textos a editar
        usernameField.setEditable(editing);
        bioField.setEditable(editing);
        locationComboBox.setDisable(false);

        //Vista de botones
        editButton.setDisable(editing);
        saveButton.setDisable(!editing);
        cancelButton.setDisable(!editing);
    }

    private void makeCircular(ImageView imageView){
        double radius = Math.min(imageView.getFitWidth(), imageView.getFitHeight()) / 2;

        Circle clip = new Circle (imageView.getFitWidth() / 2, imageView.getFitHeight() / 2, radius);
        imageView.setClip(clip);
    }

    @FXML
    private void handleCancelEdit() {
        loadUserProfile();
        setEditMode(false);
    }

    @FXML
    private void handleBackToFeed() {
        MainWin.showFeed();
    }

    private void setupHoverEffect(){
        profileImageView.setOnMouseEntered(e -> {
            editIconView.setVisible(true);
        });

        profileImageView.setOnMouseExited(e -> {
            editIconView.setVisible(false);
        });
    }

    private void refreshPostsSection() {
        if (currentUser == null || currentUser.getPosts() == null) {
            postsListView.setItems(FXCollections.observableArrayList());
            postsCountLabel.setText("Posts: 0");
            clearFavouritePreview();
            return;
        }

        List<Post> sortedPosts = currentUser.getPosts().stream()
                .sorted(Comparator.comparing(Post::getDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        List<String> postLines = sortedPosts.stream()
                .map(post -> {
                    String title = post.getTitle() == null || post.getTitle().isBlank() ? "Untitled" : post.getTitle();
                    String date = post.getDate() == null ? "no-date" : post.getDate().toString();
                    return date + " - " + title;
                })
                .collect(Collectors.toList());

        postsListView.setItems(FXCollections.observableArrayList(postLines));
        postsCountLabel.setText("Posts: " + sortedPosts.size());
        updateFavouritePreview(sortedPosts);
    }

    private void updateFavouritePreview(List<Post> posts) {
        clearFavouritePreview();
        List<Post> favourites = posts.stream()
                .filter(Post::getIsFavourite)
                .limit(3)
                .collect(Collectors.toList());

        ImageView[] slots = {favourite1, favourite2, favourite3};
        for (int i = 0; i < favourites.size(); i++) {
            String imagePath = favourites.get(i).getImagePath();
            if (imagePath != null && !imagePath.isBlank()) {
                slots[i].setImage(new Image(imagePath, true));
            }
        }
    }

    private void clearFavouritePreview() {
        favourite1.setImage(null);
        favourite2.setImage(null);
        favourite3.setImage(null);
    }
}

