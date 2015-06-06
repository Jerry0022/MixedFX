package de.mixedfx.network.examples;

import org.apache.commons.collections.Predicate;

import de.mixedfx.java.ApacheTools;
import de.mixedfx.list.Identifiable;
import de.mixedfx.network.ParticipantManager;

/**
 * A user shall represent a person. A user is identified world unique and ... After initializing the
 * network connection and having a unique pid the instance of myUser is published to the other
 * network participants.
 *
 * @author Jerry
 *
 */
public abstract class User implements Identifiable
{
	protected Predicate getByPID()
	{
		return ApacheTools.convert(t -> ((User) t).pid == this.pid);
	}

	protected int	pid;

	protected User()
	{
		this.pid = ParticipantManager.UNREGISTERED;
	}

	/**
	 * Is called by the {@link UserManager}.
	 *
	 * @param newPID
	 */
	protected void updatePID(final int newPID)
	{
		this.pid = newPID;
	}

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
