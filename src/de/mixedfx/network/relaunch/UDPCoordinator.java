package de.mixedfx.network.relaunch;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.bushe.swing.event.EventTopicSubscriber;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.eventbus.EventBusService;
import de.mixedfx.java.ApacheTools;
import de.mixedfx.network.relaunch.NetworkConfig.States;

class UDPCoordinator implements EventTopicSubscriber<Object>
{
	public static final String				RECEIVE	= "RECEIVE";
	public static final String				ERROR	= "ERROR";

	public static final EventBusService		service	= new EventBusService("UDPCoordinator");

	/**
	 * Just a list of all who made them known at least once (maybe aren't still active).
	 */
	public static ListProperty<UDPDetected>	allAdresses;

	static
	{
		UDPCoordinator.allAdresses = new SimpleListProperty<>(FXCollections.observableArrayList(new ArrayList<>()));
	}

	private boolean							running;

	private final UDPIn						in;
	private final UDPOut					out;

	public UDPCoordinator() throws IOException
	{
		/*
		 * Get events of UDPOut and UDPIn
		 */
		UDPCoordinator.service.subscribe(UDPCoordinator.ERROR, this);
		UDPCoordinator.service.subscribe(UDPCoordinator.RECEIVE, this);

		/*
		 * Start UDP Server and if started successfully start UDPClient
		 */
		this.in = new UDPIn();
		this.out = new UDPOut();
	}

	public synchronized void startUDPFull()
	{
		this.running = true;

		this.in.start();
		if (this.running)
		{
			this.out.start();
		}
	}

	public synchronized void stopUDPFull()
	{
		this.running = false;

		if (this.out != null)
		{
			this.out.close();
		}
		if (this.in != null)
		{
			this.in.close();
		}
	}

	@SuppressWarnings("unchecked")
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
						{
							if (this.compareIP(nicAdress, packet.getAddress()))
							{
								ownOne = true;
							}
						}
					}
				}
				catch (final SocketException e)
				{}

				if (!ownOne)
				{
					final UDPDetected newDetected = new UDPDetected(packet.getAddress(), NetworkConfig.States.valueOf(packetMessage), new Date());

					// Add all sending NICs to list
					final Predicate predicate = ApacheTools.convert(UDPDetected.getByAddress(newDetected.address));
					if (CollectionUtils.exists(UDPCoordinator.allAdresses, predicate))
					{
						CollectionUtils.select(UDPCoordinator.allAdresses, predicate).forEach(t ->
						{
							((UDPDetected) t).update(newDetected.status, new Date());
						});
					}
					else
					{
						UDPCoordinator.allAdresses.add(newDetected);
					}

					// If I'm searching and the other one is a server or bound to server then let's
					// connect
					if (NetworkConfig.status.get().equals(States.Unbound) && (newDetected.status.equals(NetworkConfig.States.Server) || newDetected.status.equals(NetworkConfig.States.BoundToServer)))
					{
						NetworkManager.t.startFullTCP(packet.getAddress());
					}
				}
			}
			else
				if (topic.equals(UDPCoordinator.ERROR))
				{
					NetworkManager.t.stopTCPFull();
					this.stopUDPFull();

					EventBusExtended.publishAsyncSafe(NetworkManager.NETWORK_FATALERROR, data);
				}
		}
	}

	private boolean compareIP(final InetAddress ip1, final InetAddress ip2)
	{
		return ip1.getHostAddress().equals(ip2.getHostAddress());
	}
}