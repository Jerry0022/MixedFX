package de.mixedfx.gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class Layouting implements ChangeListener<Boolean>
{
	private ChangeListener<Boolean> property;

	public Layouting(ChangeListener<Boolean> property)
	{
		this.property = property;
	}

	@Override
	public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
	{
		this.property.changed(observable, oldValue, newValue);
	}
}
