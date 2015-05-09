package de.mixedfx.network;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Overall
{
	public static ObjectProperty<NetworkStatus>	status	= new SimpleObjectProperty<>(NetworkStatus.Unbound);

	public enum NetworkStatus
	{
		Server, BoundToServer, Unbound;
	}
}
