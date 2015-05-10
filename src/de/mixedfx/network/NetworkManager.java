package de.mixedfx.network;

import java.net.InetAddress;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;

import org.bushe.swing.event.EventBus;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.network.NetworkConfig.States;
import de.mixedfx.network.messages.Message;

/**
 * <pre>
 * Builds up a network. Manually only host server must be called and {@link Message} received or
 * send. Connection to a host server or someone who is connected to the host server will be
 * established automatically.
 *
 * If an error occur only a {@link NetworkManager#NETWORK_FATALERROR} is sent over the eventBus.
 *
 * If you may want to set the port use {@link NetworkManager#setPort(int)}. (Usually only necessary
 * if there was an fatal error)
 * </pre>
 *
 * @author Jerry
 *
 */
public class NetworkManager
{
	/**
	 * Register for this event over {@link EventBusExtended} or {@link EventBus} if you want to be
	 * informed that there was a network error which closed the entire network. You have to call
	 * again {@link NetworkManager#init()} to initialize the network. Further information: This
	 * error does relate to the UDP Server in most cases (not if {@link NetworkConfig#status} =
	 * {@link NetworkConfig.States#Server}). The cause probably is the Port. React to that error
	 * with {@link NetworkManager#setPort(int)} with the recommendation to choose a number between
	 * 10 000 and 60 000!
	 */
	public static final String		NETWORK_FATALERROR	= "NETWORK_FATALERROR";

	/**
	 * Represents the status. True means the network connection is established with at least one
	 * Participant (as {@link NetworkConfig.States#Server} or
	 * {@link NetworkConfig.States#BoundToServer}). False means that no connection is yet
	 * established. This value changes only, it is not invalidated.
	 */
	public static BooleanProperty	online;

	protected static TCPCoordinator	t;
	protected static UDPCoordinator	u;

	/**
	 * <pre>
	 * Initializes UDP and TCP connection. Start listening and broadcasting on UDP connection and
	 * automatically sets up a TCP connection if {@link NetworkConfig.States#BoundToServer} or if
	 * {@link NetworkConfig.States#Server} application was found.
	 * 
	 * <b>Use</b> {@link MessageBus} to deal with
	 * {@link Message}!
	 * 
	 * <b>Use</b>
	 * {@link NetworkManager#online} [reading] to listen to the NetworkStatus.
	 *
	 * <pre>
	 */
	public static void init()
	{
		NetworkManager.online = new SimpleBooleanProperty(false);
		NetworkConfig.status.addListener((ChangeListener<States>) (observable, oldValue, newValue) ->
		{
			// Do not invalidate online status, just invoke on change!
			if (newValue.equals(States.Unbound))
				if (NetworkManager.online.get())
					NetworkManager.online.set(false);
			if (!newValue.equals(States.Unbound))
				if (!NetworkManager.online.get())
					NetworkManager.online.set(true);
		});

		NetworkManager.t = new TCPCoordinator();
		NetworkManager.u = new UDPCoordinator();
	}

	/**
	 * Default port is {@link NetworkConfig#PORT}. This port is mostly necessary for the UDP Server.
	 * In case of TCP the port + 1 to 5 is asked if port is not available.
	 */
	public static void setPort(final int portNumber)
	{
		NetworkConfig.PORT = portNumber;

		NetworkManager.t.stopTCPFull();
		NetworkManager.u.stopUDPFull();

		NetworkManager.init();
	}

	/**
	 * @return Returns the current port.
	 */
	public static int getPort()
	{
		return NetworkConfig.PORT;
	}

	public static void main(final String[] args)
	{
		NetworkManager.init();

		NetworkManager.u.allAdresses.addListener((ListChangeListener<InetAddress>) c ->
		{
			c.next();
			System.out.println("ALL: " + c.getAddedSubList().get(0));
		});
		NetworkManager.u.allServerAdresses.addListener((ListChangeListener<InetAddress>) c ->
		{
			c.next();
			System.out.println("SERVER: " + c.getAddedSubList().get(0));
		});

		while (true)
			;
	}
}