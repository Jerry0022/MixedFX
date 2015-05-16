package de.mixedfx.network;

/**
 * A user shall represent a person. A user is identified world unique and ...
 *
 * @author Jerry
 *
 */
public abstract class User<Identifier extends Object>
{
	public final int	pid;

	public User(final int pid)
	{
		this.pid = pid;
	}

	public abstract void identify();
}
