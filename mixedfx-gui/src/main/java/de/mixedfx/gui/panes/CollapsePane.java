package de.mixedfx.gui.panes;

import de.mixedfx.gui.RegionManipulator;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import jfxtras.labs.animation.BindableTransition;

/**
 * A pane which contains to Regions. First only the right region is shown. After collapse is called
 * the right region will is shrunk to its minimum size (left pane will now use the rest space).
 *
 * @author Jerry
 */
public class CollapsePane extends HBox {
    private final static Duration ANIMATION_DURATION_DEFAULT = Duration.seconds(3);
    private final Duration duration;
    private final HBox leftSide;
    private final HBox rightSide;
    private final BindableTransition animation;
    private CollapseDoneInterface doneEvent;

    public CollapsePane(final Region left, final Region right) {
        this(left, right, "", "", CollapsePane.ANIMATION_DURATION_DEFAULT);
    }

    public CollapsePane(final Region left, final Region right, final Image backgroundImageLeft, final Image backgroundImageRight, final Duration duration) {
        this.duration = duration;
        this.setAlignment(Pos.CENTER_RIGHT);

        this.leftSide = new HBox(left);
        RegionManipulator.bindBackground(left, backgroundImageLeft);
        this.leftSide.setAlignment(Pos.CENTER);
        this.leftSide.setPrefWidth(0);
        this.leftSide.managedProperty().bind(this.leftSide.visibleProperty());
        this.leftSide.setVisible(false);

        this.rightSide = new HBox(right);
        RegionManipulator.bindBackground(right, backgroundImageRight);
        this.rightSide.setAlignment(Pos.CENTER);

        HBox.setHgrow(this.leftSide, Priority.NEVER);
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(this.rightSide, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.NEVER);

        // Initialize animation and bind leftSide's width to the fullSize minus
        // the the inner part of the rightSide
        this.animation = new BindableTransition(this.duration);
        this.leftSide.prefWidthProperty().bind(this.animation.fractionProperty().multiply(this.widthProperty().subtract(right.widthProperty())));

        this.getChildren().addAll(this.leftSide, this.rightSide);
    }

    public CollapsePane(final Region left, final Region right, final String cssIDLeft, final String cssIDRight, final Duration duration) {
        this.duration = duration;
        this.setAlignment(Pos.CENTER_RIGHT);

        this.leftSide = new HBox(left);
        this.leftSide.setId(cssIDLeft);
        this.leftSide.setAlignment(Pos.CENTER);
        this.leftSide.setPrefWidth(0);
        this.leftSide.managedProperty().bind(this.leftSide.visibleProperty());
        this.leftSide.setVisible(false);

        this.rightSide = new HBox(right);
        this.rightSide.setId(cssIDRight);
        this.rightSide.setAlignment(Pos.CENTER);

        HBox.setHgrow(this.leftSide, Priority.NEVER);
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(this.rightSide, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.NEVER);

        // Initialize animation and bind leftSide's width to the fullSize minus
        // the the inner part of the rightSide
        this.animation = new BindableTransition(this.duration);
        this.leftSide.prefWidthProperty().bind(this.animation.fractionProperty().multiply(this.widthProperty().subtract(right.widthProperty())));

        this.getChildren().addAll(this.leftSide, this.rightSide);
    }

    public void collapse() {
        this.leftSide.setVisible(true);
        this.leftSide.managedProperty().unbind();
        this.animation.setOnFinished(arg0 ->
        {
            // Revert growing
            HBox.setHgrow(this.leftSide, Priority.ALWAYS);
            // HBox.setHgrow(this.left, Priority.ALWAYS);
            HBox.setHgrow(this.rightSide, Priority.NEVER);

            // Undo all bindings
            this.leftSide.prefWidthProperty().unbind();

            if (this.doneEvent != null) {
                this.doneEvent.collapseDone();
            }
        });
        this.animation.play();
    }

    public void setOnFinished(final CollapseDoneInterface eventHandler) {
        this.doneEvent = eventHandler;
    }

    public interface CollapseDoneInterface {
        void collapseDone();
    }
}
