package de.mixedfx.config;

import de.mixedfx.file.FileObject;

/**
 * A singleton wrapper of {@link ConfigMaker}.
 * 
 * @author Jerry
 *
 */
public class SettingsManager
{
	public static String		fileName	= "settings";
	private static ConfigMaker	configMaker;

	/**
	 * Initializes the ConfigMaker and writes the initial file.
	 */
	static
	{
		SettingsManager.configMaker = ConfigHandler.read(FileObject.create().setPath(System.getProperty("user.dir")).setName(SettingsManager.fileName));
	}

	/**
	 * Usually should not be used. Only e. g. if you want to write Colors with
	 * ConfigHandler.
	 *
	 * @return Returns the used unique ConfigMaker.
	 */
	public static ConfigMaker getConfigMaker()
	{
		return SettingsManager.configMaker;
	}

	/**
	 * Sets a default value or reads out the written value and returns the maybe
	 * modified correct value. Write and read
	 *
	 * @param section
	 *            The section without brackets. E.g. "General" is in the config
	 *            file "[General]"
	 * @param key
	 *            The identifier of this key-value-pair in the config file.
	 * @param defaultValue
	 *            The default value of this section specific key-value-pair in
	 *            the config file.
	 * @return Returns the value or if something is corrupt or value is simply
	 *         empty it returns the specified defaultValue.
	 */
	public static String getValue(final String section, final String key, final String defaultValue)
	{

		return SettingsManager.configMaker.getValue(section, key, defaultValue);
	}

	/**
	 * Writes the value into the config from now it is usable with the read
	 * methods.
	 *
	 * @param section
	 *            The section without brackets. E.g. "General" is in the config
	 *            file "[General]"
	 * @param key
	 *            The identifier of this key-value-pair in the config file.
	 * @param value
	 *            The value of this section specific key-value-pair in the
	 *            config file.
	 */
	public static void setValue(final String section, final String key, final String value)
	{
		SettingsManager.configMaker.setValue(section, key, value);
	}
}
