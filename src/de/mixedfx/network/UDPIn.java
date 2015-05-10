package de.mixedfx.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import de.mixedfx.network.NetworkConfig.States;

public class UDPIn
{
	private DatagramSocket	socket;

	/**
	 * Asynchronous
	 */
	public void start()
	{
		try
		{
			UDPIn.this.socket = new DatagramSocket(NetworkConfig.PORT, InetAddress.getByName("0.0.0.0"));
			UDPIn.this.listen();
		}
		catch (SocketException | UnknownHostException e)
		{
			if (!NetworkConfig.status.get().equals(States.Server))
				UDPCoordinator.service.publishSync(UDPCoordinator.ERROR, e);
		}
	}

	public void close()
	{
		this.socket.close();
	}

	private void listen()
	{
		final Thread thread = new Thread(() ->
		{
			while (true)
			{
				final byte[] recvBuf = new byte[15000];
				final DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
				try
				{
					UDPIn.this.socket.receive(receivePacket);
					UDPCoordinator.service.publishAsync(UDPCoordinator.RECEIVE, receivePacket);
				}
				catch (final Exception e)
				{
					if (!(e instanceof SocketException)) // SocketException => stop() was called.
						UDPCoordinator.service.publishSync(UDPCoordinator.ERROR, e);
					break;
				} // BLOCKING
			}
		});
		thread.setDaemon(true);
		thread.start();
	}
}
