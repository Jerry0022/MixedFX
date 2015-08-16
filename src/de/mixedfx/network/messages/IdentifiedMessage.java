package de.mixedfx.network.messages;

import java.util.ArrayList;

import de.mixedfx.network.ConnectivityManager;
import de.mixedfx.network.user.User;

public abstract class IdentifiedMessage extends Message
{
	private Object				fromUserID;
	private ArrayList<Object>	toUserIDs;

	public IdentifiedMessage()
	{
		this.fromUserID = ConnectivityManager.myUniqueUser.getIdentifier();
		this.toUserIDs = new ArrayList<>();
	}

	/**
	 * @param toUserIDs
	 *            May not be null. If empty it is a broadcast otherwise a multi- or unicast.
	 */
	public void setReceivers(ArrayList<Object> toUserIDs)
	{
		if (toUserIDs == null)
			throw new IllegalArgumentException("Parameter toUserIDs may not be null!");

		this.toUserIDs = toUserIDs;
	}

	/**
	 * Only the identifier of the users will be used to map the message.
	 * 
	 * @param toUsers
	 *            If empty or null it is a broadcast otherwise a multi- or unicast.
	 */
	public void setReceivers(User... toUsers)
	{
		for (User user : toUsers)
			this.toUserIDs.add(user.getIdentifier());
	}

	public Object getFromUserID()
	{
		return this.fromUserID;
	}

	public ArrayList<Object> getToUserIDs()
	{
		return this.toUserIDs;
	}
}