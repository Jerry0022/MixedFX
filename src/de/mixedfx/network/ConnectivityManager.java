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

	static
	{
		ConnectivityManager.state = new SimpleObjectProperty<>(State.OFFLINE);
		ConnectivityManager.tcp_user_map = new Hashtable<>(16);
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
							if (ConnectivityManager.tcp_user_map.containsKey(tcp1.remoteAddress))
							{
								final User oldUser = ConnectivityManager.tcp_user_map.get(tcp1.remoteAddress).getOriginalUser();
								ConnectivityManager.tcp_user_map.remove(tcp1.remoteAddress);
								synchronized (ConnectivityManager.get().otherUsers)
								{
									OverlayNetwork overlayToRemove = null;
									for (final OverlayNetwork network : ConnectivityManager.get().otherUsers.get(ConnectivityManager.get().otherUsers.indexOf(oldUser)).networks)
									{
										if (network.getIP().equals(tcp1.remoteAddress))
											overlayToRemove = network;
									}
									if (overlayToRemove != null)
										ConnectivityManager.get().otherUsers.get(ConnectivityManager.get().otherUsers.indexOf(oldUser)).networks.remove(overlayToRemove);
								}
								Log.network.debug("Removed user message from list: " + tcp1.remoteAddress);
								synchronized (ConnectivityManager.get().otherUsers)
								{
									if (!ConnectivityManager.tcp_user_map.containsValue(new UserMessage(oldUser)))
										ConnectivityManager.get().otherUsers.remove(oldUser);
									else
										Log.network.info("User is still available over other connection! ");
								}
								if (ConnectivityManager.tcp_user_map.keySet().isEmpty())
									ConnectivityManager.state.set(State.SEARCHING);
							}
						}
					} else if (c.wasAdded())
					{
						for (final TCPClient tcp2 : c.getAddedSubList())
						{
							synchronized (NetworkManager.t.tcpClients)
							{
								final UserMessage message = new UserMessage(ConnectivityManager.get().myUniqueUser);
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
				final UserMessage userMessage = (UserMessage) message;
				if (!userMessage.getOriginalUser().equals(ConnectivityManager.get().myUniqueUser))
				{
					synchronized (NetworkManager.t.tcpClients)
					{
						if (ConnectivityManager.tcp_user_map.keySet().isEmpty())
							ConnectivityManager.state.set(State.ONLINE);
						// Update mapping
						ConnectivityManager.tcp_user_map.put(userMessage.getFromIP(), userMessage);
						Log.network.debug("Put UserMessage " + userMessage + " from ip " + userMessage.getFromIP() + " to my list!");
					}
					synchronized (ConnectivityManager.get().otherUsers)
					{
						final User newUser = userMessage.getOriginalUser();
						newUser.networks.add(MasterNetworkHandler.get(userMessage.getFromIP()));
						if (ConnectivityManager.get().otherUsers.contains(newUser))
						{
							ConnectivityManager.get().otherUsers.get(ConnectivityManager.get().otherUsers.indexOf(newUser)).merge(newUser);
						} else
							ConnectivityManager.get().otherUsers.add(newUser);
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

	@SuppressWarnings("rawtypes")
	private static ConnectivityManager INSTANCE;

	public static ObjectProperty<State> state;

	protected static Hashtable<InetAddress, UserMessage> tcp_user_map;

	public static <T extends User> ConnectivityManager<T> get()
	{
		if (ConnectivityManager.INSTANCE == null)
			ConnectivityManager.INSTANCE = new ConnectivityManager<T>();
		return ConnectivityManager.INSTANCE;
	}

	/**
	 * Should be used only by using synchronized on this object!
	 */
	public ListProperty<T> otherUsers;

	private T myUniqueUser;

	public ConnectivityManager()
	{
		this.otherUsers = new SimpleListProperty<T>(FXCollections.observableArrayList());
	}

	public T getMyUser()
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
		this.myUniqueUser = myUser;
	}

	public void start()
	{
		if (this.myUniqueUser == null)
			throw new IllegalStateException("Please first set a user!");
		NetworkManager.start();
		ConnectivityManager.state.set(State.SEARCHING);
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
				for (final User user : ConnectivityManager.get().otherUsers)
					for (final OverlayNetwork network : user.networks)
						network.updateLatency();
			}
		});
	}

	public void start(final T myUniqueUser)
	{
		if (this.myUniqueUser == null)
			this.setMyUser(myUniqueUser);
		else
			this.myUniqueUser.merge(myUniqueUser);
		this.start();
	}

	public void stop()
	{
		NetworkManager.stop();
		ConnectivityManager.state.set(State.OFFLINE);
	}

	public void switchStatus()
	{
		if (ConnectivityManager.state.get().equals(State.OFFLINE))
			this.start();
		else
			this.stop();
	}
}
