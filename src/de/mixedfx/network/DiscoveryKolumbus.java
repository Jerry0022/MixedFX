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
	private final static int	DISCOVER_INTERVAL	= 1000;

	private DatagramSocket		socket;

	public DiscoveryKolumbus() throws Exception
	{
		socket = new DatagramSocket();
		socket.setBroadcast(true);
	}

	public synchronized void startDiscovering()
	{
		boolean socketUnclosed = true;
		while (socketUnclosed)
		{
			byte[] sendData = Discovery.Messages.DISCOVERY.toString().getBytes();

			// Try the 255.255.255.255 first
			try
			{
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), Discovery.PORT);
				socket.send(sendPacket);
			}
			catch (Exception e)
			{
				socketUnclosed = false;
			}

			try
			{
				Thread.sleep(DISCOVER_INTERVAL);
			}
			catch (InterruptedException e2)
			{
			}

			// Broadcast the message over all the network interfaces
			Enumeration<NetworkInterface> interfaces;
			try
			{
				interfaces = NetworkInterface.getNetworkInterfaces();
				while (interfaces.hasMoreElements())
				{
					NetworkInterface networkInterface = interfaces.nextElement();

					// Don't want to broadcast to loopback interfaces or disabled interface
					if (networkInterface.isLoopback() || !networkInterface.isUp())
						continue;

					for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses())
					{
						// IPv6 is not supported here
						InetAddress broadcast = interfaceAddress.getBroadcast();
						if (broadcast == null)
							continue;

						// Send the broadcast package!
						try
						{
							DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, Discovery.PORT);
							socket.send(sendPacket);
							System.out.println("SENT");
						}
						catch (Exception e)
						{
							socketUnclosed = false;
						}
					}
				}
			}
			catch (SocketException e1)
			{
				socketUnclosed = false;
			}

			try
			{
				Thread.sleep(DISCOVER_INTERVAL);
			}
			catch (InterruptedException e2)
			{
			}
		}
	}

	public void stopDiscovering()
	{
		socket.close();
	}
}
