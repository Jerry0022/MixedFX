package de.mixedfx.network;

import java.util.UUID;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;

import org.apache.logging.log4j.Level;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.inspector.Inspector;
import de.mixedfx.logging.Log;
import de.mixedfx.network.NetworkConfig.States;
import de.mixedfx.network.examples.ExampleUniqueService;
import de.mixedfx.network.examples.ExampleUser;
import de.mixedfx.network.examples.User;
import de.mixedfx.network.examples.UserManager;

public class ConnectivityManager
{
	/**
	 * Read-only.
	 */
	public static ObjectProperty<Status>	status;

	public enum Status
	{
		/**
		 * The user is identified in the network with an PID
		 */
		Online,

		/**
		 * The user connected to a network, but is not yet identified
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
		/*
		 * Set Establishing if connected but not yet registered as participant or searching if not
		 * connected but searching for a network.
		 */
		NetworkConfig.status.addListener((ChangeListener<States>) (observable, oldValue, newValue) ->
		{
			if (newValue.equals(States.Server) || newValue.equals(States.BoundToServer))
			{
				ConnectivityManager.status.set(Status.Establishing);
			}
			else
			{
				ConnectivityManager.status.set(Status.Searching);
			}
		});

		/*
		 * Set ONLINE if I'm registered as particpant
		 */
		ParticipantManager.PARTICIPANTS.addListener((ListChangeListener<Integer>) c ->
		{
			if (c.getList().size() > 0)
			{
				ConnectivityManager.status.set(Status.Online);
				if (ParticipantManager.PARTICIPANTS.get(0).equals(ParticipantManager.MY_PID.get()))
				{
					synchronized (NetworkConfig.status)
					{
						if (NetworkConfig.status.get().equals(States.BoundToServer))
						{
							ServiceManager.client();
						}
					}
				}
			}
		});

		/*
		 * Leave network if host my host is newer than another one and connect to this one.
		 */
		// Show all directly found applications host and all directly found Server (Not the
		// bound to server ones) which were once online while this application was online.
		UDPCoordinator.allAdresses.addListener((ListChangeListener<UDPDetected>) c ->
		{
			c.next();
			final UDPDetected detected = c.getAddedSubList().get(0);
			synchronized (NetworkConfig.status)
			{
				// If another server makes itself known, check if it was created before my Server
				// and if
				// so reconnect to it!
				if (NetworkConfig.status.get().equals(NetworkConfig.States.Server) && detected.status.equals(States.Server) && NetworkConfig.statusChangeTime.get().after(detected.statusSince))
				{
					// Force reconnect
					Log.network.info("Older server detected on " + detected.address.getHostAddress() + " => Force reconnect to this server!");
					Inspector.runNowAsDaemon(() ->
					{
						ConnectivityManager.force();
					});
				}
			}
			Log.network.debug("A new or updated NIC was detected: " + c.getAddedSubList().get(0).address + "!" + c.getAddedSubList().get(0).status + "!" + c.getAddedSubList().get(0).statusSince);
		});
	}

	/**
	 * Enables a searching for a network.
	 */
	public static void on()
	{
		NetworkManager.start();
		ConnectivityManager.status.set(Status.Searching);
	}

	/**
	 * <pre>
	 * Reconnects the network:
	 * If network is shutdown it calls {@link #on()}. - Searching
	 * If network is running and searching it calls {@link NetworkManager#host()}. - Hosting immediately
	 * If network is running and connected it reconnects, calling {@link NetworkManager#leave()} and immediately {@link NetworkManager#host()}. - Hosting immediately.
	 * If network is running and hosting it reconnects, calling {@link NetworkManager#leave()}. - Searching and after some time hosting (time depends on which rang joined in last network).
	 * </pre>
	 */
	public static void force()
	{
		if (!NetworkManager.running)
		{
			ConnectivityManager.on();
		}

		switch (NetworkConfig.status.get())
		{
			case Unbound:
				NetworkManager.host();
				break;
			case BoundToServer:
				NetworkManager.leave();
				NetworkManager.host();
				break;
			case Server:
				NetworkManager.leave();
				break;
		}
	}

	/**
	 * Disable all connections and searching for a network.
	 */
	public static void off()
	{
		NetworkManager.stop();
		ConnectivityManager.status.set(Status.Offline);
	}

	public static void main(final String[] args)
	{
		// Log fatal errors (network reacted already to this error)
		AnnotationProcessor.process(new ConnectivityManager());

		// Log NetworkConfig.status
		NetworkConfig.status.addListener((ChangeListener<States>) (observable, oldValue, newValue) -> Log.network.debug("NetworkConfig status changed from " + oldValue.toString().toUpperCase() + " to " + newValue.toString().toUpperCase()));

		// Log Participants
		ParticipantManager.PARTICIPANTS.addListener((ListChangeListener<Integer>) c ->
		{
			Log.network.info("Participants changed to: " + ParticipantManager.PARTICIPANTS);
		});

		// Log ConnectivityManager.status
		ConnectivityManager.status.addListener((ChangeListener<Status>) (observable, oldValue, newValue) ->
		{
			Log.network.info("ConnectivityManager status changed from " + oldValue.toString().toUpperCase() + " to " + newValue.toString().toUpperCase());
		});

		// Create example user
		final String id = UUID.randomUUID().toString();
		final ExampleUser user = new ExampleUser(id.substring(id.length() - 7, id.length()));

		// Register example services
		final UserManager userManager = new UserManager(user);
		userManager.allUsers.addListener((ListChangeListener<User>) c ->
		{
			Log.network.info("UserManager list changed to: " + userManager.allUsers);
		});
		ServiceManager.register(userManager);
		ServiceManager.register(new ExampleUniqueService());

		// Start network activity and immediately after that start TCP as host.
		ConnectivityManager.force();

		// Don't let the application stop itself!
		while (true)
		{
			;
		}
	}

	@EventTopicSubscriber(topic = NetworkManager.NETWORK_FATALERROR)
	public void error(final String topic, final Exception exception)
	{
		Log.network.catching(Level.ERROR, exception);
	}
}
