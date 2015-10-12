package de.mixedfx.network.messages;

import de.mixedfx.network.user.User;
import lombok.NonNull;
import lombok.ToString;

@ToString
public class UserMessage<T extends User> extends Message
{
	private final T myUser;

	public UserMessage(@NonNull T myUser)
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
}
