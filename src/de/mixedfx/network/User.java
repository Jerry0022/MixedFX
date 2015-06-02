package de.mixedfx.network;

/**
 * A user shall represent a person. A user is identified world unique and ... After initializing the
 * network connection and having a unique pid the instance of myUser is published to the other
 * network participants.
 *
 * @author Jerry
 *
 */
public abstract class User
{
	protected int	pid;

	/**
	 * This method is called after a User is received by the network. If a user is already in the
	 * network this method should return false. If this method returns false, the user will be
	 * kicked from the network. This implies that only the newest user is in the network and there
	 * can't be a user twice.
	 *
	 * @param user
	 * @return Returns true if the user is equal to the other user, otherwise false.
	 */
	public abstract boolean equals(User user);
}
