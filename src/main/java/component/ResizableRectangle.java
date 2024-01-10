package component;

import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class ResizableRectangle extends Rectangle {
    private static final int HANDLE_MIN_WIDTH = 10;
    private static final int HANDLE_MIN_HEIGHT = 10;
    Runnable onCenterHandleDragged;
    Runnable onLeftHandleDragged;
    Runnable onRightHandleDragged;
    Runnable onTopHandleDragged;
    Runnable onBottomHandleDragged;
    private double rectangleStartX;
    private double rectangleStartY;
    private boolean interactionEnabled = true;
    private List<Rectangle> handles = new ArrayList<>();

    public ResizableRectangle(double x, double y, double width, double height, Group group) {
        super(x, y, width, height);

        group.getChildren().add(this);
        handles.add(this);
    }

    private void makeCenterHandle(Group group, boolean horizontal, boolean vertical) {
        Rectangle centerHandle = new Rectangle();
        centerHandle.xProperty().bind(super.xProperty().add(HANDLE_MIN_WIDTH));
        centerHandle.yProperty().bind(super.yProperty().add(HANDLE_MIN_HEIGHT));
        centerHandle.widthProperty().bind(super.widthProperty().subtract(HANDLE_MIN_WIDTH));
        centerHandle.heightProperty().bind(super.heightProperty().subtract(HANDLE_MIN_HEIGHT * 2));

        group.getChildren().add(centerHandle);
        handles.add(centerHandle);

        centerHandle.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            if (!interactionEnabled) {
                return;
            }

            centerHandle.getParent().setCursor(Cursor.MOVE);
        });

        centerHandle.setFill(Color.TRANSPARENT);

        centerHandle.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (!interactionEnabled) {
                return;
            }

            rectangleStartX = super.getX();
            rectangleStartY = super.getY();
            double offsetX = event.getX() - rectangleStartX;
            double offsetY = event.getY() - rectangleStartY;
            double newX = super.getX() + offsetX;
            double newY = super.getY() + offsetY;

            if (horizontal && newX >= 0 && newX + super.getWidth() <= super.getParent().getBoundsInLocal().getWidth()) {
                double centerX = newX - super.getWidth() / 2;
                super.setX(centerX);
            }

            if (vertical && newY >= 0 && newY + super.getHeight() <= super.getParent().getBoundsInLocal().getHeight()) {
                double centerY = newY - super.getHeight() / 2;
                super.setY(centerY);
            }

            if (onCenterHandleDragged != null) {
                onCenterHandleDragged.run();
            }
        });
    }

    private void makeTopHandle(Group group) {
        Rectangle topHandle = new Rectangle();
        topHandle.xProperty().bind(super.xProperty().add(10));
        topHandle.yProperty().bind(super.yProperty());
        topHandle.widthProperty().bind(super.widthProperty().subtract(10));
        topHandle.setHeight(HANDLE_MIN_HEIGHT);

        group.getChildren().add(topHandle);
        handles.add(topHandle);

        topHandle.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            if (!interactionEnabled) {
                return;
            }

            topHandle.getParent().setCursor(Cursor.N_RESIZE);
        });

        topHandle.setFill(Color.TRANSPARENT);

        topHandle.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (!interactionEnabled) {
                return;
            }

            rectangleStartY = super.getY();
            double offsetY = event.getY() - rectangleStartY;
            double newY = super.getY() + offsetY;

            if (newY >= 0 && newY <= super.getY() + super.getHeight() - (topHandle.getHeight() / 2)) {
                super.setY(newY);
                super.setHeight(super.getHeight() - offsetY);
            }

            if (onTopHandleDragged != null) {
                onTopHandleDragged.run();
            }
        });
    }

    // Use this to disable the entire resizable rectangle (since we aren't allowed to override the setDisable method)
    public void setInteractionEnabled(boolean interactionEnabled) {
        this.interactionEnabled = interactionEnabled;
        for (Rectangle handle : handles) {
            handle.setDisable(!interactionEnabled);
        }
    }

    private void makeBottomHandle(Group group) {
        Rectangle bottomHandle = new Rectangle();
        bottomHandle.xProperty().bind(super.xProperty().add(10));
        bottomHandle.yProperty().bind(super.yProperty().add(super.heightProperty().subtract(10)));
        bottomHandle.widthProperty().bind(super.widthProperty().subtract(10));
        bottomHandle.setHeight(HANDLE_MIN_HEIGHT);

        group.getChildren().add(bottomHandle);
        handles.add(bottomHandle);

        bottomHandle.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            if (!interactionEnabled) {
                return;
            }

            bottomHandle.getParent().setCursor(Cursor.S_RESIZE);
        });

        bottomHandle.setFill(Color.TRANSPARENT);

        bottomHandle.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (!interactionEnabled) {
                return;
            }

            rectangleStartY = super.getY();
            double offsetY = event.getY() - rectangleStartY;
            if (offsetY >= 0 && offsetY <= super.getY() + super.getHeight() - (bottomHandle.getHeight() / 2)) {
                super.setHeight(offsetY);
            }

            if (onBottomHandleDragged != null) {
                onBottomHandleDragged.run();
            }
        });
    }

    private void makeLeftHandle(Group group) {
        Rectangle leftHandle = new Rectangle();
        leftHandle.xProperty().bind(super.xProperty());
        leftHandle.yProperty().bind(super.yProperty());
        leftHandle.setWidth(HANDLE_MIN_WIDTH);

        leftHandle.heightProperty().bind(super.heightProperty());

        group.getChildren().add(leftHandle);
        handles.add(leftHandle);

        leftHandle.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            if (!interactionEnabled) {
                return;
            }

            leftHandle.getParent().setCursor(Cursor.W_RESIZE);
        });

        leftHandle.setFill(Color.TRANSPARENT);

        leftHandle.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (!interactionEnabled) {
                return;
            }

            rectangleStartX = super.getX();
            double offsetX = event.getX() - rectangleStartX;
            double newX = super.getX() + offsetX;

            if (newX >= 0 && newX <= super.getX() + super.getWidth() - (leftHandle.getWidth() / 2)) {
                super.setX(newX);
                super.setWidth(super.getWidth() - offsetX);
            }

            if (onLeftHandleDragged != null) {
                onLeftHandleDragged.run();
            }
        });
    }

    private void makeRightHandle(Group group) {
        Rectangle rightHandle = new Rectangle();
        rightHandle.xProperty().bind(super.xProperty().add(super.widthProperty()));
        rightHandle.yProperty().bind(super.yProperty());
        rightHandle.setWidth(HANDLE_MIN_WIDTH);
        rightHandle.heightProperty().bind(super.heightProperty());

        group.getChildren().add(rightHandle);
        handles.add(rightHandle);

        rightHandle.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            if (!interactionEnabled) {
                return;
            }

            rightHandle.getParent().setCursor(Cursor.E_RESIZE);
        });

        rightHandle.setFill(Color.TRANSPARENT);

        rightHandle.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (!interactionEnabled) {
                return;
            }

            rectangleStartX = super.getX();
            double offsetX = event.getX() - rectangleStartX;
            if (offsetX >= 0 && offsetX <= super.getX() + super.getWidth() - (rightHandle.getWidth() / 2)) {
                super.setWidth(offsetX);
            }

            if (onRightHandleDragged != null) {
                onRightHandleDragged.run();
            }
        });
    }

    public void setOnCenterHandleDragged(Runnable onCenterHandleDragged) {
        this.onCenterHandleDragged = onCenterHandleDragged;
    }

    public void setOnLeftHandleDragged(Runnable onLeftHandleDragged) {
        this.onLeftHandleDragged = onLeftHandleDragged;
    }

    public void setOnRightHandleDragged(Runnable onRightHandleDragged) {
        this.onRightHandleDragged = onRightHandleDragged;
    }

    public void setOnTopHandleDragged(Runnable onTopHandleDragged) {
        this.onTopHandleDragged = onTopHandleDragged;
    }

    public void setOnBottomHandleDragged(Runnable onBottomHandleDragged) {
        this.onBottomHandleDragged = onBottomHandleDragged;
    }

    public static HandleBuilder builder() {
        return new HandleBuilder();
    }

    public static class HandleBuilder {
        private boolean shouldMakeCenterHandle = false;
        private boolean centerHandleHorizontal = false;
        private boolean centerHandleVertical = false;
        private boolean shouldMakeTopHandle = false;
        private boolean shouldMakeBottomHandle = false;
        private boolean shouldMakeLeftHandle = false;
        private boolean shouldMakeRightHandle = false;

        public HandleBuilder makeCenterMovableHorizontally() {
            this.shouldMakeCenterHandle = true;
            this.centerHandleHorizontal = true;
            return this;
        }

        public HandleBuilder makeCenterMovableVertically() {
            this.shouldMakeCenterHandle = true;
            this.centerHandleVertical = true;
            return this;
        }

        public HandleBuilder makeTopHandle() {
            this.shouldMakeTopHandle = true;
            return this;
        }

        public HandleBuilder makeBottomHandle() {
            this.shouldMakeBottomHandle = true;
            return this;
        }

        public HandleBuilder makeLeftHandle() {
            this.shouldMakeLeftHandle = true;
            return this;
        }

        public HandleBuilder makeRightHandle() {
            this.shouldMakeRightHandle = true;
            return this;
        }

        public ResizableRectangle build(double x, double y, double width, double height, Group group) {
            ResizableRectangle resizableRectangle = new ResizableRectangle(x, y, width, height, group);

            if (shouldMakeCenterHandle) {
                resizableRectangle.makeCenterHandle(group, centerHandleHorizontal, centerHandleVertical);
            }

            if (shouldMakeTopHandle) {
                resizableRectangle.makeTopHandle(group);
            }

            if (shouldMakeBottomHandle) {
                resizableRectangle.makeBottomHandle(group);
            }

            if (shouldMakeLeftHandle) {
                resizableRectangle.makeLeftHandle(group);
            }

            if (shouldMakeRightHandle) {
                resizableRectangle.makeRightHandle(group);
            }

            return resizableRectangle;
        }
    }
}
