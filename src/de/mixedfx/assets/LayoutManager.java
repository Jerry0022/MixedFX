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
import de.mixedfx.gui.EasyModifierConfig;
import de.mixedfx.gui.RegionManipulator;
import de.mixedfx.java.StringArrayList;
import de.mixedfx.logging.Log;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

public class LayoutManager
{
	/**
	 * The amount of (dynamic) images maximal cached for this application!
	 */
	public static final int IMAGE_CACHE_SIZE = 10;

	/**
	 * If there is a file of this name (case is ignored), e. g. style.css, this file is loaded as .css file!
	 */
	public static final String STYLE_FILE_NAME = "style";

	public static String defaultLayoutName = "Layout1";

	public final LoadingCache<String, Image> imageCache;

	public final FileObject standardLayoutDir;

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
		this.standardLayoutDir = FileObject.create().setPath(DataHandler.fuse(layoutDir.getFullPath(), "Standard"));
		this.root = root;
		this.imageCache = CacheBuilder.newBuilder().maximumSize(LayoutManager.IMAGE_CACHE_SIZE).build(new CacheLoader<String, Image>()
		{
			@Override
			public Image load(final String key) throws Exception
			{
				return LayoutManager.this.readImage(FileObject.create().setPath(DataHandler.fuse(LayoutManager.this.layoutDir.getFullPath(), LayoutManager.this.currentLayout.get())).setFullName(key));
			}
		});

		// Create Layout directory if it doesn't exist
		if (!this.layoutDir.toFile().exists())
		{
			try
			{
				FileUtils.forceMkdir(this.layoutDir.toFile());
			} catch (final IOException e)
			{
				Log.assets.fatal("Can't create layout directory! " + layoutDir);
			}
		}

		// Create default layout directory
		if (!this.standardLayoutDir.toFile().exists())
		{
			try
			{
				FileUtils.forceMkdir(this.standardLayoutDir.toFile());
			} catch (final IOException e)
			{
				Log.assets.fatal("Can't create pre use directory! " + layoutDir);
			}
		}

		// Apply default layout
		if (this.root != null)
			this.applyLayout(this.currentLayout.get());
	}

	/**
	 * Applies the given layout. This includes that if layout is not found layout will be created! If this is used in combination with {@link Layouter} a new created layout will be a clone on the hdd
	 * of the current layout if a layout was previously applied!
	 *
	 * @param layout
	 *            The name of the layout. If null a layout with the name {@link LayoutManager#defaultLayoutName} is created!
	 */
	public void applyLayout(String layout)
	{
		if (!Platform.isFxApplicationThread())
			throw new IllegalStateException("Must run from FX Thread!");
		if (this.root == null)
			throw new IllegalArgumentException("The root must be set! Or please use Layouter!");

		if (layout == null)
			layout = LayoutManager.defaultLayoutName;

		// If there is no layout with the given name create a new one!
		final FileObject layoutFullPath = FileObject.create().setPath(this.layoutDir.getFullPath()).setFullName(layout);
		if (!layoutFullPath.toFile().exists())
		{
			try
			{
				DataHandler.createFolder(layoutFullPath);
			} catch (final IOException e)
			{
				Log.assets.error("Could not create layout! " + layoutFullPath);
			}
		}

		// Apply layout
		final Collection<File> files = DataHandler.listFiles(layoutFullPath);
		// Add files of standard folder if they aren't already in the specific layout folder
		DataHandler.listFiles(this.standardLayoutDir).stream().filter(t -> !files.stream().anyMatch(u -> FileObject.create(t).getName().equalsIgnoreCase(FileObject.create(u).getName()))).forEach(s -> files.add(s));
		for (final File file : files)
		{
			if (FileObject.create(file).getName().equalsIgnoreCase(LayoutManager.STYLE_FILE_NAME))
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
					RegionManipulator.bindBackground((Region) node, this.readImage(fileObject));
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
	public Image readDynamicImage(final String name)
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

	/**
	 * Reads an image either from the layout specific path or if it doesn't exist from the standard path.
	 *
	 * @param usualImage
	 *            The layout specific file as FileObject.
	 * @return Returns the layout specifc image or a standard image or a tranparent image.
	 */
	private Image readImage(final FileObject usualImage)
	{
		Image image;
		if ((image = MasterHandler.read(usualImage, Image.class)).equals(ImageProducer.getTransparent()))
			image = MasterHandler.read(LayoutManager.this.standardLayoutDir.setFullName(usualImage.getFullName()), Image.class);
		return image;
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
	 * @return Returns true if successful or false if not. Also false if the layout is the only and current one (if standard one is not there).
	 */

	public boolean removeLayout(final String layout)
	{
		if (layout.equalsIgnoreCase(this.currentLayout.get()))
		{
			if (this.getList().size() > 1)
				this.applyLayout(this.getList().get(this.getList().indexOf(layout) == 0 ? 1 : 0));
			else
			{
				Log.assets.warn("Can't remove current layout " + layout + " because there is no other one!");
				return false;
			}
		}
		final boolean result = DataHandler.deleteFile(this.layoutDir.setFullName(layout));
		Log.assets.trace("Layout " + layout + " removed " + (result ? "succesfully" : "unsuccessfully") + "!");
		return result;
	}

	/**
	 * @param oldName
	 *            Old name of the layout (folder).
	 * @param newName
	 *            New name of the layout (folder).
	 * @return Returns true if success or false if no success, e. g. a layout with this name already exists
	 */
	public boolean renameLayout(final String oldName, final String newName)
	{
		final File oldFolder = FileObject.create().setPath(DataHandler.fuse(this.layoutDir.getFullPath(), oldName)).toFile();
		final File newFolder = FileObject.create().setPath(DataHandler.fuse(this.layoutDir.getFullPath(), newName)).toFile();
		if (this.currentLayout.get().equalsIgnoreCase(oldName))
			this.currentLayout.set(newName);
		final boolean result = oldFolder.renameTo(newFolder);
		Log.assets.trace("Layout " + oldName + " renamed to " + newName + " " + (result ? "succesfully" : "unsuccessfully") + "!");
		return result;
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
