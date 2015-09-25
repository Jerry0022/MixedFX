package de.mixedfx.gui.panes;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class _AdvancedSplitWorking extends StackPane implements ChangeListener<Number> {
    public static Duration defaultDuration = Duration.seconds(2);
    private final Region referencePane;
    private final SlideTransition animation;
    private final DoubleProperty slidingFrac;
    private final DoubleProperty slidingPixel;
    public ObjectProperty<Duration> duration;
    public Region constantPane;
    private HBox worker;
    private Region shiftingPane;

    public _AdvancedSplitWorking() {
        this.setMinSize(0, 0);
        this.setAlignment(Pos.TOP_LEFT);

        this.referencePane = new Pane();
        this.getChildren().add(this.referencePane);

        this.duration = new SimpleObjectProperty<Duration>(_AdvancedSplitWorking.defaultDuration);
        this.animation = new SlideTransition(this.duration.get());
        this.animation.setInterpolator(Interpolator.EASE_BOTH);
        this.slidingFrac = new SimpleDoubleProperty(0.0);
        this.slidingFrac.addListener(this);
        this.slidingPixel = new SimpleDoubleProperty(0.0);
        this.worker = new HBox();
        this.getChildren().add(new Group(this.worker));

    }

    public _AdvancedSplitWorking(final Region constantPane, final Region shiftingPane) {
        this();

        this.constantPane = constantPane;
        this.shiftingPane = shiftingPane;
        this.shiftingPane.setStyle("-fx-background-color: yellow");

        this.constantPane.prefWidthProperty().bind(
                this.referencePane.widthProperty().subtract(this.slidingPixel));
        this.constantPane.prefHeightProperty().bind(this.referencePane.heightProperty());

        this.shiftingPane.widthProperty().addListener(
                (ChangeListener<Number>) (observable, oldValue, newValue) -> {
                    // Must be done in the next GUI frame process step because
                    // otherwise the this.constantPane width calculation already
                    // took place and is not done before the next action is
                    // fired.
                    Platform.runLater(() -> {
                        if (this.animation.getStatus() != Animation.Status.RUNNING
                                && this.animation.getRate() == 1) {
                            this.slidingPixel.set(newValue.doubleValue());
                        }
                    });
                });
        this.shiftingPane.prefHeightProperty().bind(this.referencePane.heightProperty());

        this.closeSidebar();

        this.worker.getChildren().addAll(this.constantPane, this.shiftingPane);

    }

    public void closeSidebar() {
        this.animation.setRate(-1.0);
        this.animation.play();
    }

    public void openSidebar() {
        this.animation.setRate(1.0);
        this.animation.play();
    }

    @Override
    public void changed(final ObservableValue<? extends Number> observable, final Number oldValue,
                        final Number newValue) {
        this.slidingPixel.set(newValue.doubleValue() * this.shiftingPane.getWidth());
    }

    public Region getConstantPane() {
        return this.constantPane;
    }

    public void setConstantPane(Region constantPane) {
        this.constantPane = constantPane;
        this.constantPane.prefWidthProperty().bind(
                this.referencePane.widthProperty().subtract(this.slidingPixel));
        this.constantPane.prefHeightProperty().bind(this.referencePane.heightProperty());
        this.worker.getChildren().clear();
        if (this.shiftingPane == null) {
            this.worker.getChildren().addAll(constantPane);
        }
        else {
            this.worker.getChildren().addAll(constantPane, this.shiftingPane);
        }
    }

    public Region getShiftingPane() {
        return this.shiftingPane;
    }

    public void setShiftingPane(Region shiftingPane) {
        this.shiftingPane = shiftingPane;
        this.shiftingPane.setStyle("-fx-background-color: yellow");
        this.shiftingPane.widthProperty().addListener(
                (ChangeListener<Number>) (observable, oldValue, newValue) -> {
                    // Must be done in the next GUI frame process step because
                    // otherwise the this.constantPane width calculation already
                    // took place and is not done before the next action is
                    // fired.
                    Platform.runLater(() -> {
                        if (this.animation.getStatus() != Animation.Status.RUNNING
                                && this.animation.getRate() == 1) {
                            this.slidingPixel.set(newValue.doubleValue());
                        }
                    });
                });
        this.shiftingPane.prefHeightProperty().bind(this.referencePane.heightProperty());
        this.closeSidebar();
        this.worker.getChildren().clear();
        this.worker.getChildren().addAll(this.constantPane, this.shiftingPane);
    }

    private class SlideTransition extends Transition {
        private SlideTransition() {

        }

        private SlideTransition(final Duration duration) {
            this.setCycleDuration(duration);
        }

        @Override
        protected void interpolate(final double frac) {
            _AdvancedSplitWorking.this.slidingFrac.set(frac);
        }
    }
}
