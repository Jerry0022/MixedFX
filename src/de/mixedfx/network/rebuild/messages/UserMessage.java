package de.mixedfx.network.rebuild.messages;

import java.util.ArrayList;
import java.util.List;

import de.mixedfx.network.rebuild.user.User;

public class UserMessage extends Message
{
	private final boolean		lostUser;
	private final User			myUser;
	private final List<Object>	userIDs;

	public UserMessage(User myUser)
	{
		this.lostUser = false;
		this.myUser = myUser;
		this.userIDs = new ArrayList<>();
		this.userIDs.add(myUser.getIdentifier());
	}

	public User getOriginalUser()
	{
		return this.myUser;
	}

	public void addHop(Object userIdentifier)
	{
		this.userIDs.add(userIdentifier);
	}

	/**
	 * First user in the list of identifier is the original sender!
	 */
	public List<Object> getList()
	{
		return this.userIDs;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		for (Object id : getList())
			builder.append(id + ", ");
		return "UserIDs: " + builder.toString();
	}
}
