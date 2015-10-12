package de.mixedfx.network;

import de.mixedfx.inspector.Inspector;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Date;
import java.util.Enumeration;

@Component
class UDPOut
{
	@Autowired
	@Qualifier(value = "Network")
	Logger LOGGER;

	private DatagramSocket socket;

	/**
	 * Asynchronous broadcasting on {@link NetworkConfig#TRIES_AMOUNT} available ports! If no broadcast socket couldn't be opened throws {@link UDPCoordinator#ERROR}!
	 * 
	 * @throws SocketException
	 */
	public synchronized void start() throws SocketException
	{
		UDPOut.this.socket = new DatagramSocket();
		UDPOut.this.socket.setBroadcast(true);
		UDPOut.this.broadcast();
	}

	public synchronized void close()
	{
		if (this.socket != null)
			this.socket.close();
		this.socket = null;
	}

	private void broadcast()
	{
		Inspector.runNowAsDaemon(() ->
		{
			Exception exception = new Exception("No exception occured!");

			while (true)
			{
				boolean worked = true;
				worked = false;

				byte[] sendData = null;
				UDPDetected me = new UDPDetected(new Date());

				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				try
				{
					ObjectOutputStream os = new ObjectOutputStream(outputStream);
					os.writeObject(me);
					sendData = outputStream.toByteArray();
				} catch (Exception e)
				{
					UDPCoordinator.service.publishSync(UDPCoordinator.ERROR, e);
				}

				/*
				 * Try the 255.255.255.255 first
				 */
				for (int i = 0; i < NetworkConfig.TRIES_AMOUNT; i++)
				{
					try
					{
						assert sendData != null;
						final DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"),
								NetworkConfig.PORT.get() + i * NetworkConfig.TRIES_STEPS);
						this.socket.send(sendPacket);
						worked = true;
					} catch (final Exception e)
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
								} catch (final Exception e)
								{
									exception = e;
								}
							}
						}
					}
				} catch (final Exception ignored)
				{
				}

				if (!worked)
				{
					break;
				}

				LOGGER.trace("Sent UDP message! " + me.getTimeStamp());

				try
				{
					Thread.sleep(NetworkConfig.UDP_BROADCAST_INTERVAL);
				} catch (final Exception e)
				{
					LOGGER.fatal("UDP broadcast interval could not be applied!");
				}
			}

			// If this was not closed by user
			if (!(exception instanceof NullPointerException))
			{
				UDPCoordinator.service.publishSync(UDPCoordinator.ERROR, new Exception("No network device suitable for UDP broadcast."));
			}
		});
	}
}
