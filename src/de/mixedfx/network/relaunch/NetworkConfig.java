package de.mixedfx.network.relaunch;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

class NetworkConfig
{
	/**
	 * Tries amount for TCP and UDP to switch ports before they throw an (maybe fatal) Error.
	 */
	public static final int					TRIES_AMOUNT		= 5;

	/**
	 * Tries steps for TCP and UDP to switch ports. Means {@link NetworkConfig#PORT} + thisValue *
	 * {@link NetworkConfig#TRIES_AMOUNT}.
	 */
	public static final int					TRIES_STEPS			= 3;

	/**
	 * {@link UDPOut} broadcast interval.
	 */
	public static final int					BROADCAST_INTERVAL	= 1000;

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
	public static IntegerProperty			PORT				= new SimpleIntegerProperty(8888);

	protected static ObjectProperty<States>	status				= new SimpleObjectProperty<>(States.Unbound);

	static
	{
		NetworkConfig.PORT.addListener((ChangeListener<Number>) (observable, oldValue, newValue) ->
		{
			if (newValue.intValue() + NetworkConfig.TRIES_AMOUNT * NetworkConfig.TRIES_STEPS > Integer.MAX_VALUE)
			{
				System.out.println("PORT plus TRIES_AMOUNT * TRIES_STEPS extends maximum integer value! Change was undone!");
				NetworkConfig.PORT.set(oldValue.intValue());
			}
		});
	}

	public enum States
	{
		Server, BoundToServer, Unbound;
	}
}
