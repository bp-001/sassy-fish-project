package com.example.controllers;

import com.example.MainWin;
import com.example.data_access.DbAccessManager;
import com.example.usermodel.Post;
import com.example.usermodel.Tag;

import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class FeedController {

    @FXML private VBox feedContainer;

    @FXML
    private void initialize() {
        refreshFeed();
    }

    @FXML
    private void openCreatePost(ActionEvent event) {
        MainWin.showCreatePostWindow();
    }

    @FXML
    private void openProfile(ActionEvent event) {
        MainWin.showProfile();
    }

    private void refreshFeed() {
        feedContainer.getChildren().clear();

        try (DbAccessManager dbManager = new DbAccessManager()) {
            for (Post post : dbManager.findAllPosts()) {
                feedContainer.getChildren().add(buildPostCard(post));
            }
        } catch (Exception e) {
            Label error = new Label("Could not load posts from database: " + e.getMessage());
            error.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
            feedContainer.getChildren().add(error);
            return;
        }

        if (feedContainer.getChildren().isEmpty()) {
            Label empty = new Label("No posts yet. Create your first post!");
            empty.setStyle("-fx-text-fill: #475569; -fx-font-size: 16px; -fx-font-weight: bold;");
            feedContainer.getChildren().add(empty);
        }
    }

    private HBox buildPostCard(Post post) {
        HBox card = new HBox();
        card.setSpacing(12);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #f1f5f9;"
                + "-fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 1);");

        Region accent = new Region();
        accent.setPrefWidth(6);
        accent.setStyle("-fx-background-color: " + (post.getIsFavourite() ? "#ff6b9d" : "#60a5fa") + "; -fx-background-radius: 4;");

        ImageView thumbnail = new ImageView(resolvePostImage(post));
        thumbnail.setFitWidth(120);
        thumbnail.setFitHeight(90);
        thumbnail.setPreserveRatio(true);
        thumbnail.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 6;");

        VBox content = new VBox();
        content.setSpacing(6);

        String username = post.getUser() == null ? "Unknown" : post.getUser().getUsername();
        String title = safe(post.getTitle(), "Untitled");
        String description = safe(post.getDescription(), "");
        String date = post.getDate() == null ? "No date" : post.getDate().toString();
        String stars = starsFor(post.getStarRating());
        String tags = post.getTags().isEmpty()
                ? "No tags"
                : post.getTags().stream().map(Tag::name).reduce((a, b) -> a + " | " + b).orElse("No tags");

        Label titleLabel = new Label(title + "  by @" + username);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

            Label metaLabel = new Label(date + "   " + stars + "   " + (post.getIsFavourite() ? "Favourite" : ""));
        metaLabel.setStyle("-fx-text-fill: #64748b;");

        Label tagsLabel = new Label("Tags: " + tags);
        tagsLabel.setStyle("-fx-text-fill: #7c3aed; -fx-font-weight: bold;");

        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: #334155;");

        Button commentButton = new Button("Comment");
        commentButton.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-font-weight: bold;");
        commentButton.setOnAction(event -> openCommentDialog(post));

        content.getChildren().addAll(titleLabel, metaLabel, tagsLabel, descLabel, commentButton);
        card.getChildren().addAll(accent, thumbnail, content);
        return card;
    }

    private void openCommentDialog(Post post) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Comment Post");
        dialog.setHeaderText("Comment on: " + safe(post.getTitle(), "Untitled"));
        dialog.setContentText("Your comment:");

        DialogPane pane = dialog.getDialogPane();
        pane.setPrefWidth(420);

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        String comment = result.get().trim();
        if (comment.isEmpty()) {
            return;
        }

        System.out.println("[COMMENT] @" + (post.getUser() == null ? "unknown" : post.getUser().getUsername())
                + " post='" + safe(post.getTitle(), "Untitled") + "' -> " + comment);

        javafx.scene.control.Alert savedAlert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION,
                "Comment saved (simulation mode).",
                ButtonType.OK);
        savedAlert.setHeaderText("Comment added");
        savedAlert.showAndWait();
    }

    private Image resolvePostImage(Post post) {
        String imagePath = post.getImagePath();
        if (imagePath != null && !imagePath.isBlank()) {
            return new Image(imagePath, true);
        }

        var defaultResource = FeedController.class.getResource("/default pfp.jpg");
        if (defaultResource != null) {
            return new Image(defaultResource.toExternalForm(), true);
        }

        var fallbackPng = FeedController.class.getResource("/default.png");
        if (fallbackPng != null) {
            return new Image(fallbackPng.toExternalForm(), true);
        }

        return null;
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String starsFor(double rating) {
        int rounded = Math.max(0, Math.min(5, (int) Math.round(rating)));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(i < rounded ? "★" : "☆");
        }
        return sb.toString();
    }
}