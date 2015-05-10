package de.mixedfx.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;

import com.sun.javafx.collections.ObservableListWrapper;

class DiscoveryIndia
{
	/**
	 * IP Addresses can be got via {@link InetAddress#getHostAddress()}. The list is updated by
	 * another
	 */
	public ReadOnlyListProperty<InetAddress>	discovered;

	private DatagramSocket						socket;

	/**
	 * @throws Exception
	 *             Throws exception if socket can't be opened on the port or SecurityManager
	 *             disallows it
	 */
	public DiscoveryIndia() throws Exception
	{
		discovered = new ReadOnlyListWrapper<>(new ObservableListWrapper<InetAddress>(new ArrayList<InetAddress>()));

		// Keep a socket open to listen to all the UDP trafic that is destined for this port
		socket = new DatagramSocket(Discovery.PORT, InetAddress.getByName("0.0.0.0"));
		socket.setBroadcast(true);
	}

	/**
	 * Starts listening for the {@link Discovery.Messages#DISCOVERY} and puts the InetAddresses of
	 * the sender into the {@link #discovered} ListProperty.
	 */
	public synchronized void startListening()
	{
		try
		{
			// Wait until first SEARCH message was received.
			while (true)
			{
				System.out.println(getClass().getName() + "Server" + ">>>Ready to receive broadcast packets!");

				// Receive a packet
				byte[] recvBuf = new byte[15000];
				DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
				socket.receive(packet); // BLOCKING

				if (new String(packet.getData(), 0, packet.getLength()).equals(Discovery.Messages.DISCOVERY.toString()))
				{

					if (!containsIP(packet.getAddress()))
					{
						addIP(packet.getAddress());
					}
				}
			}
		}
		catch (IOException e)
		{
		}
		finally
		{
			socket.close();
		}
	}

	public void stopListening()
	{
		socket.close();
	}

	private void addIP(InetAddress ip)
	{
		synchronized (discovered)
		{
			Runnable r = new Runnable()
			{
				@Override
				public void run()
				{
					discovered.add(ip);
				}
			};
			Thread t = new Thread(r);
			t.start();
		}
	}

	public void removeIP(InetAddress ip)
	{
		synchronized (discovered)
		{
			discovered.remove(ip);
		}
	}

	private boolean containsIP(InetAddress ip)
	{
		synchronized (discovered)
		{
			return discovered.contains(ip);
		}
	}
}
