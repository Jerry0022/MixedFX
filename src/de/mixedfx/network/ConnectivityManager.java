package de.mixedfx.network;

import java.net.InetAddress;
import java.util.Hashtable;

import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.VetoTopicEventListener;

import de.mixedfx.inspector.Inspector;
import de.mixedfx.logging.Log;
import de.mixedfx.network.messages.Message;
import de.mixedfx.network.messages.UserMessage;
import de.mixedfx.network.user.User;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;

public class ConnectivityManager<T extends User>
{
	public enum State
	{
		OFFLINE, SEARCHING, ONLINE;
	}

	private static ConnectivityManager<? extends User> INSTANCE;

	public static ConnectivityManager<? extends User> get()
	{
		return ConnectivityManager.INSTANCE;
	}

	public ObjectProperty<State> state;

	protected Hashtable<InetAddress, UserMessage<T>> tcp_user_map;

	/**
	 * Should be used only by using synchronized on this object!
	 */
	public ListProperty<T> otherUsers;

	private ObjectProperty<T> myUniqueUser;

	public ConnectivityManager(final T myUser)
	{
		ConnectivityManager.INSTANCE = this;
		this.otherUsers = new SimpleListProperty<T>(FXCollections.observableArrayList());
		this.state = new SimpleObjectProperty<>(State.OFFLINE);
		this.tcp_user_map = new Hashtable<>(16);
		this.myUniqueUser = new SimpleObjectProperty<>(myUser);
		NetworkManager.t.tcpClients.addListener((ListChangeListener<TCPClient>) c ->
		{
			while (c.next())
			{
				synchronized (NetworkManager.t.tcpClients)
				{
					if (c.wasRemoved())
					{
						for (final TCPClient tcp1 : c.getRemoved())
						{
							if (this.tcp_user_map.containsKey(tcp1.remoteAddress))
							{
								final T oldUser = this.tcp_user_map.get(tcp1.remoteAddress).getOriginalUser();
								this.tcp_user_map.remove(tcp1.remoteAddress);
								synchronized (this.otherUsers)
								{
									OverlayNetwork overlayToRemove = null;
									for (final OverlayNetwork network : this.otherUsers.get(this.otherUsers.indexOf(oldUser)).networks)
									{
										if (network.getIP().equals(tcp1.remoteAddress))
											overlayToRemove = network;
									}
									if (overlayToRemove != null)
										this.otherUsers.get(this.otherUsers.indexOf(oldUser)).networks.remove(overlayToRemove);
								}
								Log.network.debug("Removed user message from list: " + tcp1.remoteAddress);
								synchronized (this.otherUsers)
								{
									if (!this.tcp_user_map.containsValue(new UserMessage<T>(oldUser)))
										this.otherUsers.remove(oldUser);
									else
										Log.network.info("User is still available over other connection! ");
								}
								if (this.tcp_user_map.keySet().isEmpty())
									this.state.set(State.SEARCHING);
							}
						}
					} else if (c.wasAdded())
					{
						for (final TCPClient tcp2 : c.getAddedSubList())
						{
							synchronized (NetworkManager.t.tcpClients)
							{
								final UserMessage<T> message = new UserMessage<T>(this.myUniqueUser.get());
								message.setToIP(tcp2.remoteAddress);
								MessageBus.send(message);
								Log.network.debug("Sending " + message + " to " + tcp2.remoteAddress);
							}
						}
					}
				}
			}
		});
		// Attention: Does only subscribe if at least one normal other subscriber to!
		EventBus.subscribeVetoListenerStrongly(MessageBus.MESSAGE_RECEIVE, (VetoTopicEventListener<Message>) (topic, message) ->
		{
			if (message instanceof UserMessage)
			{
				Log.network.debug("UserMessage received: " + message);
				final UserMessage<T> userMessage = (UserMessage<T>) message;
				if (!userMessage.getOriginalUser().equals(this.myUniqueUser))
				{
					synchronized (NetworkManager.t.tcpClients)
					{
						if (this.tcp_user_map.keySet().isEmpty())
							this.state.set(State.ONLINE);
						// Update mapping
						this.tcp_user_map.put(userMessage.getFromIP(), userMessage);
						Log.network.debug("Put UserMessage " + userMessage + " from ip " + userMessage.getFromIP() + " to my list!");
					}
					synchronized (this.otherUsers)
					{
						final T newUser = userMessage.getOriginalUser();
						newUser.networks.add(MasterNetworkHandler.get(userMessage.getFromIP()));
						if (this.otherUsers.contains(newUser))
						{
							this.otherUsers.get(this.otherUsers.indexOf(newUser)).merge(newUser);
						} else
							this.otherUsers.add(newUser);
					}
				} else
					Log.network.debug("UserMessage was from me!");
				return true;
			} else
				return false;
		});
		MessageBus.registerForReceival(message ->
		{
			// Just to let the above veto listener work!
		} , true);
	}

	public ObjectProperty<T> getMyUser()
	{
		return this.myUniqueUser;
	}

	public void restart()
	{
		this.stop();
		this.start();
	}

	public void restart(final T user)
	{
		this.stop();
		this.start(user);
	}

	public void setMyUser(final T myUser)
	{
		this.myUniqueUser.set(myUser);
	}

	public void start()
	{
		if (this.myUniqueUser.get() == null)
			throw new IllegalStateException("Please first set a user!");

		// Start network
		NetworkManager.start();
		this.state.set(State.SEARCHING);

		// Start updating overlay networks
		Inspector.runNowAsDaemon(() ->
		{
			while (NetworkManager.running)
			{
				try
				{
					Thread.sleep(NetworkConfig.ICMP_INTERVAL);
				} catch (final Exception e)
				{
				}
				for (final User user : this.otherUsers)
					for (final OverlayNetwork network : user.networks)
						network.updateLatency();
			}
		});
	}

	public void start(final T myUniqueUser)
	{
		if ((this.myUniqueUser.get() == null))
			this.setMyUser(myUniqueUser);
		else
			this.myUniqueUser.get().merge(myUniqueUser);
		this.start();
	}

	public void stop()
	{
		NetworkManager.stop();
		this.state.set(State.OFFLINE);
	}

	public void switchStatus()
	{
		if (this.state.get().equals(State.OFFLINE))
			this.start();
		else
			this.stop();
	}

	public void updatedUser()
	{
		// TODO Check
		final UserMessage<T> message = new UserMessage<T>(this.myUniqueUser.get());
		MessageBus.send(message);
	}
}
