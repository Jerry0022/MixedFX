package de.mixedfx.network;

import java.net.DatagramPacket;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import de.mixedfx.eventbus.EventBusService;
import de.mixedfx.network.Overall.NetworkStatus;

public class UDPCoordinator implements org.bushe.swing.event.EventTopicSubscriber<Object>
{
	public static final String			RECEIVE	= "RECEIVE";
	public static final String			ERROR	= "error";

	public static final EventBusService	service	= new EventBusService("UDPCoordinator");
	private volatile BooleanProperty	current_status;

	public UDPCoordinator()
	{
		UDPCoordinator.service.subscribe(UDPCoordinator.ERROR, this);
		UDPCoordinator.service.subscribe(UDPCoordinator.RECEIVE, this);

		this.current_status = new SimpleBooleanProperty(false);

		final UDPIn in = new UDPIn();
		in.start();

		final UDPOut out = new UDPOut();
		out.start();
	}

	private synchronized void handleNetworkerror(final Exception e)
	{
		e.printStackTrace();
	}

	@Override
	public synchronized void onEvent(final String topic, final Object data)
	{
		if (topic.equals(UDPCoordinator.RECEIVE))
		{
			final DatagramPacket packet = (DatagramPacket) data;
			System.out.println(packet.getAddress().toString());
			if (Overall.status.get().equals(NetworkStatus.Server) || Overall.status.get().equals(NetworkStatus.BoundToServer))
			{
				// Reply
				;
				;
			}
			else
				// Show in console that there is another Server
				;
		}
		else if (topic.equals(UDPCoordinator.ERROR))
			if (this.current_status.get())
				this.handleNetworkerror((Exception) data);
			else
				; // Willed closed connection
	}
}
