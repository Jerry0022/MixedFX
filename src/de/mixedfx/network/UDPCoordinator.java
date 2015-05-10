package de.mixedfx.network;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.eventbus.EventBusService;
import de.mixedfx.network.NetworkConfig.States;

public class UDPCoordinator implements org.bushe.swing.event.EventTopicSubscriber<Object>
{
	public static final String			RECEIVE	= "RECEIVE";
	public static final String			ERROR	= "ERROR";

	public static final EventBusService	service	= new EventBusService("UDPCoordinator");

	/**
	 * Just a list of all who made them known at least once (aren't necessarily still active).
	 */
	public ListProperty<InetAddress>	allAdresses;

	/**
	 * Just a list of hosts who made them known at least once (aren't necessarily still active).
	 */
	public ListProperty<InetAddress>	allServerAdresses;

	private final UDPIn					in;
	private final UDPOut				out;

	public UDPCoordinator()
	{
		this.allAdresses = new SimpleListProperty<>(FXCollections.observableArrayList(new ArrayList<>()));
		this.allServerAdresses = new SimpleListProperty<>(FXCollections.observableArrayList(new ArrayList<>()));

		UDPCoordinator.service.subscribe(UDPCoordinator.ERROR, this);
		UDPCoordinator.service.subscribe(UDPCoordinator.RECEIVE, this);

		this.in = new UDPIn();
		this.in.start();

		this.out = new UDPOut();
		this.out.start();
	}

	private void handleNetworkerror(final Exception e)
	{
		NetworkManager.t.stopTCPFull();
		this.stopUDPFull();

		// E. G. if two not servers were started => UDP Server BindException of the second
		EventBusExtended.publishSyncSafe(NetworkManager.NETWORK_FATALERROR, e);
	}

	public void stopUDPFull()
	{
		this.out.close();
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

				// Add all sending NICs to list
				if (!this.allAdresses.contains(packet.getAddress()))
					this.allAdresses.add(packet.getAddress());

				// Add all sending server NICs to list
				if (packetMessage.equals(NetworkConfig.States.Server.toString()) && !this.allServerAdresses.contains(packet.getAddress()))
					this.allServerAdresses.add(packet.getAddress());

				// If I'm searching and the other one is a server or bound to server then let's
				// connect
				if (NetworkConfig.status.get().equals(States.Unbound) && (packetMessage.equals(NetworkConfig.States.Server.toString()) || packetMessage.equals(NetworkConfig.States.BoundToServer.toString())))
					NetworkManager.t.startFullTCP(packet.getAddress());
			}
			else
				if (topic.equals(UDPCoordinator.ERROR))
					this.handleNetworkerror((Exception) data);
		}
	}
}
