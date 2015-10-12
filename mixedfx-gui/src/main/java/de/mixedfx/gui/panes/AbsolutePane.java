package de.mixedfx.gui.panes;

import javafx.beans.value.ChangeListener;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

import java.util.HashMap;

/**
 * A {@link Pane} which allows to set a child to a x or y coordinate relative to the size before
 * adding the child or afterwards.
 *
 * @author Jerry
 */
public class AbsolutePane extends Pane {
    final HashMap<Region, Number> widthDB = new HashMap<Region, Number>();
    final HashMap<Region, Number> heightDB = new HashMap<Region, Number>();

    /**
     * Positions a node relative to the pane size.
     *
     * @param node          The node (maybe already part of this pane or not)
     * @param relativeX     as percentage
     * @param relativeY     as percentage.
     * @param referencePane If null the node doesn't scale. Otherwise it scales according to the size changes
     *                      of the referencePane (as long as node and referencePane are part of the scene
     *                      graph). Pay attention that the node AND the referencePane are INSTANTIATED BEFORE!
     */
    public void setNodeCenter(final Region node, final double relativeX, final double relativeY, final Region referencePane) {
        node.translateXProperty().unbind();
        node.translateYProperty().unbind();

        // Binds x and y coordinate of the (rectangle) node's center point to
        // the given percentage.
        node.translateXProperty().bind(this.widthProperty().multiply(relativeX).subtract(node.widthProperty().divide(2)));
        node.translateYProperty().bind(this.heightProperty().multiply(relativeY).subtract(node.heightProperty().divide(2)));

        if (referencePane != null)
            this.makeScaleDependant(node, referencePane);

        // Add the node to this pane if not already done
        if (!this.getChildren().contains(node))
            this.getChildren().add(node);
    }

    /**
     * Positions a node relative to the pane size.
     *
     * @param node          The node (maybe already part of this pane or not)
     * @param relativeX     as percentage
     * @param relativeY     as percentage.
     * @param referencePane If null the node doesn't scale. Otherwise it scales according to the size changes
     *                      of the referencePane (as long as node and referencePane are part of the scene
     *                      graph). Pay attention that the node AND the referencePane are INSTANTIATED BEFORE!
     */
    public void setNodeTopLeft(final Region node, final double relativeX, final double relativeY, final Region referencePane) {
        node.translateXProperty().unbind();
        node.translateYProperty().unbind();

        // Binds x and y coordinate of the (rectangle) node's center point to
        // the given percentage.
        node.translateXProperty().bind(this.widthProperty().multiply(relativeX));
        node.translateYProperty().bind(this.heightProperty().multiply(relativeY));

        if (referencePane != null)
            this.makeScaleDependant(node, referencePane);

        // Add the node to this pane if not already done
        if (!this.getChildren().contains(node))
            this.getChildren().add(node);
    }

    private void makeScaleDependant(final Region node, final Region referencePane) {
        final ChangeListener<Number> widthListener = (observable, oldValue, newValue) ->
        {
            if (oldValue.doubleValue() <= 0)
                AbsolutePane.this.widthDB.put(referencePane, newValue);
            else
                node.scaleXProperty().set(newValue.doubleValue() / AbsolutePane.this.widthDB.get(referencePane).doubleValue());
        };

        final ChangeListener<Number> heightListener = (observable, oldValue, newValue) ->
        {
            if (oldValue.doubleValue() <= 0)
                AbsolutePane.this.heightDB.put(referencePane, newValue);
            else
                node.scaleYProperty().set(newValue.doubleValue() / AbsolutePane.this.heightDB.get(referencePane).doubleValue());
        };

        referencePane.widthProperty().addListener(widthListener);
        referencePane.heightProperty().addListener(heightListener);

        final ChangeListener<Parent> removeListener = (observable, oldValue, newValue) ->
        {
            if (newValue == null) {
                referencePane.widthProperty().removeListener(widthListener);
                this.widthDB.remove(referencePane);
                referencePane.heightProperty().removeListener(heightListener);
                this.heightDB.remove(referencePane);
            }
        };

        node.parentProperty().addListener(removeListener);
        referencePane.parentProperty().removeListener(removeListener);
    }
}
