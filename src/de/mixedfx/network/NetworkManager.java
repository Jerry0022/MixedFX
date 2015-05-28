package de.mixedfx.network;

import java.io.IOException;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;

import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.inspector.Inspector;
import de.mixedfx.java.CustomSysOutErr;
import de.mixedfx.network.NetworkConfig.States;
import de.mixedfx.network.messages.Message;
import de.mixedfx.network.messages.RegisteredMessage;

// TODO Step-by-step guide, TODO SyncedManager
/**
 * <p>
 * Builds up a network. You can <b>receive or send (a child of) {@link RegisteredMessage} via
 * {@link MessageBus}</b>. Connection to a host server or someone who is connected to the host
 * server will be established automatically. <br>
 * To get an information about the <b>network status or if you want to host get
 * {@link NetworkManager#online} or set it manually to
 * {@link NetworkManager.OnlineStates#Established} or {@link NetworkManager.OnlineStates#Offline}
 * </b><br>
 * <b>{@link ParticipantManager#PARTICIPANTS}</b> shows at any time <b>all connected and registered
 * participants</b> (including this application) if online. {@link ParticipantManager#MY_PID} is the
 * applications participant id (hosts id is 1) if online, otherwise it is 0<br>
 * If an error occur only a <b>{@link NetworkManager#NETWORK_FATALERROR}</b> is sent over the
 * EventBus[Extended]. <br>
 * If you may want to set the port use {@link NetworkManager#setPort(int)} (after setting the port
 * the entire network is restarted). (Usually only necessary if there was an fatal error!)
 * </p>
 *
 * @author Jerry
 *
 */
public class NetworkManager
{
	public enum OnlineStates
	{
		/**
		 * Online means that the full connection is established and you are registered as
		 * participant in the half meshed network.
		 */
		Online(3),

		/**
		 * Host was found and full connection is established. But you are not already registered as
		 * participant of the half meshed network.
		 */
		Established(1),

		/**
		 * Offline means no host was found (UDP connection is running, if there was no
		 * {@link NetworkManager#NETWORK_FATALERROR}). Set {@link NetworkManager#online} to
		 * {@link NetworkManager.OnlineStates#Established} to host.
		 */
		Offline(0);

		private final int	numVal;

		OnlineStates(final int numVal)
		{
			this.numVal = numVal;
		}

		public int getNumVal()
		{
			return this.numVal;
		}
	}

	/**
	 * Register for this event over {@link EventBusExtended} or {@link EventBus} if you want to be
	 * informed that there was a network error which closed the entire network. Not recommended: You
	 * may call again {@link NetworkManager#init()} to initialize the network. Further information:
	 * This error does <b>relate to the UDP Server in most cases</b> (not if
	 * {@link NetworkConfig#status} = {@link NetworkConfig.States#Server}). The cause probably is
	 * the port. React to that <b>port error with {@link NetworkManager#setPort(int)}</b> with the
	 * recommendation to choose a random number between 10 000 and 60 000!
	 */
	public static final String					NETWORK_FATALERROR	= "NETWORK_FATALERROR";

	/**
	 * Represents the status, bidirectional binding (setting) is possible!
	 *
	 * <p>
	 * If triggered <b>internally</b>: <br>
	 * See description of {@link OnlineStates#Online}, {@link OnlineStates#Established} and
	 * {@link OnlineStates#Offline}!
	 * </p>
	 *
	 * <p>
	 * If triggered <b>from you</b>: <br>
	 * {@link OnlineStates#Established} means you are the {@link NetworkConfig.States#Server}.
	 * Others can now connect (automatically). <br>
	 * Set {@link OnlineStates#Offline} means the connection will be interrupted. <br>
	 * ATTENTION: Do NOT set {@link OnlineStates#Online} (it is read-only)!
	 * </p>
	 *
	 */
	public static ObjectProperty<OnlineStates>	online				= new SimpleObjectProperty<>(OnlineStates.Offline);	;
	private static Boolean						onlineBlocker;

	protected static TCPCoordinator				t;
	protected static UDPCoordinator				u;
	private static boolean						blockUDP			= false;

