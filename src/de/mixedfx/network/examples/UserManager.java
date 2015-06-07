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

public class UserManager implements P2PService, MessageReceiver, ListChangeListener<Integer>
{
	/*
	 * TODO Listen to UDPCoordinator.allAddresses to know which InetAdresses can be reached! How to
	 * connect this information to the User? May implement a service who just broadcast his
	 * IP-Addresses and his user identification if the network devices online state changed.
	 */
	public final User						myUser;

	public final SimpleListProperty<User>	allUsers;

	public UserManager(final User myUser)
	{
		this.myUser = myUser;
		this.allUsers = new SimpleListProperty<>(FXCollections.observableArrayList());
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
		MessageBus.registerForReceival(this);
		this.myUser.updatePID(ParticipantManager.MY_PID.get());
		synchronized (ParticipantManager.PARTICIPANTS)
		{
			ParticipantManager.PARTICIPANTS.addListener(this);
			for (final Integer i : ParticipantManager.PARTICIPANTS)
			{
				if (i != ParticipantManager.MY_PID.get())
				{
					final ExampleUser user = new ExampleUser("");
					user.updatePID(i);
					this.allUsers.add(user);
				}
			}
		}

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
					Log.network.info("Another user in this network has the same identifier: " + this.myUser.getIdentifier() + " therefore network connection is shutdown!");
					ConnectivityManager.off();
				}
				else
				{
					Log.network.trace("Information about User received, identified by: " + newUser.getIdentifier());
					final User foundUser = (User) CollectionUtils.select(this.allUsers, newUser.getByPID()).iterator().next();
					if (foundUser != null)
					{
						this.allUsers.set(this.allUsers.indexOf(foundUser), newUser);
					}
					else
					{
						Log.network.warn("UserMessage of user " + newUser.getIdentifier() + " with PID " + newUser.pid + " received but there is no participant with this PID!");
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
						Log.network.trace("New user with PID" + pid);
						final ExampleUser user = new ExampleUser("");
						user.updatePID(pid);
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
							final ExampleUser removedUser = new ExampleUser("");
							removedUser.updatePID(pid);
							final User foundUser = (User) CollectionUtils.select(this.allUsers, removedUser.getByPID()).iterator().next();
							if (foundUser != null)
							{
								this.allUsers.remove(foundUser);
								Log.network.trace("User with ID " + foundUser.getIdentifier() + " and PID " + pid + " lost!");
							}
						}
					}
			}
		}
	}
}
