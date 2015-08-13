package de.mixedfx.networkd;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import org.apache.commons.collections.CollectionUtils;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.inspector.Inspector;
import de.mixedfx.java.ApacheTools;
import de.mixedfx.logging.Log;
import de.mixedfx.network.NetworkConfig.States;
import de.mixedfx.network.messages.GoodByeMessage;
import de.mixedfx.network.messages.ParticipantMessage;
import javafx.beans.value.ChangeListener;

public class TCPCoordinator
{
	public static final String CONNECTION_LOST = "TCP_CONNECTION_LOST";

	/**
	 * In case of not being the {@link NetworkConfig.States#SERVER} this is the ID of the connection which is directly or indirectly bound to the
	 * server.
	 */
	public static int localNetworkMainID;

	/**
	 * The id of the first connection!
	 */
	public static int localNetworkFirstID;

	/**
	 * This is the incremental ID of the connections to initiated from other clients.
	 */
	public static AtomicInteger localNetworkID;

	static
	{
		TCPCoordinator.localNetworkMainID = 0;
		TCPCoordinator.localNetworkFirstID = 1;
		TCPCoordinator.localNetworkID = new AtomicInteger(1);
	}

	private final TCPServer	tcpServer;
	private final TCPClient	tcpClient;

	public TCPCoordinator()
	{
		// Start listening on TCP connection lost event
		AnnotationProcessor.process(this);

		this.tcpServer = new TCPServer();
		this.tcpClient = new TCPClient();

		// Add listener to NetworkConfig.status to force starting or stopping all TCP connections.
		NetworkConfig.STATUS.addListener((ChangeListener<States>) (observable, oldValue, newValue) ->
		{
			synchronized (NetworkConfig.STATUS)
			{
				// Switch Server on if requested
				if (newValue.equals(States.SERVER))
				{
					// If already connected to server stop connection
					if (oldValue.equals(States.BOUNDTOSERVER))
					{
						this.stopTCPFull(); // => Stops also TCP Server && set to unbound; see
						// below
					}

					// Start TCP Server
					try
					{
						this.tcpServer.start();
					}
					catch (final IOException e)
					{
						Log.network.fatal("Error occured while starting TCP server: " + e.getCause().getMessage());
						// If failed to start server set status back to Unbound
						Inspector.runNowAsDaemon(() ->
						{
							synchronized (NetworkConfig.STATUS)
							{
								NetworkConfig.STATUS.set(States.UNBOUND);
							}
						});
					}
				}

				// Switch off
				if (oldValue.equals(States.SERVER) || oldValue.equals(States.BOUNDTOSERVER))
				{
					this.stopTCPFull();
				}
			}
		});
	}

	@EventTopicSubscriber(topic = TCPCoordinator.CONNECTION_LOST)
	public void lostConnection(final String topic, final Integer clientID)
	{
		synchronized (NetworkConfig.STATUS)
		{
			Log.network.debug("Participant with pid " + clientID + " lost! Is my connection directly to the server? " + clientID.equals(TCPCoordinator.localNetworkMainID));

			// Check if this connection is my main connection to the server
			if (clientID.equals(TCPCoordinator.localNetworkMainID))
			{
				this.stopTCPFull();
				ParticipantManager.stop();
			}
			else
			{
				// Get lost client connection
				final Predicate<Connection> getByClientID = t -> t.clientID == clientID;
				final Connection lostConnection = (Connection) CollectionUtils.select(this.tcpServer.connectionList, ApacheTools.convert(getByClientID)).iterator().next();

				// Get all connected and fully registered PIDs behind this lost connection
				final Collection<Integer> allParticipated = lostConnection.uid_pid_map.values();
				allParticipated.removeAll(Collections.singleton(null));

				// Send message that these PIDs are lost
				final ParticipantMessage pMessage = new ParticipantMessage();
				pMessage.uID = "";
				pMessage.ids.addAll(allParticipated);

				// Send or publish depending on PID distribution power
				if (!NetworkConfig.STATUS.get().equals(NetworkConfig.States.SERVER))
				{
					EventBusExtended.publishSyncSafe(MessageBus.MESSAGE_SEND, pMessage);
				}
				else
				{
					EventBusExtended.publishSyncSafe(MessageBus.MESSAGE_RECEIVE, pMessage);
				}
			}
		}
	}

	/**
	 * Usually called by {@link UDPCoordinator}. This starts the client connection from me to the BoundToServer or directly to the Server.
	 *
	 * @param ip
	 */
	public void startFullTCP(final InetAddress ip)
	{
		synchronized (NetworkConfig.STATUS)
		{
			if (!NetworkManager.running)
			{
				return;
			}

			Log.network.info("Start TCP connection to (BoundTo-)Server: " + ip.getHostAddress());

			try
			{

				this.tcpClient.start(ip);
			}
			catch (final IOException e)
			{
				Log.network.error("Error occured while starting TCP client: " + e.getCause().getMessage());
				return;
			}

			try
			{
				this.tcpServer.start();
			}
			catch (final IOException e)
			{
				Log.network.fatal("Error occured while starting TCP server: " + e.getCause().getMessage());
				this.stopTCPFull();
				return;
			}

			NetworkConfig.STATUS.set(States.BOUNDTOSERVER);
		}
	}

	/**
	 * Can be called from outside to reset the connection.
	 */
	public synchronized void stopTCPFull()
	{
		synchronized (NetworkConfig.STATUS)
		{
			Log.network.info("Stop TCP connection!");

			// Send a GoodBye to everyone who is still available to avoid ghost connections.
			final GoodByeMessage goodbyeMessage = new GoodByeMessage();
			EventBusExtended.publishSyncSafe(Connection.MESSAGE_CHANNEL_SEND, goodbyeMessage);

			// Stop first client, which is the connection to the server and afterwards all my bound
			// clients.
			this.tcpClient.stop();
			this.tcpServer.stop();

			TCPCoordinator.localNetworkID.set(localNetworkFirstID);

			// May force this method to run twice but this has no performace influence! See constructor for more information.
			NetworkConfig.STATUS.set(States.UNBOUND);
		}
	}
}
