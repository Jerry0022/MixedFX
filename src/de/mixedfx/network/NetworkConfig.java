package de.mixedfx.network;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

class NetworkConfig
{
	/**
	 * Default port is 8888 for TCP and UDP.
	 */
	public static int						PORT	= 8888;

	public static ObjectProperty<States>	status	= new SimpleObjectProperty<>(States.Unbound);

	public enum States
	{
		Server, BoundToServer, Unbound;
	}

}
