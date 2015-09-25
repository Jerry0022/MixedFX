package de.mixedfx.gui.panes;

import de.mixedfx.gui.RegionManipulator;
import javafx.animation.Transition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * A Pane which contains to side by side panes. First only the mainScreen is shown, after {@link #showDetailed()} is called only the detailed screen
 * is visible. This can be undone with {@link #showMain()}. It is based on the Apple iOS 8 screen change animation.
 *
 * @author Jerry
 */
public class SlidePane extends ScrollPane {
    public final DoubleProperty slidingFrac;
    private final StackPane largePane;
    private final SlideTransition slideAnimation;
    private Region mainScreen;
    private Region detailedScreen;

    public SlidePane(final Region mainScreen, final Region detailedScreen, final Duration duration) {
        this(duration);

        this.setMain(mainScreen);
        this.setDetailed(detailedScreen);
    }

    public SlidePane(Duration duration) {
        // Remove focussable border from ScrollPane
        this.getStylesheets().add(this.getClass().getResource("/de/mixedfx/gui/panes/SlidePane.css").toExternalForm());

        // Remove ScrollBars
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.NEVER);

        this.slideAnimation = new SlideTransition(duration);
        this.slidingFrac = new SimpleDoubleProperty();

        this.largePane = new StackPane();
        this.largePane.setMinSize(0, 0);
        RegionManipulator.bindAllHeight(largePane, this.heightProperty());
        RegionManipulator.bindAllWidth(largePane, this.widthProperty());
        this.setContent(largePane);
    }

    /**
     * Must be set before this pane is part of the scene.
     */
    public void setMain(Region mainScreen) {
        this.mainScreen = mainScreen;

        // Start positioning the DetailedScreen outside the pane on the right side
        this.mainScreen.translateXProperty().bind(this.mainScreen.widthProperty().multiply(this.slidingFrac).multiply(-1));

        largePane.getChildren().add(this.mainScreen);
    }

    /**
     * Must be set before this pane is part of the scene.
     */
    public void setDetailed(Region detailedScreen) {
        this.detailedScreen = detailedScreen;

        // Start positioning the DetailedScreen outside the pane on the right side
        this.detailedScreen.translateXProperty().bind(this.mainScreen.widthProperty().add(this.mainScreen.translateXProperty()));

        largePane.getChildren().add(this.detailedScreen);
    }

    public void showMain() {
        this.slideAnimation.setRate(-1.0);
        if (this.slidingFrac.get() != 0) {
            this.slideAnimation.play();
        }
    }

    public void showDetailed() {
        this.slideAnimation.setRate(1.0);

        if (this.slidingFrac.get() != 1) {
            this.slideAnimation.play();
        }
    }

    private class SlideTransition extends Transition {
        public SlideTransition(final Duration duration) {
            this.setCycleDuration(duration);
        }

        @Override
        protected void interpolate(final double frac) {
            SlidePane.this.slidingFrac.set(frac);
        }

    }
}
