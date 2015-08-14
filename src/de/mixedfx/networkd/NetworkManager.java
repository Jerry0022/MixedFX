package de.mixedfx.networkd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.inspector.Inspector;
import de.mixedfx.logging.Log;
import de.mixedfx.network.NetworkConfig.States;
import javafx.beans.value.ChangeListener;
import javafx.util.Duration;

public class NetworkManager
{
	/**
	 * Register for this event over {@link EventBusExtended} if you want to be informed that there was a network error which closed the entire
	 * network. Further information: This error does <b>relate to the UDP Server in most cases</b> (but not in case of if {@link NetworkConfig#STATUS}
	 * = {@link NetworkConfig.States#SERVER}). The cause is probably the port. React to that <b>port error with NetworkManager#setPort(int)</b> with
	 * the recommendation to choose a random number between 10 000 and 60 000!
	 */
	public static final String NETWORK_FATALERROR = "NETWORK_FATALERROR";

	protected static volatile boolean running;

	protected static TCPCoordinator	t;
	private static UDPCoordinator	u;

	static
	{
		NetworkManager.t = new TCPCoordinator();
		NetworkManager.u = new UDPCoordinator();

		ServiceManager.register(new NetworkSinceService());

		NetworkConfig.STATUS.addListener((ChangeListener<States>) (observable, oldValue, newValue) ->
		{
			switch (newValue)
			{
				case UNBOUND:
					ServiceManager.stop();
					/*
					 * Auto reconnect: Situation: Fall back from Server or BoundToServer to Unbound. Just let it happen but reconnect after an
					 * interval multiplied with the index of when I joined the network.
					 */
					// Copy values
					final List<Integer> lastActivityList = new ArrayList<>(ParticipantManager.PARTICIPANTS);
					Collections.sort(lastActivityList);
					final int myIndex = lastActivityList.indexOf(ParticipantManager.MY_PID.get()) + 1;

					// Stop ParticipantManager
					ParticipantManager.stop();

					final Duration waitTime = Duration.millis(NetworkConfig.UDP_BROADCAST_INTERVAL).multiply(NetworkConfig.RECONNECT_TOLERANCE).multiply(myIndex);
					Log.network.info("Wait " + waitTime.toSeconds() + " seconds before try to start server.");

					Inspector.runLater(() ->
					{
						synchronized (NetworkConfig.STATUS)
						{
							Log.network.debug("Try reconnect only if this status is not already 'BoundToServer': " + NetworkConfig.STATUS.get());
							if (!NetworkConfig.STATUS.get().equals(States.BOUNDTOSERVER))
							{
								Log.network.info("Autostart Server!");
								NetworkManager.host();
							}
						}
					} , waitTime);
					break;
				case BOUNDTOSERVER:
					ParticipantManager.start().connect();
					// See ParticipantManager for the start of client() in ConnectivityManager
					// ServiceManager shall be first client() if I got my PID from the server back!
					break;
				case SERVER:
					// Add me as server also as participant
					ParticipantManager.MY_PID.set(ParticipantManager.PARTICIPANT_NUMBER);
					ParticipantManager.PARTICIPANTS.add(ParticipantManager.PARTICIPANT_NUMBER++);
					ParticipantManager.start();
					ServiceManager.host();
					break;
				default:
					break;
			}
		});
	}

	public synchronized static void start()
	{
		if (!running)
		{
			NetworkManager.u.startUDPFull();
			NetworkManager.running = true;
		}
	}

	/**
	 * If Network is running ({@link #start()} was called before), this method sets this client to host the p2p network!
	 */
	public synchronized static void host()
	{
		if (NetworkManager.running)
		{
			NetworkConfig.STATUS.set(States.SERVER);
		}
	}

	/**
	 * If Network is running ({@link #start()} was called before and maybe {@link #host()}), this method sets this client to leave the p2p network! It
	 * may reconnect immediately to a boundToServer or hosts itself a Server after some while.
	 */
	public synchronized static void leave()
	{
		if (NetworkManager.running)
		{
			NetworkManager.t.stopTCPFull();
		}
	}

	public synchronized static void stop()
	{
		NetworkManager.running = false;
		NetworkManager.t.stopTCPFull();
		NetworkManager.u.stopUDPFull();
	}
}