package de.mixedfx.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

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
			UDPIn.this.socket = new DatagramSocket();
		}
		catch (final SocketException e)
		{
			UDPCoordinator.service.publishSync(UDPCoordinator.ERROR, e);
		}
		UDPIn.this.listen();
	}

	public void stop()
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
					System.out.println("RECEIVED");
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
