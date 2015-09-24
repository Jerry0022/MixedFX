package de.mixedfx.network.messages;

import de.mixedfx.network.user.User;

public class UserMessage<T extends User> extends Message
{
	private final T myUser;

	public UserMessage(final T myUser)
	{
		this.myUser = myUser;
	}

	@Override
	public boolean equals(final Object userMessage)
	{
		if (!(userMessage instanceof UserMessage))
			return false;
		else
		{
			final UserMessage<T> message = (UserMessage<T>) userMessage;
			return this.getOriginalUser().equals(message.getOriginalUser());
		}
	}

	public T getOriginalUser()
	{
		return this.myUser;
	}

	@Override
	public String toString()
	{
		return "UserMessage with User: " + this.myUser;
	}
}