	/**
	 * <p>
	 * Initializes UDP and TCP connection. Start listening and broadcasting on UDP connection and
	 * automatically sets up a TCP connection if {@link NetworkConfig.States#BoundToServer} or if
	 * {@link NetworkConfig.States#Server} application was found.
	 *
	 * <b>Use</b> {@link MessageBus} to deal with {@link Message}!
	 *
	 * <b>Use</b> {@link NetworkManager#online} [reading] to listen to the NetworkStatus. (this
	 * method init() should be called after register a listener to online status).
	 *
	 * <p>
	 */
	public static void init()
	{
		NetworkManager.onlineBlocker = new Boolean(false);
		NetworkManager.online.addListener((ChangeListener<OnlineStates>) (observable, oldValue, newValue) ->
		{
			synchronized (NetworkManager.onlineBlocker)
			{
				if (!NetworkManager.onlineBlocker.booleanValue())
				{
					NetworkManager.onlineBlocker = true;
					if (NetworkManager.blockUDP && !NetworkManager.online.get().equals(OnlineStates.Offline))
						NetworkManager.online.set(OnlineStates.Offline);
					// If variable is set to true from outside start host.
					if (newValue.getNumVal() == 1 && !NetworkManager.blockUDP) // Established
						NetworkConfig.status.set(NetworkConfig.States.Server);
					else
						if (newValue.getNumVal() == 0) // Offline
							NetworkConfig.status.set(NetworkConfig.States.ServerGoOff);
					NetworkManager.onlineBlocker = false;
				}
				else
					switch (NetworkManager.online.get())
					{
						case Online:
							synchronized (NetworkConfig.status)
							{
								if (NetworkConfig.status.get().equals(NetworkConfig.States.Server))
									SyncedManager.host();
								else
								{
									final Thread thread = new Thread(() -> SyncedManager.client());
									thread.setDaemon(true);
									thread.start();
								}
							}
							break;
						case Established:
							break;
						case Offline:
							SyncedManager.stop();
							break;
						default:
							break;
					}
			}
		});

		NetworkConfig.status.addListener((ChangeListener<States>) (observable, oldValue, newValue) ->
		{
			synchronized (NetworkManager.onlineBlocker)
			{
				if (!NetworkManager.onlineBlocker.booleanValue())
				{
					NetworkManager.onlineBlocker = true;
					// Do not invalidate online status, just invoke on change!
					if (newValue.equals(States.Unbound))
						if (NetworkManager.online.get().getNumVal() > 0) // Not offline
							NetworkManager.online.set(OnlineStates.Offline);
					if (!newValue.equals(States.Unbound))
						if (NetworkManager.online.get().getNumVal() < 1) // Offline
							NetworkManager.online.set(OnlineStates.Established);
					NetworkManager.onlineBlocker = false;
				}
				// Otherwise it was called externally
			}

			System.out.println("OK??? " + newValue);
			switch (newValue)
			{
				case ServerGoOff:
				case Unbound:
					ParticipantManager.stop();
					break;
				case BoundToServer:
					ParticipantManager.start().connect();
					break;
				case Server:
					// Add me as server also as participant
					ParticipantManager.MY_PID.set(ParticipantManager.PARTICIPANT_NUMBER);
					ParticipantManager.PARTICIPANTS.add(ParticipantManager.PARTICIPANT_NUMBER++);
					ParticipantManager.start();
					NetworkManager.online.set(OnlineStates.Online);
					break;
				default:
					break;
			}
		});

		NetworkManager.t = new TCPCoordinator();

		try
		{
			NetworkManager.u = new UDPCoordinator();
		}
		catch (final IOException e)
		{
			NetworkManager.blockUDP = true;
		}
	}

	/**
	 * <p>
	 * Sets the default port for UDP and TCP. After this it shuts down the complete network and
	 * calls {@link NetworkManager#init()} again. Default port is {@link NetworkConfig#PORT}. This
	 * port is mostly necessary for the UDP Server. In case of TCP the port + 1 (to 5) is asked if
	 * port is not available.
	 * </p>
	 */
	public static void setPort(final int portNumber)
	{
		synchronized (NetworkConfig.status)
		{
			NetworkConfig.PORT = portNumber;

			if (NetworkManager.t != null)
				NetworkManager.t.stopTCPFull();
			if (NetworkManager.u != null)
				NetworkManager.u.stopUDPFull();

			NetworkManager.blockUDP = false;

			NetworkManager.init();
		}
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
		CustomSysOutErr.init();

		SyncedManager.register(new UserManager());

		// Catch fatal errors to show (network reacted already to this error)
		AnnotationProcessor.process(new NetworkManager());

		// Show internal status changes
		// NetworkConfig.status.addListener((ChangeListener<States>) (observable, oldValue,
		// newValue) -> System.out.println("OLD: " + oldValue + "! NEW: " + newValue));

		// Show online status
		NetworkManager.online.addListener((ChangeListener<OnlineStates>) (observable, oldValue, newValue) ->
		{
			System.out.println("NEW ONLINE STATUS: " + newValue);
		});

		ParticipantManager.PARTICIPANTS.addListener((ListChangeListener<Integer>) c ->
		{
			System.out.println(ParticipantManager.PARTICIPANTS);
		});

		// INITIALIZE NETWORK (this is the only line which has to be called once!)
		NetworkManager.init();

		// NetworkManager.online.set(OnlineStates.Established);

		Inspector.runLater(() ->
		{
			System.out.println("Executed1");
			NetworkManager.online.set(OnlineStates.Offline);
			// TODO Doesnt work because set offline is only for servers

			Inspector.runLater(() ->
			{
				System.out.println("Executed2");
				// NetworkManager.online.set(OnlineStates.Established);
			});
		});

		try
		{
			// Show all directly found applications host and all directly found Server (Not the
			// bound to
			// server ones) which were once online while this application was online.
			NetworkManager.u.allAdresses.addListener((ListChangeListener<String>) c ->
			{
				c.next();
				System.out.println("ALL: " + c.getAddedSubList().get(0));
			});
			NetworkManager.u.allServerAdresses.addListener((ListChangeListener<String>) c ->
			{
				c.next();
				System.out.println("SERVER: " + c.getAddedSubList().get(0));
			});
		}
		catch (final Exception e)
		{
			// If fatal error occured
		}

		while (true)
			;
	}

	@EventTopicSubscriber(topic = NetworkManager.NETWORK_FATALERROR)
	public void error(final String topic, final Exception exception)
	{
		System.out.println("Caught error: " + exception.getMessage());
	}
}
