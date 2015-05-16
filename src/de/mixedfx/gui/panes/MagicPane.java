package de.mixedfx.gui.panes;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * A pane in which exchanges panes.
 *
 * @author Jerry
 *
 */
public class MagicPane extends StackPane
{
	/**
	 * This pane just changes its content to the property's current pane.
	 *
	 * @param pane
	 *            The property which contains always the current pane.
	 */
	public MagicPane(final ObjectProperty<Region> pane)
	{
		this.setMinSize(0, 0);

		pane.addListener((ChangeListener<Region>) (observable, oldValue, newValue) ->
		{
			this.getChildren().clear();
			this.getChildren().add(newValue);
		});

		if (pane.get() != null)
			this.getChildren().add(pane.get());
	}
}
