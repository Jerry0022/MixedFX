package de.mixedfx.config;

import de.mixedfx.file.FileObject;

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
	 * Usually should not be used. Only e. g. if you want to write Colors with ConfigHandler.
	 *
	 * @return
	 */
	public static ConfigMaker getConfigMaker()
	{
		return SettingsManager.configMaker;
	}

	/**
	 * Sets a default value or reads out the written value and returns the maybe modified correct
	 * value. Write and read
	 *
	 * @param section
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String getValue(final String section, final String key, final String defaultValue)
	{
		final ConfigItem item = new ConfigItem(section, key);

		if (SettingsManager.configMaker.getValue(item).equals(""))
			SettingsManager.configMaker.setConfigItem(item.setValue(defaultValue));
		else
			SettingsManager.configMaker.setConfigItem(item.setValue(SettingsManager.configMaker.getValue(item)));

		return item.getValue();
	}

	/**
	 * Writes the value into the config from now it is usable with the read methods.
	 *
	 * @param section
	 * @param key
	 * @param value
	 */
	public static void setValue(final String section, final String key, final String value)
	{
		SettingsManager.configMaker.setConfigItem(new ConfigItem(section, key, value));
	}
}
