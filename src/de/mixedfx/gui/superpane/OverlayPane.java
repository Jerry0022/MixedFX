package de.mixedfx.gui.superpane;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

class OverlayPane extends Rectangle
{
	protected OverlayPane()
	{
		this.setFill(Color.valueOf("rgba(0, 0, 0, 0.35)"));
	}
}
