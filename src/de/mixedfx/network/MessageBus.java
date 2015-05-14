package de.mixedfx.network;

import java.util.ArrayList;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.network.messages.Message;

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
 * <li>By using {@link MessageBus#send(Message)} (async) or
 * {@link MessageBus#registerForReceival(MessageReceiver)}!</li>
 * </ol>
 *
 * @author Jerry
 */
public class MessageBus
{
	/**
	 * String for the {@link EventBusExtended}. Please submit a {@link Message} object.
	 */
	public static final String	MESSAGE_SEND	= "MESSAGE_SEND";

	/**
	 * String for the {@link EventBusExtended}. You will receive a {@link Message} object.
	 */
	public static final String	MESSAGE_RECEIVE	= "MESSAGE_RECEIVE";

	public interface MessageReceiver
	{
		/**
		 * Is called synchronized if a message was received. Therefore Messages will be received
		 * through this method in the same order they were received from the network.
		 *
		 * @param message
		 */
		public void receive(Message message);
	}

	/**
	 * Sends a message asynchronously!
	 *
	 * @param message
	 *            Message to send. {@link Message#fromServer} will be set to true if this
	 *            application is the {@link NetworkConfig.States#Server}. If application is
	 *            {@link NetworkConfig.States#BoundToServer} then it will be internally
	 *            automatically forwarded - no manual forwarding is required.
	 */
	public static void send(final Message message)
	{
		// TODO SUBSCRIBE!
		EventBusExtended.publishAsyncSafe(MessageBus.MESSAGE_SEND, message);
	}

	private static MessageBus					intermediateReceiver;
	private static ArrayList<MessageReceiver>	receiverList	= new ArrayList<>();

	/**
	 * To undo this use {@link MessageBus#unregisterForReceival(MessageReceiver)}.
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

		MessageBus.receiverList.add(receiver);
	}

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
		for (final MessageReceiver receiver : MessageBus.receiverList)
			receiver.receive(message);
	}
}
