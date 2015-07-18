package de.mixedfx.gui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import de.mixedfx.assets.MasterHandler;
import de.mixedfx.file.FileObject;

public class LayoutElement<T> implements ChangeListener<T>
{
	public final String				name;
	public final ObjectProperty<T>	object;

	private final Class<?>			type;
	private FileObject				lastLayoutPath;

	public LayoutElement(final String name, final Class<?> type)
	{
		this.name = name;
		this.type = type;
		this.object = new SimpleObjectProperty<>();
		this.object.addListener(this);
	}

	@SuppressWarnings("unchecked")
	public void update(final FileObject path)
	{
		this.object.removeListener(this);
		this.object.set((T) MasterHandler.read((this.lastLayoutPath = path).clone().setName(this.name), this.type));
		this.object.addListener(this);
	}

	@Override
	public void changed(final ObservableValue<? extends T> observable, final T oldValue, final T newValue)
	{
		MasterHandler.write(this.lastLayoutPath.clone().setName(this.name), newValue);
	}
}
