package de.mixedfx.network.rebuild.messages;

import de.mixedfx.network.rebuild.user.User;

public class UserMessage extends Message
{
	private final User myUser;

	public UserMessage(User myUser)
	{
		this.myUser = myUser;
	}

	public User getOriginalUser()
	{
		return this.myUser;
	}

	@Override
	public String toString()
	{
		return "UserMessage with User: " + myUser;
	}
}
