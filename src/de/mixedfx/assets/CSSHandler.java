package de.mixedfx.assets;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import de.mixedfx.file.DataHandler;
import de.mixedfx.file.FileObject;
import de.mixedfx.logging.Log;

public class CSSHandler
{
	public static final String	prefix		= "style";
	public static final String	extension	= "css";

	/**
	 * Reads a CSS file
	 *
	 * @param fileObject
	 *            The CSS file location
	 * @return Returns the content of the CSS file as String or an empty String if not found
	 */
	public static String read(final FileObject fileObject)
	{
		fileObject.setPrefix(CSSHandler.prefix);
		fileObject.setExtension(CSSHandler.extension);

		try
		{
			final File f = DataHandler.readFile(fileObject);
			return FileUtils.readFileToString(f);
		}
		catch (final IOException e)
		{
			Log.assets.error("Could not read CSS file: " + fileObject.toString());
			return "";
		}
	}

	/**
	 * Writes css as string to file.
	 *
	 * @param fileObject
	 * @param css
	 *            CSS commands as String
	 */
	public static void write(final FileObject fileObject, final String css)
	{
		fileObject.setPrefix(CSSHandler.prefix);
		fileObject.setExtension(CSSHandler.extension);

		try
		{
			DataHandler.deleteFile(fileObject);
			FileUtils.writeStringToFile(fileObject.toFile(), css);
		}
		catch (final IOException e)
		{
			Log.assets.error("Could not write CSS to file: " + fileObject.toString());
		}
	}
}
