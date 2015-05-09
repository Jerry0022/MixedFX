package de.mixedfx.network;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Overall
{
	public static ObjectProperty<NetworkStatus>	status	= new SimpleObjectProperty<>(NetworkStatus.Server);

	public enum NetworkStatus
	{
		Server, BoundToServer, Unbound;
	}

	public final static int	PORT_UDP	= 8888;
}
