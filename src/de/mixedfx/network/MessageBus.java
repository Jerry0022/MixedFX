package de.mixedfx.network;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.logging.Log;
import de.mixedfx.network.messages.IdentifiedMessage;
import de.mixedfx.network.messages.Message;
import de.mixedfx.network.messages.RegisteredMessage;

/**
 * <p>
 * You can send and receive messages over the network (if established, otherwise nothing will
 * happen) in two ways (in the end it's the same way):
 * </p>
 * <ol>
 * <li>By using {@link EventBusExtended#publishAsyncSafe(String, Object)} (async) /
 * {@link EventBusExtended#publishSyncSafe(String, Object)} (sync) with
 * {@link MessageBus#MESSAGE_SEND} [methods must be "public"] or writing a method with the
 * Annotation {@link EventTopicSubscriber} subscribing for the topic
 * {@link MessageBus#MESSAGE_RECEIVE} and call once {@link AnnotationProcessor#process(Object)}</li>
 * <li>By using {@link MessageBus#send(RegisteredMessage)} (async) or
 * {@link MessageBus#registerForReceival(MessageReceiver)}!</li>
 * </ol>
 *
 * @author Jerry
 */
public class MessageBus
{
	/**
	 * String for the {@link EventBusExtended}. Please submit a {@link RegisteredMessage} object.
	 * Only for NOT {@link RegisteredMessage}!
	 */
	protected static final String MESSAGE_SEND = Connection.MESSAGE_CHANNEL_SEND;

	/**
	 * String for the {@link EventBusExtended}. You will receive a {@link RegisteredMessage} object.
	 *
	 * <pre>
	 * Maybe messages won't received in the right (chronological) order if they were not send by the same
	 * sender (this is because of the network architecture and the message size).
	 * </pre>
	 */
	public static final String MESSAGE_RECEIVE = "MESSAGE_RECEIVE";

	public interface MessageReceiver
	{
		/**
		 * Is called synchronized if a message was received. Therefore Messages will be received
		 * through this method in the same order they were received from the network.
		 *
		 * @param message
		 */
		public void receive(RegisteredMessage message);
	}

	private static MessageBus									intermediateReceiver;
	private static ArrayList<WeakReference<MessageReceiver>>	receiverList	= new ArrayList<>();

	/**
	 * <b>Sends a message asynchronously! Updates sender id if the message is an
	 * {@link IdentifiedMessage}.</b> Furthermore internally: {@link Message#fromServer} will be set
	 * to true if this application is the {@link NetworkConfig.States#Server}. Message will be internally automatically forwarded
	 * - no manual forwarding is required.
	 *
	 * @param message
	 *            Message to send.
	 */
	public static synchronized void send(final RegisteredMessage message)
	{
		if (message instanceof IdentifiedMessage)
		{
			((IdentifiedMessage) message).updateSenderID();
		}
		EventBusExtended.publishAsyncSafe(Connection.MESSAGE_CHANNEL_SEND, message);
	}

	/**
	 * To undo this use {@link MessageBus#unregisterForReceival(MessageReceiver)}. Done with {@link WeakReference}.
	 *
	 * @param receiver
	 *            Receiver will be informed asynchronously!
	 */
	public static synchronized void registerForReceival(final MessageReceiver receiver)
	{
		if (MessageBus.receiverList.isEmpty())
		{
			MessageBus.intermediateReceiver = new MessageBus();
			AnnotationProcessor.process(MessageBus.intermediateReceiver);
		}

		MessageBus.receiverList.add(new WeakReference<MessageBus.MessageReceiver>(receiver));
	}

	/**
	 * Works silently. If receiver is not registered this method returns without throwing an
	 * exception.
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
		if (message instanceof RegisteredMessage)
		{
			for (final WeakReference<MessageReceiver> receiver : MessageBus.receiverList)
			{
				Log.network.warn("RECEIVE");
				if (receiver.get() != null)
					receiver.get().receive((RegisteredMessage) message);
			}
		}
	}
}
