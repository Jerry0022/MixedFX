package de.mixedfx.network.relaunch;

import java.io.IOException;

import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.eventbus.EventBusExtended;
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
	public static final String		NETWORK_FATALERROR	= "NETWORK_FATALERROR";

	protected static TCPCoordinator	t;
	private static UDPCoordinator	u;

	static
	{
		NetworkConfig.status.addListener((ChangeListener<States>) (observable, oldValue, newValue) ->
		{
			switch (newValue)
			{
				case Unbound:
					ParticipantManager.stop();
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

	public static void start()
	{
		NetworkManager.u.startUDPFull();
	}

	public static void stop()
	{
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
		NetworkConfig.status.set(States.Server);

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
