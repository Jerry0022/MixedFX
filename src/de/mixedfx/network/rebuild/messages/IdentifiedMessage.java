package de.mixedfx.network.rebuild.messages;

import java.util.ArrayList;

import de.mixedfx.network.rebuild.ConnectivityManager;
import de.mixedfx.network.rebuild.user.User;

public class IdentifiedMessage extends Message
{
	private Object				fromUserID;
	private ArrayList<Object>	toUserIDs;

	/**
	 * @param toUserIDs
	 *            May not be null. If empty it is a broadcast otherwise a multi- or unicast.
	 */
	public IdentifiedMessage(ArrayList<Object> toUserIDs)
	{
		if (toUserIDs == null)
			throw new IllegalArgumentException("Parameter toUserIDs may not be null");

		this.fromUserID = ConnectivityManager.myUniqueUser.getIdentifier();
		this.toUserIDs = toUserIDs;
	}

	/**
	 * Only the identifier of the users will be used to map the message.
	 * 
	 * @param toUserIDs
	 *            May not be null. If empty it is a broadcast otherwise a multi- or unicast.
	 */
	public IdentifiedMessage(User... toUsers)
	{
		if (toUsers == null)
			throw new IllegalArgumentException("Parameter toUserIDs may not be null");

		this.fromUserID = ConnectivityManager.myUniqueUser.getIdentifier();

		this.toUserIDs = new ArrayList<>();
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
