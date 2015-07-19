package de.mixedfx.gui;

import java.io.Serializable;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import de.mixedfx.assets.MasterHandler;
import de.mixedfx.file.FileObject;

/**
 * A ObjectProperty representing a GUI element. If you set this property the change is immediately
 * written to the disk in the {@link LayoutManager#currentLayout}. If you change a layout this
 * property's content is immediately changed according to the new layout!
 *
 * @param <T>
 *            {@link Image} (may be an transparent image), {@link String} (may be an empty String)
 *            or another {@link Serializable} (value may be null)
 *
 * @author Jerry
 */
public class LayoutElement<T> extends SimpleObjectProperty<T> implements ChangeListener<T>
{
	public final String		name;

	private final Class<?>	type;
	private FileObject		lastLayoutPath;

	/**
	 * @param name
	 *            A (for this type of content unique) name for saving it to the disk.
	 * @param type
	 *            must be the class of the generic type of {@link LayoutElement}
	 */
	public LayoutElement(final String name, final Class<?> type)
	{
		this.name = name;
		this.type = type;
		this.addListener(this);
	}

	@SuppressWarnings("unchecked")
	protected void update(final FileObject path)
	{
		this.removeListener(this);
		this.set((T) MasterHandler.read((this.lastLayoutPath = path).clone().setName(this.name), this.type));
		this.addListener(this);
	}

	@Override
	public void changed(final ObservableValue<? extends T> observable, final T oldValue, final T newValue)
	{
		MasterHandler.write(this.lastLayoutPath.clone().setName(this.name), newValue);
	}
}
