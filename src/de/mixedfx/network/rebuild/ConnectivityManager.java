package de.mixedfx.network.rebuild;

import java.net.InetAddress;
import java.util.Hashtable;

import org.bushe.swing.event.VetoTopicEventListener;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.logging.Log;
import de.mixedfx.network.rebuild.MessageBus.MessageReceiver;
import de.mixedfx.network.rebuild.messages.Message;
import de.mixedfx.network.rebuild.messages.UserMessage;
import de.mixedfx.network.rebuild.user.User;
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
		state = new SimpleObjectProperty<>();
		otherUsers = new SimpleListProperty<User>(FXCollections.observableArrayList());
		tcp_user_map = new Hashtable<>(16);
		NetworkManager.t.tcpClients.addListener(new ListChangeListener<TCPClient>()
		{
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends TCPClient> c)
			{
				while (c.next())
				{
					synchronized (NetworkManager.t.tcpClients)
					{
						if (c.wasRemoved())
						{
							for (TCPClient tcp : c.getRemoved())
							{
								if (tcp_user_map.contains(tcp.remoteAddress))
								{
									User oldUser = tcp_user_map.get(tcp.remoteAddress).getOriginalUser();
									synchronized (otherUsers)
									{
										otherUsers.remove(oldUser);
									}
									tcp_user_map.remove(tcp.remoteAddress);
									Log.network.debug("Removed user message from list: " + tcp.remoteAddress);
									if (tcp_user_map.keySet().isEmpty())
										state.set(State.SEARCHING);
								}
							}
						}
					}

					for (TCPClient tcp : c.getAddedSubList())
					{
						synchronized (NetworkManager.t.tcpClients)
						{
							UserMessage message = new UserMessage(ConnectivityManager.myUniqueUser);
							message.setToIP(tcp.remoteAddress);
							MessageBus.send(message);
							Log.network.debug("Sending UserMessage " + message + " to " + tcp.remoteAddress);
						}
					}
				}
			}
		});
		// Attention: Does only subscribe if at least one normal other subscriber to!
		EventBusExtended.subscribeVetoListenerStrongly(MessageBus.MESSAGE_RECEIVE, new VetoTopicEventListener<Message>()
		{
			@Override
			public boolean shouldVeto(String topic, Message message)
			{
				if (message instanceof UserMessage)
				{

					Log.network.debug("UserMessage received: " + message);
					UserMessage userMessage = (UserMessage) message;
					if (!userMessage.getOriginalUser().equals(ConnectivityManager.myUniqueUser))
					{
						synchronized (NetworkManager.t.tcpClients)
						{
							if (tcp_user_map.keySet().isEmpty())
								state.set(State.ONLINE);
							// Update mapping
							tcp_user_map.put(userMessage.getFromIP(), userMessage);
							Log.network.debug("Put UserMessage " + userMessage + " from ip " + userMessage.getFromIP() + " to my list!");
						}
						synchronized (otherUsers)
						{
							User newUser = userMessage.getOriginalUser();
							if (otherUsers.contains(newUser))
							{
								Log.network.fatal("SETTING UP");
								otherUsers.set(otherUsers.indexOf(newUser), newUser);
							} else
								otherUsers.add(newUser);
						}
					} else
						Log.network.debug("UserMessage was from me!");
					return true;
				} else
					return false;
			}
		});
		MessageBus.registerForReceival(new MessageReceiver()
		{
			@Override
			public void receive(Message message)
			{
				// Just to let the above veto listener work!
			}
		}, true);
	}

	public static void start(User myUniqueUser)
	{
		ConnectivityManager.myUniqueUser = myUniqueUser;
		NetworkManager.start();
		state.set(State.SEARCHING);
	}

	public static void stop()
	{
		NetworkManager.stop();
		ConnectivityManager.myUniqueUser = null;
		state.set(State.OFFLINE);
	}

	public static void restart()
	{
		User myUser = myUniqueUser;
		stop();
		start(myUser);
	}
}
