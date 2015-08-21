package de.mixedfx.assets;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import de.mixedfx.file.DataHandler;
import de.mixedfx.file.FileObject;
import de.mixedfx.gui.EasyModifier;
import de.mixedfx.gui.EasyModifierConfig;
import de.mixedfx.gui.RegionManipulator;
import de.mixedfx.java.StringArrayList;
import de.mixedfx.logging.Log;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

public class LayoutManager
{
	/**
	 * The amount of (dynamic) images maximal cached for this application!
	 */
	public static final int cacheSize = 10;

	/**
	 * If there is a file of this name (case is ignored), e. g. style.css, this file is loaded as .css file!
	 */
	public static final String styleFileName = "style";

	public final LoadingCache<String, Image> imageCache;

	public final FileObject layoutDir;

	public StringProperty currentLayout;

	public Node root;

	public String layoutableClass;

	/**
	 * First found Layout will be used!
	 *
	 * @param layoutDir
	 *            The directory which will contain the layout folders.
	 */
	public LayoutManager(final FileObject layoutDir)
	{
		this(layoutDir, null);
	}

	/**
	 * @param layoutDir
	 *            The directory which will contain the layout folders.
	 */
	public LayoutManager(final FileObject layoutDir, final String defaultLayout)
	{
		this(null, layoutDir, defaultLayout);
	}

	/**
	 * First found Layout will be used!
	 *
	 * @param layoutDir
	 *            The directory which will contain the layout folders.
	 */
	public LayoutManager(final Node root, final FileObject layoutDir)
	{
		this(root, layoutDir, null);
	}

	/**
	 * @param layoutDir
	 *            The directory which will contain the layout folders.
	 */
	public LayoutManager(final Node root, final FileObject layoutDir, final String defaultLayout)
	{
		if (!Platform.isFxApplicationThread())
			throw new IllegalStateException("Must run from FX Thread!");
		if ((root != null) && (root.getScene() == null))
			throw new IllegalStateException("The root must be part of the scene once you create a LayoutManager.");

		this.currentLayout = new SimpleStringProperty(defaultLayout);
		this.layoutDir = layoutDir;
		this.root = root;
		this.imageCache = CacheBuilder.newBuilder().maximumSize(LayoutManager.cacheSize).build(new CacheLoader<String, Image>()
		{
			@Override
			public Image load(final String key) throws Exception
			{
				return ImageHandler.readImage(FileObject.create().setPath(DataHandler.fuse(LayoutManager.this.layoutDir.getFullPath(), LayoutManager.this.currentLayout.get())).setFullName(key));
			}
		});

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
		if (this.root != null)
			this.applyLayout(defaultLayout);
	}

	/**
	 * Applies the given layout. This includes that if layout is not found layout will be created! If this is used in combination with {@link Layouter} a new created layout will be a clone on the hdd
	 * of the current layout if a layout was previously applied!
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
		final FileObject layoutFullPath = FileObject.create().setPath(this.layoutDir.getFullPath()).setFullName(layout);
		if (!layoutFullPath.toFile().exists())
		{
			try
			{
				DataHandler.createFolder(layoutFullPath);
				if ((this.layoutableClass != null) && (this.root instanceof Parent))
					EasyModifier.runOnAllSubNodes((Parent) this.root, this.layoutableClass, true, (parent, doIt) ->
					{
						if (parent instanceof Region)
						{
							final Region region = (Region) parent;
							final Image image = region.getBackground().getImages().get(0).getImage();
							if (image != null)
								LayoutManager.this.saveElement(parent.getId(), image);
						}
					});
				return;
			} catch (final IOException e)
			{
				Log.assets.error("Could not create layout! " + layoutFullPath);
				return;
			}
		}

		// Apply layout
		final Collection<File> files = DataHandler.listFiles(layoutFullPath);
		for (final File file : files)
		{
			if (FileObject.create(file).getName().equalsIgnoreCase("style"))
				try
				{
					this.root.getScene().getStylesheets().add(file.toURI().toURL().toExternalForm());
					Log.assets.trace("Loaded layout stylesheet!");
				} catch (final MalformedURLException e)
				{
				}
			else
			{
				final FileObject fileObject = FileObject.create(file);
				final String id = fileObject.getName();
				final Node node = this.root.lookup("#" + id);
				if (node instanceof Region)
					RegionManipulator.bindBackground((Region) node, MasterHandler.read(fileObject, Image.class));
				else if (node != null) // If node is null the image is a dynamic image!
					Log.assets.warn("The node " + node + " for the id " + id + " is not a Region. Only Regions are supported for layouting!");
			}
		}

		this.currentLayout.set(layout);

	}

	/**
	 * @return Returns a list of all available layouts!
	 */
	public List<String> getList()
	{
		final StringArrayList all = new StringArrayList(DataHandler.getSubFolderList(this.layoutDir));
		if (all.size() == 0)
		{
			this.applyLayout(null);
		}
		return all;
	}

	/**
	 * Reads an image dynamically!
	 *
	 * @return Returns an image which is now loaded from the disk.
	 */
	public Image readImage(final String name)
	{
		try
		{
			return this.imageCache.get(name);
		} catch (final ExecutionException e)
		{
			Log.assets.error("Exception shown while reading image from cache or disk!", e);
			return ImageProducer.getMonoColored(Color.RED);
		}
	}

	protected void register(final EasyModifierConfig config)
	{
		this.layoutableClass = config.staticClass;
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
			if (this.getList().size() > 1)
				this.applyLayout(this.getList().get(this.getList().indexOf(layout) == 0 ? 1 : 0));
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
	protected synchronized void saveElement(final String id, final Image image)
	{
		if (id.matches("^[A-Z0-9]+$"))
			throw new IllegalArgumentException("ID may contain only letters and numbers!");
		MasterHandler.write(FileObject.create().setPath(DataHandler.fuse(this.layoutDir.getFullPath(), this.currentLayout.get())).setName(id), image);
	}
}
