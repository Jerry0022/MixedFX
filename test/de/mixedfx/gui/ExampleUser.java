package de.mixedfx.gui;

import de.mixedfx.network.user.User;

@SuppressWarnings("serial")
public class ExampleUser extends User
{
	private final String identifier;

	public ExampleUser(final String identifier)
	{
		this.identifier = identifier;
	}

	@Override
	public String getIdentifier()
	{
		return this.identifier;
	}
}
