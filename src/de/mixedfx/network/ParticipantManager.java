package de.mixedfx.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import de.mixedfx.network.MessageBus.MessageReceiver;
import de.mixedfx.network.NetworkManager.OnlineStates;
import de.mixedfx.network.messages.Message;
import de.mixedfx.network.messages.ParticipantMessage;

public class ParticipantManager implements MessageReceiver
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
	public static final AtomicInteger			MY_PID						= new AtomicInteger();

	private static ParticipantManager			pManager;

	static
	{
		ParticipantManager.pManager = new ParticipantManager();
	}

	protected static ParticipantManager start()
	{
		// Listen for others
		MessageBus.registerForReceival(ParticipantManager.pManager);
		return ParticipantManager.pManager;
	}

	protected static void stop()
	{
		ParticipantManager.MY_PID.set(0);
		ParticipantManager.PARTICIPANTS.get().clear();
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
		if (message instanceof ParticipantMessage)
		{
			final ParticipantMessage pMessage = (ParticipantMessage) message;

			if (pMessage.uID.equals("") && NetworkConfig.status.get().equals(NetworkConfig.States.Server))
			{
				for (final Integer i : pMessage.ids)
					ParticipantManager.PARTICIPANTS.remove(i);
				pMessage.uID = String.valueOf(ParticipantManager.PARTICIPANT_NUMBER_SERVER);
				pMessage.ids.clear();
				pMessage.ids.addAll(ParticipantManager.PARTICIPANTS.get());
				MessageBus.send(pMessage);
			}
			else
				if (!pMessage.uID.equals(""))
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
							ParticipantManager.PARTICIPANTS.removeIf(t -> !pMessage.ids.contains(t));
							for (final int i : pMessage.ids)
								if (!ParticipantManager.PARTICIPANTS.contains(i))
									ParticipantManager.PARTICIPANTS.add(i);
							System.out.println("UPDATED ALL: " + ParticipantManager.PARTICIPANTS);
						}
						else
							// Not yet registered
							if (pMessage.uID.equals(this.myUID))
							{
								final int myID = pMessage.ids.get(0);
								ParticipantManager.MY_PID.set(myID);
								ParticipantManager.PARTICIPANTS.addAll(pMessage.ids);
								NetworkManager.online.set(OnlineStates.Online);
							}
		}
	}
}
