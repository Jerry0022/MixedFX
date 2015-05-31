package de.mixedfx._network;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

class NetworkConfig
{
	/**
	 * <p>
	 * Default port is 8888 for TCP and UDP.
	 *
	 * If TCP port is not available (as tcp client or server) it will try automatically other ports
	 * ({@link TCPCoordinator#PORT_TRIES}).
	 *
	 * If UDP port fails a {@link NetworkManager#NETWORK_FATALERROR} is thrown (can be thrown also
	 * if other errors occur).
	 * </p>
	 */
	public static int						PORT	= 8888;

	public static ObjectProperty<States>	status	= new SimpleObjectProperty<>(States.Unbound);

	public enum States
	{
		Server, BoundToServer, Unbound, ServerGoOff;
	}

}
