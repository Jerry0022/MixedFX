package de.mixedfx.network.relaunch;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class ConnectivityManager
{
	public static ObjectProperty<Status>	status;

	public enum Status
	{
		/**
		 * This means the user is identified in the network with an PID
		 */
		Online,

		/**
		 * This means the user connected to a network, but is not yet identified
		 */
		Establishing,

		/**
		 * The user is not connected to a network, but still searching for one
		 */
		Searching,

		/**
		 * The user is not connected to a network and not searching
		 */
		Offline;
	}

	static
	{
		ConnectivityManager.status = new SimpleObjectProperty<>(Status.Offline);
		// TODO Listening to UDPCoordinator#allAddresses. If an entry was replaced or new and it is
		// a Server read out hostedTime and if older than mine call leave() if this one is not
		// offline (enabling to reconnect to other server).
		// TODO Add time of invalidating state to UDP messages.
	}

	/**
	 * Enables a searching for a network.
	 */
	public static void on()
	{

	}

	/**
	 * <pre>
	 * Reconnects the network:
	 * If network is shutdown it calls {@link #on()}. => Searching
	 * If network is running and searching it calls {@link NetworkManager#host()}. => Hosting immediately
	 * If network is running and connected it reconnects, calling {@link NetworkManager#leave()} and immediately {@link NetworkManager#host()}. => Hosting immediately.
	 * If network is running and hosting it reconnects, calling {@link NetworkManager#leave()}. => Searching and after some time hosting (time depends on which rang joined in last network).
	 * </pre>
	 */
	public static void force()
	{

	}

	/**
	 * Disable all connections and searching for a network.
	 */
	public static void off()
	{

	}
}
