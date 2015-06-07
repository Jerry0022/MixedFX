package de.mixedfx.network.examples;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;

import org.apache.commons.collections.CollectionUtils;

import de.mixedfx.logging.Log;
import de.mixedfx.network.ConnectivityManager;
import de.mixedfx.network.MessageBus;
import de.mixedfx.network.MessageBus.MessageReceiver;
import de.mixedfx.network.ParticipantManager;
import de.mixedfx.network.ServiceManager.P2PService;
import de.mixedfx.network.messages.RegisteredMessage;
import de.mixedfx.network.messages.UserMessage;

@SuppressWarnings({ "unchecked", "serial" })
public class UserManager<T extends User> implements P2PService, MessageReceiver, ListChangeListener<Integer>
{
	/*
	 * TODO Listen to UDPCoordinator.allAddresses to know which InetAdresses can be reached! How to
	 * connect this information to the User? May implement a service who just broadcast his
	 * IP-Addresses and his user identification if the network devices online state changed.
	 */
	public final T						myUser;

	public final SimpleListProperty<T>	allUsers;

	public UserManager(final T myUser)
	{
		this.myUser = myUser;
		this.allUsers = new SimpleListProperty<>(FXCollections.observableArrayList());
	}

	/**
	 * An anonymous user does only have a PID by default and the identifier is null. May overwrite
	 * this method to let an not yet identified user have also other predefined attributes.
	 *
	 * @return Returns a representation of a ghost user.
	 */
	public T getAnonymous(final int pid)
	{
		return (T) new User()
		{
			@Override
			public Object getIdentifier()
			{
				return null;
			}

			@Override
			public boolean equals(final User user)
			{
				return user.getIdentifier().equals(this.getIdentifier());
			}
		};
	}

	@Override
	public void stop()
	{
		MessageBus.unregisterForReceival(this);
		this.myUser.updatePID(ParticipantManager.UNREGISTERED);
		synchronized (ParticipantManager.PARTICIPANTS)
		{
			ParticipantManager.PARTICIPANTS.removeListener(this);
		}
		this.allUsers.clear();
		Log.network.debug("UserManager stopped!");
	}

	@Override
	public void start()
	{
		this.myUser.updatePID(ParticipantManager.MY_PID.get());
		synchronized (ParticipantManager.PARTICIPANTS)
		{
			ParticipantManager.PARTICIPANTS.addListener(this);
			for (final Integer pid : ParticipantManager.PARTICIPANTS)
			{
				if (pid != ParticipantManager.MY_PID.get())
				{
					this.allUsers.add(this.getAnonymous(pid));
				}
			}
		}
		MessageBus.registerForReceival(this);

		MessageBus.send(new UserMessage(this.myUser));
		Log.network.debug("UserManager started! My id: " + this.myUser.getIdentifier());
	}

	@Override
	public void receive(final RegisteredMessage message)
	{
		if (message instanceof UserMessage)
		{
			synchronized (this.allUsers)
			{
				final User newUser = ((UserMessage) message).getUser();
				if (newUser.getIdentifier().equals(this.myUser.getIdentifier()))
				{
					Log.network.info("Network is totally shutdown because user may exist at least twice, my user: " + this.myUser + " the other one: " + newUser);
					ConnectivityManager.off();
				}
				else
				{
					Log.network.trace("Information about User received: " + newUser);
					final User foundUser = (User) CollectionUtils.select(this.allUsers, newUser.getByPID()).iterator().next();
					if (foundUser != null)
					{
						this.allUsers.set(this.allUsers.indexOf(foundUser), (T) newUser);
					}
					else
					{
						Log.network.warn("UserMessage of user " + newUser + " received but there is no participant with this PID!");
					}
				}
			}
		}
	}

	@Override
	public void onChanged(final javafx.collections.ListChangeListener.Change<? extends Integer> c)
	{
		synchronized (this.allUsers)
		{
			while (c.next())
			{
				if (c.wasAdded())
				{
					final UserMessage message = new UserMessage(this.myUser);
					for (final int pid : c.getAddedSubList())
					{
						final T user = this.getAnonymous(pid);
						Log.network.trace("New user registered: " + user);
						this.allUsers.add(user);
						message.receivers.add(pid);
					}
					MessageBus.send(message);
				}
				else
					if (c.wasRemoved())
					{
						for (final int pid : c.getRemoved())
						{
							final User foundUser = (User) CollectionUtils.select(this.allUsers, this.getAnonymous(pid).getByPID()).iterator().next();
							if (foundUser != null)
							{
								this.allUsers.remove(foundUser);
								Log.network.trace("User with ID " + foundUser + " lost!");
							}
						}
					}
			}
		}
	}
}
