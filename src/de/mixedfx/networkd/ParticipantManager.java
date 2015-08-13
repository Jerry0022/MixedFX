package de.mixedfx.networkd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.logging.Log;
import de.mixedfx.network.messages.Message;
import de.mixedfx.network.messages.ParticipantMessage;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class ParticipantManager
{
	public final static int UNREGISTERED = 0;

	/**
	 * Last id is always the server, first id is always the newest online client. Is maybe empty. Contains also {@link ParticipantManager#MY_PID}!
	 */
	public static SimpleListProperty<Integer> PARTICIPANTS = new SimpleListProperty<Integer>(FXCollections.observableList(Collections.synchronizedList(new ArrayList<>())));

	public static final int	PARTICIPANT_NUMBER_NOTREGISTERED	= 0;
	public static final int	PARTICIPANT_NUMBER_SERVER			= 1;
	public static int		PARTICIPANT_NUMBER					= ParticipantManager.PARTICIPANT_NUMBER_SERVER;

	/**
	 * 0 means the application is not yet registered in the network.
	 */
	public static final AtomicInteger MY_PID;

	private static ParticipantManager pManager;

	static
	{
		MY_PID = new AtomicInteger(PARTICIPANT_NUMBER_NOTREGISTERED);
		ParticipantManager.pManager = new ParticipantManager();
	}

	/**
	 * Subscribe for {@link ParticipantMessage} receival.
	 * 
	 * @return Returns this.
	 */
	protected static ParticipantManager start()
	{
		// Listen for others
		AnnotationProcessor.process(ParticipantManager.pManager);
		return ParticipantManager.pManager;
	}

	protected static void stop()
	{
		AnnotationProcessor.unprocess(ParticipantManager.pManager);
		ParticipantManager.MY_PID.set(ParticipantManager.UNREGISTERED);
		ParticipantManager.PARTICIPANT_NUMBER = PARTICIPANT_NUMBER_SERVER;
		ParticipantManager.PARTICIPANTS.get().clear();
	}

	/**
	 * Is only used once to request the network id.
	 */
	private String myUID;

	public ParticipantManager()
	{
	}

	public void connect()
	{
		final ParticipantMessage message = new ParticipantMessage();
		this.myUID = message.uID;
		Log.network.debug("Participant Request from me as client sending: " + message.uID + "   !   " + message.ids);
		EventBusExtended.publishSyncSafe(MessageBus.MESSAGE_SEND, message);
	}

	@EventTopicSubscriber(topic = MessageBus.MESSAGE_RECEIVE)
	public void receive(final String topic, final Message message)
	{
		if (message instanceof ParticipantMessage)
		{
			synchronized (ParticipantManager.PARTICIPANTS)
			{
				final ParticipantMessage pMessage = (ParticipantMessage) message;

				if (pMessage.uID.equals("") && NetworkConfig.STATUS.get().equals(NetworkConfig.States.SERVER))
				{
					// PIDs were lost
					for (final Integer i : pMessage.ids)
					{
						ParticipantManager.PARTICIPANTS.remove(i);
					}
					// Send updated list to all clients
					pMessage.uID = String.valueOf(ParticipantManager.PARTICIPANT_NUMBER_SERVER);
					pMessage.ids.clear();
					pMessage.ids.addAll(ParticipantManager.PARTICIPANTS);
					EventBusExtended.publishSyncSafe(MessageBus.MESSAGE_SEND, message);
				}
				else if (!pMessage.uID.equals(""))
				{
					if (pMessage.ids.isEmpty()) // PID Request from client
					{
						final int clientNr = ParticipantManager.PARTICIPANT_NUMBER++;
						Log.network.debug("Participant Request from client: " + pMessage.uID + "   !   " + pMessage.ids);
						pMessage.ids.add(clientNr);
						pMessage.ids.addAll(ParticipantManager.PARTICIPANTS);
						Log.network.debug("Participant Response from me as server sending: " + pMessage.uID + "   !   " + pMessage.ids);
						EventBusExtended.publishSyncSafe(MessageBus.MESSAGE_SEND, message);
						ParticipantManager.PARTICIPANTS.add(0, clientNr);
					}
					else
						// Response from server
						if (ParticipantManager.MY_PID.get() != ParticipantManager.PARTICIPANT_NUMBER_NOTREGISTERED)
					{
						// Remove lost (list was updated)
						ParticipantManager.PARTICIPANTS.removeIf(t -> !pMessage.ids.contains(t));

						// Add new ones
						for (final int i : pMessage.ids)
						{
							if (!ParticipantManager.PARTICIPANTS.contains(i))
							{
								ParticipantManager.PARTICIPANTS.add(i);
							}
						}
						Log.network.debug("Participant Update from Server: " + ParticipantManager.PARTICIPANTS);
					}
					else
							// Not yet registered
							if (pMessage.uID.equals(this.myUID))
					{
						final int myID = pMessage.ids.get(0);
						ParticipantManager.MY_PID.set(myID);
						ParticipantManager.PARTICIPANTS.addAll(pMessage.ids);
						Log.network.debug("Participant Response from Server: " + ParticipantManager.PARTICIPANTS);
						ServiceManager.client();
					}
				}
			}
		}
	}
}
