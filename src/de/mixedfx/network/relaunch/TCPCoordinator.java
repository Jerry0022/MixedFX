package de.mixedfx.network.relaunch;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import javafx.beans.value.ChangeListener;

import org.apache.commons.collections.CollectionUtils;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.inspector.Inspector;
import de.mixedfx.java.ApacheTools;
import de.mixedfx.network.messages.Message;
import de.mixedfx.network.messages.ParticipantMessage;
import de.mixedfx.network.relaunch.NetworkConfig.States;

public class TCPCoordinator
{
	public static final String	CONNECTION_LOST	= "TCP_CONNECTION_LOST";

	/**
	 * In case of not being the {@link NetworkConfig.States#Server} this is the ID of the connection
	 * which is directly or indirectly bound to the server.
	 */
	public static AtomicInteger	localNetworkMainID;

	/**
	 * This is the incremental ID of the connections to initiated from other clients.
	 */
	public static AtomicInteger	localNetworkID;

	static
	{
		TCPCoordinator.localNetworkMainID = new AtomicInteger(0);
		TCPCoordinator.localNetworkID = new AtomicInteger(1);
	}

	private final TCPServer		tcpServer;
	private final TCPClient		tcpClient;

	public TCPCoordinator()
	{
		// Start listening on TCP connection lost event
		AnnotationProcessor.process(this);

		this.tcpServer = new TCPServer();
		this.tcpClient = new TCPClient();

		// Add listener to NetworkConfig.status to force starting TCP Server.
		NetworkConfig.status.addListener((ChangeListener<States>) (observable, oldValue, newValue) ->
		{
			synchronized (NetworkConfig.status)
			{
				// Switch Server on if requested
				if (newValue.equals(States.Server))
				{
					// If already connected to server stop connection
					if (oldValue.equals(States.BoundToServer))
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
						// If failed to start server set status back to Unbound
						Inspector.runNowAsDaemon(() ->
						{
							synchronized (NetworkConfig.status)
							{
								NetworkConfig.status.set(States.Unbound);
							}
						});
					}
				}

				// Switch off
				if (oldValue.equals(States.Server) || oldValue.equals(States.BoundToServer))
				{
					this.stopTCPFull();
				}
			}
		});
	}

	@EventTopicSubscriber(topic = TCPCoordinator.CONNECTION_LOST)
	public synchronized void lostConnection(final String topic, final Integer clientID)
	{
		synchronized (NetworkConfig.status)
		{
			System.out.println("LOST: " + clientID.equals(TCPCoordinator.localNetworkMainID.get()) + clientID);
			// Check if this connection is my main connection to the server
			if (clientID.equals(TCPCoordinator.localNetworkMainID.get()))
			{
				this.stopTCPFull();
				ParticipantManager.PARTICIPANTS.clear();
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
				if (!NetworkConfig.status.get().equals(NetworkConfig.States.Server))
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
	 * Usually called by {@link UDPCoordinator}. This starts the client connection from me to the
	 * BoundToServer or directly to the Server.
	 *
	 * @param ip
	 */
	public synchronized void startFullTCP(final InetAddress ip)
	{
		synchronized (NetworkConfig.status)
		{
			if (!NetworkManager.running)
			{
				return;
			}

			System.out.println("START!" + ip);
			try
			{

				this.tcpClient.start(ip);
			}
			catch (final IOException e)
			{
				return;
			}

			try
			{
				this.tcpServer.start();
			}
			catch (final IOException e)
			{
				this.stopTCPFull();
				return;
			}

			NetworkConfig.status.set(States.BoundToServer);
		}
	}

	/**
	 * Can be called from outside to reset the connection.
	 */
	public synchronized void stopTCPFull()
	{
		synchronized (NetworkConfig.status)
		{
			System.out.println("CLOSE EVERYTHING");

			// Send a GoodBye to everyone who is still available to avoid ghost connections.
			final Message goodbyeMessage = new Message();
			goodbyeMessage.goodbye = true;
			EventBusExtended.publishSyncSafe(Connection.MESSAGE_CHANNEL_SEND, goodbyeMessage);

			// Stop first client, which is the connection to the server and afterwards all my bound
			// clients.
			this.tcpClient.stop();
			this.tcpServer.stop();

			TCPCoordinator.localNetworkID.set(1);

			NetworkConfig.status.set(States.Unbound);
		}
	}
}
