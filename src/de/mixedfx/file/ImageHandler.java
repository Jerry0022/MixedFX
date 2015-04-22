package de.mixedfx.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;

import de.mixedfx.image.ImageProducer;

public class ImageHandler
{
	/**
	 * If to use JavaFX BackgroundLoading, see also {@link Image}. Default: false
	 */
	public static boolean	backgroundLoading	= false;

	/**
	 * The preferred prefix.
	 */
	public static String	prefix				= "img";
	/**
	 * The preferred extension (only needed for writing actions).
	 */
	public static String	extension			= "png";

	/**
	 * Same as {@link ImageHandler#readImage(FileObject)} but applies also the image prefix to the
	 * fileObject before reading!
	 *
	 * @param fileObject
	 * @return
	 */
	public static Image readImageFormatted(final FileObject fileObject)
	{
		fileObject.setPrefix(ImageHandler.prefix);
		return ImageHandler.readImage(fileObject);
	}

	/**
	 * Reads an image. Doesn't throw an exception because even if the image is not found it returns
	 * a transparent one (of one pixel).
	 *
	 * @param fileObject
	 *            The image to retrieve. The extension can be omitted.
	 * @return Returns the found image or a transparent one.
	 */
	public static Image readImage(final FileObject fileObject)
	{
		try
		{
			return new Image(DataHandler.readFile(fileObject).toURI().toString(), ImageHandler.backgroundLoading);
		}
		catch (final FileNotFoundException e)
		{
			return ImageProducer.getTransparentImage();
		}
	}

	/**
	 * Writes an image object to the destination. Overwrites existing files.
	 *
	 * @param destination
	 *            The destination FileObject. The prefix and extension are overwritten with the
	 *            default one, see also {@link #prefix} and {@link #extension}
	 * @param toWrite
	 * @return Returns true on success or false if the image could not be saved.
	 */
	public static boolean writeImage(final FileObject destination, final Image toWrite)
	{
		destination.setPrefix(ImageHandler.prefix);
		destination.setExtension(ImageHandler.extension);

		// Delete first an existing file
		DataHandler.deleteFile(destination.getFullPath());

		final PixelReader pixelReader = toWrite.getPixelReader();
		final int width = (int) toWrite.getWidth();
		final int height = (int) toWrite.getHeight();

		final WritableImage writeableImage = new WritableImage(width, height);
		final PixelWriter pixelWriter = writeableImage.getPixelWriter();

		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
			{
				final Color color = pixelReader.getColor(x, y);
				pixelWriter.setColor(x, y, color);
			}

		try
		{
			final File file = DataHandler.writeFile(destination);
			if (file == null)
				throw new IOException();
			ImageIO.write(SwingFXUtils.fromFXImage(writeableImage, null), destination.getExtension(), file);
		}
		catch (final IOException e)
		{
			return false;
		}

		return true;
	}

	/**
	 * Copies image by reading the source {@link #readImage(FileObject)} and writing it to the
	 * destination. See also {@link #writeImage(FileObject, Image)}
	 *
	 * @param source
	 * @param destination
	 */
	public static void copyImage(final FileObject source, final FileObject destination)
	{
		ImageHandler.writeImage(destination, ImageHandler.readImage(source));
	}

	/**
	 * Removes an image and applies the prefix.
	 *
	 * @param destination
	 */
	public static void deleteImageFormatted(final FileObject destination)
	{
		destination.setPrefix(ImageHandler.prefix);
		DataHandler.deleteFile(destination.getFullPath());
	}

	/**
	 * Removes an image and applies the prefix.
	 *
	 * @param destination
	 */
	public static void deleteImage(final FileObject destination)
	{
		DataHandler.deleteFile(destination.getFullPath());
	}
}
