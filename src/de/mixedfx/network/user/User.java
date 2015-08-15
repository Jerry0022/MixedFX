package de.mixedfx.network.user;

import java.io.IOException;
import java.io.Serializable;

import de.mixedfx.list.Identifiable;
import de.mixedfx.network.OverlayNetwork;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

/**
 * A user shall represent a person. A user is identified world unique and ... After initializing the network connection and having a unique pid the instance of myUser is published to the other network
 * participants.
 *
 * @author Jerry
 *
 */
@SuppressWarnings("serial")
public abstract class User implements Identifiable, Serializable
{
	/**
	 * A list of current networks of the user to which this local client is directly connected.
	 */
	public transient ListProperty<OverlayNetwork> networks;

	protected User()
	{
		this.networks = new SimpleListProperty<>(FXCollections.observableArrayList());
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		this.networks = new SimpleListProperty<>(FXCollections.observableArrayList());
	}

	public void merge(User newUser)
	{
		this.networks.addAll(newUser.networks);
		mergeMe(newUser);
	}

	/**
	 * This method enables you to use properties and link users directly to GUI. If a user is updated with a new UserProfile what shall I do with the new fields? Do nothing here if a new user profile
	 * doesn't care the old one!
	 * 
	 * @param newUser
	 *            The new one with the same id! All other values might have changed!
	 */
	public abstract void mergeMe(User newUser);

	@Override
	public String toString()
	{
		return "UserID: " + this.getIdentifier();
	}

	/**
	 * This method is called after a User is received by the network. If a user is already in the network this method should return true. If this method returns false, the user will be kicked from the
	 * network. This implies that only the newest user is in the network and maybe not even him and there can't be a user twice.
	 *
	 * @param user
	 * @return Returns true if the user is equal to the other user, otherwise false.
	 */
	@Override
	public boolean equals(Object user)
	{
		if (user instanceof User)
			return this.getIdentifier().equals(((User) user).getIdentifier());
		else
			return false;
	};
}
