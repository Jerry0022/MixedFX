package de.mixedfx.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

class DiscoveryKolumbus
{
	/**
	 * Alternates between broadcast the discovery message via default broadcast (255.255.255.255)
	 * and via the broadcast address of every network interface (every subnet).
	 */
	private final static int		DISCOVER_INTERVAL	= 1000;

	private final DatagramSocket	socket;

	public DiscoveryKolumbus() throws Exception
	{
		this.socket = new DatagramSocket();
		this.socket.setBroadcast(true);
	}

	public synchronized void startDiscovering()
	{
		boolean socketUnclosed = true;
		while (socketUnclosed)
		{
			final byte[] sendData = Discovery.Messages.DISCOVERY.toString().getBytes();

			// Try the 255.255.255.255 first
			try
			{
				final DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), Discovery.PORT_UDP_CLIENT);
				this.socket.send(sendPacket);
			}
			catch (final Exception e)
			{
				socketUnclosed = false;
			}

			try
			{
				Thread.sleep(DiscoveryKolumbus.DISCOVER_INTERVAL);
			}
			catch (final InterruptedException e2)
			{
			}

			// Broadcast the message over all the network interfaces
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
							final DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, Discovery.PORT_UDP_CLIENT);
							this.socket.send(sendPacket);
							System.out.println("SENT");
						}
						catch (final Exception e)
						{
							socketUnclosed = false;
						}
					}
				}
			}
			catch (final SocketException e1)
			{
				socketUnclosed = false;
			}

			try
			{
				Thread.sleep(DiscoveryKolumbus.DISCOVER_INTERVAL);
			}
			catch (final InterruptedException e2)
			{
			}
		}
	}

	public void stopDiscovering()
	{
		this.socket.close();
	}
}
