package de.mixedfx.network;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;

public class DependantList<E> extends SimpleListProperty<E>
{
	public DependantList(final ObservableList<?> mainList)
	{
	}
}
