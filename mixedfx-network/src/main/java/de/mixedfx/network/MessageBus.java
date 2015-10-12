package de.mixedfx.network;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.inspector.Inspector;
import de.mixedfx.network.messages.IdentifiedMessage;
import de.mixedfx.network.messages.Message;
import lombok.Setter;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.springframework.stereotype.Component;

import java.lang.ref.WeakReference;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * @author Jerry
 */
@Component
public class MessageBus {
	public static final String MESSAGE_RECEIVE = "MESSAGE_RECEIVE";

	private
	@Setter
	ConnectivityManager<?> connectivityManager;

	private ArrayList<Object> receiverList = new ArrayList<>();

    /**
     * To undo this use
     * {@link MessageBus#unregisterForReceival(MessageReceiver)}. Done with
     * {@link WeakReference}.
     *
     * @param receiver
     *            Receiver will be informed asynchronously!
     * @param strongly
     *            Set true if you want to use this as an Anonymous Inner Object!
     */
    public synchronized void registerForReceival(final MessageReceiver receiver, final boolean strongly) {
	if (this.receiverList.isEmpty()) {
		AnnotationProcessor.process(this);
	}

	if (strongly) {
	    this.receiverList.add(receiver);
	} else {
		this.receiverList.add(new WeakReference<>(receiver));
	}
    }

    /**
     * @param message
     *            Message to send.
     */
    public synchronized void send(final Message message) {
		final Runnable run = () -> {
			if (message instanceof IdentifiedMessage) {
				final IdentifiedMessage idMessage = (IdentifiedMessage) message;
				idMessage.setFromUserID(connectivityManager.getMyUniqueUser().getIdentifier());
				synchronized (this.connectivityManager.getTcp_user_map()) {
					if (idMessage.getToUserIDs().isEmpty()) {
						this.connectivityManager.getTcp_user_map().keySet().forEach(inetAddress -> {
							message.setToIP(inetAddress);
							EventBusExtended.publishAsyncSafe(Connection.MESSAGE_CHANNEL_SEND, message);
						});
					} else {
						/*
						 * Go through all receivers, for each one go through all tcp connections, find first whose user matches to receiver and send message to him
						 */
						// TODO First statement only for testing purposes, does sort algorithm work?
						System.out.println("TEST IF INETADDRESSES ARE CORRECTLY SORTED!");
						this.connectivityManager.getTcp_user_map().keySet().stream().sorted((tcp1, tcp2) ->
						{
							// Find most local one
							try {
								final int[] largestSubnet = {0, 0};
								NetworkInterface.getByInetAddress(tcp1).getInterfaceAddresses().stream().filter(interfaceAddress1 -> interfaceAddress1.getAddress().equals(tcp1)).forEach(interfaceAddress -> largestSubnet[0] = Math.max(largestSubnet[0], interfaceAddress.getNetworkPrefixLength()));
								NetworkInterface.getByInetAddress(tcp2).getInterfaceAddresses().stream().filter(interfaceAddress1 -> interfaceAddress1.getAddress().equals(tcp2)).forEach(interfaceAddress -> largestSubnet[1] = Math.max(largestSubnet[1], interfaceAddress.getNetworkPrefixLength()));
								return Integer.compare(largestSubnet[0], largestSubnet[1]);
							} catch (SocketException e) {
								return 0;
							}
						}).forEach(inetAddress1 -> System.out.println(inetAddress1));
						idMessage.getToUserIDs().forEach(uID ->
										this.connectivityManager.getTcp_user_map().keySet().stream().sorted((tcp1, tcp2) ->
										{
											// Find most local one
											try {
												final int[] largestSubnet = {0, 0};
												NetworkInterface.getByInetAddress(tcp1).getInterfaceAddresses().stream().filter(interfaceAddress1 -> interfaceAddress1.getAddress().equals(tcp1)).forEach(interfaceAddress -> largestSubnet[0] = Math.max(largestSubnet[0], interfaceAddress.getNetworkPrefixLength()));
												NetworkInterface.getByInetAddress(tcp2).getInterfaceAddresses().stream().filter(interfaceAddress1 -> interfaceAddress1.getAddress().equals(tcp2)).forEach(interfaceAddress -> largestSubnet[1] = Math.max(largestSubnet[1], interfaceAddress.getNetworkPrefixLength()));
												return Integer.compare(largestSubnet[0], largestSubnet[1]);
											} catch (SocketException e) {
												return 0;
											}
										}).filter(inetAddress -> this.connectivityManager.getTcp_user_map().get(inetAddress).getOriginalUser().getIdentifier().equals(uID))
												.findFirst().ifPresent(concreteIP -> {
											message.setToIP(concreteIP);
											EventBusExtended.publishAsyncSafe(Connection.MESSAGE_CHANNEL_SEND, message);
											// TODO BREAK to avoid double sending
										})
						);
					}
				}
			} else {
				EventBusExtended.publishAsyncSafe(Connection.MESSAGE_CHANNEL_SEND, message);
			}
		};
		if (Thread.holdsLock(this.connectivityManager.getTcp_user_map()))

		{
			Inspector.runNowAsDaemon(run);
		} else

		{
			run.run();
		}
    }

    /**
     * Works silently. If receiver is not registered this method returns without
     * throwing an exception. Also if receiver didn't subscribe strongly.
     *
     * @param receiver
     */
    public synchronized void unregisterForReceival(final MessageReceiver receiver) {
		this.receiverList.remove(receiver);
		if (this.receiverList.isEmpty())
			AnnotationProcessor.unprocess(this);
	}

    @EventTopicSubscriber(topic = MESSAGE_RECEIVE)
    public void getMessage(final String topic, final Message message) {
		for (final Object receiver : this.receiverList) {
			if (receiver instanceof MessageReceiver) {
				((MessageReceiver) receiver).receive(message);
			} else if ((receiver instanceof WeakReference)
					&& (((WeakReference<MessageReceiver>) receiver).get() != null)) {
				((WeakReference<MessageReceiver>) receiver).get().receive(message);
			}
		}
	}
}
