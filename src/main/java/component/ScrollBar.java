package component;

import javafx.scene.layout.AnchorPane;

import java.util.function.Consumer;

public abstract class ScrollBar extends AnchorPane {
    public abstract void onScroll(Consumer<ScrollBar> callback);

    public abstract void scrollBy(double delta);

    public abstract void scrollTo(double position);

    public abstract double getScrollableLength();

    public abstract double getAbsolutePosition();

    public abstract double getRelativePosition();
}
