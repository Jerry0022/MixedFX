package de.mixedfx.button;

import javafx.beans.binding.DoubleExpression;
import javafx.beans.value.ChangeListener;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Region;

public class RegionManipulator
{
	/**
	 * Binds all width properties of a region to a {@link DoubleExpression}.
	 *
	 * @param region
	 *            Region which sizes shall be bound.
	 * @param property
	 *            Double to which the min, pref and max width of the region shall be bound.
	 */
	private static void bindAllWidth(final Region region, final DoubleExpression property)
	{
		region.minWidthProperty().bind(property);
		region.prefWidthProperty().bind(property);
		region.maxWidthProperty().bind(property);
	}

	/**
	 * Binds all height properties of a region to a {@link DoubleExpression}.
	 *
	 * @param region
	 *            Region which sizes shall be bound.
	 * @param property
	 *            Double to which the min, pref and max height of the region shall be bound.
	 */
	private static void bindAllHeight(final Region region, final DoubleExpression property)
	{
		region.minHeightProperty().bind(property);
		region.prefHeightProperty().bind(property);
		region.maxHeightProperty().bind(property);
	}

	/**
	 * Every Region can have a background. This method applies an image as background filling the
	 * whole (background) area of the Region. It resizes automatically with changes of the region.
	 *
	 * @param region
	 *            Region whose background shall be set.
	 * @param image
	 *            Image which shall be the background of the region.
	 */
	private static void bindBackground(final Region region, final Image image)
	{
		region.heightProperty().addListener((ChangeListener<Number>) (observable, oldValue, newValue) ->
		{
			region.setBackground(new Background(new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(region.widthProperty().get(), region.heightProperty().get(), false, false, false, false))));
		});
	}
}
