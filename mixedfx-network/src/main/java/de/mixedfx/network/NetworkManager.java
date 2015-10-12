package de.mixedfx.network;

import de.mixedfx.eventbus.EventBusExtended;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NetworkManager
{
	/**
	 * Register for this event over {@link EventBusExtended} if you want to be informed that there was a network error which closed the entire network. Further information: This error does <b>relate
	 * to the UDP Server in most cases</b>. The cause is probably the port. React to that <b>port error with NetworkManager#setPort(int)</b> with the recommendation to choose a random number between
	 * 10 000 and 60 000!
	 */
	public static final String NETWORK_FATALERROR = "NETWORK_FATALERROR";

	protected volatile boolean running;

	@Autowired
	public TCPCoordinator t;
	@Autowired
	public UDPCoordinator u;

	public synchronized void start()
	{
		if (!running)
		{
			u.startUDPFull();
			t.startServer();
			running = true;
		}
	}

	public synchronized void stop()
	{
		running = false;
		t.stopTCPFull();
		u.stopUDPFull();
	}
}
