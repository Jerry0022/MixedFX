package de.mixedfx.network;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import de.mixedfx.eventbus.EventBusService;
import de.mixedfx.network.Overall.NetworkStatus;

public class UDPCoordinator implements org.bushe.swing.event.EventTopicSubscriber<Object>
{
	public static final String			RECEIVE	= "RECEIVE";
	public static final String			ERROR	= "error";

	public static final EventBusService	service	= new EventBusService("UDPCoordinator");

	/**
	 * Just a list of all who made them known at least once (aren't necessarily still active).
	 */
	public ListProperty<InetAddress>	allAdresses;

	/**
	 * Just a list of hosts who made them known at least once (aren't necessarily still active).
	 */
	public ListProperty<InetAddress>	allServerAdresses;

	/**
	 * The current host.
	 */
	public InetAddress					nextHostAdress;

	public UDPCoordinator()
	{
		this.allAdresses = new SimpleListProperty<>(FXCollections.observableArrayList(new ArrayList<>()));
		this.allServerAdresses = new SimpleListProperty<>(FXCollections.observableArrayList(new ArrayList<>()));

		UDPCoordinator.service.subscribe(UDPCoordinator.ERROR, this);
		UDPCoordinator.service.subscribe(UDPCoordinator.RECEIVE, this);

		final UDPIn in = new UDPIn();
		in.start();

		final UDPOut out = new UDPOut();
		out.start();
	}

	private void handleNetworkerror(final Exception e)
	{
		e.printStackTrace();
	}

	@Override
	public synchronized void onEvent(final String topic, final Object data)
	{
		if (topic.equals(UDPCoordinator.RECEIVE))
		{
			final DatagramPacket packet = (DatagramPacket) data;
			final String packetMessage = new String(packet.getData(), 0, packet.getLength());

			// Add all sending NICs to list
			if (!this.allAdresses.contains(packet.getAddress()))
				this.allAdresses.add(packet.getAddress());

			// Add all sending server NICs to list
			if (packetMessage.equals(Overall.NetworkStatus.Server.toString()) && !this.allServerAdresses.contains(packet.getAddress()))
				this.allServerAdresses.add(packet.getAddress());

			// If I'm searching and the other one is a server or bound to server then let's connect
			if (Overall.status.get().equals(NetworkStatus.Unbound) && (packetMessage.equals(Overall.NetworkStatus.Server.toString()) || packetMessage.equals(Overall.NetworkStatus.BoundToServer.toString())))
			{
				this.nextHostAdress = packet.getAddress();
				// TODO Open TCP Connection to first server replied and save if host or boundhost
				// (of packetMessage)
				System.out.println(Overall.NetworkStatus.valueOf(packetMessage));
				Overall.status.set(Overall.NetworkStatus.valueOf(packetMessage));
			}
		}
		else if (topic.equals(UDPCoordinator.ERROR))
			this.handleNetworkerror((Exception) data);
	}
}
