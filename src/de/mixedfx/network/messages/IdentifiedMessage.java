package de.mixedfx.network.messages;

import java.util.ArrayList;
import java.util.List;

import de.mixedfx.network.MessageBus;
import de.mixedfx.network.ParticipantManager;
import de.mixedfx.network.examples.User;
import de.mixedfx.network.examples.UserManager;

@SuppressWarnings("serial")
public abstract class IdentifiedMessage extends RegisteredMessage
{
	/**
	 * <p>
	 * If receivers contains no values then this message is a broadcast to everyone else except me
	 * (default). <br>
	 * If receivers contains one value or more the message is received by these participants.
	 * </p>
	 */
	private final List<Object>	receiverUserIDs;

	/**
	 * Senders user id, which is automatically set!
	 */
	private Object				senderUserID;

	public IdentifiedMessage()
	{
		this.receiverUserIDs = new ArrayList<>();
		this.senderUserID = UserManager.myUser.getIdentifier();
	}

	/**
	 * @return Default is an emtpy list of users' identifier.
	 */
	public List<Object> getReceiverUserIDs()
	{
		return this.receiverUserIDs;
	}

	/**
	 * Saves the users' ids and the pids, all other information are discarded.
	 *
	 * @param receiverUsers
	 *            The users' ids who shall receive this message. Null or an empty list means
	 *            broadcast.
	 */
	public void setReceiverUserIDs(List<User> receiverUsers)
	{
		if (receiverUsers == null)
		{
			receiverUsers = new ArrayList<>();
		}

		this.receivers.clear();
		this.receiverUserIDs.clear();

		for (final User user : receiverUsers)
		{
			this.receivers.add(user.pid);
			this.receiverUserIDs.add(user.getIdentifier());
		}
	}

	public Object getSenderUserID()
	{
		return this.senderUserID;
	}

	/**
	 * Is called automatically if you send this message by calling
	 * {@link MessageBus#send(RegisteredMessage)}!
	 */
	public void updateSenderID()
	{
		this.sender = ParticipantManager.MY_PID.get();
		this.senderUserID = UserManager.myUser.getIdentifier();
	}
}
