package de.mixedfx.network.examples;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.collections.CollectionUtils;

import de.mixedfx.logging.Log;
import de.mixedfx.network.ConnectivityManager;
import de.mixedfx.network.MessageBus;
import de.mixedfx.network.MessageBus.MessageReceiver;
import de.mixedfx.network.ParticipantManager;
import de.mixedfx.network.ServiceManager.P2PService;
import de.mixedfx.network.UDPDetected;
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
	 * All current online users except {@link UserManager#myUser}!
	 */
	public static SimpleListProperty<User> allUsers;

	private final List<InetAddress>					myNICs;
	private final ListChangeListener<UDPDetected>	udpListener;

	public UserManager(final T myUser)
	{
		UserManager.allUsers = new SimpleListProperty<>(FXCollections.synchronizedObservableList(FXCollections.observableArrayList()));
		UserManager.myUser = myUser;
		this.myNICs = new ArrayList<InetAddress>();
		this.udpListener = c ->
		{
			while (c.next())
			{
				// If added or updated:
				if (c.wasAdded())
				{
					// TODO Do this also at the beginning
					for (final UDPDetected detected : c.getAddedSubList())
					{
						final InetAddress otherOnesAddress = detected.address;
						// TODO Request as broadcast who has this NIC address? With my UserID and my
						// NIC address :)
						// TODO If such a request got => Do I have the NIC address? If yes, Get user
						// by got UserID and update the recognized related networks! Send back
						// response with same content but changed NIC addresses and set my User as
						// related one. Mark as response to not have a loop.
					}

					// Update list of all NIC addresses
					final ArrayList<InetAddress> newNICs = new ArrayList<>();
					try
					{
						final Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
						while (nics.hasMoreElements())
						{
							final NetworkInterface nic = nics.nextElement();
							for (final InetAddress nicAdress : Collections.list(nic.getInetAddresses()))
							{
								newNICs.add(nicAdress);
							}
						}
					}
					catch (final SocketException e)
					{
						Log.network.error("Could not detect NetworkInterfaces!");
					}

					// IMPROVEMENT Could be improved by comparing the two lists
					this.myNICs.clear();
					this.myNICs.addAll(newNICs);

					// Call my user to everyone as now
				}
			}
		};
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
		// UDPCoordinator.allAdresses.removeListener(this.udpListener);

		MessageBus.unregisterForReceival(this);
		UserManager.myUser.updatePID(ParticipantManager.UNREGISTERED);
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
		UserManager.myUser.updatePID(ParticipantManager.MY_PID.get());
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

		// UDPCoordinator.allAdresses.addListener(this.udpListener);

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
				if (newUser.getIdentifier().equals(UserManager.myUser.getIdentifier()))
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
