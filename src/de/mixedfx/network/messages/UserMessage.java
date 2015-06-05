package de.mixedfx.network.messages;

import de.mixedfx.network.examples.User;

public class UserMessage extends RegisteredMessage
{
	private final User	user;

	public UserMessage(final User myUser)
	{
		this.user = myUser;
	}

	public User getUser()
	{
		return this.user;
	}
}
