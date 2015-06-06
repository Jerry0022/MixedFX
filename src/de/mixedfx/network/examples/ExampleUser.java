package de.mixedfx.network.examples;

public class ExampleUser extends User
{
	/**
	 * If equals "" the user is not yet identified but part of the network!
	 */
	private final String	identifier;

	public ExampleUser(final String identifier)
	{
		this.identifier = identifier;
	}

	@Override
	public String getIdentifier()
	{
		return this.identifier;
	}

	@Override
	public boolean equals(final User user)
	{
		return this.getIdentifier().equals(user.getIdentifier());
	}
}
