package de.mixedfx.network.messagesd;

import de.mixedfx.network.user.User;

@SuppressWarnings("serial")
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
