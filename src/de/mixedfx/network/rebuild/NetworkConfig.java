package de.mixedfx.network.rebuild;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import de.mixedfx.logging.Log;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;

public class NetworkConfig
{
	/**
	 * Tries amount for TCP and UDP to switch ports before they throw an (maybe fatal) Error.
	 */
	public static final int TRIES_AMOUNT = 5;

	/**
	 * Tries steps for TCP and UDP to switch ports. Means {@link NetworkConfig#PORT} + thisValue * {@link NetworkConfig#TRIES_AMOUNT}.
	 */
	public static final int TRIES_STEPS = 3;

	/**
	 * {@link UDPOut} broadcast interval in milliseconds.
	 */
	public static final int UDP_BROADCAST_INTERVAL = 3000;

	/**
	 * For which interval in milliseconds one of the TCP connection shall wait until continue sending the next object.
	 */
	public static final int TCP_UNICAST_INTERVAL = 10;

	/**
	 * <p>
	 * Default port is 8888 for TCP and UDP.
	 *
	 * If TCP port is not available (as tcp client or server) it will try automatically other ports ({@link NetworkConfig#TRIES_AMOUNT}).
	 *
	 * If UDP port fails a {@link NetworkManager#NETWORK_FATALERROR} is thrown (can be thrown also if other errors occur).
	 * </p>
	 */
	public static IntegerProperty PORT = new SimpleIntegerProperty(8888);

	/**
	 * Is null if no date was published by the service. May need some time to get this value filled if I'm a client!
	 */
	protected static AtomicReference<Date> networkExistsSince = new AtomicReference<>(null);

	static
	{
		NetworkConfig.PORT.addListener((ChangeListener<Number>) (observable, oldValue, newValue) ->
		{
			if (newValue.intValue() + NetworkConfig.TRIES_AMOUNT * NetworkConfig.TRIES_STEPS > Integer.MAX_VALUE || newValue.intValue() <= 0)
			{
				Log.network.error("PORT plus TRIES_AMOUNT * TRIES_STEPS extends minimum / maximum integer value! Change was undone!");
				NetworkConfig.PORT.set(oldValue.intValue());
			}
		});
	}
}
