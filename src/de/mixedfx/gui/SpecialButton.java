package de.mixedfx.gui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import de.mixedfx.assets.ImageProducer;

public class SpecialButton extends Button
{
	/**
	 * Applies no text and default image and size.
	 */
	public SpecialButton()
	{
		this(null, null);
	}

	/**
	 * @param text
	 *            If null no text will be displayed on the button
	 * @param backgroundImage
	 *            If null a mono colored 1x1 pixel will be set
	 */
	public SpecialButton(final String text, ObjectProperty<Image> backgroundImage)
	{
		super(text == null ? "" : text);

		// This default button size can be changed after initialization
		this.setPrefSize(50, 30);

		if (backgroundImage == null)
		{
			backgroundImage = new SimpleObjectProperty<>(ImageProducer.getMonoColored(new Color(226, 0, 116, 0.6)));
		}

		RegionManipulator.bindBackgrond(this, backgroundImage);
	}
}
