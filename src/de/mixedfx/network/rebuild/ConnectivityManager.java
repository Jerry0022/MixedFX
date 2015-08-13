package de.mixedfx.network.rebuild;

import java.net.InetAddress;
import java.util.Hashtable;

import org.bushe.swing.event.VetoTopicEventListener;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.logging.Log;
import de.mixedfx.network.rebuild.messages.Message;
import de.mixedfx.network.rebuild.messages.UserMessage;
import de.mixedfx.network.rebuild.user.User;
import javafx.collections.ListChangeListener;

public class ConnectivityManager
{
	private static Hashtable<InetAddress, UserMessage> tcp_user_map;

	private static User myUniqueUser;

	static
	{
		tcp_user_map = new Hashtable<>(16);
		NetworkManager.t.tcpClients.addListener(new ListChangeListener<TCPClient>()
		{
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends TCPClient> c)
			{
				while (c.next())
				{
					if (c.wasRemoved())
						for (TCPClient tcp : c.getRemoved())
						{
							tcp_user_map.remove(tcp.remoteAddress);
							Log.network.debug("Removed user message from list: " + tcp.remoteAddress);
						}

					for (TCPClient tcp : c.getAddedSubList())
					{
						synchronized (NetworkManager.t.tcpClients)
						{
							UserMessage message = new UserMessage(ConnectivityManager.myUniqueUser);
							for (InetAddress inet : tcp_user_map.keySet())
							{
								if (!inet.equals(tcp.remoteAddress))
									for (Object ids : tcp_user_map.get(inet).getList())
										message.addHop(ids);
							}
							message.setToIP(tcp.remoteAddress);
							MessageBus.send(message);
							Log.network.debug("Sending UserMessage " + message + " to " + tcp.remoteAddress);
						}
					}
				}
			}
		});
		EventBusExtended.subscribeVetoListener(MessageBus.MESSAGE_RECEIVE, new VetoTopicEventListener<Message>()
		{
			@Override
			public boolean shouldVeto(String topic, Message data)
			{
				if (data instanceof UserMessage)
				{
					UserMessage userMessage = (UserMessage) data;
					// Prevent death circles if more than one connection exist to the same client
					if (!userMessage.getOriginalUser().equals(ConnectivityManager.myUniqueUser))
						synchronized (NetworkManager.t.tcpClients)
						{
							// Update mapping
							tcp_user_map.put(userMessage.getFromIP(), userMessage);
							Log.network.debug("Put UserMessage " + userMessage + " from ip " + userMessage.getFromIP() + " to my list!");
							// Go through all other available connections
							for (InetAddress inet : tcp_user_map.keySet())
							{
								// Don't send back to direct sender!
								if (!inet.equals(userMessage.getFromIP()))
								{
									// Does the other ones know already the original? If yes don't forward
									// Don't send to clients who already have a path to the original user.
									if (!tcp_user_map.get(inet).getList().contains(userMessage.getOriginalUser().getIdentifier()))
									{
										userMessage.addHop(ConnectivityManager.myUniqueUser.getIdentifier());
										userMessage.setToIP(inet);
										MessageBus.send(userMessage);
										Log.network.debug("Forward message " + userMessage + " to " + inet);
									}
								}
							}
						}
					return true;
				} else
					return false;
			}
		});
	}

	public static void start(User myUniqueUser)
	{
		ConnectivityManager.myUniqueUser = myUniqueUser;
		NetworkManager.start();
	}

	public static void stop()
	{
		NetworkManager.stop();
		ConnectivityManager.myUniqueUser = null;
	}
}
