package de.mixedfx.list;

public class User implements Identifiable
{
	int i;

	public User(final int i)
	{
		this.i = i;
	}

	@Override
	public Object getIdentifier()
	{
		return this.i;
	}

	@Override
	public boolean equals(final Object e)
	{
		if (e instanceof Identifiable)
		{
			final Identifiable toCompare = (Identifiable) e;
			return toCompare.getIdentifier().equals(this.getIdentifier());
		}
		else
		{
			return false;
		}
	}
}
