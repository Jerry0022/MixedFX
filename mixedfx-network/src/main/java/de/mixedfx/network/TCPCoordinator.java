package de.mixedfx.network;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.inspector.Inspector;
import de.mixedfx.network.messages.GoodByeMessage;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.util.Duration;
import lombok.NonNull;
import org.apache.logging.log4j.Logger;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

@Component
public class TCPCoordinator
{
	public static final String CONNECTION_LOST = "TCP_CONNECTION_LOST";

	@Autowired
	@Qualifier(value = "Network")
	Logger LOGGER;

	@Autowired
	private NetworkManager networkManager;
	@Autowired
	private TCPServer tcpServer;
	public final ListProperty<TCPClient>	tcpClients;

	public TCPCoordinator()
	{
		this.tcpClients = new SimpleListProperty<>(FXCollections.observableArrayList());

		// Start listening on TCP connection lost event
		AnnotationProcessor.process(this);
	}

	@EventTopicSubscriber(topic = TCPCoordinator.CONNECTION_LOST)
	public void lostConnection(final String topic, final Connection connection)
	{
		LOGGER.debug("TCP Connection with IP " + connection.ip + " lost!");

		synchronized (tcpClients)
		{
			(new ArrayList<>(tcpClients)).stream().filter(tcpClient -> tcpClient.remoteAddress.equals(connection.ip)).forEach(tcpClient1 -> tcpClients.remove(tcpClient1));
			LOGGER.debug("All remaining tcp clients: " + tcpClients);
		}
	}

	public synchronized void startServer()
	{
		try
		{
			this.tcpServer.start();
			// Start listening for the incoming connections
			this.tcpServer.connectionList.addListener((ListChangeListener<TCPClient>) c -> {
				synchronized (tcpClients) {
					while (c.next()) {
						if (c.wasAdded())
							tcpClients.addAll(c.getAddedSubList());
						else if (c.wasRemoved())
							tcpClients.removeAll(c.getRemoved());
					}
				}
			});
		} catch (final IOException e)
		{
			LOGGER.fatal("Error occured while starting TCP server: ", e);
			this.stopTCPFull();
			EventBusExtended.publishSyncSafe(NetworkManager.NETWORK_FATALERROR, e);
		}
	}

	/**
	 * Usually called by {@link UDPCoordinator}. This starts the client connection from me to another client.
	 *
	 * @param ip
	 */
	public void startFullTCP(@NonNull InetAddress ip)
	{
		// Maybe a UDP request was still caught.
		if (!networkManager.running)
			return;

		// Already connected with this client over this specific NIC?
		synchronized (tcpClients)
		{
			tcpClients.stream().filter(tcpClient -> tcpClient.remoteAddress.equals(ip)).forEach(tcpClient1 -> {
				LOGGER.trace("Already connected to: " + ip);
				return;
			});
		}

		// Start outgoing connection
		try
		{
			LOGGER.info("Start TCP connection to: " + ip.getHostAddress());
			TCPClient client = new TCPClient().start(ip);
			synchronized (tcpClients)
			{
				if (client != null)
					this.tcpClients.add(client);
			}
		} catch (final IOException e)
		{
			LOGGER.warn("Error occurred while starting TCP client: " + e);
			return;
		}
		LOGGER.debug("Full TCP connection established to " + ip);
	}

	/**
	 * Can be called from outside to stop all TCP connections and the listening TCP Server!
	 */
	public void stopTCPFull()
	{
		synchronized (tcpClients)
		{
			LOGGER.info("Stop TCP connection!");

			// Send a GoodBye to everyone who is still available to avoid ghost connections.
			tcpClients.forEach(tcpClient -> {
				final GoodByeMessage goodbyeMessage = new GoodByeMessage();
				goodbyeMessage.setToIP(tcpClient.remoteAddress);
				EventBusExtended.publishSyncSafe(Connection.MESSAGE_CHANNEL_SEND, goodbyeMessage);
				LOGGER.debug("Sent GoodByeMessage!");
			});

			Inspector.sleep(Duration.millis(NetworkConfig.TCP_UNICAST_INTERVAL * 2));

			// Force every tcp connection to stop
			tcpClients.forEach(tcpClient -> tcpClient.stop());
			tcpClients.clear();

			// Stop listening for incoming connections
			this.tcpServer.stop();
		}
	}
}
