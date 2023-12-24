package mixer;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.ParallelCamera;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import piano.view.ScrollBar;

import java.util.function.Consumer;

public class MixerController {
    public BorderPane rootBorderPane;
    public VBox toolBarRoot;


    private AnchorPane mixerSceneRoot;
    Group mixerGroup;
    SubScene mixerSubScene;


    static class MixerTrack extends AnchorPane {
        public MixerTrack(int index) {
            super();

//            this.setBackground(new Background(new BackgroundFill(Color.DARKGRAY.darker(), null, null)));

            this.setPrefSize(100, 375);

            BorderPane borderPane = new BorderPane();

            AnchorPane.setTopAnchor(borderPane, 0.0);
            AnchorPane.setBottomAnchor(borderPane, 0.0);
            AnchorPane.setLeftAnchor(borderPane, 0.0);
            AnchorPane.setRightAnchor(borderPane, 0.0);

            this.getChildren().add(borderPane);


            Label titleLabel = new Label("Track " + index);
            titleLabel.setTextFill(Color.WHITE);
            titleLabel.setPrefWidth(100);
            titleLabel.setPrefHeight(25);
            borderPane.setTop(titleLabel);

            HBox hBox = new HBox();
            hBox.setPrefHeight(25);
            hBox.setPrefWidth(100);
            hBox.setBackground(new Background(new BackgroundFill(Color.DARKGRAY.darker(), null, null)));
            hBox.setSpacing(2.5);
            hBox.setAlignment(Pos.CENTER);
            borderPane.setCenter(hBox);

            Button muteButton = new Button("M");
            muteButton.setPrefWidth(25);
            muteButton.setPrefHeight(25);
            hBox.getChildren().add(muteButton);

            Button soloButton = new Button("S");
            soloButton.setPrefWidth(25);
            soloButton.setPrefHeight(25);
            hBox.getChildren().add(soloButton);

            Rectangle sliderTrack = new Rectangle(10, 250);
            sliderTrack.setFill(Color.DARKGRAY.darker());
            borderPane.setBottom(sliderTrack);

        }
    }

    public void initialize() {


        mixerSceneRoot = new AnchorPane();
        mixerSceneRoot.setPrefSize(1000, 500);
        mixerGroup = new Group();
        mixerSubScene = new SubScene(mixerGroup, 1000, 1000);
        mixerSubScene.setCamera(new ParallelCamera());
        mixerSubScene.setManaged(false);
        mixerSubScene.widthProperty().bind(mixerSceneRoot.widthProperty());
        mixerSubScene.heightProperty().bind(mixerSceneRoot.heightProperty());
        mixerSceneRoot.getChildren().add(mixerSubScene);

        AnchorPane.setTopAnchor(mixerSubScene, 0.0);
        AnchorPane.setBottomAnchor(mixerSubScene, 0.0);
        AnchorPane.setLeftAnchor(mixerSubScene, 0.0);
        AnchorPane.setRightAnchor(mixerSubScene, 0.0);

        rootBorderPane.setCenter(mixerSceneRoot);

        ScrollBar scrollBar = new ScrollBar(Orientation.HORIZONTAL);
        rootBorderPane.setBottom(scrollBar);

        scrollBar.setOnScroll((Consumer<Double>) event -> {
            mixerGroup.setTranslateX(mixerSceneRoot.getHeight() * -event);
        });

        for (int i = 0; i < 15; i++) {
            var track = new MixerTrack(i);
            track.setTranslateX(i * 100);
            mixerGroup.getChildren().add(track);
        }
    }
}
