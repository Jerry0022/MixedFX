package de.mixedfx.network;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.beans.value.ChangeListener;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.network.NetworkConfig.States;
import de.mixedfx.network.messages.Message;

public class TCPCoordinator
{
	public static final int		PORT_TRIES		= 5;
	public static final String	CONNECTION_LOST	= "TCP_CONNECTION_LOST";

	public static AtomicInteger	localNetworkMainID;
	public static AtomicInteger	localNetworkID;

	private final TCPServer		tcpServer;
	private final TCPClient		tcpClient;

	public TCPCoordinator()
	{
		TCPCoordinator.localNetworkMainID = new AtomicInteger(0);
		TCPCoordinator.localNetworkID = new AtomicInteger(1);

		AnnotationProcessor.process(this);

		this.tcpServer = new TCPServer();
		this.tcpClient = new TCPClient();

		NetworkConfig.status.addListener((ChangeListener<States>) (observable, oldValue, newValue) ->
		{
			synchronized (NetworkConfig.status)
			{
				// Switch Server on
				if (newValue.equals(States.Server))
				{
					// If already connected to server stop connection
					if (oldValue.equals(States.BoundToServer))
						this.stopTCPFull(); // => Stops also TCP Server && set to unbound; see
					// below

					try
					{
						this.tcpServer.start();
					}
					catch (final IOException e)
					{
						final Thread t = new Thread(() ->
						{
							synchronized (NetworkConfig.status)
							{
								NetworkConfig.status.set(States.Unbound);
							}
						});
						t.setDaemon(true);
						t.start();
					}
				}

				// Switch Server off
				if (oldValue.equals(States.Server))
					this.stopTCPFull();
			}
		});
	}

	@EventTopicSubscriber(topic = TCPCoordinator.CONNECTION_LOST)
	public synchronized void lostConnection(final String topic, final Integer clientID)
	{
		synchronized (NetworkConfig.status)
		{
			System.out.println("LOST: " + clientID.equals(TCPCoordinator.localNetworkMainID.get()) + clientID);
			if (clientID.equals(TCPCoordinator.localNetworkMainID.get()))
				this.stopTCPFull();
		}
	}

	/**
	 * Usually called by {@link UDPCoordinator}.
	 *
	 * @param ip
	 */
	public synchronized void startFullTCP(final InetAddress ip)
	{
		synchronized (NetworkConfig.status)
		{
			System.out.println("START!" + ip);
			try
			{

				this.tcpClient.start(ip, NetworkConfig.PORT);
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

			final Message message = new Message();
			System.out.println("SEND MESSAGE FROM START!");
			EventBusExtended.publishSyncSafe(Connection.MESSAGE_CHANNEL_SEND, message);
			System.out.println("SEND MESSAGE DONE :)");
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
			final Message goodbyeMessage = new Message();
			goodbyeMessage.goodbye = true;
			EventBusExtended.publishSyncSafe(Connection.MESSAGE_CHANNEL_SEND, goodbyeMessage);
			this.tcpClient.stop();
			this.tcpServer.stop();
			NetworkConfig.status.set(States.Unbound);
		}
	}
}
