package de.mixedfx.network;

import de.mixedfx.eventbus.EventBusExtended;

public class NetworkManager
{
	/**
	 * Register for this event over {@link EventBusExtended} if you want to be informed that there was a network error which closed the entire network. Further information: This error does <b>relate
	 * to the UDP Server in most cases</b>. The cause is probably the port. React to that <b>port error with NetworkManager#setPort(int)</b> with the recommendation to choose a random number between
	 * 10 000 and 60 000!
	 */
	public static final String NETWORK_FATALERROR = "NETWORK_FATALERROR";

	protected static volatile boolean running;

	public static TCPCoordinator	t;
	public static UDPCoordinator	u;

	static
	{
		NetworkManager.t = new TCPCoordinator();
		NetworkManager.u = new UDPCoordinator();
	}

	public synchronized static void start()
	{
		if (!running)
		{
			NetworkManager.u.startUDPFull();
			NetworkManager.t.startServer();
			NetworkManager.running = true;
		}
	}

	public synchronized static void stop()
	{
		NetworkManager.running = false;
		NetworkManager.t.stopTCPFull();
		NetworkManager.u.stopUDPFull();
	}
}
