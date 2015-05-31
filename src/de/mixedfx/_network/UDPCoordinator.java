package de.mixedfx._network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

import org.bushe.swing.event.EventTopicSubscriber;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.eventbus.EventBusService;
import de.mixedfx.network.NetworkConfig.States;

class UDPCoordinator implements EventTopicSubscriber<Object>
{
	public static final String			RECEIVE	= "RECEIVE";
	public static final String			ERROR	= "ERROR";

	public static final EventBusService	service	= new EventBusService("UDPCoordinator");

	/**
	 * Just a list of all who made them known at least once (aren't necessarily still active).
	 */
	public ListProperty<String>			allAdresses;

	/**
	 * Just a list of hosts who made them known at least once (aren't necessarily still active).
	 */
	public ListProperty<String>			allServerAdresses;

	private final UDPIn					in;
	private final UDPOut				out;

	public UDPCoordinator() throws IOException
	{
		this.allAdresses = new SimpleListProperty<>(FXCollections.observableArrayList(new ArrayList<>()));
		this.allServerAdresses = new SimpleListProperty<>(FXCollections.observableArrayList(new ArrayList<>()));

		UDPCoordinator.service.subscribe(UDPCoordinator.ERROR, this);
		UDPCoordinator.service.subscribe(UDPCoordinator.RECEIVE, this);

		this.in = new UDPIn();
		if (!this.in.start())
			throw new IOException("UDP exception occured!");
		else
		{
			this.out = new UDPOut();
			if (!this.out.start())
				throw new IOException("UDP exception occured!");
		}
	}

	private void handleNetworkerror(final Exception e)
	{
		NetworkManager.t.stopTCPFull();
		this.stopUDPFull();

		// E. G. if two not servers were started => UDP Server BindException of the second
		EventBusExtended.publishAsyncSafe(NetworkManager.NETWORK_FATALERROR, e);
	}

	public void stopUDPFull()
	{
		if (this.out != null)
			this.out.close();
		if (this.in != null)
			this.in.close();
	}

	@Override
	public synchronized void onEvent(final String topic, final Object data)
	{
		synchronized (NetworkConfig.status)
		{
			if (topic.equals(UDPCoordinator.RECEIVE))
			{
				final DatagramPacket packet = (DatagramPacket) data;
				final String packetMessage = new String(packet.getData(), 0, packet.getLength());

				// System.out.println(packet.getAddress().getHostAddress());

				/*
				 * Check all interfaces if it was a broadcast to myself!
				 */
				boolean ownOne = false;
				try
				{
					final Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
					while (nics.hasMoreElements())
					{
						final NetworkInterface nic = nics.nextElement();
						for (final InetAddress nicAdress : Collections.list(nic.getInetAddresses()))
							if (this.compare(nicAdress, packet.getAddress()))
								ownOne = true;
					}
				}
				catch (final SocketException e)
				{
				}

				if (!ownOne)
				{
					// Add all sending NICs to list
					if (!this.allAdresses.contains(packet.getAddress().getHostAddress()))
						this.allAdresses.add(packet.getAddress().getHostAddress());

					// Add all sending server NICs to list
					if (packetMessage.equals(NetworkConfig.States.Server.toString()) && !this.allServerAdresses.contains(packet.getAddress().getHostAddress()))
						this.allServerAdresses.add(packet.getAddress().getHostAddress());

					// If I'm searching and the other one is a server or bound to server then let's
					// connect
					if (NetworkConfig.status.get().equals(States.Unbound) && (packetMessage.equals(NetworkConfig.States.Server.toString()) || packetMessage.equals(NetworkConfig.States.BoundToServer.toString())))
						NetworkManager.t.startFullTCP(packet.getAddress());
				}
			}
			else if (topic.equals(UDPCoordinator.ERROR))
				this.handleNetworkerror((Exception) data);
		}
	}

	private boolean compare(final InetAddress ip1, final InetAddress ip2)
	{
		return ip1.getHostAddress().equals(ip2.getHostAddress());
	}
}
