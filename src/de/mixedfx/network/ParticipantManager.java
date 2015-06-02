package de.mixedfx.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.network.messages.Message;
import de.mixedfx.network.messages.ParticipantMessage;

public class ParticipantManager
{
	/**
	 * Last id is always the server, first id is always the newest online client. Is maybe empty.
	 */
	public static SimpleListProperty<Integer>	PARTICIPANTS				= new SimpleListProperty<Integer>(FXCollections.observableList(Collections.synchronizedList(new ArrayList<>())));

	private static final int					PARTICIPANT_NUMBER_SERVER	= 1;
	protected static int						PARTICIPANT_NUMBER			= ParticipantManager.PARTICIPANT_NUMBER_SERVER;

	/**
	 * 0 means the application is not yet registered in the network.
	 */
	public static final AtomicInteger			MY_PID;

	private static ParticipantManager			pManager;

	static
	{
		MY_PID = new AtomicInteger();
		ParticipantManager.pManager = new ParticipantManager();
	}

	protected static ParticipantManager start()
	{
		// Listen for others
		AnnotationProcessor.process(ParticipantManager.pManager);
		return ParticipantManager.pManager;
	}

	protected static void stop()
	{
		ParticipantManager.MY_PID.set(0);
		ParticipantManager.PARTICIPANT_NUMBER = 1;
		ParticipantManager.PARTICIPANTS.get().clear();
		AnnotationProcessor.unprocess(ParticipantManager.pManager);
	}

	/**
	 * Is only used once to request the network id.
	 */
	private String	myUID;

	public ParticipantManager()
	{
	}

	public void connect()
	{
		final ParticipantMessage message = new ParticipantMessage();
		this.myUID = message.uID;
		EventBusExtended.publishSyncSafe(MessageBus.MESSAGE_SEND, message);
	}

	@EventTopicSubscriber(topic = MessageBus.MESSAGE_RECEIVE)
	public synchronized void receive(final String topic, final Message message)
	{
		if (message instanceof ParticipantMessage)
		{
			final ParticipantMessage pMessage = (ParticipantMessage) message;

			if (pMessage.uID.equals("") && NetworkConfig.status.get().equals(NetworkConfig.States.Server))
			{
				// PIDs were lost
				for (final Integer i : pMessage.ids)
				{
					ParticipantManager.PARTICIPANTS.remove(i);
				}
				pMessage.uID = String.valueOf(ParticipantManager.PARTICIPANT_NUMBER_SERVER);
				pMessage.ids.clear();
				pMessage.ids.addAll(ParticipantManager.PARTICIPANTS);
				EventBusExtended.publishSyncSafe(MessageBus.MESSAGE_SEND, message);
			}
			else
				if (!pMessage.uID.equals(""))
				{
					if (pMessage.ids.isEmpty()) // PID Request from client
					{
						final int clientNr = ParticipantManager.PARTICIPANT_NUMBER++;
						System.err.println(pMessage.uID + "   !   " + pMessage.ids);
						pMessage.ids.add(clientNr);
						pMessage.ids.addAll(ParticipantManager.PARTICIPANTS);
						System.err.println(pMessage.uID + "   !   " + pMessage.ids);
						EventBusExtended.publishSyncSafe(MessageBus.MESSAGE_SEND, message);
						ParticipantManager.PARTICIPANTS.add(0, clientNr);
					}
					else
						// Response from server
						if (ParticipantManager.PARTICIPANTS.size() > 0) // Already registered
						{
							// Remove lost
							ParticipantManager.PARTICIPANTS.removeIf(t -> !pMessage.ids.contains(t));

							// Add new ones
							for (final int i : pMessage.ids)
							{
								if (!ParticipantManager.PARTICIPANTS.contains(i))
								{
									ParticipantManager.PARTICIPANTS.add(i);
								}
							}
							System.out.println("UPDATED ALL: " + ParticipantManager.PARTICIPANTS);
						}
						else
							// Not yet registered
							if (pMessage.uID.equals(this.myUID))
							{
								final int myID = pMessage.ids.get(0);
								ParticipantManager.MY_PID.set(myID);
								ParticipantManager.PARTICIPANTS.addAll(pMessage.ids);
								// NetworkManager.online.set(OnlineStates.Online);
							}
				}
		}
	}
}
