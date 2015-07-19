package de.mixedfx.gui;

import java.io.IOException;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.apache.commons.io.FileUtils;

import de.mixedfx.file.DataHandler;
import de.mixedfx.file.FileObject;
import de.mixedfx.java.StringArrayList;
import de.mixedfx.logging.Log;

public class LayoutManager
{
	public final FileObject				layoutDir;

	public StringProperty				currentLayout;

	public final List<LayoutElement<?>>	layoutElements;

	/**
	 * @param layoutDir
	 *            The directory where the layout folders are / shall be created.
	 */
	public LayoutManager(final FileObject layoutDir, final List<LayoutElement<?>> layoutElements, final String defaultLayout)
	{
		this.currentLayout = new SimpleStringProperty(defaultLayout);
		this.layoutDir = layoutDir;
		this.layoutElements = layoutElements;

		if (!layoutDir.toFile().exists())
		{
			try
			{
				FileUtils.forceMkdir(layoutDir.toFile());
			}
			catch (final IOException e)
			{
				Log.assets.error("Layout directory could not have been created! " + layoutDir);
			}
		}

		// Apply default layout
		this.applyLayout(defaultLayout);
	}

	/**
	 * @return Returns a list of all available layouts!
	 */
	public List<String> getList()
	{
		final StringArrayList all = new StringArrayList(DataHandler.getSubFolderList(this.layoutDir));
		if (all.size() == 0)
		{
			all.add("Default");
		}
		return all;
	}

	/**
	 * Applies the given layout. This includes that if layout is not found layout will be created!
	 *
	 * @param layout
	 *            The name of the layout.
	 */
	public void applyLayout(final String layout)
	{
		// If there is no layout with the given name create a new one!
		if (!this.getList().contains(layout))
		{
			try
			{
				DataHandler.createFolder(FileObject.create().setPath(this.layoutDir.getFullPath()).setFullName(layout));
			}
			catch (final IOException e)
			{
				return;
			}
		}

		// Apply layout
		final FileObject currentLayout = this.layoutDir.clone().setPath(DataHandler.fuse(this.layoutDir.getFullPath(), layout));
		for (final LayoutElement<?> le : this.layoutElements)
		{
			le.update(currentLayout);
		}

		this.currentLayout.set(layout);
	}

	/**
	 * Removes the layout also from the hdd! You can't remove the currentLayout.
	 *
	 * @param layout
	 *            The name of the layout which shall be deleted.
	 */
	public void removeLayout(final String layout)
	{
		if (layout.equalsIgnoreCase(this.currentLayout.get()))
		{
			return;
		}
		DataHandler.deleteFile(this.layoutDir.setFullName(layout));
	}
}
