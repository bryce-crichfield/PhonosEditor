import com.huskerdev.openglfx.GLCanvasAnimator;
import com.huskerdev.openglfx.OpenGLCanvas;
import com.huskerdev.openglfx.lwjgl.LWJGLExecutor;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.joml.*;
import three.*;

import java.io.File;
import java.lang.Math;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.lwjgl.opengl.GL11.*;

public class Controller {
    public AnchorPane center;
    public ToggleButton toggleNotGate;
    public ToggleButton toggleAndGate;
    public ToggleButton toggleOrGate;
    public ToggleButton toggleXorGate;
    public ToggleButton togglePencil;
    public ToggleButton toggleSelect;
    public ToggleButton toggleEraser;
    public Spinner<Integer> lightXField;
    public Spinner<Integer> lightYField;
    public Spinner<Integer> lightZField;
    public Label cameraXLabel;
    public Label cameraYLabel;
    public Label cameraZLabel;

    Camera camera = new Camera();
    Light light = new Light();
    Float lastMouseX = 0f;
    Float lastMouseY = 0f;
    Map<String, Mesh> meshes = new HashMap<>();
    public static final int BOARD_WIDTH = 1;
    public static final int BOARD_HEIGHT = 1;
    InstanceRenderer instanceRenderer;
    Model[] board = new Model[BOARD_WIDTH * BOARD_HEIGHT];

