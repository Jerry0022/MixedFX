package de.mixedfx.network.user;

import java.util.NoSuchElementException;

import org.apache.commons.collections.CollectionUtils;

import de.mixedfx.logging.Log;
import de.mixedfx.network.ConnectivityManager;
import de.mixedfx.network.MessageBus;
import de.mixedfx.network.MessageBus.MessageReceiver;
import de.mixedfx.network.ParticipantManager;
import de.mixedfx.network.ServiceManager.P2PService;
import de.mixedfx.network.messages.RegisteredMessage;
import de.mixedfx.network.messages.UserMessage;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;

@SuppressWarnings({ "unchecked", "serial" })
public class UserManager<T extends User> implements P2PService, MessageReceiver, ListChangeListener<Integer>
{
	/**
	 * MyUser which can't be changed until {@link ConnectivityManager#off()} is called and a new UserManager with a new myUser is registered to the
	 * network and {@link ConnectivityManager#on()} is called.
	 */
	public static User myUser;

	/**
	 * All current online users except {@link UserManager#myUser}! BUT the user are first put in this list unidentified ({@link User#getIdentifier()}
	 * returns null) and later replaced! Use {@link UserManager#isIdentified(User)}} to prevent exceptions!
	 */
	public static SimpleListProperty<User> allUsers;

	static
	{
		UserManager.allUsers = new SimpleListProperty<>(FXCollections.synchronizedObservableList(FXCollections.observableArrayList()));
	}

	/**
	 * @param user
	 *            The user which shall be checked.
	 * @return Returns true if the identifier of the user is not null! Returns false if the user joins the network, a few moments later he should be
	 *         identified!
	 */
	public static boolean isIdentified(User user)
	{
		return user.getIdentifier() != null;
	}

	public UserManager(final T myUser)
	{
		UserManager.myUser = myUser;
	}

	/**
	 * An anonymous user does only have a PID by default and the identifier is null. May overwrite this method to let an not yet identified user have
	 * also other predefined attributes.
	 *
	 * @return Returns a representation of a ghost user.
	 */
	public T getAnonymous(final int hisPID)
	{
		return (T) new User()
		{
			{
				this.pid = hisPID;
			}

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
	public synchronized void stop()
	{
		MessageBus.unregisterForReceival(this);
		UserManager.myUser.pid = ParticipantManager.UNREGISTERED;
		synchronized (ParticipantManager.PARTICIPANTS)
		{
			ParticipantManager.PARTICIPANTS.removeListener(this);
		}
		UserManager.allUsers.clear();
		Log.network.debug("UserManager stopped!");
	}

	@Override
	public synchronized void start()
	{
		Log.network.trace("UserManager starts!");
		MessageBus.registerForReceival(this);
		UserManager.myUser.pid = ParticipantManager.MY_PID.get();
		synchronized (ParticipantManager.PARTICIPANTS)
		{
			for (final Integer pid : ParticipantManager.PARTICIPANTS)
			{
				if (pid != ParticipantManager.MY_PID.get())
				{
					UserManager.allUsers.add(this.getAnonymous(pid));
				}
			}
			ParticipantManager.PARTICIPANTS.addListener(this);
		}
		MessageBus.send(new UserMessage(UserManager.myUser));

		Log.network.debug("UserManager started! My user: " + UserManager.myUser);
	}

	@Override
	public synchronized void receive(final RegisteredMessage message)
	{
		Log.network.warn("RECEIVED USER MESSAGE" + message);
		if (message instanceof UserMessage)
		{
			synchronized (UserManager.allUsers)
			{
				final User newUser = ((UserMessage) message).getUser();
				if (newUser.pid != UserManager.myUser.pid && newUser.getIdentifier().equals(UserManager.myUser.getIdentifier()))
				{
					Log.network.info("Network is totally shutdown because user may exist at least twice, my user: " + UserManager.myUser + " the other one: " + newUser);
					ConnectivityManager.off();
				}
				else
				{
					Log.network.trace("Information about User received: " + newUser);
					try
					{
						final User foundUser = (User) CollectionUtils.select(UserManager.allUsers, newUser.getByPID()).iterator().next();
						UserManager.allUsers.set(UserManager.allUsers.indexOf(foundUser), newUser);
					}
					catch (final NoSuchElementException e)
					{
						Log.network.warn("UserMessage of user " + newUser + " received but there is no participant with this PID!");
					}
				}
			}
		}
	}

	@Override
	public synchronized void onChanged(final javafx.collections.ListChangeListener.Change<? extends Integer> c)
	{
		/*
		 * If new participant is in the network add an anonymous user to the list. If one is lost, remove him from this list.
		 */
		synchronized (UserManager.allUsers)
		{
			while (c.next())
			{
				if (c.wasAdded())
				{
					final UserMessage message = new UserMessage(UserManager.myUser);
					for (final int pid : c.getAddedSubList())
					{
						Log.network.trace("!!!New PID: " + pid);
						final T user = this.getAnonymous(pid);
						Log.network.trace("New user registered: " + user);
						UserManager.allUsers.add(user);
						message.receivers.add(pid);
					}
					MessageBus.send(message);
				}
				else if (c.wasRemoved())
				{
					for (final int pid : c.getRemoved())
					{
						try
						{
							Log.network.trace("!!!Lost PID: " + pid);
							final User foundUser = (User) CollectionUtils.select(UserManager.allUsers, this.getAnonymous(pid).getByPID()).iterator().next();
							UserManager.allUsers.remove(foundUser);
						}
						catch (final NoSuchElementException e)
						{
							Log.network.warn("UserMessage of user with pid " + pid + " received but there is no participant with this PID!");
						}
					}
				}
			}
		}
	}
}
