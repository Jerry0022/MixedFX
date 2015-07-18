package de.mixedfx.assets;

import java.io.IOException;

import javafx.scene.image.Image;

import org.apache.commons.io.FileUtils;

import de.mixedfx.file.DataHandler;
import de.mixedfx.file.FileObject;
import de.mixedfx.java.EasyGson;
import de.mixedfx.logging.Log;

/**
 * Additionally to the other Handler this Handler applies the prefix before reading / writing. Can
 * write:
 * <ul>
 * <li>Images</li>
 * <li>CSS</li>
 * <li>others</li>
 * </ul>
 *
 * @author Jerry
 *
 */
public class MasterHandler
{
	@SuppressWarnings("unchecked")
	public static <T extends Object> T read(final FileObject file, final Class<? extends T> type)
	{
		if (type.equals(Image.class))
		{
			file.setPrefix(ImageHandler.prefix);
			return (T) ImageHandler.readImage(file);
		}
		else
		{
			try
			{
				return (T) DataHandler.createOrFindFile(file);
			}
			catch (IOException | InterruptedException e)
			{
				Log.assets.error("Could not read/access file! " + file);
				return null;
			}
		}
	}

	public static void write(final FileObject file, final Object object)
	{
		if (object instanceof Image)
		{
			ImageHandler.writeImage(file, (Image) object);
		}
		else
		{
			DataHandler.writeFile(file);
			try
			{
				FileUtils.writeStringToFile(file.toFile(), EasyGson.toGSON(object));
			}
			catch (final IOException e)
			{
				Log.assets.error("Could not write/access file! " + file);
			}
		}
	}

	public static void remove(final FileObject file, final Class<?> type)
	{
		if (type.equals(Image.class))
		{
			file.setPrefix(ImageHandler.prefix);
		}
		DataHandler.deleteFile(file);
	}
}
