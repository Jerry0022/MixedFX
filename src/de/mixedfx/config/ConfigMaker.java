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
	private final INIConfiguration	config;

	/**
	 * @param filePath
	 * @throws Exception
	 */
	public ConfigMaker(final FileObject filePath) throws Exception
	{
		this(filePath, false);
	}

	/**
	 * @param filePath
	 * @param reset
	 * @throws Exception
	 */
	public ConfigMaker(final FileObject filePath, final boolean reset) throws Exception
	{
		if (reset)
			DataHandler.deleteFile(filePath);

		final FileBasedConfigurationBuilder<INIConfiguration> builder = new Configurations().iniBuilder(DataHandler.createOrFindFile(filePath));
		builder.setAutoSave(true);
		this.config = builder.getConfiguration();
	}

	/**
	 * Read-Only
	 *
	 * @return
	 */
	public ArrayList<String> getSections()
	{
		return new ArrayList<String>(this.config.getSections());
	}

	/**
	 * Read-Only
	 *
	 * @param section
	 * @return
	 */
	public ArrayList<ConfigItem> getKeys(final String section)
	{
		final ArrayList<ConfigItem> result = new ArrayList<ConfigItem>();

		final Iterator<String> iterator = this.config.getKeys(section);
		while (iterator.hasNext())
		{
			final String fullKey = iterator.next();

			final String[] sectionKey = fullKey.split("\\.");
			final ConfigItem item = new ConfigItem(section, sectionKey[1]);
			result.add(this.readIniValue(item));
		}

		return result;
	}

	/**
	 * Read-Only
	 *
	 * @param item
	 * @return
	 */
	public String getValue(final ConfigItem item)
	{
		for (final ConfigItem k : this.getKeys(item.getSection()))
			if (k.equals(item))
				return k.getValue();
		return "";
	}

	/**
	 * Use this function to write! Write-Only
	 *
	 * @param item
	 */
	public ConfigMaker setConfigItem(final ConfigItem item)
	{
		this.config.setProperty(item.getSectionKey(), item.getValue());
		return this;
	}

	/**
	 * Reads the value and fills it into the ConfigItem. Read-Only
	 *
	 * @param item
	 *            Section and Key is used from that item.
	 * @return Returns the item.
	 */
	public ConfigItem readIniValue(final ConfigItem item)
	{
		item.setValue((String) this.config.getProperty(item.getSectionKey()));
		return item;
	}

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
