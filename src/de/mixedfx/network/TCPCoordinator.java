package de.mixedfx.network;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.beans.value.ChangeListener;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.network.Overall.NetworkStatus;

public class TCPCoordinator
{
	public static final String	TCP_CONNECTION_LOST	= "TCP_CONNECTION_LOST";

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

		Overall.status.addListener((ChangeListener<NetworkStatus>) (observable, oldValue, newValue) ->
		{
			synchronized (Overall.status)
			{
				// Switch Server on
				if (newValue.equals(NetworkStatus.Server))
				{
					// If already connected to server stop connection
					if (oldValue.equals(NetworkStatus.BoundToServer))
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
							synchronized (Overall.status)
							{
								Overall.status.set(NetworkStatus.Unbound);
							}
						});
						t.setDaemon(true);
						t.start();
					}
				}

				// Switch Server off
				if (oldValue.equals(NetworkStatus.Server))
					this.stopTCPFull();
			}
		});
	}

	@EventTopicSubscriber(topic = TCPCoordinator.TCP_CONNECTION_LOST)
	public synchronized void lostConnection(final String topic, final Integer clientID)
	{
		if (clientID.equals(TCPCoordinator.localNetworkMainID.get()))
			this.stopTCPFull();
	}

	/**
	 * Usually called by {@link UDPCoordinator}.
	 *
	 * @param ip
	 */
	public synchronized void startFullTCP(final InetAddress ip)
	{
		synchronized (Overall.status)
		{
			try
			{
				this.tcpClient.start(ip, Overall.PORT);
				/**
				 * <pre>
				 * TODO If TCP Client Connection fails (not if interrupted by this class)
				 * this.stopTCPServer();
				 * </pre>
				 */
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

			Overall.status.set(NetworkStatus.BoundToServer);
		}
	}

	/**
	 * Can be called from outside to reset the connection.
	 */
	public synchronized void stopTCPFull()
	{
		synchronized (Overall.status)
		{
			this.tcpClient.stop();
			this.tcpServer.stop();
			Overall.status.set(NetworkStatus.Unbound);
		}
	}
}
