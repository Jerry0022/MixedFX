package de.mixedfx.network;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import de.mixedfx.network.MessageBus.MessageReceiver;
import de.mixedfx.network.messages.Message;
import de.mixedfx.network.messages.ParticipantMessage;

public class ParticipantManager implements MessageReceiver
{
	/**
	 * Last id is always the server, first id is always the newest client.
	 */
	public static SimpleListProperty<Integer>	PARTICIPANTS				= new SimpleListProperty(FXCollections.observableArrayList());

	public static final int						PARTICIPANT_NUMBER_SERVER	= 1;
	public static int							PARTICIPANT_NUMBER			= ParticipantManager.PARTICIPANT_NUMBER_SERVER;

	private static ParticipantManager			pManager;

	public static ParticipantManager start()
	{
		// Listen for others
		ParticipantManager.pManager = new ParticipantManager();
		MessageBus.registerForReceival(ParticipantManager.pManager);
		return ParticipantManager.pManager;
	}

	public static void stop()
	{
		ParticipantManager.PARTICIPANTS.clear();
		MessageBus.unregisterForReceival(ParticipantManager.pManager);
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
		MessageBus.send(message);
	}

	@Override
	public synchronized void receive(final Message message)
	{
		System.out.println(message instanceof ParticipantMessage);
		if (!(message instanceof ParticipantMessage))
			return;

		final ParticipantMessage pMessage = (ParticipantMessage) message;

		if (pMessage.ids.isEmpty()) // Message from client
		{
			final int clientNr = ParticipantManager.PARTICIPANT_NUMBER++;
			ParticipantManager.PARTICIPANTS.add(0, clientNr);
			System.err.println(pMessage.uID + "   !   " + pMessage.ids);
			pMessage.ids.addAll(ParticipantManager.PARTICIPANTS);
			System.err.println(pMessage.uID + "   !   " + pMessage.ids);
			MessageBus.send(pMessage);
		}
		else
			// Message from server
			if (ParticipantManager.PARTICIPANTS.size() > 0) // Already registered
			{
				ParticipantManager.PARTICIPANTS.clear();
				ParticipantManager.PARTICIPANTS.addAll(pMessage.ids);
				System.err.println("UPDATED ALL: " + ParticipantManager.PARTICIPANTS);
			}
			else
				// Not yet registered
				if (pMessage.uID.equals(this.myUID))
				{
					final int myID = pMessage.ids.get(0);
					System.err.println("JUHU: MyID is: " + myID);
					ParticipantManager.PARTICIPANTS.addAll(pMessage.ids);
				}
	}
}
