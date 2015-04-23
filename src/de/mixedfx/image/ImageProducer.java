package de.mixedfx.image;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * @author Jerry
 *
 */
public class ImageProducer
{
	/**
	 * Creates a transparent image.
	 *
	 * @return Returns an image which has exactly one transparent pixel (rgba = 0).
	 */
	public static Image getTransparentImage()
	{
		final WritableImage writeableImage = new WritableImage(1, 1);
		final PixelWriter pixelWriter = writeableImage.getPixelWriter();

		final Color transparentColor = new Color(0, 0, 0, 0);
		pixelWriter.setColor(0, 0, transparentColor);

		return writeableImage;
	}

	/**
	 * See also {@link #getMonoColored(Color, double)}.
	 *
	 * @param monoColor
	 * @return Returns an image which has exactly one colored pixel (alpha = 1).
	 */
	public static Image getMonoColored(final Color monoColor)
	{
		return ImageProducer.getMonoColored(monoColor, 1.0);
	}

	/**
	 * Creates a - maybe opaque - image of one color.
	 *
	 * @param monoColor
	 * @param alpha
	 *            Alpha value for opacity from 0 to 1
	 * @return Returns an image which has exactly one colored pixel.
	 */
	public static Image getMonoColored(Color monoColor, final double alpha)
	{
		final WritableImage writeableImage = new WritableImage(1, 1);
		final PixelWriter pixelWriter = writeableImage.getPixelWriter();

		// Ignore set opacity
		monoColor = new Color(monoColor.getRed(), monoColor.getGreen(), monoColor.getBlue(), alpha);
		pixelWriter.setColor(0, 0, monoColor);

		return writeableImage;
	}
}
