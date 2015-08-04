package de.mixedfx.network.user;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;

import org.apache.commons.collections.Predicate;

import de.mixedfx.java.ApacheTools;
import de.mixedfx.list.Identifiable;
import de.mixedfx.logging.Log;
import de.mixedfx.network.ParticipantManager;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;

/**
 * A user shall represent a person. A user is identified world unique and ... After initializing the network connection and having a unique pid the
 * instance of myUser is published to the other network participants.
 *
 * @author Jerry
 *
 */
@SuppressWarnings("serial")
public abstract class User implements Identifiable, Serializable
{
	/**
	 * If the user is not yet identified!
	 */
	public int pid;

	/**
	 * A list of current networks of the user to which this local client is directly connected.
	 */
	public transient MapProperty<InetAddress, Long> networks;

	protected User()
	{
		this.pid = ParticipantManager.UNREGISTERED;
		this.networks = new SimpleMapProperty<>(FXCollections.observableHashMap());
	}

	public Predicate getByPID()
	{
		return ApacheTools.convert(t -> ((User) t).pid == this.pid);
	}

	public void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		User user = (User) in.readObject();
		Log.network.error("CALLED");
		this.networks = new SimpleMapProperty<>(FXCollections.observableHashMap());
	}

	@Override
	public String toString()
	{
		return "UserID: " + this.getIdentifier() + "; PID: " + this.pid;
	}

	/**
	 * This method is called after a User is received by the network. If a user is already in the network this method should return true. If this
	 * method returns false, the user will be kicked from the network. This implies that only the newest user is in the network and maybe not even him
	 * and there can't be a user twice.
	 *
	 * @param user
	 * @return Returns true if the user is equal to the other user, otherwise false.
	 */
	public boolean equals(final User user)
	{
		return this.getIdentifier().equals(user.getIdentifier());
	};
}
