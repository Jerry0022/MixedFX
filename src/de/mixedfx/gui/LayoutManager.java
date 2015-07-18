package de.mixedfx.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import de.mixedfx.file.DataHandler;
import de.mixedfx.file.FileObject;
import de.mixedfx.logging.Log;

public class LayoutManager
{
	public final FileObject				layoutDir;

	public final List<LayoutElement<?>>	layoutElements;

	/**
	 * @param layoutDir
	 *            The directory where the layout folders are / shall be created.
	 */
	public LayoutManager(final FileObject layoutDir, final List<LayoutElement<?>> layoutElements, final String defaultLayout)
	{
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
		this.layoutDir = layoutDir;
		this.layoutElements = layoutElements;

		// Apply default layout
		this.applyLayout(defaultLayout);
	}

	/**
	 * @return Returns a list of all available layouts!
	 */
	public List<String> getList()
	{
		final ArrayList<String> all = DataHandler.getSubFolderList(this.layoutDir);
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
	}

	public void removeLayout(final String layout)
	{
		DataHandler.deleteFile(this.layoutDir.setFullName(layout));
	}
}
