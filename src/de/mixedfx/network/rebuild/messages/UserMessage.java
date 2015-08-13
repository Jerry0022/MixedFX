package de.mixedfx.network.rebuild.messages;

import java.util.ArrayList;
import java.util.List;

import de.mixedfx.network.rebuild.user.User;

public class UserMessage extends Message
{
	private final boolean		lostUser;
	private final User			myUser;
	private final List<Object>	users;

	public UserMessage(User myUser)
	{
		this.lostUser = false;
		this.myUser = myUser;
		this.users = new ArrayList<>();
		this.users.add(myUser.getIdentifier());
	}

	public User getOriginalUser()
	{
		return this.myUser;
	}

	public void addHop(User myUser)
	{
		this.users.add(myUser.getIdentifier());
	}

	/**
	 * First user in the list of identifier is the original sender!
	 */
	public List<Object> getList()
	{
		return this.users;
	}
}
