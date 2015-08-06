package de.mixedfx.network;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import de.mixedfx.logging.Log;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

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
	public static final int UDP_BROADCAST_INTERVAL = 1000;

	/**
	 * How often I shall wait for the broadcast interval. After that a reconnect is done.
	 */
	public static final int RECONNECT_TOLERANCE = 3;

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
	 * Is set to {@link States#Server} only from outside the {@link TCPCoordinator} to force starting network! Is set to {@link States#Unbound} in the
	 * same way! {@link States#BoundToServer} can only be set from the inner network automatically!
	 */
	protected static ObjectProperty<States> STATUS = new SimpleObjectProperty<>(States.Unbound);

	/**
	 * Is null if no date was published by the service. May need some time to get this value filled if I'm a client!
	 */
	protected static AtomicReference<Date> networkExistsSince = new AtomicReference<>(null);

	static
	{
		STATUS.addListener(new ChangeListener<NetworkConfig.States>()
		{
			@Override
			public void changed(ObservableValue<? extends States> observable, States oldValue, States newValue)
			{
				newValue.stateSince = new Date().getTime();
			}
		});

		NetworkConfig.PORT.addListener((ChangeListener<Number>) (observable, oldValue, newValue) ->
		{
			if (newValue.intValue() + NetworkConfig.TRIES_AMOUNT * NetworkConfig.TRIES_STEPS > Integer.MAX_VALUE || newValue.intValue() <= 0)
			{
				Log.network.error("PORT plus TRIES_AMOUNT * TRIES_STEPS extends minimum / maximum integer value! Change was undone!");
				NetworkConfig.PORT.set(oldValue.intValue());
			}
		});
	}

	public enum States
	{
		Server, BoundToServer, Unbound;

		/**
		 * Updated the every time the status is set active!
		 */
		public long stateSince = new Date().getTime();

		public Date getStateSince()
		{
			return new Date(stateSince);
		}

		public boolean equals(States object)
		{
			if (object == null || !(object instanceof States))
				return false;
			else
				return this.toString().equals(object.toString());
		}
	}
}
