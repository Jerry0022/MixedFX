package de.mixedfx.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Date;
import java.util.Enumeration;

import de.mixedfx.logging.Log;

class UDPOut
{
	private DatagramSocket socket;

	/**
	 * Asynchronous broadcasting on {@link NetworkConfig#TRIES_AMOUNT} available ports! If no broadcast socket couldn't be opened throws
	 * {@link UDPCoordinator#ERROR}!
	 */
	public synchronized void start()
	{
		try
		{
			UDPOut.this.socket = new DatagramSocket();
			UDPOut.this.socket.setBroadcast(true);
			UDPOut.this.broadcast();
		}
		catch (final SocketException e)
		{
			UDPCoordinator.service.publishSync(UDPCoordinator.ERROR, e);
		}
	}

	public synchronized void close()
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

				final byte[] sendData;
				synchronized (NetworkConfig.status)
				{
					sendData = (new Date().toInstant().toString() + "!" + NetworkConfig.status.get().toString() + "!" + NetworkConfig.statusChangeTime.get().toInstant().toString() + "!"
							+ ParticipantManager.MY_PID.get()).getBytes();
				}

				/*
				 * Try the 255.255.255.255 first
				 */
				for (int i = 0; i < NetworkConfig.TRIES_AMOUNT; i++)
				{
					try
					{
						final DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"),
								NetworkConfig.PORT.get() + i * NetworkConfig.TRIES_STEPS);
						this.socket.send(sendPacket);
						worked = true;
					}
					catch (final Exception e)
					{
						exception = e;
					}
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
						{
							continue;
						}

						for (final InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses())
						{
							// IPv6 is not supported here
							final InetAddress broadcast = interfaceAddress.getBroadcast();
							if (broadcast == null)
							{
								continue;
							}

							// Send the broadcast package!
							for (int i = 0; i < NetworkConfig.TRIES_AMOUNT; i++)
							{
								try
								{
									final DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, NetworkConfig.PORT.get() + i * NetworkConfig.TRIES_STEPS);
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
				}
				catch (final Exception e1)
				{
				}

				if (!worked)
				{
					break;
				}

				Log.network.trace("Sent UDP message!");

				try
				{
					Thread.sleep(NetworkConfig.BROADCAST_INTERVAL);
				}
				catch (final Exception e)
				{
				}
			}

			// If this was not closed by user
			if (!(exception instanceof NullPointerException))
			{
				UDPCoordinator.service.publishSync(UDPCoordinator.ERROR, new Exception("No network device suitable for UDP broadcast."));
			}
		});
		thread.setDaemon(true);
		thread.start();
	}
}