    public void initialize() {
        OpenGLCanvas canvas = OpenGLCanvas.create(LWJGLExecutor.LWJGL_MODULE);

        center.widthProperty().addListener((observable, oldValue, newValue) -> {
            canvas.setPrefWidth(newValue.doubleValue());
        });

        center.heightProperty().addListener((observable, oldValue, newValue) -> {
            canvas.setPrefHeight(newValue.doubleValue());
        });

        canvas.setAnimator(new GLCanvasAnimator(60));

        canvas.addOnInitEvent((event) -> {

            // Load meshes
            meshes = Mesh.loadMeshesFromDirectory("model");

            Mesh cube = meshes.get("Cube");
            int instanceCount = BOARD_WIDTH * BOARD_HEIGHT;
            try {
                instanceRenderer = new InstanceRenderer(cube, instanceCount);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // Create the board with cubes
            for (int x = 0; x < BOARD_WIDTH; x++) {
                for (int y = 0; y < BOARD_HEIGHT; y++) {
                    Model model = new Model(meshes.get("cube"));

                    Matrix4f transform = new Matrix4f();
                    transform.translate(x, y, 0);
                    model.setTransform(transform);

                    Material material = new Material();
                    model.setMaterial(material);

                    board[x * BOARD_WIDTH + y] = model;
                }
            }

            camera.translate(BOARD_WIDTH / 2, BOARD_WIDTH / 2, BOARD_WIDTH);
            light.setPosition(BOARD_WIDTH / 2, BOARD_WIDTH / 2, 15);

            glViewport(0, 0, (int) canvas.getWidth(), (int) canvas.getHeight());
        });

        canvas.addOnReshapeEvent((event) -> {
            // set the window size
            int width = (int) canvas.getWidth();
            int height = (int) canvas.getHeight();
            glViewport(0, 0, width, height);

            // set the camera aspect ratio
            float aspect = (float) width / (float) height;
            camera.resizeScreen(aspect);
        });

        canvas.addOnRenderEvent((event) -> {
            glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);


            try {
                instanceRenderer.load(board);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            instanceRenderer.render(camera, light);
        });

        canvas.addOnDisposeEvent((event) -> {
            instanceRenderer.dispose();
        });

        canvas.setOnScroll(event -> {
            float delta = (float) event.getDeltaY() * 0.01f;
            camera.translate(0, 0, delta);
        });

        canvas.setOnMouseDragged(event -> {
            float deltaX = (float) (event.getX() - lastMouseX);
            float deltaY = (float) (event.getY() - lastMouseY);

            if (event.isSecondaryButtonDown()) {
                // only translate by the dominant direction
                if (Math.abs(deltaX) > Math.abs(deltaY))
                    camera.translate(-deltaX / 100, 0, 0);
                else
                    camera.translate(0, deltaY / 100, 0);
            }

            lastMouseX = (float) event.getX();
            lastMouseY = (float) event.getY();
        });

        canvas.setOnMouseMoved(event -> {
            lastMouseX = (float) event.getX();
            lastMouseY = (float) event.getY();
        });

        canvas.setOnMouseClicked(event -> {
            if (!event.getButton().equals(MouseButton.PRIMARY)) {
                return;
            }
            camera.resizeScreen((float) canvas.getWidth() / (float) canvas.getHeight());

            float normMouseX = (float) (event.getX() / canvas.getWidth() * 2 - 1);
            float normMouseY = (float) (event.getY() / canvas.getHeight() * 2 - 1);
            Vector4f rayClip = new Vector4f(normMouseX, normMouseY, 1, 1);

            Matrix4f inverseProjection = camera.getProjection().invert(new Matrix4f());
            Vector4f rayEye = inverseProjection.transform(rayClip);

            Matrix4f inverseView = camera.getView().invert(new Matrix4f());
            Vector4f rayWorld = inverseView.transform(rayEye);

            Vector3f rayDirection = new Vector3f(rayWorld.x, rayWorld.y, rayWorld.z);
            rayDirection.normalize();

            // Get the ray's intersection with the XY plane
            Vector3f rayOrigin = camera.getPosition();
            Vector3f planePoint = new Vector3f(0, 0, 0.5f);
            Vector3f planeNormal = new Vector3f(0, 0, 1);

            float d = planePoint.dot(planeNormal);
            float t = (d - rayOrigin.dot(planeNormal)) / rayDirection.dot(planeNormal);

            if (t >= 0) {
                Vector3f intersection = new Vector3f(rayOrigin).add(rayDirection.mul(t));
                String intersectionX = String.format("%.2f", intersection.x);
                String intersectionY = String.format("%.2f", intersection.y );
                System.out.println("Intersection: " + intersectionX + ", " + intersectionY);

                int tileX = (int) Math.floor(intersection.x);
                int tileY = (int) Math.floor(intersection.y);
                if (tileX < 0 || tileX >= BOARD_WIDTH || tileY < 0 || tileY >= BOARD_HEIGHT) {
                    System.out.println("Out of bounds");
                    return;
                } else {
                    Model model = board[tileX * BOARD_WIDTH + tileY];
                    Material material = model.getMaterial();
                    material.setAmbientColor(1, 0, 0);
                }
            } else {
                System.out.println("No intersection");
            }
        });

        center.getChildren().add(canvas);

        // only one toggle button can be selected at a time
        ToggleGroup toggleGroup = new ToggleGroup();
        toggleNotGate.setToggleGroup(toggleGroup);
        toggleAndGate.setToggleGroup(toggleGroup);
        toggleOrGate.setToggleGroup(toggleGroup);
        toggleXorGate.setToggleGroup(toggleGroup);
        togglePencil.setToggleGroup(toggleGroup);
        toggleSelect.setToggleGroup(toggleGroup);
        toggleEraser.setToggleGroup(toggleGroup);


        lightXField.valueProperty().addListener((observable, oldValue, newValue) -> {
            float x = Float.parseFloat(newValue.toString());
            light.setPosition(x, light.getPosition().y, light.getPosition().z);
        });

        lightYField.valueProperty().addListener((observable, oldValue, newValue) -> {
            float y = Float.parseFloat(newValue.toString());
            light.setPosition(light.getPosition().x, y, light.getPosition().z);
        });

        lightZField.valueProperty().addListener((observable, oldValue, newValue) -> {
            float z = Float.parseFloat(newValue.toString());
            light.setPosition(light.getPosition().x, light.getPosition().y, z);
        });

        // Create a timer that copies the camera position to the labels
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                Vector3f position = camera.getPosition();
                cameraXLabel.setText(String.format("%.2f", position.x));
                cameraYLabel.setText(String.format("%.2f", position.y));
                cameraZLabel.setText(String.format("%.2f", position.z));
            }
        };

        timer.start();
    }

    public void newProject(ActionEvent actionEvent) {
        // create a modal dialog and load the "ProjectDialog.fxml" file
        Stage stage = new Stage();
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.setTitle("New Project");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("ProjectDialog.fxml"));
        try {
            stage.setScene(new javafx.scene.Scene(loader.load()));
            stage.showAndWait();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
