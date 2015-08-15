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
								if (tcp_user_map.containsKey(tcp.remoteAddress))
								{
									User oldUser = tcp_user_map.get(tcp.remoteAddress).getOriginalUser();
									tcp_user_map.remove(tcp.remoteAddress);
									// synchronized (otherUsers)
									// {
									// OverlayNetwork overlayToRemove = null;
									// for (OverlayNetwork network : oldUser.networks)
									// if (network.getIP().equals(tcp.remoteAddress))
									// overlayToRemove = network;
									// if (overlayToRemove != null)
									// oldUser.networks.remove(overlayToRemove);
									// }
									Log.network.debug("Removed user message from list: " + tcp.remoteAddress);
									synchronized (otherUsers)
									{
										if (!tcp_user_map.containsValue(new UserMessage(oldUser)))
											otherUsers.remove(oldUser);
										else
											Log.network.info("User is still available over other connection! ");
									}
									if (tcp_user_map.keySet().isEmpty())
										state.set(State.SEARCHING);
								}
							}
						} else if (c.wasAdded())
						{
							for (TCPClient tcp : c.getAddedSubList())
							{
								synchronized (NetworkManager.t.tcpClients)
								{
									UserMessage message = new UserMessage(ConnectivityManager.myUniqueUser);
									message.setToIP(tcp.remoteAddress);
									MessageBus.send(message);
									Log.network.debug("Sending " + message + " to " + tcp.remoteAddress);
								}
							}
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
							newUser.networks.add(MasterNetworkHandler.get(userMessage.getFromIP()));
							if (otherUsers.contains(newUser))
							{
								otherUsers.get(otherUsers.indexOf(newUser)).merge(newUser);
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
