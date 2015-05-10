package de.mixedfx.network.archive;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;

import com.sun.javafx.collections.ObservableListWrapper;

import de.mixedfx.network.NetworkConfig;
import de.mixedfx.network.NetworkConfig.States;

class DiscoveryIndia
{
	/**
	 * IP Addresses can be got via {@link InetAddress#getHostAddress()}. The list is updated by
	 * another
	 */
	public ReadOnlyListProperty<InetAddress>	discovered;

	private final DatagramSocket				socket;

	/**
	 * @throws Exception
	 *             Throws exception if socket can't be opened on the port or SecurityManager
	 *             disallows it
	 */
	public DiscoveryIndia() throws Exception
	{
		this.discovered = new ReadOnlyListWrapper<>(new ObservableListWrapper<InetAddress>(new ArrayList<InetAddress>()));

		// Keep a socket open to listen to all the UDP trafic that is destined for this port
		this.socket = new DatagramSocket(Discovery.PORT_UDP_SERVER, InetAddress.getByName("0.0.0.0"));
		this.socket.setBroadcast(true);
	}

	/**
	 * Starts listening for the {@link Discovery.Messages#DISCOVERY_REQUEST} and puts the
	 * InetAddresses of the sender into the {@link #discovered} ListProperty.
	 */
	public synchronized void startListening()
	{
		try
		{
			// Wait until first SEARCH message was received.
			while (true)
			{
				System.out.println(this.getClass().getName() + "Server" + ">>>Ready to receive broadcast packets!");

				// Receive a packet
				final byte[] recvBuf = new byte[15000];
				final DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
				this.socket.receive(packet); // BLOCKING

				if (new String(packet.getData(), 0, packet.getLength()).equals(Discovery.Messages.DISCOVERY_REQUEST.toString()))
					if (!this.containsIP(packet.getAddress())) // TODO Really necessary?
						if (NetworkConfig.status.get().equals(NetworkConfig.States.Server) || NetworkConfig.status.get().equals(States.BoundToServer))
						{
							final byte[] sendData = Discovery.Messages.DISCOVERY_ANSWER.toString().getBytes();

							try
							{
								final DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), Discovery.PORT_UDP_CLIENT);
								this.socket.send(sendPacket);
								System.out.println("SENT ANSWER");
								this.addIP(packet.getAddress());
							}
							catch (final IOException e)
							{
							}
						}
			}
		}
		catch (final IOException e)
		{
		}
		finally
		{
			this.socket.close();
		}
	}

	public void stopListening()
	{
		this.socket.close();
	}

	private void addIP(final InetAddress ip)
	{
		synchronized (this.discovered)
		{
			final Runnable r = () -> DiscoveryIndia.this.discovered.add(ip);
			final Thread t = new Thread(r);
			t.setDaemon(true);
			t.start();
		}
	}

	public void removeIP(final InetAddress ip)
	{
		synchronized (this.discovered)
		{
			this.discovered.remove(ip);
		}
	}

	private boolean containsIP(final InetAddress ip)
	{
		synchronized (this.discovered)
		{
			return this.discovered.contains(ip);
		}
	}
}
