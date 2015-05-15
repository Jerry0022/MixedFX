package de.mixedfx.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

class UDPOut
{
	private static final int		BROADCAST_INTERVAL	= 1000;

	private volatile DatagramSocket	socket;

	/**
	 * Asynchronous
	 */
	public boolean start()
	{
		try
		{
			UDPOut.this.socket = new DatagramSocket();
			UDPOut.this.socket.setBroadcast(true);
			UDPOut.this.broadcast();
			return true;
		}
		catch (final SocketException e)
		{
			UDPCoordinator.service.publishSync(UDPCoordinator.ERROR, e);
			return false;
		}
	}

	public void close()
	{
		if (this.socket != null)
			this.socket.close();
		this.socket = null;
	}

	private synchronized void broadcast()
	{
		final Thread thread = new Thread(() ->
		{
			boolean worked = true;
			Exception exception = new Exception();
			while (worked)
			{
				worked = false;

				final byte[] sendData = NetworkConfig.status.get().toString().getBytes();

				/*
				 * Try the 255.255.255.255 first
				 */
				try
				{
					final DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), NetworkConfig.PORT);
					this.socket.send(sendPacket);
					worked = true;
				}
				catch (final Exception e)
				{
					exception = e;
				}

				/*
				 * Broadcast the message over all the network interfaces
				 */
				Enumeration<NetworkInterface> interfaces;
				try
				{
					interfaces = NetworkInterface.getNetworkInterfaces();
					while (interfaces.hasMoreElements())
					{
						final NetworkInterface networkInterface = interfaces.nextElement();

						// Don't want to broadcast to loopback interfaces or disabled interface
						if (networkInterface.isLoopback() || !networkInterface.isUp())
							continue;

						for (final InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses())
						{
							// IPv6 is not supported here
							final InetAddress broadcast = interfaceAddress.getBroadcast();
							if (broadcast == null)
								continue;

							// Send the broadcast package!
							try
							{
								final DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, NetworkConfig.PORT);
								this.socket.send(sendPacket);
								worked = true;
							}
							catch (final Exception e)
							{
								exception = e;
							}
						}
					}
				}
				catch (final Exception e1)
				{}

				if (!worked)
					break;

				System.out.println("SENT");

				try
				{
					Thread.sleep(UDPOut.BROADCAST_INTERVAL);
				}
				catch (final Exception e)
				{}
			}
			if (!(exception instanceof NullPointerException))
				UDPCoordinator.service.publishSync(UDPCoordinator.ERROR, new Exception("No network device suitable for UDP broadcast."));
		});
		thread.setDaemon(true);
		thread.start();
	}
}
