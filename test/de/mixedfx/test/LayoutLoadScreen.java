package de.mixedfx.test;

import de.mixedfx.assets.ImageProducer;
import de.mixedfx.gui.RegionManipulator;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class LayoutLoadScreen extends HBox
{
	public LayoutLoadScreen()
	{
		this.setPrefSize(50, 50);
		RegionManipulator.bindBackground(this, ImageProducer.getMonoColored(Color.RED));
	}
}
