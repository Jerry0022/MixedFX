package de.mixedfx.network;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;

import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.java.CustomSysOutErr;
import de.mixedfx.network.NetworkConfig.States;
import de.mixedfx.network.messages.Message;

/**
 * <p>
 * Builds up a network. Manually only host server must be called and {@link Message} received or
 * send. Connection to a host server or someone who is connected to the host server will be
 * established automatically.
 *
 * To get an information about the network status or if you want to host get/set
 * {@link NetworkManager#online}!
 *
 * If an error occur only a {@link NetworkManager#NETWORK_FATALERROR} is sent over the eventBus.
 *
 * If you may want to set the port use {@link NetworkManager#setPort(int)}. (Usually only necessary
 * if there was an fatal error!)
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
	 * informed that there was a network error which closed the entire network. You have to call
	 * again {@link NetworkManager#init()} to initialize the network. Further information: This
	 * error does relate to the UDP Server in most cases (not if {@link NetworkConfig#status} =
	 * {@link NetworkConfig.States#Server}). The cause probably is the port. React to that error
	 * with {@link NetworkManager#setPort(int)} with the recommendation to choose a random number
	 * between 10 000 and 60 000!
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
					// If variable is set to true from outside start host.
					if (newValue.getNumVal() == 1) // Established
						NetworkConfig.status.set(NetworkConfig.States.Server);
					else
						if (newValue.getNumVal() == 0) // Offline
							NetworkConfig.status.set(NetworkConfig.States.ServerGoOff);
					NetworkManager.onlineBlocker = false;
				}
				// Otherwise it was called internally
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

			switch (newValue)
			{
				case Unbound:
					ParticipantManager.stop();
					break;
				case BoundToServer:
					ParticipantManager.start().connect();
					break;
				case Server:
					// Add me as server also as participant
					ParticipantManager.PARTICIPANTS.add(ParticipantManager.PARTICIPANT_NUMBER++);
					ParticipantManager.start();
					break;
				default:
					break;
			}
		});

		NetworkManager.t = new TCPCoordinator();
		NetworkManager.u = new UDPCoordinator();
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

		// Catch fatal errors to show (network reacted already to this error)
		AnnotationProcessor.process(new NetworkManager());

		// Show internal status changes
		NetworkConfig.status.addListener((ChangeListener<States>) (observable, oldValue, newValue) -> System.out.println("OLD: " + oldValue + "! NEW: " + newValue));

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

		NetworkManager.online.set(OnlineStates.Established);

		// Show all directly found applications host and all directly found Server (Not the bound to
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

		while (true)
			;
	}

	@EventTopicSubscriber(topic = NetworkManager.NETWORK_FATALERROR)
	public void error(final String topic, final Exception exception)
	{
		System.out.println("Caught error: " + exception.getMessage());
	}
}
