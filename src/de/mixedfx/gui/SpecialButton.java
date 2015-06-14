package de.mixedfx.gui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.paint.Color;
import de.mixedfx.image.ImageProducer;

public class SpecialButton extends Button implements ChangeListener<Object>
{
	private final ObjectProperty<Image>	backgroundImage;

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

		// React on button size changes
		this.widthProperty().addListener(this);
		this.heightProperty().addListener(this);

		if (backgroundImage == null)
		{
			backgroundImage = new SimpleObjectProperty<>(ImageProducer.getMonoColored(new Color(226, 0, 116, 0.6)));
		}

		this.backgroundImage = backgroundImage;

		// React on image changes, e. g. by LayoutManager
		this.backgroundImage.addListener(this);

		this.updateBackground(backgroundImage.get());
	}

	public void setBackgroundImage(final ObjectProperty<Image> backgroundImage)
	{
		// Remove old listener so that the changed method is not called twice or
		// more often (because of old bound imageProperties)
		backgroundImage.removeListener(this);

		// React on image changes, e. g. by LayoutManager
		backgroundImage.addListener(this);

		this.updateBackground(backgroundImage.get());
	}

	@Override
	public void changed(final ObservableValue<? extends Object> observable, final Object oldValue, final Object newValue)
	{
		if (newValue instanceof Image)
		{
			this.updateBackground((Image) newValue);
		}
		else
			if (newValue instanceof Number)
			{
				if (this.getBackground() != null)
				{
					if (this.getBackground().getImages().size() > 0)
					{
						this.updateBackground(this.getBackground().getImages().get(0).getImage());
					}
				}
			}
	}

	public void updateBackground(final Image image)
	{
		final BackgroundSize backgroundSize = new BackgroundSize(this.getWidth(), this.getHeight(), false, false, false, false);
		final Background background = new Background(new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize));
		this.setBackground(background);
	}
}
