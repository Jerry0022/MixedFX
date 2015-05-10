package de.mixedfx.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.PortUnreachableException;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;

import org.omg.CORBA.TIMEOUT;

/**
 * Thread-safe implementation.
 * 
 * @author JerryMobil
 *
 */
public class DiscoveryManager
{
	public enum Messages
	{
		SEARCH, ACCEPTED;
	}

	private final static int	PORT			= 8888;
	private final static int	CLIENT_INTERVAL	= 3000;

	private AtomicBoolean		RUNNING;
	private DatagramSocket		socket;

	public DiscoveryManager()
	{
		RUNNING = new AtomicBoolean();
	}

	/**
	 * Starts listening for a packet. Just waits until the first {@link Messages#SEARCH} is
	 * received. Sends back an {@link Messages#ACCEPTED}.
	 * 
	 * Can be interrupted at any time with {@link DiscoveryManager#stop()}.
	 * 
	 * @throws PortUnreachableException
	 *             Thrown if {@link DiscoveryManager#PORT} is not available.
	 * @throws UnknownNetworkErrorException
	 *             Thrown if an unknown error occured.
	 */
	public void startServer() throws PortUnreachableException, UnknownNetworkErrorException
	{
		try
		{
			synchronized (RUNNING)
			{
				RUNNING.set(true);
				// Keep a socket open to listen to all the UDP trafic that is destined for this port
				socket = new DatagramSocket(PORT, InetAddress.getByName("0.0.0.0"));
				socket.setBroadcast(true);
			}

			// Wait until first SEARCH message was received.
			while (true)
			{
				System.out.println(getClass().getName() + "Server" + ">>>Ready to receive broadcast packets!");

				// Receive a packet
				byte[] recvBuf = new byte[15000];
				DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
				socket.receive(packet); // BLOCKING

				// Packet received
				System.out.println(getClass().getName() + "Server" + ">>>Discovery packet received from: " + packet.getAddress().getHostAddress());
				System.out.println(getClass().getName() + "Server" + ">>>Packet received; data: " + new String(packet.getData()));

				// Check if the packet holds the right command (message)
				String message = new String(packet.getData()).trim();
				if (message.equals(Messages.SEARCH.toString()))
				{
					byte[] sendData = Messages.ACCEPTED.toString().getBytes();

					// Send a response
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
					socket.send(sendPacket);

					System.out.println(getClass().getName() + "Server" + ">>>Sent packet to: " + sendPacket.getAddress().getHostAddress());
					break;
				}
			}

			synchronized (RUNNING)
			{
				socket.close();
				RUNNING.set(false);
				return;
			}
		}
		catch (IOException ex)
		{
			synchronized (RUNNING)
			{
				if (RUNNING.getAndSet(false))
				{
					if (ex instanceof PortUnreachableException)
						throw (PortUnreachableException) ex;
					else
						throw new UnknownNetworkErrorException("Unknown");
				}
				// Else stop() was called
				return;
			}
		}
	}

	/**
	 * <pre>
	 * Sends the {@link Messages#SEARCH} message once as broadcast and once over every available network interface
	 * (IPv6 is not supported). Ignores failures of both sending messages. 
	 * 
	 * Processes first reply message of type {@link Messages#ACCEPTED}.
	 * 
	 * If no message was received during {@link TIMEOUT} then the method calls itself again (resending the {@link Messages#SEARCH} message).
	 * </pre>
	 * 
	 * @throws PortUnreachableException
	 *             Thrown if {@link DiscoveryManager#PORT} is not available.
	 * @throws UnknownNetworkErrorException
	 *             Thrown if an unknown error occured.
	 * @return Returns the found IPv4 as String or null in case of {@link #stop()} was called
	 *         before.
	 */
	public String startClient() throws PortUnreachableException, UnknownNetworkErrorException
	{
		// Find the server using UDP broadcast
		try
		{
			synchronized (RUNNING)
			{
				RUNNING.set(true);
				// Open a random port to send the package
				socket = new DatagramSocket();
				socket.setBroadcast(true);
			}

			byte[] sendData = Messages.SEARCH.toString().getBytes();

			// Try the 255.255.255.255 first
			try
			{
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), PORT);
				socket.send(sendPacket);
				System.out.println(getClass().getName() + "Client" + ">>> Request packet sent to: 255.255.255.255 (DEFAULT)");
			}
			catch (Exception e)
			{
			}

			// Broadcast the message over all the network interfaces
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
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
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, PORT);
						socket.send(sendPacket);
					}
					catch (Exception e)
					{
					}

					System.out.println(getClass().getName() + "Client" + ">>> Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
				}
			}

			System.out.println(getClass().getName() + "Client" + ">>> Done looping over all network interfaces. Now waiting for a reply!");

			String serverIP = null;
			while (true)
			{
				// Wait for a response for some time and if timeout then resend
				byte[] recvBuf = new byte[15000];
				DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
				socket.setSoTimeout(CLIENT_INTERVAL);
				try
				{
					socket.receive(receivePacket); // BLOCKING
				}
				catch (SocketTimeoutException timeout)
				{
					return startClient();
				}

				// We have a response
				System.out.println(getClass().getName() + "Client" + ">>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress());

				// Check if the message is correct
				String message = new String(receivePacket.getData()).trim();
				if (message.equals(Messages.ACCEPTED.toString()))
				{
					serverIP = receivePacket.getAddress().getHostAddress();
					System.out.println(getClass().getName() + "Client" + ">>> Message confirmed: " + receivePacket.getAddress().getHostAddress());
					break;
				}
			}

			synchronized (RUNNING)
			{
				socket.close();
				RUNNING.set(false);
				return serverIP;
			}
		}
		catch (IOException ex)
		{
			synchronized (RUNNING)
			{
				if (RUNNING.getAndSet(false))
				{
					if (ex instanceof PortUnreachableException)
						throw (PortUnreachableException) ex;
					else
						throw new UnknownNetworkErrorException("Unknown");
				}
				// Else stop() was called
				return null;
			}
		}
	}

	/**
	 * Stops DiscoveryManager server or client.
	 */
	public void stop()
	{
		synchronized (RUNNING)
		{
			if (RUNNING.get())
			{
				RUNNING.set(false);
				socket.close();
			}
		}
	}
}
