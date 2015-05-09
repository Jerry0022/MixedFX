package de.mixedfx.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Enumeration;

class DiscoveryKolumbus
{
	/**
	 * Alternates between broadcast the discovery message via default broadcast (255.255.255.255)
	 * and via the broadcast address of every network interface (every subnet).
	 */
	private final static int		DISCOVER_TIMEOUT	= 3000;

	private final static int		DISCOVER_TRYS		= 5;

	private final DatagramSocket	socket;

	public DiscoveryKolumbus() throws Exception
	{
		this.socket = new DatagramSocket();
		this.socket.setBroadcast(true);
		this.socket.setSoTimeout(DiscoveryKolumbus.DISCOVER_TIMEOUT);
	}

	public synchronized void startDiscovering()
	{
		for (int i = 0; i < DiscoveryKolumbus.DISCOVER_TRYS; i++)
		{
			boolean worked = true;
			boolean timeout;
			while (worked)
			{
				worked = false;

				final byte[] sendData = Discovery.Messages.DISCOVERY_REQUEST.toString().getBytes();

				// Try the 255.255.255.255 first
				try
				{
					final DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), Discovery.PORT_UDP_CLIENT);
					this.socket.send(sendPacket);
					worked = true;
				}
				catch (final Exception e)
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
								System.out.println("SENT DISCOVER");
								worked = true;
							}
							catch (final Exception e)
							{
							}
						}
					}
				}
				catch (final SocketException e1)
				{
				}

				if (worked)
				{
					// TODO Do receival in separate thread
					timeout = false;
					while (!timeout)
					{
						// Receive a packet
						final byte[] recvBuf = new byte[15000];
						final DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
						try
						{
							System.out.println("WAIT");
							this.socket.receive(packet); // BLOCKING
							System.out.println("GOT: " + packet.getAddress());
							// TODO Add packet.getAdress to LIST!
						}
						catch (final IOException e)
						{
							System.out.println("failure");
							if (e instanceof SocketTimeoutException)
								timeout = true;
						}
					}
					if (timeout)
						worked = false;
				}
			}
			// Could not find a for UDP working network connection
		}
	}

	public void stopDiscovering()
	{
		this.socket.close();
	}
}
