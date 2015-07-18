package de.mixedfx.gui;

import java.io.IOException;
import java.util.List;

import de.mixedfx.file.DataHandler;
import de.mixedfx.file.FileObject;

public class LayoutManager
{
	public final FileObject				layoutDir;

	public final List<LayoutElement<?>>	layoutElements;

	/**
	 * @param layoutDir
	 *            The directory where the layout folders are / shall be created.
	 */
	public LayoutManager(final FileObject layoutDir, final List<LayoutElement<?>> layoutElements)
	{
		this.layoutDir = layoutDir;
		this.layoutElements = layoutElements;

		this.applyLayout(this.getList().get(0));
	}

	/**
	 * @return Returns a list of all available layouts!
	 */
	public List<String> getList()
	{
		return DataHandler.getSubFolderList(this.layoutDir);
	}

	/**
	 * Applies the given layout. This includes that If layout is not found layout will be created!
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
}
