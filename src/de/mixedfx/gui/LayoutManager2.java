package de.mixedfx.gui;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;

import de.mixedfx.assets.MasterHandler;
import de.mixedfx.file.DataHandler;
import de.mixedfx.file.FileObject;
import de.mixedfx.java.StringArrayList;
import de.mixedfx.logging.Log;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;

public class LayoutManager2
{
	public final FileObject layoutDir;

	public StringProperty currentLayout;

	public Node root;

	/**
	 * First found Layout will be used!
	 * 
	 * @param layoutDir
	 *            The directory which will contain the layout folders.
	 */
	public LayoutManager2(final FileObject layoutDir)
	{
		this(layoutDir, null);
	}

	/**
	 * @param layoutDir
	 *            The directory which will contain the layout folders.
	 */
	public LayoutManager2(final FileObject layoutDir, final String defaultLayout)
	{
		this(null, layoutDir, defaultLayout);
	}

	/**
	 * First found Layout will be used!
	 * 
	 * @param layoutDir
	 *            The directory which will contain the layout folders.
	 */
	public LayoutManager2(Node root, final FileObject layoutDir)
	{
		this(root, layoutDir, null);
	}

	/**
	 * @param layoutDir
	 *            The directory which will contain the layout folders.
	 */
	public LayoutManager2(Node root, final FileObject layoutDir, final String defaultLayout)
	{
		if (!Platform.isFxApplicationThread())
			throw new IllegalStateException("Must run from FX Thread!");

		this.currentLayout = new SimpleStringProperty(defaultLayout);
		this.layoutDir = layoutDir;
		this.root = root;

		if (!this.layoutDir.toFile().exists())
		{
			try
			{
				FileUtils.forceMkdir(layoutDir.toFile());
			} catch (final IOException e)
			{
				Log.assets.fatal("Can't create layout directory! " + layoutDir);
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
	 *            The name of the layout. If null a layout with the name Default is created!
	 */
	public void applyLayout(String layout)
	{
		if (!Platform.isFxApplicationThread())
			throw new IllegalStateException("Must run from FX Thread!");
		if (this.root == null)
			throw new IllegalArgumentException("The root must be set! Or please use Layouter!");

		if (layout == null)
			layout = "Default";

		// If there is no layout with the given name create a new one!
		FileObject layoutFullPath = FileObject.create().setPath(this.layoutDir.getFullPath()).setFullName(layout);
		if (!layoutFullPath.toFile().exists())

		{
			try
			{
				DataHandler.createFolder(layoutFullPath);
			} catch (final IOException e)
			{
				Log.assets.error("Could not create layout! " + layoutFullPath);
				return;
			}
		}

		// Apply layout
		Collection<File> files = DataHandler.listFiles(layoutFullPath);
		for (File file : files)
		{
			FileObject fileObject = FileObject.create(file);
			String id = fileObject.getName();
			Node node = root.lookup("#" + id);
			if (node instanceof Region)
				RegionManipulator.bindBackground((Region) node, MasterHandler.read(fileObject, Image.class));
			else
				Log.assets.warn("The node " + node + " is not a Region. Only Regions are supported for layouting!");
		}

		this.currentLayout.set(layout);

	}

	/**
	 * Removes the layout also from the hdd! Trying to remove the currentLayout means that the first layout from the list (alphabetically) is applied! You can't remove the currentLayout if there is no
	 * other layout!
	 *
	 * @param layout
	 *            The name of the layout which shall be deleted.
	 */
	public void removeLayout(final String layout)
	{
		if (layout.equalsIgnoreCase(this.currentLayout.get()))
		{
			if (getList().size() > 1)
				applyLayout(getList().get(getList().indexOf(layout) == 0 ? 1 : 0));
			else
			{
				Log.assets.warn("Can't remove current layout " + layout + " because there is no other one!");
				return;
			}
		}
		DataHandler.deleteFile(this.layoutDir.setFullName(layout));
	}

	/**
	 * @param id
	 *            An id containing only letters and numbers.
	 * @param image
	 *            The image to write to the current layout.
	 */
	protected synchronized void saveElement(String id, Image image)
	{
		if (id.matches("^[A-Z0-9]+$"))
			throw new IllegalArgumentException("ID may contain only letters and numbers!");
		MasterHandler.write(FileObject.create().setPath(DataHandler.fuse(this.layoutDir.getFullPath(), this.currentLayout.get())).setName(id), image);
	}
}
