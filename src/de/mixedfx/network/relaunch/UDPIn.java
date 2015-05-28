package de.mixedfx.network.relaunch;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

class UDPIn
{
	private final ArrayList<DatagramSocket>	sockets;

	protected UDPIn()
	{
		this.sockets = new ArrayList<>();
	}

	/**
	 * Asynchronous listening on 1 to {@link NetworkConfig#TRIES_AMOUNT} ports. If can't listen on
	 * at least one port and wasn't stopped by user, it throws an {@link UDPCoordinator#ERROR} with
	 * the (last) exception!
	 */
	public synchronized void start()
	{
		Exception lastException = null;

		// Listen on 5 ports! If can't listen on
		for (int i = 0; i < NetworkConfig.TRIES_AMOUNT; i++)
		{
			try
			{
				final DatagramSocket datagramSocket = new DatagramSocket(NetworkConfig.PORT.get() + i * NetworkConfig.TRIES_STEPS, InetAddress.getByName("0.0.0.0"));
				this.sockets.add(datagramSocket);
				this.listen(datagramSocket);
			}
			catch (final Exception e)
			{
				lastException = e;
			}
		}

		if (this.sockets.isEmpty())
		{
			UDPCoordinator.service.publishSync(UDPCoordinator.ERROR, lastException);
		}
	}

	public synchronized void close()
	{
		for (final DatagramSocket socket : this.sockets)
		{
			socket.close();
			this.sockets.remove(socket);
		}
	}

	private void listen(final DatagramSocket socket)
	{
		final Thread thread = new Thread(() ->
		{
			while (true)
			{
				final byte[] recvBuf = new byte[15000];
				final DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
				try
				{
					socket.receive(receivePacket); // BLOCKING
					UDPCoordinator.service.publishAsync(UDPCoordinator.RECEIVE, receivePacket);
				}
				catch (final Exception e)
				{
					// Remove me from available sockets!
					this.sockets.remove(socket);
					// Check if others are available, if not and i wasn't forced to stop publish an
					// error!
					if (this.sockets.isEmpty())
					{
						if (!(e instanceof SocketException)) // I was forced to close!
						{
							UDPCoordinator.service.publishSync(UDPCoordinator.ERROR, e);
						}
					}
					break; // Stop this thread!
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}
}
