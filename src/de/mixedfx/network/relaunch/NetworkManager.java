package de.mixedfx.network.relaunch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.util.Duration;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.inspector.Inspector;
import de.mixedfx.java.CustomSysOutErr;
import de.mixedfx.network.relaunch.NetworkConfig.States;

public class NetworkManager
{
	/**
	 * Register for this event over {@link EventBusExtended} if you want to be informed that there
	 * was a network error which closed the entire network. Further information: This error does
	 * <b>relate to the UDP Server in most cases</b> (but not in case of if
	 * {@link NetworkConfig#status} = {@link NetworkConfig.States#Server}). The cause is probably
	 * the port. React to that <b>port error with {@link NetworkManager#setPort(int)}</b> with the
	 * recommendation to choose a random number between 10 000 and 60 000!
	 */
	public static final String			NETWORK_FATALERROR	= "NETWORK_FATALERROR";

	protected static volatile boolean	running;

	protected static TCPCoordinator		t;
	private static UDPCoordinator		u;

	static
	{
		NetworkConfig.status.addListener((ChangeListener<States>) (observable, oldValue, newValue) ->
		{
			switch (newValue)
			{
				case Unbound:
					/*
					 * Auto reconnect: Situation: Fall back from Server or BoundToServer to Unbound.
					 * Just let it happen but reconnect after an interval multiplied with the index
					 * of when I joined the network.
					 */
					// Copy values
					final List<Integer> lastActivityList = new ArrayList<>(ParticipantManager.PARTICIPANTS);
					Collections.sort(lastActivityList);
					final int myIndex = lastActivityList.indexOf(ParticipantManager.MY_PID.get());

					// Stop ParticipantManager
					ParticipantManager.stop();

					System.out.println("Seconds to wait: " + Duration.millis(NetworkConfig.BROADCAST_INTERVAL).multiply(NetworkConfig.RECONNECT_TOLERANCE).multiply(myIndex).toMillis());

					Inspector.runLater(() ->
					{
						System.out.println("ACHTUNG: " + NetworkConfig.status.get());
						if (!NetworkConfig.status.get().equals(States.BoundToServer))
						{
							System.out.println("ACHTUNG SERVER AUTO START");
							NetworkManager.host();
						}
					}, Duration.millis(NetworkConfig.BROADCAST_INTERVAL).multiply(NetworkConfig.RECONNECT_TOLERANCE).multiply(myIndex));
					break;
				case BoundToServer:
					ParticipantManager.start().connect();
					break;
				case Server:
					// Add me as server also as participant
					ParticipantManager.MY_PID.set(ParticipantManager.PARTICIPANT_NUMBER);
					ParticipantManager.PARTICIPANTS.add(ParticipantManager.PARTICIPANT_NUMBER++);
					ParticipantManager.start();
					break;
				default:
					break;
			}
		});

		NetworkManager.t = new TCPCoordinator();

		try
		{
			NetworkManager.u = new UDPCoordinator();
		}
		catch (final IOException e)
		{}
	}

	public synchronized static void start()
	{
		NetworkManager.u.startUDPFull();
		NetworkManager.running = true;
	}

	/**
	 * If Network is running ({@link #start()} was called before), this method sets this client to
	 * host the p2p network!
	 */
	public synchronized static void host()
	{
		if (NetworkManager.running)
		{
			NetworkConfig.status.set(States.Server);
		}
	}

	/**
	 * If Network is running ({@link #start()} was called before and maybe {@link #host()}), this
	 * method sets this client to leave the p2p network! It may reconnect immediately to a
	 * boundToServer or hosts itself a Server after some while.
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

	public static void main(final String[] args)
	{
		CustomSysOutErr.init();

		// Catch fatal errors to show (network reacted already to this error)
		AnnotationProcessor.process(new NetworkManager());

		NetworkConfig.status.addListener((ChangeListener<States>) (observable, oldValue, newValue) -> System.out.println("OLD: " + oldValue + "! NEW: " + newValue));

		ParticipantManager.PARTICIPANTS.addListener((ListChangeListener<Integer>) c ->
		{
			System.out.println(ParticipantManager.PARTICIPANTS);
		});

		// Show all directly found applications host and all directly found Server (Not the
		// bound to
		// server ones) which were once online while this application was online.
		UDPCoordinator.allAdresses.addListener((ListChangeListener<UDPDetected>) c ->
		{
			c.next();
			System.out.println("ALL: " + c.getAddedSubList().get(0).address + "!" + c.getAddedSubList().get(0).status);
		});

		NetworkManager.start();
		// Inspector.runLater(() ->
		// {
		// System.out.println("LEAVE network");
		// NetworkManager.leave();
		// }, Duration.seconds(15));

		while (true)
		{
			;
		}
	}

	@EventTopicSubscriber(topic = NetworkManager.NETWORK_FATALERROR)
	public void error(final String topic, final Exception exception)
	{
		System.out.println("Caught error: " + exception.getMessage());
	}
}
