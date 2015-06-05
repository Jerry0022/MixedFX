package de.mixedfx.network;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.java.CustomSysOutErr;
import de.mixedfx.network.NetworkConfig.States;
import de.mixedfx.network.examples.ExampleUniqueService;

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
		ConnectivityManager.status.addListener((ChangeListener<Status>) (observable, oldValue, newValue) ->
		{
			synchronized (NetworkConfig.status)
			{
				switch (newValue)
				{
					case Offline:
					case Searching:
						if (!oldValue.equals(Status.Offline) && !oldValue.equals(Status.Searching))
						{
							ServiceManager.stop();
						}
						break;
					case Online:
						if (NetworkConfig.status.get().equals(NetworkConfig.States.Server))
						{
							ServiceManager.host();
						}
						else
							if (NetworkConfig.status.get().equals(NetworkConfig.States.BoundToServer))
							{
								ServiceManager.client();
							}
						break;
					default:
						break;
				}
			}
		});

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
		ParticipantManager.PARTICIPANTS.addListener((ListChangeListener<Integer>) c ->
		{
			if (c.getList().size() > 0)
			{
				ConnectivityManager.status.set(Status.Online);
			}
		});
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
		NetworkManager.start();
		ConnectivityManager.status.set(Status.Searching);
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
		CustomSysOutErr.init();

		// Catch fatal errors to show (network reacted already to this error)
		AnnotationProcessor.process(new ConnectivityManager());

		NetworkConfig.status.addListener((ChangeListener<States>) (observable, oldValue, newValue) -> System.out.println("OLD: " + oldValue + "! NEW: " + newValue));

		ParticipantManager.PARTICIPANTS.addListener((ListChangeListener<Integer>) c ->
		{
			System.out.println(ParticipantManager.PARTICIPANTS);
		});

		ConnectivityManager.status.addListener((ChangeListener<Status>) (observable, oldValue, newValue) ->
		{
			System.out.println("CM OLD: " + oldValue + "! NEW: " + newValue);
		});

		// Show all directly found applications host and all directly found Server (Not the
		// bound to
		// server ones) which were once online while this application was online.
		UDPCoordinator.allAdresses.addListener((ListChangeListener<UDPDetected>) c ->
		{
			c.next();
			final UDPDetected detected = c.getAddedSubList().get(0);
			// If another server makes him known, check if it was created before my Server and if so
			// reconnect to it!
			if (NetworkConfig.status.get().equals(NetworkConfig.States.Server) && detected.status.equals(States.Server) && NetworkConfig.statusChangeTime.get().after(detected.statusSince))
			{
				// Force reconnect
				System.out.println("FORCE RECONNECT TO OTHER SERVER!");
				ConnectivityManager.force();
			}
			System.out.println("ALL: " + c.getAddedSubList().get(0).address + "!" + c.getAddedSubList().get(0).status + "!" + c.getAddedSubList().get(0).statusSince);
		});

		ServiceManager.register(new UserManager());
		ServiceManager.register(new ExampleUniqueService());

		ConnectivityManager.force();

		while (true)
		{
			;
		}
	}

	@EventTopicSubscriber(topic = NetworkManager.NETWORK_FATALERROR)
	public void error(final String topic, final Exception exception)
	{
		System.out.println("Caught error: " + exception.getMessage());
	}
}
