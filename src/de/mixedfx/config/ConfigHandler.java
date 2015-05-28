package de.mixedfx.config;

import javafx.scene.paint.Color;
import de.mixedfx.file.FileObject;
import de.mixedfx.gui.ColorConverter;

public class ConfigHandler
{
	public static final String	prefix		= "config";
	public static final String	extension	= "ini";

	/**
	 * <pre>
	 * Finds (= reads) or creates a new empty config (= 0 sections)!
	 * Applies automatically the {@link ConfigHandler#prefix}.
	 * Modifications are written to the file automatically (= no writeConfig)!
	 * </pre>
	 *
	 * @param fileObject
	 * @return Returns the ConfigMaker! Or throws serious ErrorExtended and returns null!
	 */
	public static ConfigMaker read(final FileObject fileObject)
	{
		fileObject.setPrefix(ConfigHandler.prefix);
		fileObject.setExtension(ConfigHandler.extension);

		try
		{
			final ConfigMaker configMaker = new ConfigMaker(fileObject);
			return configMaker;
		}
		catch (final Exception e)
		{
			// TODO Throw ErrorExtended
			// = Serious => Exit program
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param configMaker
	 * @param configItem
	 *            Section and key must be filled out, the value will be overwritten with the color
	 *            parameter.
	 * @param color
	 */
	public static void writeColor(final ConfigMaker configMaker, final ConfigItem configItem, final Color color)
	{
		configMaker.setConfigItem(configItem.setValue(ColorConverter.toRGBA(color)));
	}

	/**
	 * @param configMaker
	 * @param configItem
	 *            Section and key must be filled out
	 * @param defaultColor
	 * @return Returns the JavaFX Color
	 */
	public static Color readColor(final ConfigMaker configMaker, final ConfigItem configItem, final Color defaultColor)
	{
		Color c;
		try
		{
			c = ColorConverter.fromRGBA(configMaker.getValue(configItem));
		}
		catch (final Exception e)
		{
			c = defaultColor;
		}
		return c;
	}
}
