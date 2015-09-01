package de.mixedfx.config;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;

import de.mixedfx.file.DataHandler;
import de.mixedfx.file.FileObject;

public class ConfigMaker
{
	private final INIConfiguration config;

	/**
	 * @param filePath
	 *            The path plus file where the ini file shall be placed.
	 * @throws Exception
	 *             If something goes wrong, e. g. file couldn't be created.
	 */
	public ConfigMaker(final FileObject filePath) throws Exception
	{
		this(filePath, false);
	}

	/**
	 * @param filePath
	 *            The path plus file where the ini file shall be placed.
	 * @param reset
	 *            Reset means that the file is removed before (if it already
	 *            exists).
	 * @throws Exception
	 *             If something goes wrong, e. g. file couldn't be created.
	 */
	public ConfigMaker(final FileObject filePath, final boolean reset) throws Exception
	{
		if (reset)
			DataHandler.deleteFile(filePath);

		final FileBasedConfigurationBuilder<INIConfiguration> builder = new Configurations().iniBuilder(DataHandler.createOrFindFile(filePath));
		builder.setAutoSave(true);
		this.config = builder.getConfiguration();
	}

	/*
	 * CONVENIENCE METHODS
	 */

	/**
	 * Writes the default value or if available reads out the value. Write and
	 * read
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
	public String getValue(final String section, final String key, final String defaultValue)
	{
		final ConfigItem item = new ConfigItem(section, key);

		if (readValue(item).equals(""))
			writeConfigItem(item.setValue(defaultValue));
		else
			writeConfigItem(item.setValue(readValue(item)));

		return item.getValue();
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
	public void setValue(final String section, final String key, final String value)
	{
		writeConfigItem(new ConfigItem(section, key, value));
	}

	/*
	 * WRITE-ONLY METHODS
	 */

	/**
	 * Use this function to write! Write-Only
	 *
	 * @param item
	 */
	public ConfigMaker writeConfigItem(final ConfigItem item)
	{
		this.config.setProperty(item.getSectionKey(), item.getValue());
		return this;
	}

	/*
	 * READ-ONLY METHODS
	 */

	/**
	 * Read-Only
	 *
	 * @return Returns an ArrayList of all sections.
	 */
	public ArrayList<String> readSections()
	{
		return new ArrayList<String>(this.config.getSections());
	}

	/**
	 * Read-Only
	 *
	 * @param section
	 *            The section which shall be read out.
	 * @return Returns an Arraylist of all keys (and values and section).
	 */
	public ArrayList<ConfigItem> readKeys(final String section)
	{
		final ArrayList<ConfigItem> result = new ArrayList<ConfigItem>();

		final Iterator<String> iterator = this.config.getKeys(section);
		while (iterator.hasNext())
		{
			final String fullKey = iterator.next();

			final String[] sectionKey = fullKey.split("\\.");
			final ConfigItem item = new ConfigItem(section, sectionKey[1]);
			result.add(this.updateValue(item));
		}

		return result;
	}

	/**
	 * Reads the current value of a ConfigItem. Read-Only
	 *
	 * @param item
	 *            The item which shall be read out.
	 * @return Returns a single value or the - maybe empty - value if key was
	 *         not found.
	 */
	public String readValue(final ConfigItem item)
	{
		for (final ConfigItem k : this.readKeys(item.getSection()))
			if (k.equals(item))
				return k.getValue();
		return item.getValue();
	}

	/**
	 * Reads the value and adds it to the ConfigItem. Read-Only
	 *
	 * @param item
	 *            Section and Key is used from that item.
	 * @return Returns the item.
	 */
	public ConfigItem updateValue(final ConfigItem item)
	{
		item.setValue((String) this.config.getProperty(item.getSectionKey()));
		return item;
	}

	/*
	 * REMOVAL METHODS!
	 */

	/**
	 * Write-Only
	 */
	public ConfigMaker clearAll()
	{
		this.config.clear();
		return this;
	}

	/**
	 * Write-Only
	 *
	 * @param section
	 */
	public ConfigMaker clearSection(final String section)
	{
		this.config.clearTree(section);
		return this;
	}

	/**
	 * Write-Only
	 *
	 * @param item
	 */
	public ConfigMaker clearValue(final ConfigItem item)
	{
		item.setValue(null);
		this.config.clearProperty(item.getSectionKey());
		return this;
	}
}
