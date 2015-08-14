package de.mixedfx.network.rebuild;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.ArrayList;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.network.rebuild.messages.IdentifiedMessage;
import de.mixedfx.network.rebuild.messages.Message;

/**
 * @author Jerry
 */
public class MessageBus
{
	public static final String MESSAGE_RECEIVE = "MESSAGE_RECEIVE";

	public interface MessageReceiver
	{
		/**
		 * Is called synchronized if a message was received. Therefore Messages will be received through this method in the same order they were received from the network.
		 *
		 * @param message
		 */
		public void receive(Message message);
	}

	private static MessageBus			intermediateReceiver;
	private static ArrayList<Object>	receiverList	= new ArrayList<>();

	/**
	 * <b>Sends a message asynchronously! Updates sender id if the message is an {@link IdentifiedMessage}.</b> Furthermore internally: {@link Message#fromServer} will be set to true if this
	 * application is the {@link NetworkConfig.States#SERVER}. Message will be internally automatically forwarded - no manual forwarding is required.
	 *
	 * @param message
	 *            Message to send.
	 */
	public static synchronized void send(final Message message)
	{
		if (message instanceof IdentifiedMessage)
		{
			IdentifiedMessage idMessage = (IdentifiedMessage) message;
			synchronized (ConnectivityManager.tcp_user_map)
			{
				if (idMessage.getToUserIDs().isEmpty())
					for (InetAddress ip : ConnectivityManager.tcp_user_map.keySet())
					{
						message.setToIP(ip);
						EventBusExtended.publishAsyncSafe(Connection.MESSAGE_CHANNEL_SEND, message);
					}
				else
				{
					for (Object id : idMessage.getToUserIDs())
					{
						FindIP: for (InetAddress ip : ConnectivityManager.tcp_user_map.keySet())
						{
							if (ConnectivityManager.tcp_user_map.get(ip).getOriginalUser().getIdentifier().equals(id))
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
	}

	/**
	 * To undo this use {@link MessageBus#unregisterForReceival(MessageReceiver)}. Done with {@link WeakReference}.
	 *
	 * @param receiver
	 *            Receiver will be informed asynchronously!
	 * @param strongly
	 *            Set true if you want to use this as an Anonymous Inner Object!
	 */
	public static synchronized void registerForReceival(final MessageReceiver receiver, boolean strongly)
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
			else if ((receiver instanceof WeakReference) && ((WeakReference<MessageReceiver>) receiver).get() != null)
				((WeakReference<MessageReceiver>) receiver).get().receive(message);
		}
	}
}
