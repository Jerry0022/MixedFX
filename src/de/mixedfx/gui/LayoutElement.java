package de.mixedfx.gui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import de.mixedfx.assets.MasterHandler;
import de.mixedfx.file.FileObject;

public class LayoutElement<T>
{
	public final String				name;
	public final ObjectProperty<T>	object;

	private final Class<?>			type;

	public LayoutElement(final String name, final Class<?> type)
	{
		this.name = name;
		this.object = new SimpleObjectProperty<>();
		this.type = type;
	}

	@SuppressWarnings("unchecked")
	public void update(final FileObject path)
	{
		final FileObject fullPath = path.clone().setName(this.name);

		this.object.set((T) MasterHandler.read(fullPath, this.type));
	}
}
