package de.mixedfx.assets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javafx.scene.image.Image;
import de.mixedfx.file.DataHandler;
import de.mixedfx.file.FileObject;
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
			System.out.println(file);
			return (T) ImageHandler.readImage(file);
		}
		else
			if (type.equals(File.class))
			{
				try
				{
					file.setPrefix(CSSHandler.prefix);
					System.out.println(file);
					return (T) DataHandler.createOrFindFile(file);
				}
				catch (IOException | InterruptedException e)
				{
					Log.assets.error("Could not write/access file! " + file);
					return null;
				}
			}
			else
			{
				try
				{
					return (T) DataHandler.readFile(file);
				}
				catch (final FileNotFoundException e)
				{
					Log.assets.error("File not found! " + file);
					return null;
				}
			}

	}

	public static void write(final FileObject path, final Object object)
	{
		if (object instanceof Image)
		{
			ImageHandler.writeImage(path, (Image) object);
		}
		else
			if (object instanceof File)
			{
				CSSHandler.write(path, (String) object);
			}
	}
}
