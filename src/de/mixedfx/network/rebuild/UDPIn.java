package de.mixedfx.network.rebuild;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import de.mixedfx.inspector.Inspector;

class UDPIn
{
	private final List<DatagramSocket> sockets;

	protected UDPIn()
	{
		this.sockets = new ArrayList<>();
	}

	/**
	 * Asynchronous listening on 1 to {@link NetworkConfig#TRIES_AMOUNT} ports. If can't listen on at least one port and wasn't stopped by user, it throws an {@link UDPCoordinator#ERROR} with the
	 * (last) exception!
	 * 
	 * @throws Exception
	 *             Last exception if no sockets could be initialized.
	 */
	public synchronized void start() throws Exception
	{
		Exception lastException = null;

		// Listen on 5 ports! At least one must work!
		for (int i = 0; i < NetworkConfig.TRIES_AMOUNT; i++)
		{
			DatagramSocket datagramSocket;
			try
			{
				datagramSocket = new DatagramSocket(NetworkConfig.PORT.get() + i * NetworkConfig.TRIES_STEPS, InetAddress.getByName("0.0.0.0"));
				this.sockets.add(datagramSocket);
				this.listen(datagramSocket);
			} catch (SocketException | UnknownHostException e)
			{
				lastException = e;
			}
		}

		if (this.sockets.isEmpty())
			throw lastException;
	}

	public synchronized void close()
	{
		for (final DatagramSocket socket : this.sockets)
		{
			socket.close();
		}
		this.sockets.clear();
	}

	private void listen(final DatagramSocket socket)
	{
		Inspector.runNowAsDaemon(() ->
		{
			while (true)
			{
				final byte[] recvBuf = new byte[500];
				final DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);

				try
				{
					socket.receive(receivePacket); // BLOCKING
					UDPCoordinator.service.publishSync(UDPCoordinator.RECEIVE, receivePacket);
				} catch (final Exception e)
				{
					synchronized (UDPIn.this)
					{
						// Remove me from available sockets!
						this.sockets.remove(socket);
						// Check if others are available, if not and i wasn't forced to stop publish
						// an
						// error!
						if (this.sockets.isEmpty())
						{
							if (!(e instanceof SocketException)) // I was forced to close!
							{
								UDPCoordinator.service.publishSync(UDPCoordinator.ERROR, e);
							}
						}
					}
					break; // Stop this thread!
				}
			}
		});
	}
}
