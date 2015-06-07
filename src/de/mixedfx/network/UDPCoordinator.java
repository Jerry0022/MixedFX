package de.mixedfx.network;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.time.Instant;
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
import de.mixedfx.network.NetworkConfig.States;

public class UDPCoordinator implements EventTopicSubscriber<Object>
{
	public static final String				RECEIVE	= "RECEIVE";
	public static final String				ERROR	= "ERROR";

	public static final EventBusService		service	= new EventBusService("UDPCoordinator");

	/**
	 * Just a list of all who made them known at least once (maybe aren't still active). An replaced
	 * event to the listener is only submitted if the state changed, not if the last contact was
	 * updated. This list isn't cleared as long as the network is running!
	 */
	public static ListProperty<UDPDetected>	allAdresses;

	static
	{
		UDPCoordinator.allAdresses = new SimpleListProperty<>(FXCollections.observableArrayList(new ArrayList<>()));
	}

	private boolean							running;

	private final UDPIn						in;
	private final UDPOut					out;

	public UDPCoordinator()
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

		NetworkConfig.statusChangeTime.set(new Date());

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

		UDPCoordinator.allAdresses.clear();

		NetworkConfig.statusChangeTime.set(new Date());
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
							if (nicAdress.getHostAddress().equals(packet.getAddress().getHostAddress()))
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
					/*
					 * Register / Update the client in local list of UDP contacts.
					 */
					final UDPDetected newDetected = new UDPDetected(packet.getAddress(), Date.from(Instant.parse(packetMessage.split("\\!")[0])), NetworkConfig.States.valueOf(packetMessage.split("\\!")[1]), Date.from(Instant.parse(packetMessage.split("\\!")[2])));

					// Add all sending NICs to list
					final Predicate predicate = ApacheTools.convert(UDPDetected.getByAddress(newDetected.address));
					if (CollectionUtils.exists(UDPCoordinator.allAdresses, predicate))
					{
						CollectionUtils.select(UDPCoordinator.allAdresses, predicate).forEach(t ->
						{
							final UDPDetected localDetected = (UDPDetected) t;
							if (newDetected.timeStamp.after(localDetected.timeStamp))
							{
								final States oldStatus = States.valueOf(localDetected.status.toString());
								localDetected.update(newDetected.status, newDetected.timeStamp);
								if (!oldStatus.equals(newDetected.status))
								{
									UDPCoordinator.allAdresses.set(UDPCoordinator.allAdresses.indexOf(localDetected), localDetected);
								}
							}
							else
							{
								return; // Old UDP Packet, newer one was already received!
							}
						});
					}
					else
					{
						UDPCoordinator.allAdresses.add(newDetected);
					}

					/*
					 * If I'm searching and the other one is a server or bound to server then let's
					 * connect
					 */
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
}
