package de.mixedfx.network.rebuild;

import java.net.InetAddress;
import java.util.Hashtable;

import de.mixedfx.logging.Log;
import de.mixedfx.network.rebuild.MessageBus.MessageReceiver;
import de.mixedfx.network.rebuild.messages.Message;
import de.mixedfx.network.rebuild.messages.UserMessage;
import de.mixedfx.network.rebuild.user.User;
import javafx.collections.ListChangeListener;

public class ConnectivityManager
{
	protected static Hashtable<InetAddress, UserMessage> tcp_user_map;

	public static User myUniqueUser;

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
					synchronized (NetworkManager.t.tcpClients)
					{
						if (c.wasRemoved())
						{
							for (TCPClient tcp : c.getRemoved())
							{
								tcp_user_map.remove(tcp.remoteAddress);
								Log.network.debug("Removed user message from list: " + tcp.remoteAddress);
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
		MessageBus.registerForReceival(new MessageReceiver()
		{
			public void receive(Message message)
			{
				if (message instanceof UserMessage)
				{
					synchronized (NetworkManager.t.tcpClients)
					{
						Log.network.debug("UserMessage received: " + message);
						UserMessage userMessage = (UserMessage) message;
						if (!userMessage.getOriginalUser().equals(ConnectivityManager.myUniqueUser))
						{
							// Update mapping
							tcp_user_map.put(userMessage.getFromIP(), userMessage);
							Log.network.debug("Put UserMessage " + userMessage + " from ip " + userMessage.getFromIP() + " to my list!");
						} else
							Log.network.debug("But UserMessage was from me!");
					}
				}
			}

		}, true);
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
