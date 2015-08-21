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

public class ConnectivityManager
{
	public enum State
	{
		OFFLINE, SEARCHING, ONLINE;
	}

	public static User					myUniqueUser;
	/**
	 * Should be used only by using synchronized on this object!
	 */
	public static ListProperty<User>	otherUsers;
	public static ObjectProperty<State>	state;

	protected static Hashtable<InetAddress, UserMessage> tcp_user_map;

	static
	{
		ConnectivityManager.state = new SimpleObjectProperty<>(State.OFFLINE);
		ConnectivityManager.otherUsers = new SimpleListProperty<User>(FXCollections.observableArrayList());
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
								synchronized (ConnectivityManager.otherUsers)
								{
									OverlayNetwork overlayToRemove = null;
									for (final OverlayNetwork network : ConnectivityManager.otherUsers.get(ConnectivityManager.otherUsers.indexOf(oldUser)).networks)
									{
										if (network.getIP().equals(tcp1.remoteAddress))
											overlayToRemove = network;
									}
									if (overlayToRemove != null)
										ConnectivityManager.otherUsers.get(ConnectivityManager.otherUsers.indexOf(oldUser)).networks.remove(overlayToRemove);
								}
								Log.network.debug("Removed user message from list: " + tcp1.remoteAddress);
								synchronized (ConnectivityManager.otherUsers)
								{
									if (!ConnectivityManager.tcp_user_map.containsValue(new UserMessage(oldUser)))
										ConnectivityManager.otherUsers.remove(oldUser);
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
								final UserMessage message = new UserMessage(ConnectivityManager.myUniqueUser);
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
				if (!userMessage.getOriginalUser().equals(ConnectivityManager.myUniqueUser))
				{
					synchronized (NetworkManager.t.tcpClients)
					{
						if (ConnectivityManager.tcp_user_map.keySet().isEmpty())
							ConnectivityManager.state.set(State.ONLINE);
						// Update mapping
						ConnectivityManager.tcp_user_map.put(userMessage.getFromIP(), userMessage);
						Log.network.debug("Put UserMessage " + userMessage + " from ip " + userMessage.getFromIP() + " to my list!");
					}
					synchronized (ConnectivityManager.otherUsers)
					{
						final User newUser = userMessage.getOriginalUser();
						newUser.networks.add(MasterNetworkHandler.get(userMessage.getFromIP()));
						if (ConnectivityManager.otherUsers.contains(newUser))
						{
							ConnectivityManager.otherUsers.get(ConnectivityManager.otherUsers.indexOf(newUser)).merge(newUser);
						} else
							ConnectivityManager.otherUsers.add(newUser);
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

	public static void restart()
	{
		ConnectivityManager.stop();
		ConnectivityManager.start();
	}

	public static void restart(final User user)
	{
		ConnectivityManager.myUniqueUser = user;
		ConnectivityManager.restart();
	}

	public static void setMyUser(final User myUser)
	{
		ConnectivityManager.myUniqueUser = myUser;
	}

	public static void start()
	{
		if (ConnectivityManager.myUniqueUser == null)
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
				for (final User user : ConnectivityManager.otherUsers)
					for (final OverlayNetwork network : user.networks)
						network.updateLatency();
			}
		});
	}

	public static void start(final User myUniqueUser)
	{
		ConnectivityManager.myUniqueUser = myUniqueUser;
		ConnectivityManager.start();
	}

	public static void stop()
	{
		NetworkManager.stop();
		ConnectivityManager.state.set(State.OFFLINE);
	}

	public static void switchStatus()
	{
		if (ConnectivityManager.state.get().equals(State.OFFLINE))
			ConnectivityManager.start();
		else
			ConnectivityManager.stop();
	}
}
