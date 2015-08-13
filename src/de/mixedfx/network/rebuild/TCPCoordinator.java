package de.mixedfx.network.rebuild;

import java.io.IOException;
import java.net.InetAddress;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.logging.Log;
import de.mixedfx.network.rebuild.messages.GoodByeMessage;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;

public class TCPCoordinator
{
	public static final String CONNECTION_LOST = "TCP_CONNECTION_LOST";

	/**
	 * The id of the first connection!
	 */
	public static int CONNECTION_ID = 1;

	private final TCPServer					tcpServer;
	public final ListProperty<TCPClient>	tcpClients;

	public TCPCoordinator()
	{
		// Start listening on TCP connection lost event
		AnnotationProcessor.process(this);

		this.tcpServer = new TCPServer();
		this.tcpClients = new SimpleListProperty<>(FXCollections.observableArrayList());
	}

	@EventTopicSubscriber(topic = TCPCoordinator.CONNECTION_LOST)
	public void lostConnection(final String topic, final Connection connection)
	{
		Log.network.debug("TCP Connection with IP " + connection.ip + " lost!");

		synchronized (tcpClients)
		{
			TCPClient toRemove = null;
			for (TCPClient tcp : tcpClients)
				if (tcp.remoteAddress.equals(connection.ip))
					toRemove = tcp;
			tcpClients.remove(toRemove);
			System.out.println(tcpClients);
		}
	}

	public synchronized void startServer()
	{
		try
		{
			this.tcpServer.start();
			this.tcpServer.connectionList.addListener(new ListChangeListener<TCPClient>()
			{
				@Override
				public void onChanged(javafx.collections.ListChangeListener.Change<? extends TCPClient> c)
				{
					synchronized (tcpClients)
					{
						while (c.next())
						{
							if (c.wasAdded())
								tcpClients.addAll(c.getAddedSubList());
							else if (c.wasRemoved())
								tcpClients.removeAll(c.getRemoved());
						}
					}
				}
			});
		} catch (final IOException e)
		{
			Log.network.fatal("Error occured while starting TCP server: " + e.getCause().getMessage());
			this.stopTCPFull();
			EventBusExtended.publishSyncSafe(NetworkManager.NETWORK_FATALERROR, e);
			return;
		}
	}

	/**
	 * Usually called by {@link UDPCoordinator}. This starts the client connection from me to another client.
	 *
	 * @param ip
	 */
	public void startFullTCP(final InetAddress ip)
	{
		// Maybe a UDP request was still caught.
		if (!NetworkManager.running)
		{
			return;
		}

		// Already connected with this client over this specific NIC?
		synchronized (tcpClients)
		{
			for (TCPClient tcp : tcpClients)
			{
				if (tcp.remoteAddress.equals(ip))
				{
					Log.network.trace("Already connected to: " + ip);
					return;
				}
			}

			try
			{
				Log.network.info("Start TCP connection to: " + ip.getHostAddress());
				synchronized (tcpClients)
				{
					this.tcpClients.add(new TCPClient().start(ip));
				}
			} catch (final IOException e)
			{
				Log.network.info("Error occured while starting TCP client: " + e);
				return;
			}
		}
	}

	/**
	 * Can be called from outside to stop all TCP connections and the listening TCP Server!
	 */
	public void stopTCPFull()
	{
		synchronized (tcpClients)
		{
			Log.network.info("Stop TCP connection!");

			// Send a GoodBye to everyone who is still available to avoid ghost connections.
			for (TCPClient tcp : tcpClients)
			{
				final GoodByeMessage goodbyeMessage = new GoodByeMessage();
				goodbyeMessage.setToIP(tcp.remoteAddress);
				EventBusExtended.publishSyncSafe(Connection.MESSAGE_CHANNEL_SEND, goodbyeMessage);
			}

			for (TCPClient tcp : tcpClients)
				// Then stop it!
				tcp.stop();

			tcpClients.clear();

			this.tcpServer.stop();
		}
	}
}
