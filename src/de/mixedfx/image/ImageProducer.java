package de.mixedfx.image;

import java.util.Random;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * This class produces {@link Image} Objects from colors.
 *
 * @author Jerry
 */
public class ImageProducer
{
	/**
	 * @param image
	 * @return Returns a COPY of the given image with some randomly made transparent pixel!
	 */
	public static Image getRandomSemiTransparent(final Image image)
	{
		final WritableImage semiTransparent = new WritableImage(50, 50);
		final PixelWriter pw = semiTransparent.getPixelWriter();

		final PixelReader pr = image.getPixelReader();
		for (int i = 0; i < image.getWidth(); i++)
		{
			for (int j = 0; j < image.getHeight(); j++)
			{
				final Random random = new Random();
				if (random.nextBoolean())
				{
					pw.setColor(i, j, pr.getColor(i, j));
				}
				else
				{
					pw.setColor(i, j, Color.TRANSPARENT);
				}
			}
		}

		return semiTransparent;
	}

	/**
	 * @param image
	 * @return Returns a darkened COPY of the given image.
	 */
	public static Image getDarkerImage(final Image image)
	{
		final WritableImage darkerImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
		final PixelWriter pw = darkerImage.getPixelWriter();

		final PixelReader pr = image.getPixelReader();
		for (int i = 0; i < image.getWidth(); i++)
		{
			for (int j = 0; j < image.getHeight(); j++)
			{
				pw.setColor(i, j, pr.getColor(i, j).darker());
			}
		}

		return darkerImage;
	}

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
	 * See also {@link #getMonoColored(Color, int, int)}.
	 *
	 * @param monoColor
	 * @return Returns an image which has exactly one colored pixel.
	 */
	public static Image getMonoColored(final Color monoColor)
	{
		return ImageProducer.getMonoColored(monoColor, 1, 1);
	}

	/**
	 * @param color
	 * @param width
	 * @param height
	 * @return Returns an image of the given width and height and each pixel has the given color.
	 */
	public static Image getMonoColored(final Color color, final int width, final int height)
	{
		final WritableImage image = new WritableImage(width, height);
		final PixelWriter pw = image.getPixelWriter();

		for (int i = 0; i < image.getWidth(); i++)
		{
			for (int j = 0; j < image.getHeight(); j++)
			{
				pw.setColor(i, j, color);
			}
		}

		return image;
	}
}
