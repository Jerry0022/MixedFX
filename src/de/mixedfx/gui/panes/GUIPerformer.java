package de.mixedfx.gui.panes;

import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class GUIPerformer
{
	/**
	 *
	 */
	/**
	 * Usually if you set minimum size and you start the application you can reduce the size of the
	 * GUI. Set the real minimum size of the GUI. A scene is automatically created, added and as
	 * content the rootNode is set.
	 *
	 * @param stage
	 *            The minWidthProperty() and minHeightProperty() are bound and
	 *            {@link Stage#setScene(Scene)} is called.
	 * @param rootNode
	 *            The minSize of the root node is set once to minWidth and minHeight.
	 * @param minWidth
	 *            The starting minimum width. If you want to change the minimum width later call
	 *            rootNode.setMinSize() or bind it.
	 * @param minHeight
	 *            The starting minimum height. If you want to change the minimum height later call
	 *            rootNode.setMinSize() or bind it.
	 * @return Returns the automatically created and added scene.
	 */
	public static Scene setMinSizeAndScene(final Stage stage, final Region rootNode, final double minWidth, final double minHeight)
	{
		rootNode.setMinSize(minWidth, minHeight);

		final Scene scene = new Scene(rootNode, minWidth, minHeight);
		stage.setScene(scene);

		// Set minimum size of the total window to the roots minimum size
		// plus decoration
		stage.minHeightProperty().bind(Bindings.max(0, stage.heightProperty().subtract(scene.heightProperty()).add(rootNode.minHeightProperty())));
		stage.minWidthProperty().bind(Bindings.max(0, stage.widthProperty().subtract(scene.widthProperty()).add(rootNode.minWidthProperty())));

		return scene;
	}
}
