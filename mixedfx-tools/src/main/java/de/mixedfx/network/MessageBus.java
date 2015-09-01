package de.mixedfx.network;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.ArrayList;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.inspector.Inspector;
import de.mixedfx.network.messages.IdentifiedMessage;
import de.mixedfx.network.messages.Message;

/**
 * @author Jerry
 */
public class MessageBus
{
	public interface MessageReceiver
	{
		/**
		 * Is called synchronized if a message was received. Therefore Messages will be received through this method in the same order they were received from the network.
		 *
		 * @param message
		 */
		public void receive(Message message);
	}

	public static final String MESSAGE_RECEIVE = "MESSAGE_RECEIVE";

	private static MessageBus			intermediateReceiver;
	private static ArrayList<Object>	receiverList	= new ArrayList<>();

	/**
	 * To undo this use {@link MessageBus#unregisterForReceival(MessageReceiver)}. Done with {@link WeakReference}.
	 *
	 * @param receiver
	 *            Receiver will be informed asynchronously!
	 * @param strongly
	 *            Set true if you want to use this as an Anonymous Inner Object!
	 */
	public static synchronized void registerForReceival(final MessageReceiver receiver, final boolean strongly)
	{
		if (MessageBus.receiverList.isEmpty())
		{
			MessageBus.intermediateReceiver = new MessageBus();
			AnnotationProcessor.process(MessageBus.intermediateReceiver);
		}

		if (strongly)
			MessageBus.receiverList.add(receiver);
		else
			MessageBus.receiverList.add(new WeakReference<MessageBus.MessageReceiver>(receiver));
	}

	/**
	 * @param message
	 *            Message to send.
	 */
	public static synchronized void send(final Message message)
	{
		final Runnable run = () ->
		{
			if (message instanceof IdentifiedMessage)
			{
				final IdentifiedMessage idMessage = (IdentifiedMessage) message;
				synchronized (ConnectivityManager.get().tcp_user_map)
				{
					if (idMessage.getToUserIDs().isEmpty())
						for (final InetAddress ip : ConnectivityManager.get().tcp_user_map.keySet())
						{
							message.setToIP(ip);
							EventBusExtended.publishAsyncSafe(Connection.MESSAGE_CHANNEL_SEND, message);
						}
					else
					{
						for (final Object id : idMessage.getToUserIDs())
						{
							FindIP: for (final InetAddress ip : ConnectivityManager.get().tcp_user_map.keySet())
							{
								if (ConnectivityManager.get().tcp_user_map.get(ip).getOriginalUser().getIdentifier().equals(id))
								{
									message.setToIP(ip);
									EventBusExtended.publishAsyncSafe(Connection.MESSAGE_CHANNEL_SEND, message);
									break FindIP; // Avoid double sending!
								}
							}
						}
					}
				}
			} else
				EventBusExtended.publishAsyncSafe(Connection.MESSAGE_CHANNEL_SEND, message);
		};
		if (Thread.holdsLock(ConnectivityManager.get().tcp_user_map))
			Inspector.runNowAsDaemon(run);
		else
			run.run();
	}

	/**
	 * Works silently. If receiver is not registered this method returns without throwing an exception. Also if receiver didn't subscribe strongly.
	 *
	 * @param receiver
	 */
	public static synchronized void unregisterForReceival(final MessageReceiver receiver)
	{
		MessageBus.receiverList.remove(receiver);

		if (MessageBus.receiverList.isEmpty())
		{
			MessageBus.intermediateReceiver = null;
			AnnotationProcessor.unprocess(MessageBus.intermediateReceiver);
		}
	}

	private MessageBus()
	{

	}

	@EventTopicSubscriber(topic = MessageBus.MESSAGE_RECEIVE)
	public void getMessage(final String topic, final Message message)
	{
		for (final Object receiver : MessageBus.receiverList)
		{
			if (receiver instanceof MessageReceiver)
				((MessageReceiver) receiver).receive(message);
			else if ((receiver instanceof WeakReference) && (((WeakReference<MessageReceiver>) receiver).get() != null))
				((WeakReference<MessageReceiver>) receiver).get().receive(message);
		}
	}
}
